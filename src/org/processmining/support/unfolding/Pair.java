package org.processmining.support.unfolding;

/**
 * Classe utilizzata per creare una coppia
 * 
 * @author Daniele Cicciarella
 *
 * @param <A> 
 * @param <B>
 */
public class Pair<A, B> 
{
    private A first;
    private B second;

    /**
     * Costruttore
     * 
     * @param first
     * @param second
     */
    public Pair(A first, B second) 
    {
    	super();
    	this.first = first;
    	this.second = second;
    }
    
    /**
     * Restituisce il primo argomento
     * 
     * @return this.first
     */
    public A getFirst() 
    {
    	return this.first;
    }

    /**
     * Setta il primo argomento
     * 
     * @param first
     */
    public void setFirst(A first) 
    {
    	this.first = first;
    }

    /**
     * Legge il secondo argomento
     * 
     * @return this.second
     */
    public B getSecond() 
    {
    	return second;
    }

    /**
     * Setta il secondo argomento
     * 
     * @param second
     */
    public void setSecond(B second) 
    {
    	this.second = second;
    }
}
