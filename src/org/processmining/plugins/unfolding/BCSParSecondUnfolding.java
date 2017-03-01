package org.processmining.plugins.unfolding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.converters.bpmn2pn.ClonePetrinet;
import org.processmining.support.localconfiguration.LocalConfiguration;
import org.processmining.support.localconfiguration.LocalConfigurationMap;
import org.processmining.support.unfolding.Combination;
import org.processmining.support.unfolding.Pair;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.Utility;

/**
 * Converte un rete di Petri in una BCS unfolding
 * 
 * @author Daniele Cicciarella
 */
public class BCSParSecondUnfolding {// implements Runnable {
	/* Contesto di ProM */
	protected PluginContext context;

	/* Reti di petri */
	protected Petrinet petrinet, unfolding;

	/* Variabili per la trasformazione della rete di Petri in N* */
	protected Place i, o;
	protected Transition reset;

	/* Coda contenente le configurazioni da analizzare */
	protected LinkedList<LocalConfiguration> queue = new LinkedList<LocalConfiguration>();
	/*
	 * Mappa ogni nodo della rete di Petri a un uno o più nodi della rete di
	 * unfolding
	 */
	protected Map<PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap = new HashMap<PetrinetNode, ArrayList<PetrinetNode>>();

	/* Mappa ogni nodo della rete di unfolding a un nodo della rete di Petri */
	protected Map<PetrinetNode, PetrinetNode> unf2PetriMap = new HashMap<PetrinetNode, PetrinetNode>();

	/*
	 * Mappa ogni transizione della rete di unfolding con il rispettivo marking
	 */
	protected Map<PetrinetNode, ArrayList<PetrinetNode>> markingMap = new HashMap<PetrinetNode, ArrayList<PetrinetNode>>();

	protected Map<Transition, ArrayList<Thread>> threads = new HashMap<Transition, ArrayList<Thread>>();
	protected ArrayList<Transition> transitionEsaminate = new ArrayList<Transition>();

	/* Mappa ogni transizione con storia dei suoi xor-split */
	// protected HashMap <PetrinetNode, ArrayList<Pair>> xorMap = new HashMap
	// <PetrinetNode, ArrayList<Pair>>();

	/*
	 * Mappa le configurazioni locali di ogni transizione delle rete di
	 * unfolding
	 */
	protected LocalConfigurationMap localConfigurationMap = new LocalConfigurationMap();

	/* Mappa i livelock e deadlock e altre statistiche */
	protected StatisticMap statisticMap = new StatisticMap();
	// protected Transition t2;
	// protected Place pi;

	/**
	 * Costruttore
	 * 
	 * @param context
	 *            contesto di ProM
	 * @param petrinet
	 *            rete di petri originale
	 */
	BCSParSecondUnfolding(PluginContext context, Petrinet petrinet) {
		this.context = context;
		this.petrinet = PetrinetFactory.clonePetrinet(petrinet);
		ClonePetrinet pnc = new ClonePetrinet(petrinet.getLabel());
		pnc.cloneFrom(petrinet, true, true, true, false, false);
		this.petrinet = pnc;
		this.unfolding = PetrinetFactory.newPetrinet("Unfolding from Petrinet");
	}

	/**
	 * Converte una rete di Petri in una rete BCS unfolding
	 * 
	 * @return la rete BCS unfolding e le sue statistiche
	 */
	public Object[] convert() {
		i = Utility.getStartNode(petrinet);
		o = Utility.getEndNode(petrinet);

		/* Inizio la costruzione della rete inserendo la piazza iniziale i1 */
		Place i1 = unfolding.addPlace(i.getLabel());
		refreshCorrispondence(i, i1);

		/* Trasformo la rete di Petri N in N* */
		reset = petrinet.addTransition("reset");
		petrinet.addArc(o, reset);
		petrinet.addArc(reset, i);
		System.out.println("UNFOLDING PARALLELO");

		initQueue(i, i1);
		visitQueue();

		System.out.println("Finished all threads");
		writeLog(context, "Extraction of the dealock points...");
		getStatistics();
		return new Object[] { unfolding, statisticMap };
	}

	/*
	 * private ExecutorService launchExecutor() { Collection<Transition>
	 * transitions = petrinet.getTransitions(); ExecutorService executor =
	 * Executors.newFixedThreadPool(transitions.size()); for (Transition t :
	 * transitions) { Thread worker = new Thread(this); threads.put(t, worker);
	 * } System.out.println("dimensione threads " + threads.size()); return
	 * executor; }
	 */

	/**
	 * Inizializzazione della coda di priorità
	 * 
	 * @param p
	 *            piazza iniziale della rete di petri
	 * @param p1
	 *            piazza iniziale della rete di unfolding
	 */
	private void initQueue(Place p, Place p1) {
		/*
		 * Per tutte le transizioni t della rete di petri attaccate alla piazza
		 * iniziale p
		 */
		for (DirectedGraphEdge<?, ?> a1 : petrinet.getGraph().getOutEdges(p)) {
			/* Creo una transizione t1 nell'unfolding e attacco p1 con t1 */
			Transition t = (Transition) a1.getTarget();
			String id = "";
			try {
				id = t.getAttributeMap().get("Original id").toString();
			} catch (NullPointerException e) {
				id = "_not_present";
			}
			Transition t1 = unfolding.addTransition(t.getLabel());
			t1.getAttributeMap().put("Original id", id);
			unfolding.addArc(p1, t1);

			/* Per tutti i place u delle rete di petri attaccate a t */
			for (DirectedGraphEdge<?, ?> a2 : petrinet.getGraph().getOutEdges(t)) {
				// Creo un place u1 nell'unfolding e attacco t1 con u1
				Place u = (Place) a2.getTarget();
				Place u1 = unfolding.addPlace(u.getLabel());
				u1.getAttributeMap().put("Original id", u.getAttributeMap().get("Original id"));
				unfolding.addArc(t1, u1);
				refreshCorrispondence((PetrinetNode) u, u1);
			}

			/* Aggiorno tutte le strutture globali e la coda */
			refreshCorrispondence(t, t1);
			queue.push(localConfigurationMap.get(t1));
		}
	}

	private void visitQueue() {
		Collection<Transition> transitions = petrinet.getTransitions();
		ExecutorService executor = Executors.newFixedThreadPool(transitions.size());
		synchronized (queue) {
			while (!queue.isEmpty()) {
				/* Estraggo una configurazione c da q */
				LocalConfiguration c = queue.pop();
				Transition t1 = c.get().get(0);
				Transition t = (Transition) unf2PetriMap.get(t1);
				/* Per ogni piazza p della rete originale attaccate a t */
				for (DirectedGraphEdge<?, ?> a1 : petrinet.getGraph().getOutEdges(t)) {

					Place p = (Place) a1.getTarget();
					Place pi = getPrecedent(t1, p);
					for (Transition tp : transitions) {
						Thread tr = createRunnable(tp, pi, this);
						executor.execute(tr);
						System.out.println("lancia thread "+tp);
					}
					/*
					 * Per ogni transizione t2 delle rete originale attaccate a
					 * p
					 */
					for (DirectedGraphEdge<?, ?> a2 : petrinet.getGraph().getOutEdges(p)) {
						Transition t2 = (Transition) a2.getTarget();
						
					}
				}
			}
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}

	}

	private Thread createRunnable(final Transition t2, final Place pi, final BCSParSecondUnfolding bcs) {

		Thread aRunnable = new Thread() {

			public void run() {
				/*
				 * petrinet, unfolding; queue = new
				 * LinkedList<LocalConfiguration>(); petri2UnfMap unf2PetriMap
				 * markingMap
				 */
				/*
				 * synchronized(bcs.petrinet){ synchronized(bcs.unfolding){
				 * synchronized(bcs.queue){ synchronized(bcs.petri2UnfMap){
				 * synchronized(bcs.unf2PetriMap){ synchronized(bcs.markingMap){
				 * synchronized(bcs.localConfigurationMap){
				 */
				synchronized (bcs) {
					System.out.println("RUN thread id " + Thread.currentThread().getId() + " abilitata "
							+ (Utility.isEnabled(petrinet, t2, petri2UnfMap) != null) + " transition " + t2);
					ArrayList<Thread>list;
					if((list=threads.get(t2))==null){
						list=new ArrayList<Thread>();
						list.add(Thread.currentThread());
						threads.put(t2, list);
					}else{
						list.add(Thread.currentThread());
					}
					List<PetrinetNode> presetT2 = null;
					ArrayList<Combination> combination = null;
					int sizeCombination = 1;
					
					/* Verifico se t2 è abilitata */
					while ((presetT2 = Utility.isEnabled(petrinet, t2, petri2UnfMap)) == null && !fine(t2)) {
						try {
							System.out.println("wait ENABLED " + t2);
							bcs.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}
					}

					if (fine(t2)){
						System.out.println("FINE");

						return;
						
					}
					System.out.println("---->presetT2 " + presetT2);
					/*
					 * Prendo il preset di t2 per creare tutte le combinazioni
					 * possibili
					 */
					List<List<PetrinetNode>> possibleCombination = new ArrayList<List<PetrinetNode>>();
					System.out.println("petri2UnfMap.get(presetT2.get(i)); " + petri2UnfMap.get(presetT2.get(0)));
					for (int i = 0; i < presetT2.size(); i++) {
						if (!unf2PetriMap.get(pi).equals(presetT2.get(i))) {
							ArrayList<PetrinetNode> array = null;
							while ((array = petri2UnfMap.get(presetT2.get(i))) == null && !fine(t2)) {
								try {
									System.out.println("wait ARRAY " + t2);
									bcs.wait();
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
									return;
								}
							}
							
							if (fine(t2)){
								System.out.println("FINE");
								return;
							}
							
							possibleCombination.add(array);
							System.out.println("transition " + t2 + " " + sizeCombination + " " + array);
							sizeCombination = sizeCombination * array.size();
						} else {
							ArrayList<PetrinetNode> array = new ArrayList<PetrinetNode>();
							array.add(pi);
							possibleCombination.add(array);
						}
					}

					/* Crea le combinazioni e filtra quelle già usate */
					combination = new ArrayList<Combination>(sizeCombination);
					Combination.create(possibleCombination, combination);
					// System.out.println(possibleCombination);

					Combination.filter(combination, (Transition) t2, petri2UnfMap, unfolding);

					String id = "";
					try {
						id = t2.getAttributeMap().get("Original id").toString();
					} catch (NullPointerException e) {
						id = "_not_present";
					}
					/* Per ogni combinazione rimanente */
					for (Combination comb : combination) {
						/*
						 * Aggiungo t2 all'unfolding il quale sarà collegato con
						 * le piazze che lo abilitano
						 */
						Transition t3 = unfolding.addTransition(t2.getLabel());
						t3.getAttributeMap().put("Original id", id);
						for (int i = 0; i < comb.getElements().size(); i++)
							unfolding.addArc((Place) comb.getElements().get(i), t3);
						
						// Verifico se l'inserimento di t3 provaca conflitto in
						// tal caso la elimino
						if (comb.isConflict(unfolding, t3)) {
							unfolding.removeTransition(t3);
						}else{
						refreshCorrispondence(t2, t3);
						System.out.println("NOTIFY");
						bcs.notifyAll();
						/* Verifico se t3 provoca cutoff */
						if (t2.equals(reset)) {
							if (markingMap.get(t3).size() == 0)
								statisticMap.addCutoff((Transition) t3);
							else
								statisticMap.addCutoffUnbounded((Transition) t3);
						} else {
							boolean isCutoff = false;
							List<PetrinetNode> postset = Utility.getPostset(petrinet, t2);
							// Verifico se una piazza finale di t2 è condivisa
							// da altre transizioni e se provoca cutoff
							for (int i = 0; i < postset.size() && !isCutoff; i++)
								isCutoff = isCutoff(t3, postset.get(i));

							// Se t3 è un punto di cutoff la configurazione non
							// deve essere aggiunta nella coda
							if (!isCutoff) {
								for (PetrinetNode p2 : postset) {
									Place p3 = unfolding.addPlace(p2.getLabel());
									p3.getAttributeMap().put("Original id", p2.getAttributeMap().get("Original id"));
									unfolding.addArc(t3, p3);
									refreshCorrispondence(p2, p3);
									bcs.notifyAll();
								}
								queue.push(localConfigurationMap.get(t3));
								System.out.println("PUSH " + Thread.currentThread().getId());
							}else{
								transitionEsaminate.add(t2);
								System.out.println("transitionEsaminate "+transitionEsaminate);
							}
							System.out.println("NOTIFY");
							bcs.notifyAll();
						}
						
						}
						
					}
					
					
					System.out.println("NOTIFY");
					bcs.notifyAll();
					System.out.println("FINISH RUN " + Thread.currentThread().getId());
				}
				//transitionEsaminate.add(t2);

			}// }}}}}}}

		};

		return aRunnable;
	}
	
	private synchronized boolean fine(Transition t){
		List<PetrinetNode> presetT=Utility.getPreset(petrinet, t);
		boolean fine=false;
		for(PetrinetNode pnn:presetT){
			List<PetrinetNode> prepresetT= Utility.getPreset(petrinet, pnn);
			for(PetrinetNode pnnn:prepresetT){
				if(transitionEsaminate.contains(pnnn)){
					transitionEsaminate.add(t);
					fine=true;
				}				
			}
		}
		System.out.println("transitionEsaminate "+transitionEsaminate+" "+t);
		return fine;
	}

	/**
	 * Verifico se una transizione provoca il cutoff
	 * 
	 * @param t
	 *            transizione da verificare
	 * @param place
	 *            una piazza finale della transizione t
	 * @return true se la transizione è un cutoff, false altrimenti
	 */
	private synchronized boolean isCutoff(Transition t, PetrinetNode place) {
		int isBounded;

		// Controllo se place è stato inserito nell'unfolding
		if (petri2UnfMap.containsKey(place)) {
			ArrayList<PetrinetNode> markingT = markingMap.get(t);

			// Se nella storia dei place di t esiste place allora è un ciclo
			for (Place h : Utility.getHistoryPlace(unfolding, t)) {
				if (unf2PetriMap.get(h).equals(place)) {
					for (DirectedGraphEdge<?, ?> a : unfolding.getGraph().getInEdges(h)) {
						isBounded = Utility.isBounded(markingT, markingMap.get(a.getSource()));
						if (isBounded == 0) {
							statisticMap.addCutoff(t);
							return true;
						} else if (isBounded > 0) {
							statisticMap.addCutoffUnbounded(t);
							return true;
						}
					}
				}
			}
			return false;
		} else
			return false;
	}

	/**
	 * Estraggo i deadlock ed effettuo le statistiche della rete
	 */
	private void getStatistics() {
		/* Inserisco i livelock trovati in un lista */
		ArrayList<Transition> cutoff = new ArrayList<Transition>(
				statisticMap.getCutoff().size() + statisticMap.getCutoffUnbounded().size());
		for (int i = 0; i < statisticMap.getCutoff().size(); i++)
			cutoff.add(statisticMap.getCutoff().get(i));
		for (int i = 0; i < statisticMap.getCutoffUnbounded().size(); i++)
			cutoff.add(statisticMap.getCutoffUnbounded().get(i));

		/* Filtro i punti di cutoff per ottenere un primo insieme di spoilers */
		ArrayList<Transition> spoilers = filterCutoff(cutoff);

		/* Individuo i deadlock */
		ArrayList<Transition> deadlock = getDeadlock(cutoff, spoilers);
		if (deadlock != null)
			statisticMap.setDeadlock(deadlock);

		/* Inserisco le altre statistiche */
		System.out.println("getStatistics");
		statisticMap.setStatistic(petrinet, unfolding, petri2UnfMap, localConfigurationMap);
	}

	/**
	 * @param cutoff
	 * @return
	 */
	private ArrayList<Transition> filterCutoff(ArrayList<Transition> cutoff) {
		ArrayList<Transition> cutoffHistory = new ArrayList<Transition>(), filter = new ArrayList<Transition>();

		/* */
		for (Transition v : cutoff) {
			cutoffHistory = new ArrayList<Transition>();
			for (Transition u : localConfigurationMap.get(v).get())
				if (!cutoffHistory.contains(u))
					cutoffHistory.add(u);

			/* */
			for (Place p : unfolding.getPlaces()) {
				if (unfolding.getGraph().getOutEdges(p).size() > 1) {
					for (DirectedGraphEdge<?, ?> a : unfolding.getGraph().getOutEdges(p)) {
						Arc arc = (Arc) a;
						Transition t = (Transition) arc.getTarget();
						if (!filter.contains(t) && !cutoffHistory.contains(t) && !(cutoff.contains(t)))
							filter.add(t);
					}
				}
			}
		}
		return filter;
	}

	/**
	 * Estrae i punti di deadlock
	 * 
	 * @param cutoff:
	 *            arraylist contenente i punti di cutoff
	 * @return arraylist contenente i punti di deadlock
	 */
	private ArrayList<Transition> getDeadlock(ArrayList<Transition> cutoff, ArrayList<Transition> spoilers) {
		Transition s = null;
		ArrayList<Transition> deadlock = null, cutoff1 = null, spoilers2 = null;

		if (!cutoff.isEmpty()) {
			Transition t = cutoff.get(0);
			ArrayList<Transition> spoilers1 = getSpoilers(t, spoilers);
			while (!spoilers1.isEmpty() && deadlock == null) {
				s = spoilers1.remove(0);
				cutoff1 = removeConflict(cutoff, s);
				spoilers2 = removeConflict(spoilers, s);
				if (cutoff1.isEmpty()) {
					deadlock = new ArrayList<Transition>();
					deadlock.add(s);
				} else {
					deadlock = getDeadlock(cutoff1, spoilers2);
					if (deadlock != null)
						deadlock.add(s);
				}
			}
			return deadlock;
		} else
			return null;
	}

	/**
	 * Prendo tutte le transizioni che sono in conflitto con il cutoff
	 * 
	 * @param t:
	 *            cutoff scelto
	 * @return spoilers: arraylist di transizioni contenenti tutte le
	 *         transizioni in conflitto con il cutoff
	 */
	private ArrayList<Transition> getSpoilers(Transition t, ArrayList<Transition> set) {
		/* Se è vuota ritorna lista vuota */
		if (set.isEmpty())
			return new ArrayList<Transition>();
		else {
			ArrayList<Transition> spoilers = new ArrayList<Transition>();
			ArrayList<Pair> xorT = Utility.getHistoryXOR(unfolding, t, null);// xorMap.get(t);

			/* Se sono in conflitto le aggiungo alla nuova lista */
			for (Transition t1 : set)
				if (Utility.isConflict(xorT, Utility.getHistoryXOR(unfolding, t1,
						null)/* xorMap.get(t1) */))
					spoilers.add(t1);
			return spoilers;
		}
	}

	/**
	 * Scelto come nuovo insieme quelle che non sono in conflitto con lo spoiler
	 * 
	 * @param cutoff:
	 *            insieme corrente di cutoff
	 * @param spoiler:
	 *            spoiler corrente
	 * @return cutoff1: arraylist di transizioni contenente la nuova lista di
	 *         cutoff
	 */
	private ArrayList<Transition> removeConflict(ArrayList<Transition> cutoff, Transition spoiler) {
		/* Se è vuota ritorna lista vuota */
		if (cutoff.isEmpty())
			return new ArrayList<Transition>();
		else {
			ArrayList<Transition> cutoff1 = new ArrayList<Transition>();
			ArrayList<Pair> xorSpoiler = Utility.getHistoryXOR(unfolding, spoiler, null);// xorMap.get(spoiler);

			/*
			 * Se le transizioni del cutoff non sono in conflitto con lo spoiler
			 * le aggiungo alla nuova lista
			 */
			for (Transition t : cutoff)
				if (t != spoiler && !Utility.isConflict(Utility.getHistoryXOR(unfolding, t,
						null)/* xorMap.get(t) */, xorSpoiler))
					cutoff1.add(t);
			return cutoff1;
		}
	}

	/**
	 * Prendo la piazza che precede la transizione nell'unfolding
	 * 
	 * @param t
	 *            transizione delle rete di Petri
	 * @param p
	 *            piazza della rete di Petri
	 * @return la piazza della rete di unfolding che precede t
	 */
	private synchronized Place getPrecedent(Transition t, Place p) {
		Place pi = null;
		ArrayList<PetrinetNode> places = petri2UnfMap.get(p);

		for (int i = 0; i < places.size(); i++)
			if (unfolding.getArc(t, places.get(i)) != null)
				pi = (Place) places.get(i);
		return pi;
	}

	/**
	 * Aggiorna le corrispondenze delle map
	 * 
	 * @param pn
	 *            nodo della rete di Petri
	 * @param pn1
	 *            nodo della rete di unfolding
	 */
	private synchronized void refreshCorrispondence(PetrinetNode pn, PetrinetNode pn1) {
		/* Aggiorno le map delle corrispondenze */

		if (!petri2UnfMap.containsKey(pn))
			petri2UnfMap.put(pn, new ArrayList<PetrinetNode>());
		petri2UnfMap.get(pn).add(pn1);
		unf2PetriMap.put(pn1, pn);

		/* Se è una transizione aggiornare le altre map */
		if (pn1 instanceof Transition) {
			localConfigurationMap.add(pn1, unfolding);
			markingMap.put(pn1, Utility.getMarking(petrinet, localConfigurationMap.get(pn1), unf2PetriMap));
			// xorMap.put(pn1, Utility.getHistoryXOR(unfolding, pn1, null));
		}
	}

	/**
	 * Scrive un messaggio di log e incrementa la barra progressiva
	 * 
	 * @param context
	 *            contesto di ProM
	 * @param log
	 *            messaggio di log
	 */
	private void writeLog(PluginContext context, String log) {
		context.log(log);
		context.getProgress().inc();
	}

	public LocalConfigurationMap getLocalConfigurationMap() {
		return localConfigurationMap;
	}
}