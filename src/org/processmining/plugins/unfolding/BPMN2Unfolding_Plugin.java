package org.processmining.plugins.unfolding;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter;
import org.processmining.support.unfolding.IdentificationMap;

import sun.misc.Contended;

/**
 * Converte un BPMNDiagram in una Occurrence net with Unfolding
 * 
 * @author Daniele Cicciarella
 */
public class BPMN2Unfolding_Plugin {
	@Plugin(
		name = "Convert BPMN diagram to Unfolding net", 
		parameterLabels = {"Petri net"},
		returnLabels = { "Petri net", "Identification Map" }, 
		returnTypes = { Petrinet.class, IdentificationMap.class }, 
		userAccessible = true, 
		help = "Convert BPMN diagram to Unfolding net"
		)
	@UITopiaVariant(
		affiliation = "Università di Pisa", 
		author = "Daniele Cicciarella", 
		email = "cicciarellad@gmail.com"
		)
	public Object[] convert(PluginContext context, BPMNDiagram bpmn) 
	{
		Petrinet petrinet;
		Object[] unfolding;
		
		/* Converte BPMNDiagram in Petri net */
		BPMN2PetriNetConverter bpmn2petrinet = new BPMN2PetriNetConverter(bpmn);
		bpmn2petrinet.convert();
		petrinet = bpmn2petrinet.getPetriNet();
		
		/* Converte Petri net in una Occurrence net con Unfolding */
		PetriNet2Unfolding petrinet2unfolding = new PetriNet2Unfolding(petrinet);
		unfolding = petrinet2unfolding.convert();
		
		context.addConnection(new UnfoldingConnection((IdentificationMap)unfolding[1], bpmn, petrinet,(Petrinet) unfolding[0]));
		
		return new Object [] {unfolding[0], unfolding[1]};
	}
}
