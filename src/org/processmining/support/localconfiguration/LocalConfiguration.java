package org.processmining.support.localconfiguration;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Crea la configurazione locale di un nodo
 * 
 * @author Daniele Cicciarella
 */
public class LocalConfiguration 
{
	/* Lista contenente la configurazione locale di un nodo */
	private ArrayList <Transition> localConfiguration;
	
	/**
	 * Costruttore
	 */
	public LocalConfiguration()
	{
		localConfiguration = new ArrayList <Transition>();
	}
	
	/**
	 * Crea la configurazione locale di un nodo
	 * 
	 * @param N rete di Petri
	 * @param pn nodo corrente
	 * @return configurazione locale di pn
	 */
	public ArrayList<Transition> set(Petrinet N, PetrinetNode pn) 
	{
		getLeastBackwardClosed(N, pn, localConfiguration);
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
	 * @param N rete di Petri
	 * @param pn nodo corrente
	 * @param lista parziale delle transazioni analizzate in precendenza
	 */
	private void getLeastBackwardClosed(Petrinet N, PetrinetNode pn, ArrayList<Transition> back) 
	{
		if(pn instanceof Transition)
		{
			// La transazione è stata visitata
			if(back.contains(pn))	
				return;

			back.add((Transition) pn);
		}
		
		/* Visita ricorsiva */
		for (Iterator<?> preset = N.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			Arc a = (Arc) preset.next();
			getLeastBackwardClosed(N, a.getSource(), back);
		}
	}
}
