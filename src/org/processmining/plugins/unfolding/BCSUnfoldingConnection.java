package org.processmining.plugins.unfolding;

import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.support.unfolding.StatisticMap;

/**
 * Connettore per la visualizzazione delle rete di Petri originale, rete di Occorrenze con la tecnica dell'unfolding e le sue statistiche
 * 
 * @author Daniele Cicciarella
 */
public class BCSUnfoldingConnection extends AbstractStrongReferencingConnection 
{	
	public static String IDENTIFICATION_MAP = "IDENTIFICATION_MAP";
	public static String PETRINET = "PETRINET";
	public static String UNFOLDING = "UNFOLDING";

	/**
	 * Inserimento delle reti di petri nel connettore
	 * 
	 * @param identificationMap map contenente le statistiche della rete di Unfolding
	 * @param petrinet rete di Petri originale
	 * @param unfolding rete di Unfolding
	 */
	public BCSUnfoldingConnection(StatisticMap identificationMap, Petrinet petrinet, Petrinet unfolding) 
	{
		super("UnfoldingConnection");
		putStrong(IDENTIFICATION_MAP, identificationMap);
		putStrong(PETRINET, petrinet);
		putStrong(UNFOLDING, unfolding);	
	}
}