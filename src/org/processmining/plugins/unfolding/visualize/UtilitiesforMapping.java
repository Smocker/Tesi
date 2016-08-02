package org.processmining.plugins.unfolding.visualize;



import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.converters.bpmn2pn.EPetrinetNode;

public class UtilitiesforMapping {	
	
	/**
	 * Ritorna il BPMNNode corrispondente al PetriNode
	 * @param PetriNode pn
	 * @return BPMNNode
	 */
		public static BPMNNode getBPMNNodeFromReverseMap(Map<?,BPMNNode> reverseMap, PetrinetNode pn){
			BPMNNode nod = null;
			EPetrinetNode pnm = new EPetrinetNode(pn);
			if(reverseMap.containsKey(pnm)){
					nod = reverseMap.get(pnm);	
			} 
			return nod;
		}
		
		
		public static BPMNNode getNodeinClone(BPMNDiagram bpmn,BPMNNode node){
			Set<BPMNNode> elenco = bpmn.getNodes();
			if(node!=null)
				for(BPMNNode nodeclone: elenco){
					Object idoc = nodeclone.getAttributeMap().get("Original id");
					Object inode = node.getAttributeMap().get("Original id");
					if( idoc.toString().equals(inode.toString())){
						return nodeclone;
					}
				}
			return null;
		}

		public static BPMNEdge<BPMNNode, BPMNNode> getArcInClone(BPMNDiagram bpmn,BPMNEdge<BPMNNode, BPMNNode> arc){
			Set<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> elencoArchi = bpmn.getEdges();
			if(arc!=null)
				for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> arcClone: elencoArchi){
					Object idoc = arcClone.getAttributeMap().get("Original id");
					Object inode = arc.getAttributeMap().get("Original id");
					if( idoc.toString().equals(inode.toString())){
						return (BPMNEdge<BPMNNode, BPMNNode>) arcClone;
					}
				}
			return null;
		}

}
