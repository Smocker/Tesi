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
	 * @param length la lunghezza dell'array
	 */
	public Combination(int length)
	{
		elements = new PetrinetNode[length];
	}
	
	/**
	 * Estrae la combinazione
	 * 
	 * @return la combinazione
	 */
	public PetrinetNode[] getElements()
	{
		return elements;
	}

	/**
	 * Setta la combinazione
	 * 
	 * @param elements la combinazione
	 */
	public void setElements(PetrinetNode[] elements) 
	{
		this.elements = elements;
	}
	
	/**
	 * Aggiunge un nuovo elemento alla combinazione
	 * 
	 * @param pn il nodo da aggiungere
	 * @return la combinazione con il nuovo elemento aggiunto
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
	 * @param places le piazze da cui creare tutte le combinazioni
	 * @return la lista delle combinazioni
	 */
	public static ArrayList<Combination> create(ArrayList<ArrayList<PetrinetNode>> places, ArrayList <Combination> combination)
	{
		rec(0, new Combination(), places, combination);
		return combination;
	}
	
	/**
	 * Costruisce in maniera ricorsiva le combinazioni
	 * 
	 * @param step il passo corrente
	 * @param comb la combinazione corrente
	 * @param places le piazze da aggiungere
	 * @param combination le combinazioni parziali
	 */
	private static void rec(int step, Combination comb, ArrayList<ArrayList<PetrinetNode>> places, ArrayList<Combination> combination) 
	{
		int size = places.get(step).size();
		for (int i = 0; i < size; ++i) 
		{
			Combination newComb = comb.add(places.get(step).get(i));
			if (step == places.size() - 1) 
				combination.add(newComb);
			else
				rec(step+1, newComb, places, combination);
		}
	}
	
	/**
	 * Elimina le combinazioni già utilizzate 
	 * 
	 * @param combination le combinazioni correnti
	 * @param t la transezione da aggiungere all'unfolding
	 * @param L1 la map da rete di Petri a rete di Unfolding
	 * @param N1 la rete di unfolding
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
	 * @param N1 la rete di unfolding
	 * @param t la transizione da verificare
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

				/* Se sono all'ultimo elemento della combinazione non ha senso aggiungere qualcosa a XOR */
				if(i != elements.length-1)
				{
					for(Pair node : nodeXOR)
						if(!XOR.contains(node))
							XOR.add(node);
				}
			}
		}
		return false;
	}
	
	/**
	 * Verifico se le piazze della combinazione sono gia' state usate
	 * 
	 * @param preset il preset della transizione da aggiungere nell'unfolding
	 * @return true se la combinazione e' stata usata, false altrimenti
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

	@Override
	public String toString() {
		return "Combination " + Arrays.toString(elements);
	}
	
	
}