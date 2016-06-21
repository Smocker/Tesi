package org.processmining.plugins.converters.bpmn2pn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.plugins.unfolding.PetrinetNodeMod;

public class InfoConversionBP2PN {
	
	/**
	 * maps each start event to an arraylist of place 
	 */
	private Map <NodeID, ArrayList <Place>> startEventMap = new HashMap <NodeID, ArrayList <Place>> ();
	
	/**
	 * maps each end event to an arraylist of place 
	 */
	private Map <NodeID, ArrayList <Place>> endEventMap = new HashMap <NodeID, ArrayList <Place>> ();
	
	/**
	 * maps each BPMN control-flow edge to a place
	 */
	private Map<BPMNEdge<BPMNNode, BPMNNode>, Place> flowMap = new HashMap<BPMNEdge<BPMNNode, BPMNNode>, Place>();
	/**
	 * maps each BPMN node to a set of Petri net nodes (transitions and places)
	 */
	private Map<BPMNNode, Set<PetrinetNode>> nodeMap = new HashMap<BPMNNode, Set<PetrinetNode>>();
	
	/*Maps Petri net node to BPMN node*/
	private Map<PetrinetNodeMod,BPMNNode> reverseMap = new HashMap<PetrinetNodeMod,BPMNNode>();
		
	public InfoConversionBP2PN(Map<NodeID, ArrayList<Place>> startEventMap,
			Map<NodeID, ArrayList<Place>> endEventMap,
			Map<BPMNEdge<BPMNNode, BPMNNode>, Place> flowMap,
			Map<BPMNNode, Set<PetrinetNode>> nodeMap,Map<PetrinetNodeMod,BPMNNode> reverseMap) {
		super();
		this.startEventMap = startEventMap;
		this.endEventMap = endEventMap;
		this.flowMap = flowMap;
		this.nodeMap = nodeMap;
		this.reverseMap = reverseMap;
	}
	public InfoConversionBP2PN(
			Map<BPMNEdge<BPMNNode, BPMNNode>, Place> flowMap,
			Map<BPMNNode, Set<PetrinetNode>> nodeMap,Map<PetrinetNodeMod,BPMNNode> reverseMap) {
		super();
		this.flowMap = flowMap;
		this.nodeMap = nodeMap;
		this.reverseMap = reverseMap;
	}
	public Map<NodeID, ArrayList<Place>> getStartEventMap() {
		return startEventMap;
	}
	public Map<NodeID, ArrayList<Place>> getEndEventMap() {
		return endEventMap;
	}
	public Map<BPMNEdge<BPMNNode, BPMNNode>, Place> getFlowMap() {
		return flowMap;
	}
	public Map<BPMNNode, Set<PetrinetNode>> getNodeMap() {
		return nodeMap;
	}

	public Map<PetrinetNodeMod,BPMNNode> getReverseMap() {
		return reverseMap;
	}
	

}
