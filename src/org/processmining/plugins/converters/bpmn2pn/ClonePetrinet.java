package org.processmining.plugins.converters.bpmn2pn;

import java.util.HashMap;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

public class ClonePetrinet extends PetrinetImpl {
	
	

	public ClonePetrinet(String label) {
		super(label);
	}

	public void cloneFrom(Petrinet net,
			boolean transitions, boolean places, boolean arcs, boolean resets, boolean inhibitors) {

		HashMap<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

		if (transitions) {
			for (Transition t : net.getTransitions()) {
				Transition copy = addTransition(t.getLabel());
				copy.setInvisible(t.isInvisible());
				copy.getAttributeMap().put("Original id", t.getAttributeMap().get("Original id"));
				mapping.put(t, copy);
			}
		}
		if (places) {
			for (Place p : net.getPlaces()) {
				Place copy = addPlace(p.getLabel());
				copy.getAttributeMap().put("Original id", p.getAttributeMap().get("Original id"));
				mapping.put(p, copy);
			}
		}
		if (arcs) {
			for (  PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e:net.getEdges()) {
				if(e instanceof Arc){
				Arc a = (Arc) e;
				mapping.put(a, addArcPrivate((PetrinetNode) mapping.get(a.getSource()), (PetrinetNode) mapping.get(a
						.getTarget()), a.getWeight(), a.getParent()));
				}
			}
		}
		
		getAttributeMap().clear();
		AttributeMap map = net.getAttributeMap();
		for (String key : map.keySet()) {
			getAttributeMap().put(key, map.get(key));
		}
	}

}