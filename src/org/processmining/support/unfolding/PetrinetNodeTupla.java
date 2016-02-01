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
	 * 
	 * @param dim dimensione dell'array
	 */
	public PetrinetNodeTupla(int dim)
	{
		elements = new PetrinetNode[dim];
	}
				
	/**
	 * Aggiunge un nuovo elemento alla tupla
	 * 
	 * @param pn PetrinetNode da aggiungere
	 * @return la tupla con il nuovo elemento aggiunto
	 */
	public PetrinetNodeTupla add(PetrinetNode pn)
	{
		PetrinetNodeTupla tupla = new PetrinetNodeTupla(elements.length+1);
		for(int j = 0; j < elements.length; ++j) 
		{
			tupla.elements[j] = elements[j];
		}		
		tupla.elements[elements.length] = pn;
		return tupla;
	}
	
	/**
	 * Estraggo l'array di PetrinetNode
	 * 
	 * @return lista di nodi
	 */
	public PetrinetNode[] getElements()
	{
		return elements;
	}

	/**
	 * Setto l'array di PetrinetNode
	 * 
	 * @param elements lista di nodi
	 */
	public void setElements(PetrinetNode[] elements) 
	{
		this.elements = elements;
	}
}