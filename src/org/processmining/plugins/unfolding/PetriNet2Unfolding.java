package org.processmining.plugins.unfolding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.support.localconfiguration.LocalConfiguration;
import org.processmining.support.localconfiguration.LocalConfigurationMap;
import org.processmining.support.localconfiguration.LocalConfigurationQueue;
import org.processmining.support.unfolding.IdentificationMap;
import org.processmining.support.unfolding.Pair;
import org.processmining.support.unfolding.PetrinetNodeTupla;
import org.processmining.support.unfolding.Utility;

/**
 * Converte un rete di Petri in una rete di unfolding
 * 
 * @author Daniele Cicciarella
 */
public class PetriNet2Unfolding 
{	
	/* Contesto di ProM */
	protected PluginContext context;
	
	/* Reti di petri */
	protected Petrinet petrinet, unfolding;

	/* Variabili per la trasformazione della rete di Petri in N* */
	protected Place i, o;
	protected Transition reset;
		
	/* Coda di priorità che contiene le configurazioni da analizzare */
	protected LocalConfigurationQueue pq = new LocalConfigurationQueue();
	
	/* Mappa ogni nodo della rete di Petri a un uno o più nodi della rete di unfolding */
	protected HashMap <PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap = new HashMap <PetrinetNode, ArrayList<PetrinetNode>>();
	
	/* Mappa ogni nodo della rete di unfolding a un nodo della rete di Petri */
	protected HashMap <PetrinetNode, PetrinetNode> unf2PetriMap = new HashMap <PetrinetNode, PetrinetNode>();
	
	/* Mappa ogni transazione della rete di unfolding con il rispettivo marking */
	protected HashMap <PetrinetNode, ArrayList<PetrinetNode>> marking = new HashMap <PetrinetNode, ArrayList<PetrinetNode>>();
	
	/* Mappa le configurazioni locali di ogni transazione delle rete di unfolding */
	protected LocalConfigurationMap localConfigurationMap = new LocalConfigurationMap();
	
	/* Mappa i livelock e deadlock e altre statistiche */
	protected IdentificationMap identificationMap = new IdentificationMap();
	
	/* Mappa ogni transazione la storia dei suoi xor-split  */
	protected HashMap <Transition, ArrayList<Pair <Place, Arc>>> xorLinks = new HashMap <Transition, ArrayList<Pair <Place, Arc>>>();
	
	/**
	 * Costruttore
	 * 
	 * @param context 
	 * @param petrinet: rete di petri originale
	 */
	PetriNet2Unfolding(PluginContext context, Petrinet petrinet) 
	{
		this.context = context;
		this.petrinet = petrinet;
	}
	
	/**
	 * Converte una rete di Petri in una rete di unfolding
	 * 
	 * @return la rete di unfolding e le sue statistiche
	 */
	public Object[] convert() 
	{
		Place i1;
		i = (Place) Utility.getStartNode(petrinet); 
		o = (Place) Utility.getEndNode(petrinet);

		/* Inizio la costruzione della rete inserendo la piazza iniziale i1 */
		unfolding = PetrinetFactory.newPetrinet("Unfolding from Petrinet");		
		i1 = unfolding.addPlace("start");		
		addCorrispondence(i, i1);
		
		/* Trasformo la rete di Petri N in N* */
		reset = petrinet.addTransition("reset");
		petrinet.addArc(o, reset);
		petrinet.addArc(reset, i);

		/* Inizializzo e visito la coda */
		initQueue(i, i1);		
		visitQueue();	
		
		/* Estraggo i deadlock ed effettuo le statistiche della rete */
		writeLog(context, "Extraction of the dealock points...");
		getStatistics();
		
		return new Object [] {unfolding, identificationMap};
	}

	/**
	 * Inizializzazione della coda di priorità
	 * 
	 * @param p piazza iniziale della rete di petri
	 * @param p1 piazza iniziale della rete di unfolding
	 */
	private void initQueue(Place p, Place p1) 
	{
		/* Per tutte le transazioni t della rete di petri attaccate alla piazza iniziale p */
		for(PetrinetNode t: Utility.getPostset(petrinet, p))
		{
			/* Creo una transazione t1 nell'unfolding e attacco p1 con t1 */
			Transition t1 = unfolding.addTransition(t.getLabel());
			unfolding.addArc(p1, t1);			
			
			/* Per tutti i place u delle rete di petri attaccate a t */
			for(PetrinetNode u: Utility.getPostset(petrinet, t))
			{
				// Creo un place u1 nell'unfolding e attacco t1 con u1
				Place u1 = unfolding.addPlace(u.getLabel());
				unfolding.addArc(t1, u1);				
				addCorrispondence(u, u1);
			}

			/* Aggiorno tutte le strutture globali e la coda*/
			refreshMap(t, t1);
			pq.insert(localConfigurationMap.get(t1));
		}
	}

	/**
	 * Visito la coda di priorità per la creazione della rete di Petri
	 */
	private void visitQueue() 
	{
		while(!pq.isEmpty())
		{
			/* Estraggo una configurazione c da q */
			LocalConfiguration c = pq.remove();
			
			/* Mappo da unfolding (t1) a petri (t) la prima transazione della configurazione */
			Transition t1 = c.get().get(0);
			Transition t = (Transition) unf2PetriMap.get(t1);
					
			/* Per ogni piazza p della rete originale attaccate a t */
			for(PetrinetNode p: Utility.getPostset(petrinet, t))
			{
				Place pi = null;				
				for(int i = 0; i < petri2UnfMap.get(p).size(); i++)
					if(unfolding.getArc(t1, petri2UnfMap.get(p).get(i)) != null)
						pi = (Place) petri2UnfMap.get(p).get(i);
				
				/* Per ogni transazione t2 delle rete originale attaccate a p */
				for(PetrinetNode t2: Utility.getPostset(petrinet, p))
				{
					ArrayList<PetrinetNodeTupla> combination = null;
					ArrayList <PetrinetNode> presetT2 = null;
					
					/* Verifico se t2 è abilitata */
					if((presetT2 = Utility.isEnabled(petrinet, t2, petri2UnfMap)) == null)
						continue;
					
					/* Calcolo tutte le combinazioni possibili in ingresso a t2 */
					ArrayList <ArrayList <PetrinetNode>> comb = new ArrayList <ArrayList <PetrinetNode>>();
					for(int i = 0; i < presetT2.size(); i++)
					{
						if(!unf2PetriMap.get(pi).equals(presetT2.get(i))) 
						{
							ArrayList <PetrinetNode> array = petri2UnfMap.get(presetT2.get(i));
							comb.add(array);
						}
						else
						{
							ArrayList <PetrinetNode> array = new ArrayList <PetrinetNode> ();
							array.add(pi);
							comb.add(array);
						}
					}
					combination = createCombination(comb);					
					filterCombination(combination, (Transition) t2);
					
					// Se tutte le combinazioni sono state usate allora scelgo un altra transazione
					if(combination.size() == 0)
						continue;

					/* Per ogni combinazione rimanente */
					for(int i = 0; i < combination.size(); i++)
					{					
						/* Aggiungo t2 all'unfolding il quale sarà collagato con le piazze che lo abilitano */
						Transition t3 = unfolding.addTransition(t2.getLabel());
						for(int j = 0; j < combination.get(i).getElements().length; j++)
							unfolding.addArc((Place) combination.get(i).getElements()[j], t3);
						
						// Verifico se l'inserimento di t3 provaca conflitto in tal caso la elimino
						if(this.isConflict(combination.get(i).getElements(), t3))
						{
							for(int j = 0; j < combination.get(i).getElements().length; j++)
								unfolding.removeArc((Place) combination.get(i).getElements()[j], t3);
							unfolding.removeTransition(t3);
							continue;
						}	
						refreshMap(t2, t3);
						
						/* Verifico i cutoff */
						if(t2.equals(reset))
						{
							if(marking.get(t3).size() == 0)
							{
								t3.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
								identificationMap.insertLiveLock((Transition) t3);
							}
							else  
							{
								t3.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
								identificationMap.insertLiveLockUnbounded((Transition) t3);
							}
						}
						else
						{							
							boolean isCutOff = false;
							for(PetrinetNode p2: Utility.getPostset(petrinet, t2))
							{
								// Controllo se un place del suo postset è stato inserito
								if(petri2UnfMap.containsKey(p2) && !isCutOff)
									isCutOff = isCutOff(t3, p2);
							}
							
							// Se t3 è un cutoff la configurazione non deve essere aggiunta nella coda
							if(!isCutOff)
							{
								for(PetrinetNode p2: Utility.getPostset(petrinet, t2))
								{
									Place p3 = unfolding.addPlace(p2.getLabel());
									unfolding.addArc(t3, p3);						
									addCorrispondence(p2, p3);
								}
								pq.insert(localConfigurationMap.get(t3));
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Verifica se i place sono in conflitto
	 * 
	 * @param t3: transazione da analizzare
	 * @param petrinetNodes: array contenente i place da contrallare
	 * @return true se sono in conflitto, false altrimenti
	 */
	private boolean isConflict(PetrinetNode[] petrinetNodes, Transition t3) {
		ArrayList <Pair <Place, Arc>> XOR = new ArrayList <Pair <Place, Arc>> (), historyXOR = new ArrayList <Pair <Place, Arc>> ();
		for(int i = 0; i < petrinetNodes.length; i++)
		{
			historyXOR = Utility.getHistoryPlaceConflictXOR(unfolding, petrinetNodes[i], unfolding.getArc(petrinetNodes[i], t3));
			if(!historyXOR.isEmpty())
			{
				/* Se due piazze condividono lo stesso xor ma provengono da percorsi diversi è un conflitto */
				for(int j = 0; j < XOR.size(); j++)
					for(int t = 0; t < historyXOR.size(); t++)
						if(XOR.get(j).getFirst().equals(historyXOR.get(t).getFirst()) && !XOR.get(j).getSecond().equals(historyXOR.get(t).getSecond()))
							return true;

				for(int t = 0; t < historyXOR.size(); t++)
					XOR.add(new Pair<Place, Arc>(historyXOR.get(t).getFirst(), historyXOR.get(t).getSecond()));
			}
			historyXOR.clear();
		}
		return false;
	}
	
	/**
	 * Verifico se una transazione provoca il cutoff
	 * 
	 * @param t transazione da verificare
	 * @param placeFinal stato finale della transazione t
	 * @return boolean
	 */
	private boolean isCutOff(Transition t, PetrinetNode placeFinal) 
	{
		int isBounded;
		
		for(Place h : Utility.getHistoryPlace(unfolding, t))
		{
			// Se nella storia dei place di t esiste placeFinal allora è un ciclo
			if(unf2PetriMap.get(h).equals(placeFinal)) 
			{						
				for(PetrinetNode transitionFinal: Utility.getPreset(unfolding, h))
				{					
					isBounded = Utility.isBounded(marking.get(t), marking.get(transitionFinal));
					if(isBounded == 0)
					{
						t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
						identificationMap.insertLiveLock(t);
						return true;
					}
					else if(isBounded > 0) 
					{
						t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);
						identificationMap.insertLiveLockUnbounded(t);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Crea tutte le possibili combinazioni
	 * 
	 * @param places: piazza da cui creare tutte le combinazioni
	 * @return result ArrayList<PetrinetNode[]> contenente le combinazioni
	 */
	private static ArrayList<PetrinetNodeTupla> createCombination(ArrayList<ArrayList<PetrinetNode>> places)
	{
		ArrayList<PetrinetNodeTupla> result = new ArrayList<PetrinetNodeTupla>();
		recCombination(0, new PetrinetNodeTupla(), places, result);
		return result;
	}
	
	/**
	 * Costruisce in maniera ricorsiva le tuple
	 * 
	 * @param step: passo corrente
	 * @param tupla: tupla corrente
	 * @param places: piazze da aggiungere
	 * @param result: tupla parziale
	 */
	private static void recCombination(int step, PetrinetNodeTupla tupla, ArrayList<ArrayList<PetrinetNode>> places, ArrayList<PetrinetNodeTupla> result ) 
	{
		int size = places.get(step).size();
		for (int i = 0; i < size; ++i) 
		{
			PetrinetNodeTupla newTupla = tupla.add(places.get(step).get(i));
			if (step == places.size() - 1) 
				result.add(newTupla);
			else
				recCombination(step+1, newTupla, places, result);
		}
	}
	
	/**
	 * Elimina le combinazioni già utilizzate 
	 * 
	 * @param combination: combinazioni correnti
	 * @param t: transazione da aggiungere all'unfolding
	 */
	private void filterCombination(ArrayList<PetrinetNodeTupla> combination, Transition t) {
		ArrayList <PetrinetNode> presetT1;
		
		if(petri2UnfMap.containsKey(t))
		{
			for(PetrinetNode t1 : petri2UnfMap.get(t)) 
			{
				presetT1 = Utility.getPreset(unfolding, t1);
				for(int j = 0; j < combination.size(); j++)
				{
					if(Utility.equalsArrayList(presetT1, combination.get(j)))
						combination.remove(j);						
				}
			}
		}
	}
	
	/**
	 * Estraggo i deadlock ed effettuo le statistiche della rete
	 */
	private void getStatistics()
	{		
		/* Inserisco i livelock trovati in un ArrayList */
		ArrayList <Transition> cutoff = new ArrayList <Transition> ();
		for(int i = 0; i < identificationMap.readLiveLock().size(); i++)
			cutoff.add(identificationMap.readLiveLock().get(i));
		for(int i = 0; i < identificationMap.readLiveLockUnbounded().size(); i++)
			cutoff.add(identificationMap.readLiveLockUnbounded().get(i));
	
		
		ArrayList <Transition> cutoffHistory = new ArrayList <Transition> ();
		
		for(Transition v: cutoff)
		{ 
			for(Transition u: localConfigurationMap.get(v).get())
			{
				if(!cutoffHistory.contains(u))
					cutoffHistory.add(u);
			}
		}
		
		/* Individuo i deadlock */
		ArrayList <Transition> deadlock = deleteCutOff(cutoff, getChoiceTransition(cutoffHistory));
		
		// Li coloro di arancione
		if(deadlock != null)
		{
			for(Transition t : deadlock)
				t.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.ORANGE);
			identificationMap.insertDeadLock(deadlock);
		}
		
		/* Inserisco le altre statistiche */
		identificationMap.setNetStatistics(unfolding);
	}

	/**
	 * Estrae i punti di deadlock
	 * 
	 * @param cutoff: arraylist contenente i punti di cutoff
	 * @return arraylist contenente i punti di deadlock
	 */
	private ArrayList<Transition> deleteCutOff(ArrayList<Transition> cutoff, ArrayList<Transition> possibleSpoilers) 
	{
		Transition t1 = null;
		ArrayList <Transition> deadlock = null, cutoff1 = null, possibleSpoilers1 = null;
		if(cutoff.isEmpty())
			return null;
		else
		{		
			Transition t = cutoff.get(0);
			ArrayList<Transition> spoilers = getSpoilers(t,possibleSpoilers);	
			while(!spoilers.isEmpty() && deadlock == null)
			{
				t1 = spoilers.remove(0);
				cutoff1 = removeConflict(cutoff, t1);
				possibleSpoilers1 = removeConflict(possibleSpoilers, t1);
				if(cutoff1.isEmpty())
				{
					deadlock  = new ArrayList <Transition>();
					deadlock.add(t1);
				}
				else
				{
					deadlock = deleteCutOff(cutoff1,possibleSpoilers1);
					if(deadlock != null)
						deadlock.add(t1);
				}
			}
			return deadlock;
		}
	}

	/**
	 * Prendo tutte le transazioni che sono in conflitto con il cutoff
	 * 
	 * @param t: cutoff scelto
	 * @return spoilers: arraylist di transazioni contenenti tutte le transazioni in conflitto con il cutoff
	 */
	private ArrayList<Transition> getSpoilers(Transition t, ArrayList<Transition> set) 
	{
		ArrayList<Transition> spoilers = new ArrayList<Transition>();
		
		/* Se sono in conflitto le aggiungo alla nuova lista */
		for(Transition t1: set)
			if(Utility.isConflit(xorLinks.get(t), xorLinks.get(t1)))
				spoilers.add(t1);
		
		return spoilers;
	}
	
	/**
	 * Scelto come nuovo insieme di cutoff quelle che non sono in conflitto con lo spoiler
	 * 
	 * @param cutoff: insieme corrente di cutoff
	 * @param spoiler: spoiler corrente
	 * @return cutoff1: arraylist di transazioni contenente la nuova lista di cutoff
	 */
	private ArrayList<Transition> removeConflict(ArrayList<Transition> cutoff, Transition spoiler) 
	{	
		ArrayList<Transition> cutoff1 = new ArrayList<Transition>();
		
		/* Se le transazioni del cutoff non sono in conflitto con lo spoiler le aggiungo alla nuova lista */
		for(Transition t: cutoff)
			if(t != spoiler && !Utility.isConflit(xorLinks.get(t), xorLinks.get(spoiler)))
				cutoff1.add(t);
		
		return cutoff1;
	}
	
	private ArrayList<Transition> getChoiceTransition(ArrayList<Transition> cutoffHistory)
	{
		ArrayList <Transition> choice = new ArrayList <Transition>();
		
		for(Place p : unfolding.getPlaces())
		{
			if(unfolding.getGraph().getOutEdges(p).size() > 1)
			{
				for (Iterator<?> i = unfolding.getGraph().getOutEdges(p).iterator(); i.hasNext();) 
				{
					Arc a = (Arc) i.next();
					Transition t = (Transition) a.getTarget();
					if(!choice.contains(t) && !cutoffHistory.contains(t))
						choice.add(t);
				}
			}
		}
		return choice;
	}
	
	/**
	 * Aggiunge le corrispondenze delle due map
	 * 
	 * @param t nodo della rete di Petri
	 * @param t1 nodo della rete di Unfolding
	 */
	private void addCorrispondence(PetrinetNode t, PetrinetNode t1)
	{
		// Se non esiste la map da petri -> unfolding la creo
		if(!petri2UnfMap.containsKey(t)) 
			petri2UnfMap.put(t, new ArrayList<PetrinetNode>());
		
		petri2UnfMap.get(t).add(t1);
		unf2PetriMap.put(t1, t);
		
		if(t1 instanceof Transition)
			xorLinks.put((Transition) t1, Utility.getHistoryPlaceConflictXOR(unfolding, t1, null));
	}
	
	/**
	 * Aggiorna le map
	 * 
	 * @param t transazione della rete di Petri
	 * @param t1 transazione delle rete di Unfolding
	 */
	private void refreshMap(PetrinetNode t, PetrinetNode t1) 
	{
		addCorrispondence(t,t1);
		localConfigurationMap.set(t1, unfolding);
		marking.put(t1, Utility.setMarking(petrinet, localConfigurationMap.get(t1), unf2PetriMap));
	}
	
	/**
	 * Scrive un messaggio di log e incrementa la barra progressiva
	 * 
	 * @param context contesto di ProM
	 * @param log messaggio di log
	 */
	private void writeLog(PluginContext context, String log)
	{
		context.log(log);
		context.getProgress().inc();
	}
}