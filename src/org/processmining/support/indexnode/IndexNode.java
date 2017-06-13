package org.processmining.support.indexnode;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

/**
 * IndexNode nodo con un indice associato
 * 
 * @author Maria Tourbanova
 */

public class IndexNode {
	private PetrinetNode node;
	private int index;
	
	public IndexNode(PetrinetNode node, int index) {
		super();
		this.node = node;
		this.index = index;

	}
	
	public PetrinetNode getNode() {
		return node;
	}
	public void setNode(PetrinetNode node) {
		this.node = node;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "["+node + ", " + index + "]";
	}
	
}
