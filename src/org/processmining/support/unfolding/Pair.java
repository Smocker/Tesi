package org.processmining.support.unfolding;

import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;

/**
 * Crea una coppia (Place, Arc)
 * 
 * @author Daniele Cicciarella
 */
public class Pair
{
    private Place first;
    private Arc second;

    /**
     * Costruttore
     * 
     * @param first primo elemento
     * @param second secondo elemento
     */
    public Pair(Place first, Arc second) 
    {
    	super();
    	this.first = first;
    	this.second = second;
    }
    
    /**
     * Restituisce il primo argomento
     * 
     * @return primo elemento
     */
    public Place getFirst() 
    {
    	return this.first;
    }

    /**
     * Setta il primo argomento
     * 
     * @param first primo elemento
     */
    public void setFirst(Place first) 
    {
    	this.first = first;
    }

    /**
     * Legge il secondo argomento
     * 
     * @return secondo elemento
     */
    public Arc getSecond() 
    {
    	return second;
    }

    /**
     * Setta il secondo argomento
     * 
     * @param secondo elemento
     */
    public void setSecond(Arc second) 
    {
    	this.second = second;
    }
    
    /**
     * Verifica se due coppie sono in conflitto
     * 
     * @param pair coppia da verificare
     * @return true se sono in conflitto, false altrimenti
     */
	public boolean isConflict(Pair pair)
	{
		return (first.equals(pair.first) && !second.equals(pair.second));
	}
	
    /**
     * Verifica se due coppie hanno gli stessi elementi 
     */
    public boolean equals(Object o)
    {
    	if(o != null && o instanceof Pair)
    	{
    		Pair pair = (Pair) o;
    		return (pair.first.equals(first) && pair.second.equals(second));
    	}	
		return false;
    }
}
