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
	 * Prende la piazza iniziale della rete di petri
	 *  
	 * @param petrinet: rete di petri
	 * @return pn: piazza iniziale o null se la rete ha un nodo senza archi in ingresso
	 */
	public static PetrinetNode getStartNode(Petrinet petrinet) 
	{		
		for(PetrinetNode pn: petrinet.getNodes())
			if(petrinet.getGraph().getInEdges(pn).isEmpty())
				return pn;
		return null;
	}
	
	/**
	 * Restituisce il preset di un nodo
	 * 
	 * @param net: rete di petri
	 * @param pn: nodo corrente
	 * @return preset: arraylist contenente il preset di pn
	 */
	public static ArrayList<PetrinetNode> getPreset(Petrinet net, PetrinetNode pn)
	{
		ArrayList<PetrinetNode> array = new ArrayList <PetrinetNode> (); 
		for (Iterator<?> inEdge = net.getGraph().getInEdges(pn).iterator(); inEdge.hasNext();) 
		{
			Arc a = (Arc) inEdge.next();
			array.add(a.getSource());
		}
		return array;
	}
	
	/**
	 * Restituisce il postset di un nodo
	 * 
	 * @param net: rete di petri
	 * @param pn: nodo corrente
	 * @return array: arraylist contenente il postset di pn
	 */
	public static ArrayList<PetrinetNode> getPostset(Petrinet net, PetrinetNode pn)
	{
		ArrayList<PetrinetNode> array = new ArrayList <PetrinetNode> (); 
		for (Iterator<?> outEdge = net.getGraph().getOutEdges(pn).iterator(); outEdge.hasNext();) 
		{
			Arc a = (Arc) outEdge.next();
			array.add(a.getTarget());
		}
		return array;
	}
	
	/**
	 * Restituisce la storia dei place di un nodo
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo corrente 
	 * @return history: storia dei place del nodo corrente
	 */
	public static ArrayList<Place> getHistoryPlace(Petrinet unfolding, PetrinetNode pn)
	{
		ArrayList <Place> history = new ArrayList <Place> ();
		getBackPlace(unfolding, pn, history);
		return history;
	}

	/**
	 * Visita all'indietro la rete di occorrenze, salvando i place attraversati
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo di partenza 
	 */
	private static void getBackPlace(Petrinet unfolding, PetrinetNode pn, ArrayList<Place> history) 
	{
		for (Iterator<?> inEdge = unfolding.getGraph().getInEdges(pn).iterator(); inEdge.hasNext();) 
		{
			if(pn instanceof Place) 
			{
				Arc a = (Arc) inEdge.next();
				Transition t = (Transition) a.getSource();
				Place p = (Place) pn;
				history.add(p);
				getBackPlace(unfolding, t, history);
			} 
			else 
			{
				Arc a = (Arc) inEdge.next();
				Place p = (Place) a.getSource();
				getBackPlace(unfolding, p, history);
			}
		}
	}
	
	/**
	 * Verifico se il marking di due configurazione provocano la rete bounded
	 * 
	 * @param cT
	 * @param cT1
	 * @param petrinet
	 * @param unf2PetriMap
	 * @param t3 
	 * @param marking 
	 * @return int
	 */
	public static int isBounded(LocalConfiguration cT, LocalConfiguration cT1, Petrinet petrinet, HashMap <PetrinetNode, PetrinetNode> unf2PetriMap, Transition t3, HashMap<PetrinetNode, ArrayList<PetrinetNode>> marking) 
	{
		ArrayList <PetrinetNode> markT = new ArrayList <PetrinetNode> (), markT1 = new ArrayList <PetrinetNode> (), mark;
		
		/* Calcolo il marking di t: postset(H(t)) - preset(H(t)) */
		for(Transition t : cT.get())
		{
			for(PetrinetNode postset : Utility.getPostset(petrinet, unf2PetriMap.get(t)))
				markT.add(postset);
		}
		for(Transition t : cT.get())
		{
			for(PetrinetNode preset : Utility.getPreset(petrinet, unf2PetriMap.get(t)))
			{
				if(markT.contains(preset))
					markT.remove(preset);
			}
		}		
		
		/* Calcolo il marking di t1 */
		for(Transition t : cT1.get())
		{
			for(PetrinetNode postset : Utility.getPostset(petrinet, unf2PetriMap.get(t)))
				markT1.add(postset);
		}
		for(Transition t : cT1.get())
		{
			for(PetrinetNode preset : Utility.getPreset(petrinet, unf2PetriMap.get(t)))
			{
				if(markT1.contains(preset))
					markT1.remove(preset);
			}
		}

		/* Verifico se è un cut-off e se provoca la rete unbounded */		
		mark = markT;
		for(int i = 0; i < markT1.size(); i++)
		{
			if(!markT.contains(markT1.get(i)))
				return -1;
			else
				markT.remove(markT1.get(i));
		}
		if (markT.isEmpty())
			return 0;
		else 
		{
			marking.put(t3, mark);
			return 1;
		}
	}
	
	/**
	 * Restituisce la storia di un nodo avente i place con più di un arco in uscita
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo corrente 
	 * @return history: storia dei place del nodo corrente
	 */
	public static ArrayList<Place> getHistoryPlaceXOR(Petrinet unfolding, PetrinetNode pn)
	{
		ArrayList <Place> history = new ArrayList <Place> ();
		getBackPlaceXOR(unfolding, pn, history);
		return history;
	}
	
	/**
	 * Visita all'indietro la rete di occorrenze, salvando i place con più di due archi in uscita
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo di partenza 
	 */
	private static void getBackPlaceXOR(Petrinet unfolding, PetrinetNode pn, ArrayList<Place> history) {
		for (Iterator<?> preset = unfolding.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			if(pn instanceof Place) 
			{
				if(unfolding.getGraph().getOutEdges(pn).size() > 1)
					if(!history.contains(pn))
						history.add((Place) pn);
				Arc a = (Arc) preset.next();
				Transition t = (Transition) a.getSource();
				getBackPlaceXOR(unfolding, t, history);
			} 
			else 
			{
				Arc a = (Arc) preset.next();
				Place p = (Place) a.getSource();
				getBackPlaceXOR(unfolding, p, history);
			}
		}
	}
	
	/**
	 * Restituisce la storia di un nodo avente i place con più di un arco in uscita
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo corrente 
	 * @return history: storia dei place del nodo corrente
	 */
	public static ArrayList <Pair <Place, Arc>> getHistoryPlaceConflictXOR(Petrinet unfolding, PetrinetNode pn, Arc a)
	{
		ArrayList <Pair <Place, Arc>> history = new ArrayList <Pair <Place, Arc>> ();
		
		/* Inizializzo l'ArrayList history con il nodo iniziale se ha più di un arco in uscita */
		if(unfolding.getGraph().getOutEdges(pn).size() > 1)
			history.add(new Pair<Place, Arc>((Place) pn, a));
		
		getBackPlaceConflictXOR(unfolding, pn, history);
		return history;
	}
	
	/**
	 * Visita all'indietro la rete di occorrenze, salvando i place con più di due archi in uscita
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo di partenza 
	 */
	private static void getBackPlaceConflictXOR(Petrinet unfolding, PetrinetNode pn, ArrayList <Pair <Place, Arc>> history) {
		for (Iterator<?> preset = unfolding.getGraph().getInEdges(pn).iterator(); preset.hasNext();) 
		{
			if(pn instanceof Place) 
			{			
				Arc a = (Arc) preset.next();
				Transition t = (Transition) a.getSource();
				getBackPlaceConflictXOR(unfolding, t, history);
			} 
			else 
			{
				Arc a = (Arc) preset.next();
				Place p = (Place) a.getSource();
				
				/* XOR */
				if(unfolding.getGraph().getOutEdges(p).size() > 1)
					history.add(new Pair<Place, Arc>(p,a));
				
				getBackPlaceConflictXOR(unfolding, p, history);
			}
		}
	}
	
	/**
	 * Verifico se un ArrayList e una PetrinetNodeTupla contengono gli stessi elementi
	 * 
	 * @param arrayT
	 * @param petrinetNodes
	 * @return boolean
	 */
	public static boolean equalsArrayList(ArrayList <PetrinetNode> arrayT, PetrinetNodeTupla petrinetNodes)
	{
		for(int i = 0; i < petrinetNodes.getElements().length; i++)
			if(!arrayT.contains(petrinetNodes.getElements()[i]))
				return false;
		return true;
	}
	
	/**
	 * Verifico se due transazioni sono in conflitto
	 * 
	 * @param unfolding
	 * @param t
	 * @param u
	 * @return boolean
	 */
	public static boolean isConflit(Petrinet unfolding, Transition t, Transition u)
	{
		ArrayList <Place> xorT = Utility.getHistoryPlaceXOR(unfolding, t), xorU = Utility.getHistoryPlaceXOR(unfolding, u);
		for(int i = 0; i < xorT.size(); i++)
			for(int j = 0; j < xorU.size(); j++)
				if(xorT.get(i).equals(xorU.get(j)))
					return true;
		return false;
	}
}
