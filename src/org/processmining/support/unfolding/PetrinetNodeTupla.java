package org.processmining.support.unfolding;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class PetrinetNodeTupla
{
	private PetrinetNode[] elements;
	
	/**
	 * Costruttore
	 */
	public PetrinetNodeTupla()
	{
		elements = new PetrinetNode[0];
	}
	
	/**
	 * Costruttore
	 */
	public PetrinetNodeTupla(int dim)
	{
		elements = new PetrinetNode[dim];
	}
				
	/**
	 * Aggiunge un nuovo elemento alla tupla
	 * 
	 * @param pn: PetrinetNode da aggiungere
	 * @return la tupla con il nuovo elemento aggiunto
	 */
	public PetrinetNodeTupla add(PetrinetNode pn)
	{
		PetrinetNodeTupla tupla = new PetrinetNodeTupla(this.elements.length+1);
		for(int j = 0; j < this.elements.length; ++j) 
		{
			tupla.elements[j] = this.elements[j];
		}		
		tupla.elements[this.elements.length] = pn;
		return tupla;
	}
	
	/**
	 * Estraggo l'array di PetrinetNode
	 * 
	 * @return array di PetrinetNode
	 */
	public PetrinetNode[] getElements()
	{
		return elements;
	}

	/**
	 * Setto l'array di PetrinetNode
	 * 
	 * @param elements: array di PetrinetNode
	 */
	public void setElements(PetrinetNode[] elements) 
	{
		this.elements = elements;
	}

}