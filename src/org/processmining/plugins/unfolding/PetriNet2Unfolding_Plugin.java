package org.processmining.plugins.unfolding;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.support.unfolding.IdentificationMap;

/**
 * Converte un Petrinet in una Occurrence net with Unfolding
 * 
 * @author Daniele Cicciarella
 */
public class PetriNet2Unfolding_Plugin {
	@Plugin(
		name = "Convert Petri net to Unfolding net", 
		parameterLabels = {"Petri net"}, 
		returnLabels = { "Identification Map", "Petri net" }, 
		returnTypes = { IdentificationMap.class, Petrinet.class }, 
		userAccessible = true, 
		help = "Convert Petri net to Unfolding net"
	)
	@UITopiaVariant(
		affiliation = "Universita' di Pisa", 
		author = "Daniele Cicciarella", 
		email = "cicciarellad@gmail.com"
	)
	public Object[] convert(PluginContext context, Petrinet petrinet) 
	{
		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(2);
		PetriNet2Unfolding conv = new PetriNet2Unfolding(context, petrinet);
		Object[] unfolding = conv.convert();
		context.getProgress().inc();
		context.log("ciao");
		
		/* Aggiungo connessione per la visualizzazione delle reti e statistiche delle rete unfoldata */
		context.addConnection(new UnfoldingConnection((IdentificationMap) unfolding[1], petrinet, (Petrinet) unfolding[0]));
		
		return new Object [] {unfolding[1], unfolding[0]} ;
	}
}