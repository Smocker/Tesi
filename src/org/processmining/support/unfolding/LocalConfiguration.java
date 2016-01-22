package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Classe utilizzata per creare la configurazione locale di un nodo
 * 
 * @author Daniele Cicciarella
 */
public class LocalConfiguration 
{
	private ArrayList <Transition> localConfiguration = new ArrayList <Transition>();
	
	/**
	 * Crea la configurazione locale di un nodo
	 * 
	 * @param unfolding
	 * @param pn
	 * @return localConfiguration
	 */
	public ArrayList<Transition> create(Petrinet unfolding, PetrinetNode pn) 
	{
		// Se pn (nodo iniziale) è una transazione deve essere inserito nella configurazione locale
		if(pn instanceof Transition) 
			localConfiguration.add((Transition) pn);
		
		for (Iterator<?> preset = unfolding.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			Arc a = (Arc) preset.next();
			getLeastBackwardClosed(unfolding, a.getSource(), localConfiguration);
		}
		return localConfiguration;
	}

	/**
	 * Restituisce la configurazione locale
	 * 
	 * @return configurazione locale
	 */
	public ArrayList<Transition> get() 
	{
		return localConfiguration;
	}
	
	/**
	 * Visita all'indietro la rete di unfolding per la costruzione della configurazione locale
	 * 
	 * @param unfolding: rete di unfolding
	 * @param pn: nodo corrente
	 * @param array: arraylist contenente le transazioni analizzate in precendenza
	 */
	private void getLeastBackwardClosed(Petrinet unfolding, PetrinetNode pn, ArrayList<Transition> array) 
	{
		if(pn instanceof Place) 
		{
			for (Iterator<?> preset = unfolding.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
			{
				Arc a = (Arc) preset.next();
				Transition t = (Transition) a.getSource();
				getLeastBackwardClosed(unfolding, t, array);
			}
		} 
		else 
		{ 				
			// La transazione è stata visitata
			if(array.contains(pn))	
				return;
			
			Transition t = (Transition) pn;
			array.add(t);						
			for (Iterator<?> preset = unfolding.getGraph().getInEdges(t).iterator(); preset.hasNext();) 
			{
				Arc a = (Arc) preset.next();
				Place p = (Place) a.getSource();
				getLeastBackwardClosed(unfolding, p, array);
			}
		}
	}
}
