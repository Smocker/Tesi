package org.processmining.plugins.unfolding;

import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.support.unfolding.IdentificationMap;

/**
 * Connettore per la visualizzazione delle rete di Petri originale, rete di Occorrenze con la tecnica dell'unfolding e le sue statistiche
 * 
 * @author Daniele Cicciarella
 */
public class UnfoldingConnection extends AbstractStrongReferencingConnection 
{	
	public static String IDENTIFICATION_MAP = "IDENTIFICATION_MAP";
	public static String PETRINET = "PETRINET";
	public static String UNFOLDING = "UNFOLDING";

	/**
	 * Inserimento delle reti di petri nel connettore
	 * 
	 * @param identificationMap
	 * @param petrinet
	 * @param unfolding
	 */
	public UnfoldingConnection(IdentificationMap identificationMap, Petrinet petrinet, Petrinet unfolding) 
	{
		super("UnfoldingConnection");
		putStrong(IDENTIFICATION_MAP, identificationMap);
		putStrong(PETRINET, petrinet);
		putStrong(UNFOLDING, unfolding);	
	}
}