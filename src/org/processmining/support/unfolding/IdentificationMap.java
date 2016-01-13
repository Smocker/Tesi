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
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	/* Variabili utilizzate per le statistiche */
	private int sizeArcs = 0, sizePlaces = 0, sizeTransitions = 0;
	private boolean isBoundedness;

	/**
	 * Costruttore
	 */
	public IdentificationMap()
	{
		this.put("Livelock Identification", new ArrayList <Transition>());
		this.put("Livelock Identification Unbounded", new ArrayList <Transition>());
		this.put("DeadLock Identification", new ArrayList <Transition>());
	}
	
	/**
	 * Inserisce il livelock
	 * 
	 * @param t
	 */
	public void insertLiveLock(Transition t) 
	{
		if(this.get("Livelock Identification").contains(t))
			return;
		this.get("Livelock Identification").add(t);
	}

	/**
	 * Restituisce i livelock
	 * 
	 * @return this.get("Livelock Identification")
	 */
	public ArrayList<Transition> readLiveLock()
	{
		return this.get("Livelock Identification");
	}
	
	/**
	 * Inserisce il livelock unbounded
	 * 
	 * @param t
	 */
	public void insertLiveLockUnbounded(Transition t)
	{
		if(this.get("Livelock Identification Unbounded").contains(t))
			return;
		this.get("Livelock Identification Unbounded").add(t);
	}
	
	/**
	 * Restituisce i livelock unbounded
	 * 
	 * @return this.get("Livelock Identification Unbounded")
	 */
	public ArrayList<Transition> readLiveLockUnbounded()
	{
		return this.get("Livelock Identification Unbounded");
	}
	
	/**
	 * Inserisce i deadlock
	 * 
	 * @param t
	 */
	public void insertDeadLock(Transition t)
	{
		if(this.get("DeadLock Identification").contains(t))
			return;
		this.get("DeadLock Identification").add(t);
	}
	
	/**
	 * Restituisce i deadlock
	 * 
	 * @return this.get("DeadLock Identification")
	 */
	public ArrayList<Transition> readDeadLock()
	{
		return this.get("DeadLock Identification");
	}
	
	/**
	 * Crea alcune statistiche della rete
	 * 
	 * @param unfolding
	 * @param marking
	 */
	public void setNetStatistics(Petrinet unfolding, HashMap<PetrinetNode, ArrayList<PetrinetNode>> marking)
	{
		for(Place p : unfolding.getPlaces())
			this.sizeArcs += unfolding.getGraph().getInEdges(p).size() + unfolding.getGraph().getOutEdges(p).size();
		this.sizePlaces = unfolding.getPlaces().size();
		this.sizeTransitions = unfolding.getTransitions().size();
		this.isBoundedness = marking.isEmpty() ? true : false;		
	}
	
	/**
	 * Mostra tutte le statistiche della rete
	 */
	public void showStatistics()
	{
		/* Mostro i livelock e deadlock */
		for(String key: this.keySet())
		{
			System.out.println("\n" + key + ": ");
			for(Transition t: this.get(key))
				System.out.println(t.getLabel() + " ");
		}
		System.out.println("\n");
		
		/* Altre statistiche della rete */
		System.out.println("Net Statistics:");
		System.out.println("Places: " + this.sizePlaces);
		System.out.println("Transitions: " + this.sizeTransitions);
		System.out.println("Arcs: " + this.sizeArcs);
		System.out.println("Boundedness: " + this.isBoundedness);
		System.out.println("\n");
	}
}