package org.processmining.plugins.unfolding;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.support.unfolding.StatisticMap;

/**
 * Converte un Petrinet in una Occurrence net with Unfolding
 * 
 * @author Daniele Cicciarella
 */
public class PetriNet2Unfolding_Plugin 
{
	@Plugin
	(
		name = "BCS Petri net to Unfolding net", 
		parameterLabels = {"Petri net"}, 
		returnLabels = {"Visualize BCS Unfolding Statistics", "Petri net"}, 
		returnTypes = { StatisticMap.class, Petrinet.class }, 
		userAccessible = true, 
		help = "Convert Petri net to Unfolding net"
	)
	@UITopiaVariant
	(
		affiliation = "University of Pisa", 
		author = "Daniele Cicciarella", 
		email = "cicciarellad@gmail.com"
	)
	public Object[] convert(PluginContext context, Petrinet petrinet) throws InterruptedException 
	{
		BCSSettimoUnfolding petrinet2Unfolding;
		Object[] unfolding;
		
		/* Settiamo la barra progressiva */
		setProgress(context, 0, 2);
		
		/* Converte la rete di Petri nella rete di unfolding */
		writeLog(context, "Conversion of the Petri net in Unfolding net...");
		petrinet2Unfolding = new BCSSettimoUnfolding(context, petrinet);
		unfolding = petrinet2Unfolding.convert();
		
		/* Aggiungo connessione per la visualizzazione delle reti e statistiche delle rete unfoldata */
		context.addConnection(new BCSUnfoldingConnection((StatisticMap) unfolding[1], petrinet, (Petrinet) unfolding[0]));
		
		return new Object [] {unfolding[1], unfolding[0]};
	}
	
	/**
	 * Setta gli step della barra progressiva
	 * 
	 * @param context contesto di ProM
	 * @param minimun minimo valore
	 * @param maximum massimo valore
	 */
	private void setProgress(PluginContext context, int minimun, int maximum)
	{
		context.getProgress().setMinimum(minimun);
		context.getProgress().setMaximum(maximum);
	}
	
	/**
	 * Scrive un messaggio di log e incrementa la barra progressiva
	 * 
	 * @param context contesto di ProM
	 * @param log messaggio di log
	 */
	private void writeLog(PluginContext context, String log)
	{
		context.log(log);
		context.getProgress().inc();
	}
}