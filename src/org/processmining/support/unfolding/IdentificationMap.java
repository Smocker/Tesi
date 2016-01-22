package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * IdentificationMap viene utilizzata per la creazione delle statistiche della rete di occorrenze con la tecnica dell'unfolding
 * 
 * @author Daniele Cicciarella
 */
public class IdentificationMap extends HashMap<String, ArrayList <Transition>>
{
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/* Variabili utilizzate per le statistiche */
	private int sizeArcs = 0, sizePlaces = 0, sizeTransitions = 0;
	private boolean isBoundedness;

	/**
	 * Costruttore
	 */
	public IdentificationMap()
	{
		put("Livelock Identification", new ArrayList <Transition>());
		put("Livelock Identification Unbounded", new ArrayList <Transition>());
		put("DeadLock Identification", new ArrayList <Transition>());
	}
	
	/**
	 * Inserisce il livelock
	 * 
	 * @param t: transazione da aggiungere
	 */
	public void insertLiveLock(Transition t) 
	{
		if(get("Livelock Identification").contains(t))
			return;
		get("Livelock Identification").add(t);
	}

	/**
	 * Restituisce i livelock
	 * 
	 * @return arraylist contenente i livelock
	 */
	public ArrayList<Transition> readLiveLock()
	{
		return get("Livelock Identification");
	}
	
	/**
	 * Inserisce il livelock unbounded
	 * 
	 * @param t: transazione da aggiungere
	 */
	public void insertLiveLockUnbounded(Transition t)
	{
		if(get("Livelock Identification Unbounded").contains(t))
			return;
		get("Livelock Identification Unbounded").add(t);
	}
	
	/**
	 * Restituisce i livelock unbounded
	 * 
	 * @return arraylist contenente i livelock unbounded
	 */
	public ArrayList<Transition> readLiveLockUnbounded()
	{
		return get("Livelock Identification Unbounded");
	}
	
	/**
	 * Inserisce i deadlock
	 * 
	 * @param t: transazione da aggiungere
	 */
	public void insertDeadLock(ArrayList<Transition> t)
	{
		if(t != null)
			put("DeadLock Identification", t);
	}
	
	/**
	 * Restituisce i deadlock
	 * 
	 * @return arraylist contenente i deadlock
	 */
	public ArrayList<Transition> readDeadLock()
	{
		return this.get("DeadLock Identification");
	}
	
	/**
	 * Crea alcune statistiche della rete
	 * 
	 * @param unfolding: rete di unfolding
	 * @param marking: map contenente le transazioni che provocano la rete unbounded con il rispettivo marking
	 */
	public void setNetStatistics(Petrinet unfolding, HashMap<PetrinetNode, ArrayList<PetrinetNode>> marking)
	{
		for(Place p : unfolding.getPlaces())
			sizeArcs += unfolding.getGraph().getInEdges(p).size() + unfolding.getGraph().getOutEdges(p).size();
		sizePlaces = unfolding.getPlaces().size();
		sizeTransitions = unfolding.getTransitions().size();
		isBoundedness = marking.isEmpty() ? true : false;		
	}
	
	/**
	 * Carico tutte le statistiche della rete in una stringa
	 * 
	 * @return stringa contenente le statistiche
	 */
	public String loadStatistics()
	{
		String out = "<html>";
		
		/* Carico i livelock e deadlock */
		for(String key: this.keySet())
		{
			if(get(key).isEmpty())
				continue;
			
			out += "<BR>" + key + ":[ ";
			for(Transition t: get(key))
				out += t.getLabel() + ", ";
			out += "]";
		}
		out += "<BR><BR>";
		
		/* Carico le altre statistiche della rete */
		out += "Net Statistics: <BR>";
		out += "Places: " + sizePlaces + "<BR>";
		out += "Transitions: " + sizeTransitions + "<BR>";
		out += "Arcs: " + sizeArcs + "<BR>";
		out += "Boundedness: " + isBoundedness + "<BR></html>";
		return out;
	}
}