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
     * @param first: primo elemento
     * @param second: secondo elemento
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
     * @return primo elemento
     */
    public A getFirst() 
    {
    	return this.first;
    }

    /**
     * Setta il primo argomento
     * 
     * @param first: primo elemento
     */
    public void setFirst(A first) 
    {
    	this.first = first;
    }

    /**
     * Legge il secondo argomento
     * 
     * @return secondo elemento
     */
    public B getSecond() 
    {
    	return second;
    }

    /**
     * Setta il secondo argomento
     * 
     * @param: secondo elemento
     */
    public void setSecond(B second) 
    {
    	this.second = second;
    }
}
