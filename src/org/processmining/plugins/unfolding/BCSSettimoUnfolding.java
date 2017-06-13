package org.processmining.plugins.unfolding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.converters.bpmn2pn.ClonePetrinet;
import org.processmining.support.indexnode.IndexNode;
import org.processmining.support.indexnode.IndexNodeMap;
import org.processmining.support.localconfiguration.LocalConfigurationMap;
import org.processmining.support.unfolding.Combination;
import org.processmining.support.unfolding.Pair;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.Utility;

/**
 * Converte un rete di Petri in una BCS unfolding
 * 
 * @author Maria Tourbanova
 */
public class BCSSettimoUnfolding {
	/* Contesto di ProM */
	protected PluginContext context;

	/* Reti di petri */
	protected Petrinet petrinet, unfolding;

	/* Variabili per la trasformazione della rete di Petri in N* */
	protected Place i, o;
	protected Transition reset;


	/*
	 * Mappa ogni nodo della rete di Petri a un uno o più nodi della rete di
	 * unfolding
	 */
	protected Map<PetrinetNode, CopyOnWriteArrayList<PetrinetNode>> petri2UnfMap = new ConcurrentHashMap<PetrinetNode, CopyOnWriteArrayList<PetrinetNode>>();

	/* Mappa ogni nodo della rete di unfolding a un nodo della rete di Petri */
	protected Map<PetrinetNode, PetrinetNode> unf2PetriMap = new ConcurrentHashMap<PetrinetNode, PetrinetNode>();

	/*
	 * Mappa ogni transizione della rete di unfolding con il rispettivo marking
	 */
	protected Map<PetrinetNode, ArrayList<PetrinetNode>> markingMap = new ConcurrentHashMap<PetrinetNode, ArrayList<PetrinetNode>>();

	/*
	 * Mappa le configurazioni locali di ogni transizione delle rete di
	 * unfolding
	 */
	protected LocalConfigurationMap localConfigurationMap = new LocalConfigurationMap();

	/* Mappa i livelock e deadlock e altre statistiche */
	protected StatisticMap statisticMap = new StatisticMap();

	private static int numberThreadCreated = 0;
	private IndexNodeMap indexNodeMap = new IndexNodeMap();

	/**
	 * Costruttore
	 * 
	 * @param context
	 *            contesto di ProM
	 * @param petrinet
	 *            rete di petri originale
	 */
	BCSSettimoUnfolding(PluginContext context, Petrinet petrinet) {
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
	public Object[] convert() throws InterruptedException {
		i = Utility.getStartNode(petrinet);
		o = Utility.getEndNode(petrinet);

		/* Inizio la costruzione della rete inserendo la piazza iniziale i1 */
		Place i1 = unfolding.addPlace(i.getLabel());
		refreshCorrispondence(i, i1);

		/* Trasformo la rete di Petri N in N* */
		reset = petrinet.addTransition("reset");
		petrinet.addArc(o, reset);
		petrinet.addArc(reset, i);

		start(i, i1);

		System.out.println("FINE");

		/* Estraggo i deadlock ed effettuo le statistiche della rete */
		writeLog(context, "Extraction of the dealock points...");
		getStatistics();

		return new Object[] { unfolding, statisticMap };
	}

	private void start(Place p, Place pi) {
		IndexNode indexPi = indexNodeMap.insertIndexNode(p, pi);
		// pi nodo unfolding
		incrementNumThread();
		Thread tr = createRunnable(indexPi);
		tr.start();

		while (getNumThread() > 0) {
		}
	}

	private Thread createRunnable(final IndexNode indexPi) {

		Thread aRunnable = new Thread() {

			public void run() {
				System.out.println("pi " + indexPi);
				Place pi = (Place) indexPi.getNode();
				Place p = (Place) unf2PetriMap.get(pi);

				// per ogni t attaccata a p
				for (DirectedGraphEdge<?, ?> a1 : petrinet.getGraph().getOutEdges(p)) {
					Transition t = (Transition) a1.getTarget();// petrinet

					boolean presetIncompleto = false;
					List<PetrinetNode> presetT = Utility.getPreset(petrinet, t);
					List<List<PetrinetNode>> possibleCombination = new ArrayList<List<PetrinetNode>>();
					ArrayList<Combination> combination = null;
					int sizeCombination = 1;

					/*
					 * Prendo il preset di t per creare tutte le combinazioni
					 * possibili
					 */
					for (int j = 0; j < presetT.size() && !presetIncompleto; j++) {
						if (!p.equals(presetT.get(j))) {
							ArrayList<PetrinetNode> array = indexNodeMap.getArrayPetrinetNodeMinPi(presetT.get(j),
									indexPi);// petri2UnfMap.get(presetT2.get(i));
							if (array.isEmpty()) {
								presetIncompleto = true;
							} else {
								possibleCombination.add(array);
								sizeCombination = sizeCombination * array.size();
							}
						} else {
							ArrayList<PetrinetNode> array = new ArrayList<PetrinetNode>();
							array.add(pi);
							possibleCombination.add(array);
						}
					}

					if (!presetIncompleto) {
						
					//	System.out.println(possibleCombination);

						/* Crea le combinazioni e filtra quelle già usate */
						combination = new ArrayList<Combination>(sizeCombination);
						Combination.create(possibleCombination, combination);
						// System.out.println(possibleCombination);

						Combination.filter(combination, (Transition) t, petri2UnfMap, unfolding);

						String id = "";
						try {
							id = t.getAttributeMap().get("Original id").toString();
						} catch (NullPointerException e) {
							id = "_not_present";
						}
						/* Per ogni combinazione rimanente */
						for (Combination comb : combination) {

							/*
							 * Aggiungo t1 all'unfolding il quale sarà collegato
							 * con le piazze che lo abilitano
							 */
							Transition t1 = unfolding.addTransition(t.getLabel());
							t1.getAttributeMap().put("Original id", id);
							for (int i = 0; i < comb.getElements().size(); i++)
								unfolding.addArc((Place) comb.getElements().get(i), t1);

							// Verifico se l'inserimento di t1 provaca conflitto
							// in tal caso la elimino
							if (!comb.isConflict(unfolding, t1)) {

								refreshCorrispondence(t, t1);

								if (t.equals(reset)) {
									if (markingMap.get(t1).size() == 0)
										statisticMap.addCutoff((Transition) t1);
									else
										statisticMap.addCutoffUnbounded((Transition) t1);
								} else {
									boolean isCutoff = false;
									List<PetrinetNode> postset = Utility.getPostset(petrinet, t);

									for (int i = 0; i < postset.size() && !isCutoff; i++)
										isCutoff = isCutoff(t1, postset.get(i));

									if (isCutoff == false) {
										for (PetrinetNode post : Utility.getPostset(petrinet, t)) {
											Place p1 = unfolding.addPlace(post.getLabel());
											p1.getAttributeMap().put("Original id",
													post.getAttributeMap().get("Original id"));
											unfolding.addArc(t1, p1);
											IndexNode indexP1 = indexNodeMap.insertIndexNode(post, p1);
											refreshCorrispondence(post, p1);
											Thread tr = createRunnable(indexP1);
											tr.start();
											incrementNumThread();
										}
									}
								}
							} else {
								unfolding.removeTransition(t1);
							}
						}
					}
				}
				decrementNumThread();
				System.out.println("FINE thread");
			}

		};
		return aRunnable;

	}

	public synchronized void incrementNumThread() {
		numberThreadCreated++;
	}

	public synchronized void decrementNumThread() {
		numberThreadCreated--;
	}

	public synchronized int getNumThread() {
		return numberThreadCreated;
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
			petri2UnfMap.put(pn, new CopyOnWriteArrayList<PetrinetNode>());    //.put(pn, new CopyOnWriteArrayList<PetrinetNode>());
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

	public synchronized LocalConfigurationMap getLocalConfigurationMap() {
		return localConfigurationMap;
	}

}