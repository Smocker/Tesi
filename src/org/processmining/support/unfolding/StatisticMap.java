package org.processmining.support.unfolding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Map contenente le statistiche della rete di unfolding
 * 
 * @author Daniele Cicciarella
 */
public class StatisticMap extends HashMap<String, ArrayList <Transition>>
{
	/* serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/* Chiavi delle map */
	private static final String CUTOFF = "Cutoff";
	private static final String CUTOFF_UNBOUNDED = "Cutoff Unbounded";
	private static final String DEADLOCK = "Deadlock";
	
	/* Variabili utilizzate per le statistiche */
	private int nArcs = 0, nPlaces = 0, nTransitions = 0;
	private boolean isSound;
	private double startTime = System.currentTimeMillis(), time = 0;

	/**
	 * Costruttore
	 */
	public StatisticMap()
	{
		put(CUTOFF, new ArrayList <Transition> ());
		put(CUTOFF_UNBOUNDED, new ArrayList <Transition> ());
		put(DEADLOCK, new ArrayList <Transition> ());
	}
	
	/**
	 * Inserisce un cutoff
	 * 
	 * @param cutoff cutoff da aggiungere
	 */
	public void addCutoff(Transition cutoff) 
	{
		cutoff.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.BLUE);
		get(CUTOFF).add(cutoff);
	}

	/**
	 * Restituisce i cutoff
	 * 
	 * @return lista contenente i cutoff
	 */
	public ArrayList<Transition> getCutoff()
	{
		return get(CUTOFF);
	}
	
	/**
	 * Inserisce un cutoff unbounded
	 * 
	 * @param cutoff cutoff da aggiungere
	 */
	public void addCutoffUnbounded(Transition cutoff)
	{
		cutoff.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.BLUE);
		get(CUTOFF_UNBOUNDED).add(cutoff);
	}
	
	/**
	 * Restituisce i cutoff unbounded
	 * 
	 * @return lista contenente i cutoff unbounded
	 */
	public ArrayList<Transition> getCutoffUnbounded()
	{
		return get(CUTOFF_UNBOUNDED);
	}
	
	/**
	 * Inserisce i deadlock
	 * 
	 * @param deadlock deadlock da aggiungere
	 */
	public void setDeadlock(ArrayList<Transition> deadlock)
	{
		for(Transition t : deadlock)
			t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
		put(DEADLOCK, deadlock);		
	}
	
	/**
	 * Restituisce i deadlock
	 * 
	 * @return lista contenente i deadlock
	 */
	public ArrayList<Transition> getDeadlock()
	{
		return get(DEADLOCK);
	}

	/**
	 * Crea le statistiche della rete
	 * 
	 * @param N1 rete di unfolding
	 */
	public void setStatistic(Petrinet N1)
	{
		/* Statistiche della rete */
		nPlaces = N1.getPlaces().size();
		nTransitions = N1.getTransitions().size();
		for(Place p : N1.getPlaces())
			nArcs += N1.getGraph().getInEdges(p).size() + N1.getGraph().getOutEdges(p).size();
		
		/* Verifico se è sound */
		isSound = get(CUTOFF_UNBOUNDED).isEmpty() && get(DEADLOCK).isEmpty();	
		
		/* Calcolo il tempo del plugin */
		time = (System.currentTimeMillis() - startTime) / 1000;
	}
	
	/**
	 * Carico tutte le statistiche della rete in una stringa html
	 * 
	 * @return le statistiche della rete
	 */
	public String getStatistic()
	{
		String out = "<html><h1 style=\"color:red;\">Diagnosis on Unfolding net</h1>";
		
		/* Tempo di esecuzione del plugin */
		out += "<BR>Runtime of the plugin: " + time + "<BR><BR>";
		
		/* Carico i livelock e deadlock */
		for(String key: keySet())
		{
			switch(key)
			{
				case CUTOFF:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the cutoff points<BR><BR>";
					else
					{
						out += "The net contains " + get(key).size() + " cutoff points:<ol>";
						for(Transition t: get(key))
							out += "<li>" + t.getLabel() + "</li>";
						out += "</ol><BR>";
					}
					break;
				}
				case CUTOFF_UNBOUNDED:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the cutoff points that make the unbounded net<BR><BR>";
					else
					{
						out += "The net contains " + get(key).size() + " cutoff points that make the unbounded net:<ol>";
						for(Transition t: get(key))
							out += "<li>" + t.getLabel() + "</li>";
						out += "</ol><BR>";
					}
					break;
				}
				case DEADLOCK:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the deadlock points<BR><BR>";
					else
					{
						out += "The net contains " + get(key).size() + " deadlock points:<ol>";
						for(Transition t: get(key))
							out += "<li>" + t.getLabel() + "</li>";
						out += "</ol><BR>";
					}
					break;
				}
			}
		}
		
		/* Carico le altre statistiche della rete */
		out += "<h2>Other statistics:</h2><ul type=\"disc\">";
		out += "<li>Number of places: " + nPlaces + "</li>";
		out += "<li>Number of transitions: " + nTransitions + "</li>";
		out += "<li>Number of arcs: " + nArcs + "</li>";
		out += "<li>Soundness: " + isSound + "</li></ul></html>";
		return out;
	}
}