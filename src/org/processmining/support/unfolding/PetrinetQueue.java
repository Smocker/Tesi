package org.processmining.support.unfolding;

import java.util.LinkedList;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * PetrinetQueue estende la classe LinkedList per la creazione della rete di occorrenze con la tecnica dell'unfolding
 * @author cicciarella
 */
public class PetrinetQueue extends LinkedList <LocalConfiguration>
{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Aggiunge una nuova configurazione nella coda
	 * 
	 * @param localConfigurationMap 
	 * @param petrinet
	 * @param t
	 */
	public void insert(LocalConfigurationMap localConfigurationMap, Petrinet petrinet, Transition t)
	{
		if(!localConfigurationMap.containsKey(t))
			localConfigurationMap.insert(t, petrinet);
		this.add(localConfigurationMap.get(t));
	}	
}
