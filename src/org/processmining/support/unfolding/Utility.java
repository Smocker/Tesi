package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.models.graphbased.directed.DirectedGraphEdge;
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
	 * @param N rete di Petri
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
	 * @param N rete di Petri
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
	 * @param N rete di Petri
	 * @param pn nodo di Petri corrente
	 * @return arraylist contenente il preset di pn
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
	 * @param N rete di Petri
	 * @param pn nodo di unfolding corrente
	 * @return arraylist di PetrinetNode contenente il postset di pn
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
	 * @param L1 mappa da rete di Petri a rete di Unfolding
	 * @return preset di t se è abilitata, null altrimenti
	 */
	public static ArrayList<PetrinetNode> isEnabled(Petrinet N, PetrinetNode t, HashMap <PetrinetNode, ArrayList<PetrinetNode>> L1)
	{		
		ArrayList <PetrinetNode> preset = getPreset(N, t); 
		if(preset.size() > 1) 
		{
			for(int i = 0; i < preset.size(); i++)
				if(!L1.containsKey(preset.get(i))) 
					return null;
		}
		return preset;
	}
	
	/**
	 * Calcola il marking di un nodo
	 * 
	 * @param N rete di Petri
	 * @param C configurazione locale del nodo
	 * @param L mappa da unfolding a rete di Petri
	 * @return marking del nodo
	 */
	public static ArrayList <PetrinetNode> getMarking(Petrinet N, LocalConfiguration C, HashMap <PetrinetNode, PetrinetNode> L)
	{
		ArrayList <PetrinetNode> marking = new ArrayList <PetrinetNode> ();
		
		for(Transition t : C.get())
		{
			for(DirectedGraphEdge<?, ?> a: N.getGraph().getOutEdges(L.get(t)))
				marking.add((PetrinetNode) a.getTarget());
		}
		for(Transition t : C.get())
		{
			for(DirectedGraphEdge<?, ?> a: N.getGraph().getInEdges(L.get(t)))
				if(marking.contains(a.getSource()))
					marking.remove(a.getSource());
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
		getBackPlacePlace(N1, pn, H);
		return H;
	}

	/**
	 * Visita all'indietro la rete di occorrenze, salvando i place attraversati
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding di partenza 
	 * @param H lista dei place di unfolding parziali
	 */
	private static void getBackPlacePlace(Petrinet N1, PetrinetNode pn, ArrayList<Place> H) 
	{
		for (Iterator<?> preset = N1.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			/* Se è un place non contenuto in H lo aggiungo */
			if(pn instanceof Place) 
			{
				if(!H.contains(pn))
					H.add((Place) pn);
				else
					return;
			}
			Arc a = (Arc) preset.next();
			getBackPlacePlace(N1, a.getSource(), H);
		}
	}
	
	/**
	 * Restituisce la storia (place, arc) di un nodo dei xor-split
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding di partenza 
	 * @return storia dei (place,arc) di pn
	 */
	public static ArrayList<Pair> getHistoryXOR(Petrinet N1, PetrinetNode pn, Arc a)
	{
		ArrayList <Pair> H = new ArrayList <Pair> ();
		
		/* Inizializzo H con il nodo iniziale se è uno xor-split */
		if(pn instanceof Place)
			if(N1.getGraph().getOutEdges(pn).size() > 1)
				H.add(new Pair((Place) pn, a));
		
		getBackPlaceXOR(N1, pn, H);
		return H;
	}
	
	/**
	 * Visita all'indietro la rete di occorrenze, salvando i xor-split
	 * 
	 * @param N1 rete di unfolding
	 * @param pn nodo di unfolding corrente 
	 * @param storia parziale delle coppie (place, arc)
	 */
	private static void getBackPlaceXOR(Petrinet N1, PetrinetNode pn, ArrayList <Pair> H) 
	{
		for (Iterator<?> preset = N1.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			Arc a = (Arc) preset.next();
			
			/* Se il nodo corrente è una transizione verifico se il suo preset contiene xor-split */
			if(pn instanceof Transition)
			{
				if(N1.getGraph().getOutEdges(a.getSource()).size() > 1)
				{
					Pair pair = new Pair((Place) a.getSource(), a);
					if (!H.contains(pair))
						H.add(pair);
					else
						return;
				}
			}
			getBackPlaceXOR(N1, a.getSource(), H);
		}
	}
	
	/**
	 * Verifico se due transazioni sono in conflitto
	 * 
	 * @param lista degli xor-split della transazione t
	 * @param lista degli xor-split della transazione u
	 * @return true se condividono almeno uno xor, false altrimenti
	 */
	public static boolean isConflict(ArrayList <Pair> xorT, ArrayList <Pair> xorU)
	{		
		/* Se hanno lo stesso place ma archi diversi è uno xor-split */
		for(int i = 0; i < xorT.size(); i++)
			for(int j = 0; j < xorU.size(); j++)
				if(xorT.get(i).isConflict(xorU.get(j)))
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
	public static boolean isEquals(ArrayList <PetrinetNode> al, PetrinetNodeTupla pnt)
	{
		for(int i = 0; i < pnt.getElements().length; i++)
			if(!al.contains(pnt.getElements()[i]))
				return false;
		return true;
	}
}