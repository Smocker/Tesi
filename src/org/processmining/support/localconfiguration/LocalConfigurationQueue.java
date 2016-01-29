package org.processmining.support.localconfiguration;

import java.util.LinkedList;

/**
 * Coda di priorita' contenente le configurazioni locali da analizzare
 * 
 * @author Daniele Cicciarella
 */
public class LocalConfigurationQueue extends LinkedList <LocalConfiguration>
{
	/* serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Aggiunge una nuova configurazione di una tranasazione nella coda
	 * 
	 * @param localConfiguration configurazione locale da aggiungere
	 */
	public void insert(LocalConfiguration localConfiguration)
	{
		add(localConfiguration);
	}	
}