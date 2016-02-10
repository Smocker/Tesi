package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Crea le combinazione delle piazze
 * 
 * @author Daniele Cicciarella
 */
public class Combination
{
	private PetrinetNode[] elements;
	
	/**
	 * Costruttore
	 */
	public Combination()
	{
		elements = new PetrinetNode[0];
	}

	/**
	 * Costruttore
	 * 
	 * @param dim dimensione dell'array
	 */
	public Combination(int dim)
	{
		elements = new PetrinetNode[dim];
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
	 * Aggiunge un nuovo elemento alla tupla
	 * 
	 * @param pn PetrinetNode da aggiungere
	 * @return la tupla con il nuovo elemento aggiunto
	 */
	public Combination add(PetrinetNode pn)
	{
		Combination tupla = new Combination(elements.length+1);
		for(int j = 0; j < elements.length; ++j) 
		{
			tupla.elements[j] = elements[j];
		}		
		tupla.elements[elements.length] = pn;
		return tupla;
	}
	
	/**
	 * Crea tutte le possibili combinazioni
	 * 
	 * @param possibleCombination piazza da cui creare tutte le combinazioni
	 * @return result ArrayList<PetrinetNode[]> contenente le combinazioni
	 */
	public static ArrayList<Combination> create(ArrayList<ArrayList<PetrinetNode>> possibleCombination, ArrayList <Combination> combination)
	{
		rec(0, new Combination(), possibleCombination, combination);
		return combination;
	}
	
	/**
	 * Costruisce in maniera ricorsiva le tuple
	 * 
	 * @param step passo corrente
	 * @param tupla tupla corrente
	 * @param places piazze da aggiungere
	 * @param result tupla parziale
	 */
	private static void rec(int step, Combination tupla, ArrayList<ArrayList<PetrinetNode>> places, ArrayList<Combination> result) 
	{
		int size = places.get(step).size();
		for (int i = 0; i < size; ++i) 
		{
			Combination newTupla = tupla.add(places.get(step).get(i));
			if (step == places.size() - 1) 
				result.add(newTupla);
			else
				rec(step+1, newTupla, places, result);
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
	public static void filter(ArrayList<Combination> combination, Transition t, HashMap<PetrinetNode, ArrayList<PetrinetNode>> L1, Petrinet N1) 
	{
		PetrinetNode [] preset = null;
		
		/* Se non è contenuto allora t non è stato mai inserito */
		if(L1.containsKey(t))
		{
			for(PetrinetNode t1 : L1.get(t)) 
			{
				preset = Utility.getPreset(N1, t1);
				for(int i = 0; i < combination.size(); i++)
				{
					if(combination.get(i).isEquals(preset))
						combination.remove(i);
				}					
			}
		}
	}

	/**
	 * Verifica se le piazze della combinazione sono in conflitto con la transizione
	 * 
	 * @param N1 rete di unfolding
	 * @param t transizione da verificare
	 * @return true se non c'e' conflitto con t, false altrimenti
	 */
	public boolean isConflict(Petrinet N1, Transition t) 
	{
		ArrayList <Pair> XOR = new ArrayList <Pair> (), nodeXOR = null;
		
		for(int i = 0; i < elements.length; i++)
		{
			nodeXOR = Utility.getHistoryXOR(N1, elements[i], N1.getArc(elements[i], t));
			if(!nodeXOR.isEmpty())
			{
				/* Se due piazze condividono lo stesso xor ma provengono da percorsi diversi è un conflitto */
				if(Utility.isConflict(XOR, nodeXOR))
					return true;

				for(int j = 0; j < nodeXOR.size(); j++)
					if(!XOR.contains(nodeXOR.get(j)))
						XOR.add(nodeXOR.get(j));
			}
		}
		return false;
	}
	
	/**
	 * Verifico se le due strutture contengono gli stessi elementi
	 * 
	 * @param preset arraylist di nodi
	 * @param pnt tupla di nodi
	 * @return true se contengono gli stessi elementi, false altrimenti
	 */
	private boolean isEquals(PetrinetNode[] preset)
	{
		/* Ordino gli array */
		Arrays.sort(preset);
		Arrays.sort(elements);
		
		for(int i = 0; i < elements.length; i++)
			if(!preset[i].equals(elements[i]))
				return false;
		return true;
	}
}