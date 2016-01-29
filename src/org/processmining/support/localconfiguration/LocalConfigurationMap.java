package org.processmining.support.localconfiguration;

import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

/**
 * Crea una map contenente per ogni nodo la configurazione locale
 * 
 * @author Daniele Cicciarella
 */
public class LocalConfigurationMap extends HashMap<PetrinetNode, LocalConfiguration> 
{
	/* serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Inserisce una configurazione locale di un nodo
	 * 
	 * @param pn nodo da inserire
	 * @param N rete di petri
	 */
	public void set(PetrinetNode pn, Petrinet N)
	{
		LocalConfiguration localConfiguration = new LocalConfiguration();
		localConfiguration.set(N, pn);
		put(pn, localConfiguration);
	}
}
