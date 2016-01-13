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
		returnLabels = { "Petri net", "Identification Map" }, 
		returnTypes = { Petrinet.class, IdentificationMap.class }, 
		userAccessible = true, 
		help = "Convert Petri net to Unfolding net"
	)
	@UITopiaVariant(
		affiliation = "Università di Pisa", 
		author = "Daniele Cicciarella", 
		email = "cicciarellad@gmail.com"
	)
	public Object[] convert(PluginContext context, Petrinet net) 
	{
		PetriNet2Unfolding conv = new PetriNet2Unfolding(net);
		Object[] unfolding = conv.convert();
		return new Object [] {unfolding[0], unfolding[1]} ;
	}
}