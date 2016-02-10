package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Crea le combinazione delle piazze
 * 
 * @author Daniele Cicciarella
 */
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
	
	/**
	 * Crea tutte le possibili combinazioni
	 * 
	 * @param places piazza da cui creare tutte le combinazioni
	 * @return result ArrayList<PetrinetNode[]> contenente le combinazioni
	 */
	public static ArrayList<PetrinetNodeTupla> createCombination(ArrayList<ArrayList<PetrinetNode>> places)
	{
		ArrayList<PetrinetNodeTupla> result = new ArrayList<PetrinetNodeTupla>();
		recCombination(0, new PetrinetNodeTupla(), places, result);
		return result;
	}
	
	/**
	 * Costruisce in maniera ricorsiva le tuple
	 * 
	 * @param step passo corrente
	 * @param tupla tupla corrente
	 * @param places piazze da aggiungere
	 * @param result tupla parziale
	 */
	private static void recCombination(int step, PetrinetNodeTupla tupla, ArrayList<ArrayList<PetrinetNode>> places, ArrayList<PetrinetNodeTupla> result ) 
	{
		int size = places.get(step).size();
		for (int i = 0; i < size; ++i) 
		{
			PetrinetNodeTupla newTupla = tupla.add(places.get(step).get(i));
			if (step == places.size() - 1) 
				result.add(newTupla);
			else
				recCombination(step+1, newTupla, places, result);
		}
	}
	
	/**
	 * Elimina le combinazioni già utilizzate 
	 * 
	 * @param combination combinazioni correnti
	 * @param transazione da aggiungere all'unfolding
	 * @param L1 mappa da rete di Petri a rete di Unfolding
	 * @param N1 rete di unfolding
	 */
	public static void filterCombination(ArrayList<PetrinetNodeTupla> combination, Transition t, HashMap<PetrinetNode, ArrayList<PetrinetNode>> L1, Petrinet N1) 
	{
		ArrayList <PetrinetNode> preset = null;
		
		/* Se non è contenuto allora t non è stato mai inserito */
		if(L1.containsKey(t))
		{
			for(PetrinetNode t1 : L1.get(t)) 
			{
				preset = Utility.getPreset(N1, t1);
				for(int j = 0; j < combination.size(); j++)
					if(Utility.isEquals(preset, combination.get(j)))
						combination.remove(j);						
			}
		}
	}
}