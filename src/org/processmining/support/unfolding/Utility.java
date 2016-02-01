package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.support.localconfiguration.LocalConfiguration;

/**
 * Metodi di supporto alla conversione delle rete di Petri in reti di unfolding
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
	 * Verifica se un nodo è abilitato
	 * 
	 * @param N rete di Petri
	 * @param t nodo da verificare
	 * @param petri2UnfMap map da rete di Petri a rete di Unfolding
	 * @return preset di t se è abilitata, null altrimenti
	 */
	public static ArrayList<PetrinetNode> isEnabled(Petrinet N, PetrinetNode t, HashMap <PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap)
	{		
		ArrayList <PetrinetNode> preset = getPreset(N, t); 
		if(preset.size() > 1) 
		{
			for(int i = 0; i < preset.size(); i++)
				if(!petri2UnfMap.containsKey(preset.get(i))) 
					return null;
		}
		return preset;
	}
	
	/**
	 * Restituisce il marking di un nodo
	 * 
	 * @param N rete di Petri
	 * @param localConfiguration configurazione locale del nodo
	 * @param L map da unfolding a petrinet
	 * @return marking del nodo
	 */
	public static ArrayList <PetrinetNode> getMarking(Petrinet N, LocalConfiguration localConfiguration, HashMap <PetrinetNode, PetrinetNode> L)
	{
		ArrayList <PetrinetNode> marking = new ArrayList <PetrinetNode> ();
		
		for(Transition t : localConfiguration.get())
		{
			for(PetrinetNode postset : Utility.getPostset(N, L.get(t)))
				marking.add(postset);
		}
		for(Transition t : localConfiguration.get())
		{
			for(PetrinetNode preset : Utility.getPreset(N, L.get(t)))
				if(marking.contains(preset))
					marking.remove(preset);
		}
		return marking;
	}
	
	/**
	 * Restituisce la storia dei place di un nodo
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding corrente 
	 * @return H storia dei place del nodo corrente
	 */
	public static ArrayList<Place> getHistoryPlace(Petrinet N1, PetrinetNode pn)
	{
		ArrayList <Place> H = new ArrayList <Place> ();
		getBackPlace(N1, pn, H);
		return H;
	}

	/**
	 * Visita all'indietro la rete di occorrenze, salvando i place attraversati
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding di partenza 
	 * @param H lista dei place di unfolding parziali
	 */
	private static void getBackPlace(Petrinet N1, PetrinetNode pn, ArrayList<Place> H) 
	{
		for (Iterator<?> i = N1.getGraph().getInEdges(pn).iterator(); i.hasNext();) 
		{
			if(pn instanceof Place) 
				H.add((Place) pn);
			Arc a = (Arc) i.next();
			getBackPlace(N1, a.getSource(), H);
		}
	}
	
	/**
	 * Restituisce la storia (place, arc) di un nodo dei xor-split
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding di partenza 
	 * @return H storia dei (place,arc) di pn
	 */
	public static ArrayList<Pair <Place, Arc>> getHistoryPlaceConflictXOR(Petrinet N1, PetrinetNode pn, Arc a)
	{
		ArrayList <Pair <Place, Arc>> H = new ArrayList <Pair <Place, Arc>> ();
		
		/* Inizializzo H con il nodo iniziale se è uno xor-split */
		if(pn instanceof Place)
			if(N1.getGraph().getOutEdges(pn).size() > 1)
				H.add(new Pair<Place, Arc>((Place) pn, a));
		
		getBackPlaceConflictXOR(N1, pn, H);
		return H;
	}
	
	/**
	 * Visita all'indietro la rete di occorrenze, salvando i xor-split
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding corrente 
	 * @param H storia parziale dei (place,arc)
	 */
	private static void getBackPlaceConflictXOR(Petrinet N1, PetrinetNode pn, ArrayList <Pair <Place, Arc>> H) 
	{
		for (Iterator<?> preset = N1.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			if(pn instanceof Place) 
			{
				Arc a = (Arc) preset.next();
				Transition t = (Transition) a.getSource();
				getBackPlaceConflictXOR(N1, t, H);
			} 
			else 
			{
				Arc a = (Arc) preset.next();
				Place p = (Place) a.getSource();
				
				/* XOR-SPLIT */
				if(N1.getGraph().getOutEdges(p).size() > 1)
					H.add(new Pair<Place, Arc>(p,a));
				
				getBackPlaceConflictXOR(N1, p, H);
			}
		}
	}
	
	/**
	 * Verifico se due transazioni sono in conflitto
	 * 
	 * @param lista degli xor-split della transazione t
	 * @param lista degli xor-split della transazione u
	 * @return true se condividono almeno uno xor, false altrimenti
	 */
	public static boolean isConflit(ArrayList <Pair <Place, Arc>> xorT, ArrayList <Pair <Place, Arc>>xorU)
	{		
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
	 * @param markingT marking della transazione t
	 * @param markingF marking della configurazione f
	 * @return intero che indica se c'è cutoff e di che tipo è
	 */
	public static int isBounded(ArrayList <PetrinetNode> markingT, ArrayList <PetrinetNode> markingF) 
	{
		ArrayList <PetrinetNode> markT = markingT, markT1 = markingF;

		/* Verifichiamo se t è un cutoff */
		for(int i = 0; i < markT1.size(); i++)
		{
			if(!markT.contains(markT1.get(i)))
				return -1;
			else
				markT.remove(markT1.get(i));
		}
		
		/* Verifico il suo tipo */
		if (markT.isEmpty())
			return 0;
		else 
			return 1;
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