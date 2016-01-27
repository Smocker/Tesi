package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * IdentificationMap viene utilizzata per la creazione delle statistiche della rete di occorrenze con la tecnica dell'unfolding
 * 
 * @author Daniele Cicciarella
 */
public class IdentificationMap extends HashMap<String, ArrayList <Transition>>
{
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/* Variabili utilizzate per le statistiche */
	private int sizeArcs = 0, sizePlaces = 0, sizeTransitions = 0;
	private boolean isBoundedness;
	private double startTime = System.currentTimeMillis(), time = 0;
	private String nameNet = null;

	/**
	 * Costruttore
	 */
	public IdentificationMap()
	{
		put("Livelock Identification", new ArrayList <Transition>());
		put("Livelock Identification Unbounded", new ArrayList <Transition>());
		put("DeadLock Identification", new ArrayList <Transition>());
	}
	
	/**
	 * Inserisce il livelock
	 * 
	 * @param t: transazione da aggiungere
	 */
	public void insertLiveLock(Transition t) 
	{
		if(get("Livelock Identification").contains(t))
			return;
		get("Livelock Identification").add(t);
	}

	/**
	 * Restituisce i livelock
	 * 
	 * @return arraylist contenente i livelock
	 */
	public ArrayList<Transition> readLiveLock()
	{
		return get("Livelock Identification");
	}
	
	/**
	 * Inserisce il livelock unbounded
	 * 
	 * @param t: transazione da aggiungere
	 */
	public void insertLiveLockUnbounded(Transition t)
	{
		if(get("Livelock Identification Unbounded").contains(t))
			return;
		get("Livelock Identification Unbounded").add(t);
	}
	
	/**
	 * Restituisce i livelock unbounded
	 * 
	 * @return arraylist contenente i livelock unbounded
	 */
	public ArrayList<Transition> readLiveLockUnbounded()
	{
		return get("Livelock Identification Unbounded");
	}
	
	/**
	 * Inserisce i deadlock
	 * 
	 * @param t: transazione da aggiungere
	 */
	public void insertDeadLock(ArrayList<Transition> t)
	{
		put("DeadLock Identification", t);
	}
	
	/**
	 * Restituisce i deadlock
	 * 
	 * @return arraylist contenente i deadlock
	 */
	public ArrayList<Transition> readDeadLock()
	{
		return get("DeadLock Identification");
	}
	
	/**
	 * Prendo il nome della rete di petri originale
	 * 
	 * @return nome della rete di petri
	 */
	public String getNameNet() 
	{
		return nameNet;
	}

	/**
	 * Setto il nome della rete di petri
	 * 
	 * @param nameNet nome della rete di petri
	 */
	public void setNameNet(String nameNet) 
	{
		this.nameNet = nameNet;
	}
	
	/**
	 * Prendo il tempo di esecuzione dell'unfolding
	 * 
	 * @return
	 */
	public double getTime() 
	{
		return time;
	}

	/**
	 * Setto il tempo di esecuzione dell'unfolding
	 * 
	 * @param time
	 */
	public void setTime() 
	{
		time = (System.currentTimeMillis() - startTime) / 1000;
	}
	
	/**
	 * Crea alcune statistiche della rete
	 * 
	 * @param petrinet: rete di petri 
	 * @param unfolding: rete di unfolding
	 * @param marking: map contenente le transazioni che provocano la rete unbounded con il rispettivo marking
	 */
	public void setNetStatistics(Petrinet petrinet, Petrinet unfolding, HashMap<PetrinetNode, ArrayList<PetrinetNode>> marking)
	{
		for(Place p : unfolding.getPlaces())
			sizeArcs += unfolding.getGraph().getInEdges(p).size() + unfolding.getGraph().getOutEdges(p).size();
		sizePlaces = unfolding.getPlaces().size();
		sizeTransitions = unfolding.getTransitions().size();
		isBoundedness = marking.isEmpty() ? true : false;	
		setNameNet(petrinet.getLabel());
		setTime();
	}
	
	/**
	 * Carico tutte le statistiche della rete in una stringa html
	 * 
	 * @return out: stringa contenente le statistiche
	 */
	public String loadStatistics()
	{
		String out = "<html><h1 style=\"color:red;\">Diagnosi sulla rete di unfolding</h1>";
		
		/* Tempo di esecuzione dell'unfolding */
		out += "<BR>Tempo di esecuzione del plugin: " + getTime() + "<BR><BR>";
		
		/* Carico i livelock e deadlock */
		for(String key: this.keySet())
		{
			if(get(key).isEmpty())
			{
				if(key == "Livelock Identification")
					out += "La rete non contiene punti di livelock<BR><BR>";
				else if(key == "Livelock Identification Unbounded")
					out += "La rete non contiene punti di livelock che rendono la rete unbounded<BR><BR>";
				else if (key == "DeadLock Identification")
					out += "La rete non contiene punti di deadlock<BR><BR>";
			}
			else
			{
				if(key == "Livelock Identification")
					out += "La rete contiene punti di livelock:<ol>";
				else if(key == "Livelock Identification Unbounded")
					out += "La rete contiene punti di livelock che rendono la rete unbounded:<ol>";
				else if (key == "DeadLock Identification")
					out += "La rete contiene punti di deadlock:<ol>";
				
				for(Transition t: get(key))
					out += "<li>" + t.getLabel() + "</li>";
				out += "</ol><BR>";
			}
		}
		
		/* Carico le altre statistiche della rete */
		out += "<h2>Altre statistiche:</h2><ul type=\"disc\">";
		out += "<li>Numero di piazze: " + sizePlaces + "</li>";
		out += "<li>Numero di transazioni: " + sizeTransitions + "</li>";
		out += "<li>Numero di archi: " + sizeArcs + "</li>";
		out += "<li>Boundedness: " + isBoundedness + "</li></ul></html>";
	
		return out;
	}
}