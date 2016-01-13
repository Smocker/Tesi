package org.processmining.plugins.unfolding;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.support.unfolding.IdentificationMap;
import org.processmining.support.unfolding.LocalConfiguration;
import org.processmining.support.unfolding.LocalConfigurationMap;
import org.processmining.support.unfolding.Pair;
import org.processmining.support.unfolding.PetrinetNodeTupla;
import org.processmining.support.unfolding.PetrinetQueue;
import org.processmining.support.unfolding.Utility;

/**
 * Classe usata dai plugins BPMN2Unfolding_Plugin and PetriNet2Unfolding_Plugin per convertire un PetriNet in una rete di occorrenze con l'unfolding
 * 
 * @author Daniele Cicciarella
 */
public class PetriNet2Unfolding 
{	
	protected Petrinet petrinet, unfolding;

	/* Coda di priorità che contiene le configurazioni da analizzare */
	protected PetrinetQueue pq = new PetrinetQueue();
	
	/* HashMap contenente le configurazioni locali di ogni transazione delle rete di Occorrenze */
	protected LocalConfigurationMap localConfigurationMap = new LocalConfigurationMap();
	
	/* Mappa ogni nodo della rete di Petri a un uno o più nodi della rete di Occorrenze */
	protected HashMap <PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap = new HashMap <PetrinetNode, ArrayList<PetrinetNode>>();
	
	/* Mappa ogni nodo della rete di Occorrenze a un nodo della rete di Petri */
	protected HashMap <PetrinetNode, PetrinetNode> unf2PetriMap = new HashMap <PetrinetNode, PetrinetNode>();
	
	/* Mappa ogni transazione della rete di occorrenze con il rispettivo marking */
	protected HashMap <PetrinetNode, ArrayList<PetrinetNode>> marking = new HashMap <PetrinetNode, ArrayList<PetrinetNode>>();
	
	/* HashMap contenente i livelock e deadlock e altre statistiche */
	protected IdentificationMap identificationMap = new IdentificationMap();

	/**
	 * Costruttore
	 * 
	 * @param petrinet: rete di petri originale
	 */
	PetriNet2Unfolding(Petrinet petrinet) 
	{
		this.petrinet = petrinet;
	}
	
	/**
	 * Converte una rete di petri in una rete di occorrenze con la tecnica dell'unfolding
	 * 
	 * @return unfolding 
	 */
	public Object[] convert() 
	{
		Place p = (Place) Utility.getStartNode(petrinet), p1;

		/* Inizio la costruzione della rete inserendo la piazza iniziale p1 */
		unfolding = PetrinetFactory.newPetrinet("Unfolding from Petrinet");		
		p1 = unfolding.addPlace("start");		
		addCorrispondence(p, p1);

		/* Inizializzazione e visito la coda */
		initQueue(p, p1);		
		visitQueue();	
		
		/* Effettuo le statistiche della rete */
		getStatistics();
		identificationMap.showStatistics();
		
		return new Object [] {unfolding, identificationMap};
	}

	/**
	 * Inizializzo la coda di priorità
	 * 
	 * @param p: piazza iniziale della rete di petri
	 * @param p1: piazza iniziale della rete di occorrenze
	 */
	private void initQueue(Place p, Place p1) 
	{
		/* Per tutte le transazioni t della rete di petri attaccate alla piazza iniziale p */
		for(PetrinetNode t: Utility.getPostset(petrinet, p))
		{
			// Creo una transazione t1 nell'unfolding e attacco p1 con t1
			Transition t1 = unfolding.addTransition(t.getLabel());
			unfolding.addArc(p1, t1);			
			addCorrispondence(t,t1);
			
			// Per tutti i place p2 delle rete di petri attaccate a t
			for(PetrinetNode p2: Utility.getPostset(petrinet, t))
			{
				// Creo un place p3 nell'unfolding e attacco t1 con p3
				Place p3 = unfolding.addPlace(p2.getLabel());
				unfolding.addArc(t1, p3);				
				addCorrispondence(p2, p3);
			}

			// Aggiungo ogni configurazione nella coda
			pq.insert(localConfigurationMap, unfolding, t1);
		}
	}

	/**
	 * 
	 */
	private void visitQueue() 
	{
		while(!pq.isEmpty())
		{
			/* Estraggo una configurazione c da q */
			LocalConfiguration c = pq.remove();
			
			/* Mappo da unfolding (t1) -> petri (t) la prima transazione della configurazione */
			ArrayList <Transition> arrayC = c.get();
			Transition t1 = arrayC.get(0);
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
					boolean isEnabled = true;
					ArrayList<PetrinetNodeTupla> combination = null;
					
					// Se il preset di t2 è maggiore di uno devo verificare se è abilitata
					ArrayList <PetrinetNode> presetT2 = Utility.getPreset(petrinet, t2); 
					if(presetT2.size() > 1) 
					{
						for(int i = 0; i < presetT2.size(); i++)
						{
							if(!petri2UnfMap.containsKey(presetT2.get(i))) 
							{
								isEnabled = false;
								break;
							} 
						}
						if(!isEnabled) {
							continue;
						}
					}	
					
					/* Se abilitata calcolo tutte le combinazioni possibili in ingresso a t2 */
					ArrayList <ArrayList <PetrinetNode>> comb = new ArrayList <ArrayList <PetrinetNode>>();
					for(int i = 0; i < presetT2.size(); i++)
					{
						if(!unf2PetriMap.get(pi).equals(presetT2.get(i))) {
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
					
					// Se tutte le combinazioni sono state usate allora vado avanti
					if(combination.size() == 0)
						continue;

					/* Per ogni combinazione rimanente */
					for(int i = 0; i < combination.size(); i++)
					{					
						/* Aggiungo t2 -> t3 all'unfolding il quale è collagato con le piazze che lo abilitano */
						Transition t3 = unfolding.addTransition(t2.getLabel());
						for(int j = 0; j < combination.get(i).getElements().length; j++)
							unfolding.addArc((Place) combination.get(i).getElements()[j], t3);
						
						// Verifico se l'inserimento di t3 nella rete di occorrenze provaca conflitto. In quel caso la elimino
						if(this.isConflict(combination.get(i).getElements(), t3))
						{
							for(int j = 0; j < combination.get(i).getElements().length; j++)
								unfolding.removeArc((Place) combination.get(i).getElements()[j], t3);
							unfolding.removeTransition(t3);
							continue;
						}
						addCorrispondence(t2,t3);
						
						/* Verifico se t3 provoca un cut-off */
						boolean isCutOff = false;
						for(PetrinetNode p2: Utility.getPostset(petrinet, t2))
						{
							// Controllo se un place del suo postset è stato inserito
							if(petri2UnfMap.containsKey(p2) && !isCutOff)
							{
								isCutOff = isCutOff(t3, isCutOff, p2);
							}
						}
						
						// Se t3 è un cut-off la configurazione non deve essere aggiunta nella coda
						if(!isCutOff)
						{
							for(PetrinetNode p2: Utility.getPostset(petrinet, t2))
							{
								Place p3 = unfolding.addPlace(p2.getLabel());
								unfolding.addArc(t3, p3);						
								addCorrispondence(p2, p3);
							}
							pq.insert(localConfigurationMap, unfolding, t3);
						}							
					}
				}
			}
		}
	}

	/**
	 * Verifico se t3 è un cut-off
	 * 
	 * @param t
	 * @param isCutOff
	 * @param p
	 * @return
	 */
	private boolean isCutOff(Transition t, boolean isCutOff, PetrinetNode p) {
		ArrayList <Place> history = Utility.getHistoryPlace(unfolding, t);
		for(int h = 0; h < history.size(); h++)
		{
			// Se nella storia dei place di t3 esiste p allora è un ciclo (grafo aciclico)
			if(unf2PetriMap.get(history.get(h)).equals(p)) 
			{
				Place pH = history.get(h);
				for(PetrinetNode tH: Utility.getPreset(unfolding, pH))
				{
					// Verifico se il cut-off provoca la rete bounded o unbounded
					LocalConfiguration cT = new LocalConfiguration ();
					cT.create(unfolding, t);
					int isBounded = Utility.isBounded(cT, localConfigurationMap.get(tH), petrinet, unf2PetriMap, t, marking);
					if(isBounded==0){
						isCutOff = true;
						identificationMap.insertLiveLock(t);
						break;
					}
					else if(isBounded > 0) {
						isCutOff = true;
						identificationMap.insertLiveLockUnbounded(t);
						break;
					}
				}
			}
		}
		return isCutOff;
	}
	

	/**
	 * Aggiunge le corrispondenze nelle due map
	 * 
	 * @param nodePetri: nodo della rete di petri
	 * @param nodeUnfolding: nodo della rete di occorrenze
	 */
	private void addCorrispondence(PetrinetNode nodePetri, PetrinetNode nodeUnfolding)
	{
		// Se non esiste la map da petri -> unfolding la creo
		if(!petri2UnfMap.containsKey(nodePetri)) 
			petri2UnfMap.put(nodePetri, new ArrayList<PetrinetNode>());
		
		petri2UnfMap.get(nodePetri).add(nodeUnfolding);
		unf2PetriMap.put(nodeUnfolding, nodePetri);
	}
	
	/**
	 * Verifica se i place sono in conflitto
	 * @param t3 
	 * 
	 * @param petrinetNodes: array contenente i place da contrallare
	 * @return boolean
	 */
	private boolean isConflict(PetrinetNode[] petrinetNodes, Transition t3) {
		ArrayList <Pair <Place, Arc>> XOR = new ArrayList <Pair <Place, Arc>> (), historyXOR = new ArrayList <Pair <Place, Arc>> ();
		for(int i = 0; i < petrinetNodes.length; i++)
		{
			historyXOR = Utility.getHistoryPlaceConflictXOR(unfolding, petrinetNodes[i], unfolding.getArc(petrinetNodes[i], t3));
			if(!historyXOR.isEmpty())
			{
				for(int j = 0; j < XOR.size(); j++)
				{
					for(int t = 0; t < historyXOR.size(); t++)
					{
						if(XOR.get(j).getFirst().equals(historyXOR.get(t).getFirst()))
						{
							if(!XOR.get(j).getSecond().equals(historyXOR.get(t).getSecond()))
								return true;
						}
					}
				}
				for(int t = 0; t < historyXOR.size(); t++)
					XOR.add(new Pair<Place, Arc>(historyXOR.get(t).getFirst(), historyXOR.get(t).getSecond()));
			}
			historyXOR.clear();
		}
		return false;
	}

	/**
	 * Crea tutte le possibili combinazioni
	 * 
	 * @param places
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
	 * @param step
	 * @param tupla
	 * @param places
	 * @param result
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
	 * @param combination
	 * @param t
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
	
	private void getStatistics()
	{
		/* Inserisco i livelock trovati in un ArrayList */
		ArrayList <Transition> cutOff = new ArrayList <Transition> ();
		for(int i = 0; i < identificationMap.readLiveLock().size(); i++)
			cutOff.add(identificationMap.readLiveLock().get(i));
		for(int i = 0; i < identificationMap.readLiveLockUnbounded().size(); i++)
			cutOff.add(identificationMap.readLiveLockUnbounded().get(i));
		
		/* Individuo i deadlock */
		while(!cutOff.isEmpty())
		{
			Transition t = cutOff.get(cutOff.size()-1);
			ArrayList <Place> xorT = Utility.getHistoryPlaceXOR(unfolding, t);

			if(!xorT.isEmpty())
			{
				LocalConfiguration c = new LocalConfiguration();
				c.create(unfolding, t);
				for(Transition t1 : unfolding.getTransitions())
				{
					if(c.get().contains(t1))
						continue;					
					ArrayList <Place> xorT1 = Utility.getHistoryPlaceXOR(unfolding, t1);
					if(xorT1.isEmpty())
						continue;
					
					boolean trovato = false;
					for(int j = 0; j < xorT.size(); j++)
					{
						if(xorT1.contains(xorT.get(j)))
						{
							trovato = true;
							identificationMap.insertDeadLock(t1);
							for(int i = 0; i < identificationMap.readDeadLock().size()-1; i++)
								if(Utility.isConflit(unfolding, identificationMap.readDeadLock().get(i), t1))
									identificationMap.readDeadLock().remove(i);
							break;
						}
					}
					if(trovato)
						break;
				}
				cutOff.remove(cutOff.size()-1);
			}
			else
				cutOff.remove(cutOff.size()-1);
		}
		
		/* Inserisco le altre statistiche */
		identificationMap.setNetStatistics(unfolding, marking);
	}
}