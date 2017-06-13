package org.processmining.support.localconfiguration;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Crea la configurazione locale di un nodo
 * 
 * @author Daniele Cicciarella
 */
public class LocalConfiguration {
	private ArrayList<Transition> localConfiguration = null;
	/**
	 * Costruttore
	 */
	public LocalConfiguration() {
		localConfiguration = new ArrayList<Transition>();
	}

	public LocalConfiguration clone() {
		LocalConfiguration ll = new LocalConfiguration();
		for (Transition tra : localConfiguration) {
			ll.addAll(tra);
		}
		return ll;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		if (obj instanceof LocalConfiguration) {
			LocalConfiguration other = (LocalConfiguration) obj;
			if(localConfiguration.size()!=other.get().size()){
				return false;
			}
			for(Transition t : localConfiguration){
				if(!other.get().contains(t)){
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * Crea la configurazione locale di un nodo
	 * 
	 * @param N
	 *            rete di Petri
	 * @param pn
	 *            nodo corrente
	 * @return configurazione locale di pn
	 */
	public ArrayList<Transition> set(Petrinet N, PetrinetNode pn) {
		getLeastBackwardClosed(N, pn, localConfiguration);
		return localConfiguration;
	}

	public ArrayList<Transition> add(Transition t) {
		if (!localConfiguration.contains(t)) {
			localConfiguration.add(t);
		}
		return localConfiguration;
	}

	public ArrayList<Transition> addAll(Transition t) {
		localConfiguration.add(t);
		return localConfiguration;
	}

	public ArrayList<Transition> remove(Transition t) {
		if (localConfiguration.contains(t)) {
			localConfiguration.remove(t);
		}
		return localConfiguration;
	}

	/**
	 * Restituisce la configurazione locale
	 * 
	 * @return configurazione locale
	 */
	public ArrayList<Transition> get() {
		return localConfiguration;
	}

	/**
	 * Visita all'indietro la rete di unfolding per la costruzione della
	 * configurazione locale
	 * 
	 * @param N
	 *            rete di Petri
	 * @param pn
	 *            nodo corrente
	 * @param lista
	 *            parziale delle transazioni analizzate in precendenza
	 */
	private void getLeastBackwardClosed(Petrinet N, PetrinetNode pn, ArrayList<Transition> back) {
		if (pn instanceof Transition) {
			// La transazione è stata visitata
			if (back.contains(pn))
				return;
			back.add((Transition) pn);
		}

		/* Visita ricorsiva */
		for (Iterator<?> preset = N.getGraph().getInEdges(pn).iterator(); preset.hasNext();) {
			Arc a = (Arc) preset.next();
			getLeastBackwardClosed(N, a.getSource(), back);
		}
	}

	@Override
	public String toString() {
		return localConfiguration.toString();
	}

}
