package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Metodi utilizzati dalla classe PetriNet2Unfolding.java
 * 
 * @author Daniele Cicciarella
 */
public class Utility 
{	
	/**
	 * Prende la piazza iniziale della rete di Petri
	 *  
	 * @param N rete di petri
	 * @return pn piazza iniziale della rete di Petri o null
	 */
	public static PetrinetNode getStartNode(Petrinet N) 
	{		
		for(PetrinetNode pn: N.getNodes())
			if(N.getGraph().getInEdges(pn).isEmpty())
				return pn;
		return null;
	}
	
	/**
	 * Prende la piazza finale della rete di Petri
	 *  
	 * @param N rete di petri
	 * @return pn piazza finale della rete di Petri o null
	 */
	public static PetrinetNode getEndNode(Petrinet N) 
	{		
		for(PetrinetNode pn: N.getNodes())
			if(N.getGraph().getOutEdges(pn).isEmpty())
				return pn;
		return null;
	}
	
	/**
	 * Restituisce il preset di un nodo
	 * 
	 * @param N rete di petri
	 * @param pn nodo di petri corrente
	 * @return preset arraylist contenente il preset di pn
	 */
	public static ArrayList<PetrinetNode> getPreset(Petrinet N, PetrinetNode pn)
	{
		ArrayList<PetrinetNode> preset = new ArrayList <PetrinetNode> (); 
		for (Iterator<?> i = N.getGraph().getInEdges(pn).iterator(); i.hasNext();) 
		{
			Arc a = (Arc) i.next();
			preset.add(a.getSource());
		}
		return preset;
	}
	
	/**
	 * Restituisce il postset di un nodo
	 * 
	 * @param N rete di petri
	 * @param pn nodo di unfolding corrente
	 * @return postset arraylist di PetrinetNode contenente il postset di pn
	 */
	public static ArrayList<PetrinetNode> getPostset(Petrinet N, PetrinetNode pn)
	{
		ArrayList<PetrinetNode> postset = new ArrayList <PetrinetNode> (); 
		for (Iterator<?> i = N.getGraph().getOutEdges(pn).iterator(); i.hasNext();) 
		{
			Arc a = (Arc) i.next();
			postset.add(a.getTarget());
		}
		return postset;
	}
	
	/**
	 * Restituisce la storia dei place di un nodo
	 * 
	 * @param U rete di unfolding
	 * @param pn nodo di unfolding corrente 
	 * @return H storia dei place del nodo corrente
	 */
	public static ArrayList<Place> getHistoryPlace(Petrinet U, PetrinetNode pn)
	{
		ArrayList <Place> H = new ArrayList <Place> ();
		getBackPlace(U, pn, H);
		return H;
	}

	/**
	 * Visita all'indietro la rete di occorrenze, salvando i place attraversati
	 * 
	 * @param U rete di unfolding
	 * @param pn nodo di unfolding di partenza 
	 * @param H lista dei place di unfolding parziali
	 */
	private static void getBackPlace(Petrinet U, PetrinetNode pn, ArrayList<Place> H) 
	{
		for (Iterator<?> i = U.getGraph().getInEdges(pn).iterator(); i.hasNext();) 
		{
			if(pn instanceof Place) 
				H.add((Place) pn);
			Arc a = (Arc) i.next();
			getBackPlace(U, a.getSource(), H);
		}
	}
	
	/**
	 * Restituisce la storia (place, arc) di un nodo dei xor-split
	 * 
	 * @param U rete di unfolding
	 * @param pn nodo di unfolding di partenza 
	 * @return H storia dei (place,arc) di pn
	 */
	public static ArrayList<Pair <Place, Arc>> getHistoryPlaceConflictXOR(Petrinet U, PetrinetNode pn, Arc a)
	{
		ArrayList <Pair <Place, Arc>> H = new ArrayList <Pair <Place, Arc>> ();
		
		/* Inizializzo H con il nodo iniziale se è uno xor-split */
		if(pn instanceof Place)
			if(U.getGraph().getOutEdges(pn).size() > 1)
				H.add(new Pair<Place, Arc>((Place) pn, a));
		
		getBackPlaceConflictXOR(U, pn, H);
		return H;
	}
	
	/**
	 * Visita all'indietro la rete di occorrenze, salvando i xor-split
	 * 
	 * @param U rete di unfolding
	 * @param pn nodo di unfolding corrente 
	 * @param H storia parziale dei (place,arc)
	 */
	private static void getBackPlaceConflictXOR(Petrinet U, PetrinetNode pn, ArrayList <Pair <Place, Arc>> H) 
	{
		for (Iterator<?> preset = U.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			if(pn instanceof Place) 
			{
				Arc a = (Arc) preset.next();
				Transition t = (Transition) a.getSource();
				getBackPlaceConflictXOR(U, t, H);
			} 
			else 
			{
				Arc a = (Arc) preset.next();
				Place p = (Place) a.getSource();
				
				/* XOR-SPLIT */
				if(U.getGraph().getOutEdges(p).size() > 1)
					H.add(new Pair<Place, Arc>(p,a));
				
				getBackPlaceConflictXOR(U, p, H);
			}
		}
	}
	
	/**
	 * Verifico se due transazioni sono in conflitto
	 * 
	 * @param U rete di unfolding
	 * @param t prima transazione dell'unfolding
	 * @param u seconda transazione dell'unfolding
	 * @return true se condividono almeno uno xor, false altrimenti
	 */
	public static boolean isConflit(Petrinet U, Transition t, Transition u)
	{
		ArrayList <Pair <Place, Arc>> xorT = Utility.getHistoryPlaceConflictXOR(U, t, null);
		ArrayList <Pair <Place, Arc>>xorU = Utility.getHistoryPlaceConflictXOR(U, u, null);
		
		/* Se hanno lo stesso place ma archi diversi è uno xor-split */
		for(int i = 0; i < xorT.size(); i++)
			for(int j = 0; j < xorU.size(); j++)
				if(xorT.get(i).getFirst().equals(xorU.get(j).getFirst()) 
						&& !xorT.get(i).getSecond().equals(xorU.get(j).getSecond()))
					return true;
		return false;
	}
	
	/**
	 * Verifico se vi è un cutoff e se esso provoca la rete unbounded
	 * 
	 * @param cT configurazione locale della transazione t
	 * @param cT1 configurazione locale della transazione t1
	 * @param N rete di Petri originale
	 * @param unf2PetriMap map da unfolding a petrinet
	 * @param T transazione che vogliamo verificare se provoca il cutoff
	 * @param marking mappa da transazione a rispettivo marking
	 * @return intero avente valore 0 se t3 e' un cutoff, 1 se t3 e' un cutoff che provoca la rete unbounded, -1 niente
	 */
	public static int isBounded(
			LocalConfiguration cT, 
			LocalConfiguration cT1, 
			Petrinet N, HashMap <PetrinetNode, PetrinetNode> unf2PetriMap, 
			Transition T, HashMap<PetrinetNode, 
			ArrayList<PetrinetNode>> marking) 
	{
		ArrayList <PetrinetNode> markT = new ArrayList <PetrinetNode> (), markT1 = new ArrayList <PetrinetNode> (), mark;
		
		/* Calcolo il marking di t */
		for(Transition t : cT.get())
		{
			for(PetrinetNode postset : Utility.getPostset(N, unf2PetriMap.get(t)))
				markT.add(postset);
		}
		for(Transition t : cT.get())
		{
			for(PetrinetNode preset : Utility.getPreset(N, unf2PetriMap.get(t)))
				if(markT.contains(preset))
					markT.remove(preset);
		}		
		
		/* Calcolo il marking di t1 */
		for(Transition t : cT1.get())
		{
			for(PetrinetNode postset : Utility.getPostset(N, unf2PetriMap.get(t)))
				markT1.add(postset);
		}
		for(Transition t : cT1.get())
		{
			for(PetrinetNode preset : Utility.getPreset(N, unf2PetriMap.get(t)))
				if(markT1.contains(preset))
					markT1.remove(preset);
		}

		/* Verifico se e' un cutoff */		
		mark = markT;
		for(int i = 0; i < markT1.size(); i++)
		{
			if(!markT.contains(markT1.get(i)))
				return -1;
			else
				markT.remove(markT1.get(i));
		}
		
		/* Verifico se il cutoff è unbounded */
		if (markT.isEmpty())
			return 0;
		else 
		{
			marking.put(T, mark);
			return 1;
		}
	}
	
	/**
	 * Verifico se le due strutture contengono gli stessi elementi
	 * 
	 * @param al arraylist di nodi
	 * @param pnt tupla di nodi
	 * @return true se contengono gli stessi elementi, false altrimenti
	 */
	public static boolean equalsArrayList(ArrayList <PetrinetNode> al, PetrinetNodeTupla pnt)
	{
		for(int i = 0; i < pnt.getElements().length; i++)
			if(!al.contains(pnt.getElements()[i]))
				return false;
		return true;
	}
}