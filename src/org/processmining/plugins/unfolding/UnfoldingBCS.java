package org.processmining.plugins.unfolding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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
import org.processmining.support.unfolding.Pair;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.Utility;

public class UnfoldingBCS {

	protected PluginContext context;

	/* Reti di petri */
	protected Petrinet petrinet, unfolding;

	/* Variabili per la trasformazione della rete di Petri in N* */
	protected Place i, o;
	protected Transition reset;

	protected LinkedBlockingQueue<LocalConfiguration> queue = new LinkedBlockingQueue<LocalConfiguration>();
//	protected LocalConfigurationMap localConfigurationMap = new LocalConfigurationMap();
	protected StatisticMap statisticMap = new StatisticMap();
	
	/* Mappa ogni nodo della rete di Petri a un uno o più nodi della rete di unfolding */
	protected Map <PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap = new HashMap <PetrinetNode, ArrayList<PetrinetNode>>();

	/* Mappa ogni nodo della rete di unfolding a un nodo della rete di Petri */
	protected Map <PetrinetNode, PetrinetNode> unf2PetriMap = new HashMap <PetrinetNode, PetrinetNode>();
	protected ArrayList<Transition> transitions=new ArrayList<Transition>();
	/**
	 * Costruttore
	 * 
	 * @param context
	 *            contesto di ProM
	 * @param petrinet
	 *            rete di petri originale
	 */
	UnfoldingBCS(PluginContext context, Petrinet petrinet) {
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
	 * @throws InterruptedException
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
		System.out.println("UNFOLDING BCS");

		/* Inizializzo e visito la coda */
		initQueue(i, i1);
		visitQueue();

		/* Estraggo i deadlock ed effettuo le statistiche della rete */
		writeLog(context, "Extraction of the dealock points...");
		getStatistics();
		System.out.println("FINE");
		return new Object[] { unfolding, statisticMap };
	}

	private void initQueue(Place p, Place p1) throws InterruptedException {
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
			LocalConfiguration lc= new LocalConfiguration();
			lc.set(unfolding, t);
			queue.put(lc);
		}
	}

	private void visitQueue() throws InterruptedException {
		/*
		 * while Q is not empty do 
		 * pull out a local configuration c from Q;
		 * calculate the corresponding marking; 
		 * forall the transition t of the Petri net do 
		 * if t is enabled then 
		 * create an instance t′;
		 * add t′ in the unfolding net; 
		 * link t′ to the place that enable it; 
		 * refresh L and L; 
		 * create the local configuration c′ = c ∪ {t′}; 
		 * if t′ is not a BCS cutoff then add 
		 * c′ to Q; 
		 * end 
		 * end 
		 * end 
		 * end
		 */
		while (!queue.isEmpty()) {
			LocalConfiguration lcm = queue.poll();
			System.out.println("LocalConfiguration " + lcm);
			System.out.println("queu size " + queue.size());
			ArrayList<PetrinetNode> marking = Utility.getMarking(petrinet, lcm);
			System.out.println("marking " + marking);

			for (Transition t : petrinet.getTransitions()) {
				System.out.println("transition " + t);
				List<PetrinetNode> preset = Utility.isEnabledFromMarking(marking, t, petrinet);
				System.out.println("preset "+preset+" di "+t);
				
				if (!preset.isEmpty()) {
					System.out.println(t+" è abilitata");

					/*create an instance t′*/
					
					System.out.println(t+" è abilitata");

					String id = "";
					try {
						id = t.getAttributeMap().get("Original id").toString();
					} catch (NullPointerException e) {
						id = "_not_present";
					}
					/* add t′ in the unfolding net */
					
					Transition t1 = unfolding.addTransition(t.getLabel());
					t1.getAttributeMap().put("Original id", id);
					unfolding.addTransition(t1.getLabel());
					
					/*link t′ to the place that enable it*/
					for (PetrinetNode p : preset) {
						Place p1 = unfolding.addPlace(p.getLabel());
						p1.getAttributeMap().put("Original id", p.getAttributeMap().get("Original id"));
						unfolding.addArc((Place) p1, t1);
						refreshCorrispondence(p, p1);
					}
					
					refreshCorrispondence(t, t1);
					
					/*create the local configuration c′ = c ∪ {t′} */
					lcm.add(t);
					
					boolean isCutoff =isCutoff(lcm, t1);
					
					if (!isCutoff) {
						for (PetrinetNode p : Utility.getPostset(petrinet, t)) {
							Place p1 = unfolding.addPlace(p.getLabel());
							p1.getAttributeMap().put("Original id", p.getAttributeMap().get("Original id"));
							unfolding.addArc(t1,(Place) p1);
							refreshCorrispondence(p, p1);
						}
						System.out.println("non è cutoff LocalConfiguration "+ lcm);
						queue.put(lcm);
					}
				}
			}
		}
	}
	/**
	 * Aggiorna le corrispondenze delle map
	 * 
	 * @param pn nodo della rete di Petri
	 * @param pn1 nodo della rete di unfolding
	 */
	private void refreshCorrispondence(PetrinetNode pn, PetrinetNode pn1) {
		/* Aggiorno le map delle corrispondenze */
		if(!petri2UnfMap.containsKey(pn)) 
			petri2UnfMap.put(pn, new ArrayList<PetrinetNode>());		
		petri2UnfMap.get(pn).add(pn1);
		unf2PetriMap.put(pn1, pn);

		/* Se è una transizione aggiornare le altre map */
		/*if(pn1 instanceof Transition)
		{
			localConfigurationMap.add(pn1, unfolding);
			markingMap.put(pn1, Utility.getMarking(petrinet, localConfigurationMap.get(pn1), unf2PetriMap));
			//xorMap.put(pn1, Utility.getHistoryXOR(unfolding, pn1, null));
		}*/
	}

	//confronto la label delle transition e NON VA BENE
	private boolean isCutoff(LocalConfiguration lc, Transition t2) {
		ArrayList<PetrinetNode> markingT2=Utility.getMarking(petrinet, lc);
		for (Transition t1 : lc.get()) {
			System.out.println(t1+" "+t2);
			if (t2.getLabel()!=t1.getLabel()) {
				LocalConfiguration lc1 = new LocalConfiguration();
				lc1.set(unfolding, t1);
				ArrayList<PetrinetNode> markingT1 = Utility.getMarking(petrinet, lc1);
				System.out.println("markingT1 " + markingT1);
				System.out.println("markingT2 " + markingT2);
				if (markingT2.size() == markingT1.size() && markingT2.containsAll(markingT1)) {
					// cutoff
					statisticMap.addCutoff(t2);
					System.out.println("cutoff " + t2);
					return true;
				} else if (markingT2.containsAll(markingT1)) {
					// cutoff unbounded
					statisticMap.addCutoffUnbounded(t2);
					System.out.println("cutoff unbounded " + t2);
					return true;
				}
			}
		}
		return false;
	}
	
	private void getStatistics() {
		/* Inserisco i livelock trovati in un lista */
		ArrayList <Transition> cutoff = new ArrayList <Transition> (statisticMap.getCutoff().size() + statisticMap.getCutoffUnbounded().size());
		for(int i = 0; i < statisticMap.getCutoff().size(); i++)
			cutoff.add(statisticMap.getCutoff().get(i));
		for(int i = 0; i < statisticMap.getCutoffUnbounded().size(); i++)
			cutoff.add(statisticMap.getCutoffUnbounded().get(i));

		/* Filtro i punti di cutoff per ottenere un primo insieme di spoilers */
		ArrayList<Transition> spoilers = filterCutoff(cutoff);

		/* Individuo i deadlock */
		ArrayList <Transition> deadlock = getDeadlock(cutoff, spoilers);		
		if(deadlock != null)
			statisticMap.setDeadlock(deadlock);

		/* Inserisco le altre statistiche */
		statisticMap.setStatistic(petrinet, unfolding, petri2UnfMap, null);
		
	}

	/**
	 * @param cutoff
	 * @return
	 */
	private ArrayList<Transition> filterCutoff(ArrayList<Transition> cutoff) 
	{
		ArrayList <Transition> cutoffHistory = new ArrayList <Transition> (), filter = new ArrayList <Transition>();

		/* */
		for(Transition v: cutoff)
		{ 
			cutoffHistory = new ArrayList <Transition> ();
	LocalConfiguration lc=new LocalConfiguration();
			for(Transition u: lc.set(petrinet, v))
				if(!cutoffHistory.contains(u))
					cutoffHistory.add(u);


			/* */
			for(Place p : unfolding.getPlaces())
			{
				if(unfolding.getGraph().getOutEdges(p).size() > 1)
				{
					for (DirectedGraphEdge<? ,?> a : unfolding.getGraph().getOutEdges(p)) 
					{
						Arc arc = (Arc) a;
						Transition t = (Transition) arc.getTarget();
						if(!filter.contains(t) && !cutoffHistory.contains(t) && !(cutoff.contains(t)))
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
	 * @param cutoff: arraylist contenente i punti di cutoff
	 * @return arraylist contenente i punti di deadlock
	 */
	private ArrayList<Transition> getDeadlock(ArrayList<Transition> cutoff, ArrayList<Transition> spoilers) 
	{
		Transition s = null;
		ArrayList <Transition> deadlock = null, cutoff1 = null, spoilers2 = null;

		if(!cutoff.isEmpty())
		{		
			Transition t = cutoff.get(0);
			ArrayList<Transition> spoilers1 = getSpoilers(t, spoilers);	
			while(!spoilers1.isEmpty() && deadlock == null)
			{
				s = spoilers1.remove(0);
				cutoff1 = removeConflict(cutoff, s);
				spoilers2 = removeConflict(spoilers, s);
				if(cutoff1.isEmpty())
				{
					deadlock = new ArrayList <Transition>();
					deadlock.add(s);
				}
				else
				{
					deadlock = getDeadlock(cutoff1, spoilers2);
					if(deadlock != null)
						deadlock.add(s);
				}
			}
			return deadlock;
		}
		else
			return null;
	}
	
	/**
	 * Scelto come nuovo insieme quelle che non sono in conflitto con lo spoiler
	 * 
	 * @param cutoff: insieme corrente di cutoff
	 * @param spoiler: spoiler corrente
	 * @return cutoff1: arraylist di transizioni contenente la nuova lista di cutoff
	 */
	private ArrayList<Transition> removeConflict(ArrayList<Transition> cutoff, Transition spoiler) 
	{	
		/* Se è vuota ritorna lista vuota */
		if(cutoff.isEmpty())
			return  new ArrayList<Transition>();
		else
		{
			ArrayList<Transition> cutoff1 = new ArrayList <Transition> ();
			ArrayList<Pair> xorSpoiler = Utility.getHistoryXOR(unfolding, spoiler, null);// xorMap.get(spoiler);

			/* Se le transizioni del cutoff non sono in conflitto con lo spoiler le aggiungo alla nuova lista */
			for(Transition t: cutoff)
				if(t != spoiler && !Utility.isConflict(Utility.getHistoryXOR(unfolding, t, null)/*xorMap.get(t)*/, xorSpoiler))
					cutoff1.add(t);
			return cutoff1;
		}
	}
	
	/**
	 * Prendo tutte le transizioni che sono in conflitto con il cutoff
	 * 
	 * @param t: cutoff scelto
	 * @return spoilers: arraylist di transizioni contenenti tutte le transizioni in conflitto con il cutoff
	 */
	private ArrayList<Transition> getSpoilers(Transition t, ArrayList<Transition> set) 
	{
		/* Se è vuota ritorna lista vuota */
		if(set.isEmpty())
			return new ArrayList <Transition> ();
		else
		{
			ArrayList<Transition> spoilers = new ArrayList <Transition> ();
			ArrayList<Pair> xorT = Utility.getHistoryXOR(unfolding, t, null);// xorMap.get(t);

			/* Se sono in conflitto le aggiungo alla nuova lista */
			for(Transition t1: set)
				if(Utility.isConflict(xorT, Utility.getHistoryXOR(unfolding, t1, null)/*xorMap.get(t1)*/))
					spoilers.add(t1);	
			return spoilers;
		}
	}
	
	private void writeLog(PluginContext context, String log) {
		context.log(log);
		context.getProgress().inc();
	}
}
