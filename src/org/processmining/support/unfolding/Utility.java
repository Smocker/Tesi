package org.processmining.support.unfolding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
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
public class Utility {
	/**
	 * Prende la piazza iniziale della rete di Petri
	 * 
	 * @param N
	 *            rete di Petri
	 * @return piazza iniziale della rete di Petri o null
	 */
	public static Place getStartNode(Petrinet N) {
		for (Place p : N.getPlaces())
			if (N.getGraph().getInEdges(p).isEmpty())
				return p;
		return null;
	}

	/**
	 * Prende la piazza finale della rete di Petri
	 * 
	 * @param N
	 *            rete di Petri
	 * @return piazza finale della rete di Petri o null
	 */
	public static Place getEndNode(Petrinet N) {
		for (Place p : N.getPlaces())
			if (N.getGraph().getOutEdges(p).isEmpty())
				return p;
		return null;
	}

	/**
	 * Restituisce il preset di un nodo
	 * 
	 * @param N
	 *            rete di Petri
	 * @param pn
	 *            nodo corrente
	 * @return preset di pn
	 */
	public static List<PetrinetNode> getPreset(Petrinet N, PetrinetNode pn) {
		int i = 0;
		List<PetrinetNode> preset = new ArrayList<PetrinetNode>(N.getGraph().getInEdges(pn).size());

		for (DirectedGraphEdge<?, ?> a : N.getGraph().getInEdges(pn)) {
			preset.add((PetrinetNode) a.getSource());
			i = i + 1;
		}
		return preset;
	}

	/**
	 * Restituisce il postset di un nodo
	 * 
	 * @param N
	 *            rete di Petri
	 * @param pn
	 *            nodo corrente
	 * @return postset di pn
	 */
	public static List<PetrinetNode> getPostset(Petrinet N, PetrinetNode pn) {
		int i = 0;
		List<PetrinetNode> postset = new ArrayList<PetrinetNode>(N.getGraph().getOutEdges(pn).size());

		for (DirectedGraphEdge<?, ?> a : N.getGraph().getOutEdges(pn)) {
			postset.add((PetrinetNode) a.getTarget());
			i = i + 1;
		}
		return postset;
	}

	/**
	 * Verifica se un nodo e' abilitato
	 * 
	 * @param N
	 *            rete di Petri
	 * @param pn
	 *            nodo da verificare
	 * @parammarking
	 * @return preset di t se e' abilitata, vuoto altrimenti
	 */
	public static List<PetrinetNode> isEnabledFromMarking(ArrayList<PetrinetNode> marking, PetrinetNode pn,
			Petrinet N) {
		List<PetrinetNode> preset = getPreset(N, pn);

		if (preset.size() > 0) {
			for (int i = 0; i < preset.size(); i++)
				if (!marking.contains(preset.get(i)))
					return new ArrayList<PetrinetNode>();
		}
		return preset;
	}

	/**
	 * @param N
	 *            rete di Petri
	 * @param t
	 *            nodo di cui calcolare il preset
	 * @param L1
	 *            map da N a N'
	 * @return preset di t su unfolding
	 */
	public static ArrayList<PetrinetNode> getPresetOnUnf(Petrinet N, PetrinetNode t,
			Map<PetrinetNode, ArrayList<PetrinetNode>> L1) {
		List<PetrinetNode> preset = getPreset(N, t);
		ArrayList<PetrinetNode> presetUnf = new ArrayList<PetrinetNode>();
		for (PetrinetNode p : preset) {
			if (L1.containsKey(p)) {
				presetUnf.addAll(L1.get(p));
			}
		}
		return presetUnf;
	}

	/**
	 * Verifica se un nodo e' abilitato
	 * 
	 * @param N
	 *            rete di Petri
	 * @param pn
	 *            nodo da verificare
	 * @param L1
	 *            map da N a N'
	 * @return preset di t se e' abilitata, null altrimenti
	 */

	public static List<PetrinetNode> isEnabled(Petrinet N, PetrinetNode pn,
			Map<PetrinetNode, ArrayList<PetrinetNode>> L1) {
		List<PetrinetNode> preset = getPreset(N, pn);
		if (preset.size() >= 1) {
			for (int i = 0; i < preset.size(); i++)
				if (!L1.containsKey(preset.get(i)))
					return null;
		}
		return preset;
	}

	/**
	 * Calcola il marking di un nodo
	 * 
	 * @param N
	 *            rete di Petri
	 * @param C
	 *            configurazione locale del nodo
	 * @param L
	 *            mappa da N' a N
	 * @return marking del nodo
	 */
	public static ArrayList<PetrinetNode> getMarking(Petrinet N, LocalConfiguration C,
			Map<PetrinetNode, PetrinetNode> L) {
		ArrayList<PetrinetNode> marking = new ArrayList<PetrinetNode>();

		for (Transition t : C.get()) {
			for (DirectedGraphEdge<?, ?> a : N.getGraph().getOutEdges(L.get(t)))
				marking.add((PetrinetNode) a.getTarget());
		}
		for (Transition t : C.get()) {
			for (DirectedGraphEdge<?, ?> a : N.getGraph().getInEdges(L.get(t)))
				if (marking.contains(a.getSource()))
					marking.remove(a.getSource());
		}
		return marking;
	}

	/**
	 * ----------Calcola il marking di un nodo
	 * 
	 * @param N
	 *            rete di Petri
	 * @param C
	 *            configurazione locale del nodo
	 * @param L
	 *            mappa da N' a N
	 * @return marking del nodo
	 */
	public static ArrayList<PetrinetNode> getMarking(Petrinet N, LocalConfiguration C) {
		ArrayList<PetrinetNode> marking = new ArrayList<PetrinetNode>();

		for (Transition t : C.get()) {
			for (DirectedGraphEdge<?, ?> a : N.getGraph().getOutEdges(t))
				marking.add((PetrinetNode) a.getTarget());
		}
		for (Transition t : C.get()) {
			for (DirectedGraphEdge<?, ?> a : N.getGraph().getInEdges(t))
				if (marking.contains(a.getSource()))
					marking.remove(a.getSource());
		}
		return marking;
	}

	/**
	 * Restituisce la storia dei place di un nodo
	 * 
	 * @param N1
	 *            rete di unfolding
	 * @param pn
	 *            nodo di partenza
	 * @return H storia dei place di pn
	 */
	public static ArrayList<Place> getHistoryPlace(Petrinet N1, PetrinetNode pn) {
		ArrayList<Place> H = new ArrayList<Place>();
		getBackPlacePlace(N1, pn, H);
		return H;
	}

	/**
	 * Visita all'indietro la rete di unfolding, salvando i place attraversati
	 * 
	 * @param N1
	 *            rete di unfolding
	 * @param pn
	 *            nodo corrente
	 * @param H
	 *            storia parziale dei place
	 */
	private static void getBackPlacePlace(Petrinet N1, PetrinetNode pn, ArrayList<Place> H) {
		for (Iterator<?> preset = N1.getGraph().getInEdges(pn).iterator(); preset.hasNext();) {
			/* Se e' un place non contenuto in H lo aggiungo */
			if (pn instanceof Place) {
				if (!H.contains(pn))
					H.add((Place) pn);
				else
					return;
			}

			Arc a = (Arc) preset.next();
			getBackPlacePlace(N1, a.getSource(), H);
		}
	}

	/**
	 * Restituisce la storia di un nodo dei (xor-split, arc)
	 * 
	 * @param N1
	 *            rete di unfolding
	 * @param pn
	 *            nodo di partenza
	 * @return storia dei (place, arc) di pn
	 */
	public static ArrayList<Pair> getHistoryXOR(Petrinet N1, PetrinetNode pn, Arc a) {
		ArrayList<Pair> H = new ArrayList<Pair>();

		/* Inizializzo H con il nodo iniziale se e' uno xor-split */
		if (pn instanceof Place)
			if (N1.getGraph().getOutEdges(pn).size() > 1)
				H.add(new Pair((Place) pn, a));

		getBackPlaceXOR(N1, pn, H);
		return H;
	}

	/**
	 * Visita all'indietro la rete di unfolding, salvando i xor-split
	 * 
	 * @param N1
	 *            rete di unfolding
	 * @param pn
	 *            nodo corrente
	 * @param storia
	 *            parziale delle coppie (xor-split, arc)
	 */
	private static void getBackPlaceXOR(Petrinet N1, PetrinetNode pn, ArrayList<Pair> H) {
		for (Iterator<?> preset = N1.getGraph().getInEdges(pn).iterator(); preset.hasNext();) {
			Arc a = (Arc) preset.next();

			/*
			 * Se il nodo corrente e' una transizione verifico se il suo preset
			 * contiene xor-split
			 */
			if (pn instanceof Transition) {
				if (N1.getGraph().getOutEdges(a.getSource()).size() > 1) {
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
	 * @param xorT
	 *            lista degli xor-split della transazione t
	 * @param xorU
	 *            lista degli xor-split della transazione u
	 * @return true se condividono almeno uno xor, false altrimenti
	 */
	public static boolean isConflict(ArrayList<Pair> xorT, ArrayList<Pair> xorU) {
		for (Pair xor : xorT)
			for (Pair xor2 : xorU)
				if (xor.isConflict(xor2))
					return true;
		return false;
	}

	/**
	 * Verifico se vi e' un cutoff e se esso provoca la rete unbounded
	 * 
	 * @param markingT
	 *            marking della transazione t
	 * @param markingU
	 *            marking della configurazione f
	 * @return intero che indica se c'e' un cutoff e, in quel caso, il suo tipo
	 */
	public static int isBounded(ArrayList<PetrinetNode> markingT, ArrayList<PetrinetNode> markingU) {
		ArrayList<PetrinetNode> markT = markingT, markU = markingU;

		/*
		 * La dimensione del marking di u non puo' essere maggiore della
		 * dimensione del marking di t
		 */
		if (markU.size() > markT.size())
			return -1;

		/* Verifichiamo se t e' un cutoff */
		for (int i = 0; i < markU.size(); i++) {
			if (!markT.contains(markU.get(i)))
				return -1;
			else
				markT.remove(markU.get(i));
		}

		/* Verifico il suo tipo */
		if (markT.isEmpty())
			return 0;
		else {
			for (PetrinetNode pn : markT)
				pn.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
			return 1;
		}
	}

	public static ArrayList<Place> getHistoryPlace(Petrinet unfolding, List<PetrinetNode> presetAbilitato,
			Map<PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap, ArrayList<PetrinetNode> markingUnfolding) {
		ArrayList<Place> history = new ArrayList<>();
		for (PetrinetNode nodoAbilitato : presetAbilitato) {
			ArrayList<PetrinetNode> nodoAbilitatoArray = petri2UnfMap.get(nodoAbilitato);
			for (PetrinetNode nodoAbilitatoUnf : nodoAbilitatoArray) {
				if (markingUnfolding.contains(nodoAbilitatoUnf)) {
					ArrayList<Place> historyPreset = Utility.getHistoryPlace(unfolding, nodoAbilitatoUnf);
					history.addAll(historyPreset);
				}

			}
		}

		return history;
	}

	public static ArrayList<Place> getHistoryPlace2(Petrinet unfolding, PetrinetNode nodogiapresente,
			Map<PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> presetedge = nodogiapresente.getGraph()
				.getInEdges(nodogiapresente);
		ArrayList<Place> preset = new ArrayList<>();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : presetedge) {
			preset.add((Place) edge.getSource());
		}
		ArrayList<Place> history = new ArrayList<>();
		for (Place p : preset) {
			ArrayList<PetrinetNode> places = petri2UnfMap.get(p);
			ArrayList<Place> historyPreset = Utility.getHistoryPlace(unfolding, p);
			history.addAll(historyPreset);
		}
		return history;
	}
}