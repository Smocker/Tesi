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
	 * Prende la piazza finale della rete di Petri
	 *  
	 * @param petrinet: rete di petri
	 * @return pn: piazza iniziale o null se la rete ha un nodo senza archi in ingresso
	 */
	public static PetrinetNode getEndNode(Petrinet petrinet) 
	{		
		for(PetrinetNode pn: petrinet.getNodes())
			if(petrinet.getGraph().getOutEdges(pn).isEmpty())
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
		ArrayList<PetrinetNode> preset = new ArrayList <PetrinetNode> (); 
		for (Iterator<?> inEdge = net.getGraph().getInEdges(pn).iterator(); inEdge.hasNext();) 
		{
			Arc a = (Arc) inEdge.next();
			preset.add(a.getSource());
		}
		return preset;
	}
	
	/**
	 * Restituisce il postset di un nodo
	 * 
	 * @param net: rete di petri
	 * @param pn: nodo corrente
	 * @return postset: arraylist di PetrinetNode contenente il postset di pn
	 */
	public static ArrayList<PetrinetNode> getPostset(Petrinet net, PetrinetNode pn)
	{
		ArrayList<PetrinetNode> postset = new ArrayList <PetrinetNode> (); 
		for (Iterator<?> outEdge = net.getGraph().getOutEdges(pn).iterator(); outEdge.hasNext();) 
		{
			Arc a = (Arc) outEdge.next();
			postset.add(a.getTarget());
		}
		return postset;
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
	 * @param unfolding: rete di Occorrenze
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
	 * @param lcT: configurazione locale della transazione t
	 * @param lcT1: configurazione locale della transazione t1
	 * @param petrinet: rete di Petri originale
	 * @param unf2PetriMap: map da unfolding a petrinet
	 * @param t3: transazione che vogliamo analizzare
	 * @param marking: ,appa ogni transazione della rete di occorrenze con il rispettivo marking
	 * @return intero avente valore 0 se t3 è un cutoff, 1 se t3 è un cutoff che provoca la rete unbounded, -1 niente
	 */
	public static int isBounded(LocalConfiguration lcT, LocalConfiguration lcT1, Petrinet petrinet, HashMap <PetrinetNode, PetrinetNode> unf2PetriMap, Transition t3, HashMap<PetrinetNode, ArrayList<PetrinetNode>> marking) 
	{
		ArrayList <PetrinetNode> markT = new ArrayList <PetrinetNode> (), markT1 = new ArrayList <PetrinetNode> (), mark;
		
		/* Calcolo il marking di t: postset(H(t)) - preset(H(t)) */
		for(Transition t : lcT.get())
		{
			for(PetrinetNode postset : Utility.getPostset(petrinet, unf2PetriMap.get(t)))
				markT.add(postset);
		}
		for(Transition t : lcT.get())
		{
			for(PetrinetNode preset : Utility.getPreset(petrinet, unf2PetriMap.get(t)))
			{
				if(markT.contains(preset))
					markT.remove(preset);
			}
		}		
		
		/* Calcolo il marking di t1 */
		for(Transition t : lcT1.get())
		{
			for(PetrinetNode postset : Utility.getPostset(petrinet, unf2PetriMap.get(t)))
				markT1.add(postset);
		}
		for(Transition t : lcT1.get())
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
	private static void getBackPlaceXOR(Petrinet unfolding, PetrinetNode pn, ArrayList<Place> history) 
	{
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
	 * Restituisce la storia di un nodo avente i place con più di un arco in uscita (viene utilizzata nell'individuazione dei deadlock)
	 * 
	 * @param unfolding: rete di occorrenze
	 * @param pn: nodo corrente 
	 * @return history: storia dei place del nodo corrente
	 */
	public static ArrayList <Pair <Place, Arc>> getHistoryPlaceConflictXORDeadLock(Petrinet unfolding, PetrinetNode pn)
	{
		ArrayList <Pair <Place, Arc>> history = new ArrayList <Pair <Place, Arc>> ();
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
	 * Verifico se due strutture contengono gli stessi elementi
	 * 
	 * @param arrayT: ArrayList di PetrinetNode
	 * @param petrinetNodes: PetrinetNodeTupla
	 * @return true se contengono gli stessi elementi, false altrimenti
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
	 * @param unfolding: rete di unfolding
	 * @param t: prima transazione
	 * @param u: seconda transazione
	 * @return true se condividono almeno uno xor, false altrimenti
	 */
	public static boolean isConflit(Petrinet unfolding, Transition t, Transition u)
	{
		ArrayList <Pair <Place, Arc>> xorT = Utility.getHistoryPlaceConflictXORDeadLock(unfolding, t), xorU = Utility.getHistoryPlaceConflictXORDeadLock(unfolding, u);
		for(int i = 0; i < xorT.size(); i++)
			for(int j = 0; j < xorU.size(); j++)
				if(xorT.get(i).getFirst().equals(xorU.get(j).getFirst()) && !xorT.get(i).getSecond().equals(xorU.get(j).getSecond()))
					return true;
		return false;
	}
	
	/**
	 * Verifico se due transazioni sono in conflitto (rispetto a isConflit la lista di <Place, Arc> è stata costruita) 
	 * 
	 * @param unfolding: rete di unfolding
	 * @param xorT: ArrayList di <Place, Arc> della transazione t
	 * @param u: transazione u
	 * @return true se condividono almeno uno xor, false altrimenti
	 */
	public static boolean isConflit2(Petrinet unfolding, ArrayList <Pair <Place, Arc>> xorT, Transition u)
	{
		ArrayList <Pair <Place, Arc>> xorU = Utility.getHistoryPlaceConflictXORDeadLock(unfolding, u);
		for(int i = 0; i < xorT.size(); i++)
			for(int j = 0; j < xorU.size(); j++)
				if(xorT.get(i).getFirst().equals(xorU.get(j).getFirst()) && !xorT.get(i).getSecond().equals(xorU.get(j).getSecond()))
					return true;
		return false;
	}
}
