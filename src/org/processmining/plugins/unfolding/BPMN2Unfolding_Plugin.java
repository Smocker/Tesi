package org.processmining.plugins.unfolding;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter;
import org.processmining.support.unfolding.IdentificationMap;

/**
 * Converte un BPMNDiagram in una Occurrence net with Unfolding
 * 
 * @author Daniele Cicciarella
 */
public class BPMN2Unfolding_Plugin {
	@Plugin(
		name = "Convert BPMN diagram to Unfolding net", 
		parameterLabels = {"Petri net"},
		returnLabels = {  "Identification Map","Petri net" }, 
		returnTypes = {  IdentificationMap.class,Petrinet.class }, 
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
		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(3);
		
		/* Converte BPMNDiagram in Petri net */
		BPMN2PetriNetConverter bpmn2petrinet = new BPMN2PetriNetConverter(bpmn);
		bpmn2petrinet.convert();
		petrinet = bpmn2petrinet.getPetriNet();
		context.log("BPMN convertito in rete di Petri");
		context.getProgress().inc();

		/* Converte Petri net in una Occurrence net con Unfolding */
		PetriNet2Unfolding petrinet2unfolding = new PetriNet2Unfolding(context, petrinet);
		unfolding = petrinet2unfolding.convert();
		context.getProgress().inc();
		
		/* Aggiungo connessione per la visualizzazione delle reti e statistiche delle rete unfoldata */
		context.addConnection(new UnfoldingConnection((IdentificationMap)unfolding[1], petrinet,(Petrinet) unfolding[0]));
		
		return new Object [] {unfolding[1], unfolding[0]};
	}
}
