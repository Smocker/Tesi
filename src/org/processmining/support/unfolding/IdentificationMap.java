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
public class IdentificationMap extends HashMap<String, ArrayList <Transition>>
{
	/* serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/* Chiavi delle map */
	private static final String LIVELOCK = "Livelock";
	private static final String LIVELOCK_UNBOUNDED = "Livelock Unbounded";
	private static final String DEADLOCK = "Deadlock";
	
	/* Variabili utilizzate per le statistiche */
	private int nArcs = 0, nPlaces = 0, nTransitions = 0;
	private boolean isSound;
	private double startTime = System.currentTimeMillis(), time = 0;

	/**
	 * Costruttore
	 */
	public IdentificationMap()
	{
		put(LIVELOCK, new ArrayList <Transition>());
		put(LIVELOCK_UNBOUNDED, new ArrayList <Transition>());
		put(DEADLOCK, new ArrayList <Transition>());
	}
	
	/**
	 * Inserisce il livelock
	 * 
	 * @param t transazione da aggiungere
	 */
	public void addLiveLock(Transition t) 
	{
		t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
		get(LIVELOCK).add(t);
	}

	/**
	 * Restituisce i livelock
	 * 
	 * @return lista contenente i livelock
	 */
	public ArrayList<Transition> getLivelock()
	{
		return get(LIVELOCK);
	}
	
	/**
	 * Inserisce il livelock unbounded
	 * 
	 * @param t transazione da aggiungere
	 */
	public void addLivelockUnbounded(Transition t)
	{
		t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
		get(LIVELOCK_UNBOUNDED).add(t);
	}
	
	/**
	 * Restituisce i livelock unbounded
	 * 
	 * @return lista contenente i livelock unbounded
	 */
	public ArrayList<Transition> getLivelockUnbounded()
	{
		return get(LIVELOCK_UNBOUNDED);
	}
	
	/**
	 * Inserisce i deadlock
	 * 
	 * @param deadlock transazioni da aggiungere
	 */
	public void setDeadlock(ArrayList<Transition> deadlock)
	{
		for(Transition t : deadlock)
			t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.ORANGE);
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
		isSound = get(LIVELOCK_UNBOUNDED).isEmpty() && get(DEADLOCK).isEmpty();	
		
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
		String out = "<html><h1 style=\"color:red;\">Diagnosi sulla rete di Unfolding</h1>";
		
		/* Tempo di esecuzione dell'unfolding */
		out += "<BR>Tempo di esecuzione del plugin: " + time + "<BR><BR>";
		
		/* Carico i livelock e deadlock */
		for(String key: keySet())
		{
			switch(key)
			{
				case LIVELOCK:
				{
					if(get(key).isEmpty())
						out += "La rete non contiene punti di cutoff<BR><BR>";
					else
					{
						out += "La rete contiene " + get(key).size() + " punti di livelock:<ol>";
						for(Transition t: get(key))
							out += "<li>" + t.getLabel() + "</li>";
						out += "</ol><BR>";
					}
					break;
				}
				case LIVELOCK_UNBOUNDED:
				{
					if(get(key).isEmpty())
						out += "La rete non contiene punti di cutoff che rendono la rete unbounded <BR><BR>";
					else
					{
						out += "La rete contiene " + get(key).size() + " punti di cutoff che rendono la rete unbounded:<ol>";
						for(Transition t: get(key))
							out += "<li>" + t.getLabel() + "</li>";
						out += "</ol><BR>";
					}
					break;
				}
				case DEADLOCK:
				{
					if(get(key).isEmpty())
						out += "La rete non contiene punti di deadlock<BR><BR>";
					else
					{
						out += "La rete contiene " + get(key).size() + " punti di deadlock:<ol>";
						for(Transition t: get(key))
							out += "<li>" + t.getLabel() + "</li>";
						out += "</ol><BR>";
					}
					break;
				}
			}
		}
		
		/* Carico le altre statistiche della rete */
		out += "<h2>Altre statistiche:</h2><ul type=\"disc\">";
		out += "<li>Numero di piazze: " + nPlaces + "</li>";
		out += "<li>Numero di transazioni: " + nTransitions + "</li>";
		out += "<li>Numero di archi: " + nArcs + "</li>";
		out += "<li>Soundness: " + isSound + "</li></ul></html>";
		return out;
	}
}