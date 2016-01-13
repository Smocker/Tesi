package org.processmining.support.unfolding;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class PetrinetNodeTupla
{
	private PetrinetNode[] elements;
	
	public PetrinetNodeTupla()
	{
		elements = new PetrinetNode[0];
	}
	
	public PetrinetNodeTupla(int dim)
	{
		elements = new PetrinetNode[dim];
	}
				
	public PetrinetNodeTupla add(PetrinetNode pn)
	{
		PetrinetNodeTupla tupla = new PetrinetNodeTupla(this.elements.length+1);
		for(int j = 0; j < this.elements.length; ++j) {
			tupla.elements[j] = this.elements[j];
		}		
		tupla.elements[this.elements.length] = pn;
		return tupla;
	}
	
	public PetrinetNode[] getElements() {
		return elements;
	}

	public void setElements(PetrinetNode[] elements) {
		this.elements = elements;
	}

}