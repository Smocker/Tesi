package org.processmining.plugins.unfolding;

import org.processmining.models.graphbased.AbstractGraphNode;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;

public class PetrinetNodeMod extends PetrinetNode  {

	public PetrinetNodeMod(
			AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> net,
			ExpandableSubNet parent, String label) {
		super(net, parent, label);
		// TODO Auto-generated constructor stub
	}
	
	public PetrinetNodeMod(PetrinetNode pn){
		super(pn.getGraph(), pn.getParent(), pn.getLabel());
	}
	
	public int hashCode() {
		return getLabel().length();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AbstractGraphNode)) {
			return false;
		}
		AbstractGraphNode node = (AbstractGraphNode) o;
		
		return node.getLabel().trim().equals(getLabel().trim());
	}

}
