package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * LocalConfigurationMap estende la classe HashMap in modo tale che per ogni nodo possiamo tener traccia della sua configurazione locale
 * 
 * @author Daniele Cicciarella
 */
public class LocalConfigurationMap extends HashMap<PetrinetNode, LocalConfiguration> 
{
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Inserisce una configurazione locale di un nodo
	 * 
	 * @param pn: nodo da analizzare
	 * @param net: rete di unfolding
	 */
	public void insert(PetrinetNode pn, Petrinet net)
	{
		if(this.containsKey(pn))
			return;
		
		/* Creo la configurazione locale di pn inserendola nella HashMap */
		LocalConfiguration localConfiguration = new LocalConfiguration();
		localConfiguration.create(net, pn);
		put(pn, localConfiguration);
	}
	
	/**
	 * Restituisce la LocalConfiguration di un nodo
	 * 
	 * @param pn: nodo da analizzare
	 * @return ArrayList di transazioni
	 */
	public ArrayList<Transition> read(PetrinetNode pn) 
	{
		if(!this.containsKey(pn))
			return null;
		else
			return this.get(pn).get();
	}
}
