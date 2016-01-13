package org.processmining.plugins.unfolding;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.support.unfolding.IdentificationMap;


public class UnfoldingConnection extends AbstractStrongReferencingConnection {
	
	public static String imap = "IMAP";
	public static String bpmn = "BPMN";
	public static String InputPetriNet = "InputPetriNet";
	public static String unfolded = "UnfoldedPN";

	public UnfoldingConnection(IdentificationMap imap, BPMNDiagram diagram, Petrinet inputp, Petrinet unfolded) {
		super("UnfoldingConnection");
		putStrong(this.imap,imap);
		putStrong(this.bpmn,diagram);
		putStrong(this.InputPetriNet,inputp);
		putStrong(this.unfolded,unfolded);
				
	}
	
	
	

}
