package org.processmining.support.unfolding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.converters.bpmn2pn.EPetrinetNode;
import org.processmining.plugins.unfolding.visualize.Palette;
import org.processmining.plugins.unfolding.visualize.UtilitiesforMapping;
import org.processmining.support.localconfiguration.LocalConfigurationMap;

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
	private static final String DEAD = "Dead";
	
	private Palette pal = new Palette();

	
	/* Variabili utilizzate per le statistiche */
	private int nArcs = 0, nPlaces = 0, nTransitions = 0;
	private boolean isSound, isWeakSound;
	private double startTime = System.currentTimeMillis(), time = 0;

	private LocalConfigurationMap localConfigurationMap;
	private Map<EPetrinetNode,BPMNNode> reverseMap;
	private Map<EPetrinetNode, BPMNEdge<BPMNNode, BPMNNode>> flowMapPNtoBP;

	/**
	 * Costruttore
	 */
	public StatisticMap()
	{
		put(CUTOFF, new ArrayList <Transition> ());
		put(CUTOFF_UNBOUNDED, new ArrayList <Transition> ());
		put(DEADLOCK, new ArrayList <Transition> ());
		put(DEAD, new ArrayList <Transition> ());
	}
	
	/**
	 * Inserisce un cutoff
	 * 
	 * @param cutoff cutoff da aggiungere
	 */
	public void addCutoff(Transition cutoff) 
	{
		cutoff.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getCutColor());
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
		cutoff.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getCutColor());
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
			t.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getDeadColor());
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
	 * Inserisce un dead
	 * 
	 * @param dead dead da aggiungere
	 */
	public void addDead(Transition dead) 
	{
		dead.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getDeadColor());
		get(DEAD).add(dead);
	}

	/**
	 * Restituisce i dead
	 * 
	 * @return lista contenente i dead
	 */
	public ArrayList<Transition> getDead()
	{
		return get(DEAD);
	}

	/**
	 * Crea le statistiche della rete
	 * 
	 * @param N rete di petri
	 * @param N1 rete di unfolding
	 * @param L1 mappa da N a N' 
	 * @param localConfigurationMap 
	 */
	public void setStatistic(Petrinet N, Petrinet N1, Map<PetrinetNode, ArrayList<PetrinetNode>> L1, LocalConfigurationMap localConfigurationMap)
	{
		this.localConfigurationMap=localConfigurationMap;
		/* Statistiche della rete */
		nPlaces = N1.getPlaces().size();
		nTransitions = N1.getTransitions().size();
		for(Place p : N1.getPlaces())
			nArcs += N1.getGraph().getInEdges(p).size() + N1.getGraph().getOutEdges(p).size();
		
		/* Verifico se c'è qualche transizione dead */
		for(Transition pn : N.getTransitions())
			if(!L1.containsKey(pn))
				addDead(pn);
		
		/* Verifico le soundness */
		isSound = get(CUTOFF_UNBOUNDED).isEmpty() && get(DEADLOCK).isEmpty() && get(DEAD).isEmpty();	
		isWeakSound = get(CUTOFF_UNBOUNDED).isEmpty() && get(DEADLOCK).isEmpty();
		
		/* Calcolo il tempo del plugin */
		time = (System.currentTimeMillis() - startTime) ;
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
				case DEAD:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the dead transitions<BR><BR>";
					else
					{
						out += "The net contains " + get(key).size() + " dead transitions:<ol>";
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
		out += "<li>Soundness: " + isSound + "</li>";
		out += "<li>Weak soundness: " + isWeakSound + "</li></ul></html>";

		return out;
	}

	@Override
	public String toString() {
		String out = "Diagnosis on Unfolding net\n";
		
		/* Tempo di esecuzione del plugin */
		out += "Runtime of the plugin: " + time + "\n";
		
		/* Carico i livelock e deadlock */
		for(String key: keySet())
		{
			switch(key)
			{
				case CUTOFF:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the cutoff points\n";
					else
					{
						out += "The net contains " + get(key).size() + " cutoff points:\n";
						for(Transition t: get(key))
							out += "* " + t.getLabel() + "\n";
						out += "\n";
					}
					break;
				}
				case CUTOFF_UNBOUNDED:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the cutoff points that make the unbounded net\n";
					else
					{
						out += "The net contains " + get(key).size() + " cutoff points that make the unbounded net:\n";
						for(Transition t: get(key))
							out += "* " + t.getLabel() + "\n";
						out += "\n";
					}
					break;
				}
				case DEADLOCK:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the deadlock points\n";
					else
					{
						out += "The net contains " + get(key).size() + " deadlock points:\n";
						for(Transition t: get(key))
							out += "* " + t.getLabel() + "\n";
						out += "\n";
					}
					break;
				}
				case DEAD:
				{
					if(get(key).isEmpty())
						out += "The net does not contain the dead transitions\n";
					else
					{
						out += "The net contains " + get(key).size() + " dead transitions:\n";
						for(Transition t: get(key))
							out += "* " + t.getLabel() + "\n";
						out += "\n";
					}
					break;
				}
			}
		}
		
		/* Carico le altre statistiche della rete */
		out += "Other statistics:\n";
		out += "Number of places: " + nPlaces + "\n";
		out += "Number of transitions: " + nTransitions + "\n";
		out += "Number of arcs: " + nArcs + "\n";
		out += "Soundness: " + isSound + "\n";
		out += "Weak soundness: " + isWeakSound + "\n";
		
		

		return out;
	}
	
	public String getCLIstat(){
		/* Carico i livelock e deadlock */
		boolean isBounded = false;
		int numCutoff = 0;
		int numDead = 0;
		int numDeadlock = 0;
		for(String key: keySet())
		{
			switch(key)
			{
				case CUTOFF:
				{
					if(get(key).isEmpty()){
						// "The net does not contain the cutoff points\n";
					}else
					{
						//out += "The net contains " + get(key).size() + " cutoff points:\n";
						numCutoff +=  get(key).size();
					}
					break;
				}
				case CUTOFF_UNBOUNDED:
				{
					if(get(key).isEmpty()){
						isBounded = true;
					}
						//out += "The net does not contain the cutoff points that make the unbounded net\n";
					else
					{
						//out += "The net contains " + get(key).size() + " cutoff points that make the unbounded net:\n";
						isBounded = false;
						numCutoff +=  get(key).size();
					}
					break;
				}
				case DEADLOCK:
				{
					if(get(key).isEmpty()){}
						//out += "The net does not contain the deadlock points\n";
					else
					{
						//out += "The net contains " + get(key).size() + " deadlock points:\n";
						numDeadlock +=  get(key).size();
					}
					break;
				}
				case DEAD:
				{
					if(get(key).isEmpty()){}
						//out += "The net does not contain the dead transitions\n";
					else
					{
						//out += "The net contains " + get(key).size() + " dead transitions:\n";
						numDead +=  get(key).size();
					}
					break;
				}
			}
		}
		
		String out = "",sound,Bounded,WeakSound = "";
		 
		sound  = isSound ? "True" : "False";
		WeakSound = isWeakSound ? "True" : "False";
		Bounded = isBounded  ? "True" : "False";
		//out = "C  D  L  isBou isWeakSo isSound\n";
		out += String.format("%s|%s|%s|%s|%s|%s|%s|",time, numCutoff,numDead,numDeadlock,Bounded,WeakSound,sound);
		// isSound;
		// isWeakSound;
		return out;
	}
	
	public void setLocalConfigurationMap(LocalConfigurationMap l ){
		this.localConfigurationMap=l;
	}
	
	public LocalConfigurationMap getLocalConfigurationMap(){
		return localConfigurationMap;
	}

	public Map<EPetrinetNode, BPMNNode> getReverseMap() {
		return reverseMap;
	}

	public void setReverseMap(Map<EPetrinetNode, BPMNNode> reverseMap) {
		this.reverseMap=reverseMap;
	}
	
	public void setFlowMap(Map<EPetrinetNode, BPMNEdge<BPMNNode, BPMNNode>> map) {
		
		this.flowMapPNtoBP=map;
	}


	public Map<EPetrinetNode, BPMNEdge<BPMNNode, BPMNNode>> getflowMapPNtoBP() {
		return flowMapPNtoBP;
	}

	
	
	public String getBPMNStatistic(BPMNDiagram bpmn)
	{
		/* Statistiche della rete */
		int nArcsBPMN = bpmn.getEdges().size(); 
		int nGateway = bpmn.getGateways().size(); 
		int nActivity = bpmn.getActivities().size();
		
		int nEvents = bpmn.getEvents().size(); 
		int nMessageFlow = bpmn.getMessageFlows().size(); 
		int nPool = bpmn.getPools().size();
		
		/* Verifico le soundness */
		isSound = get(CUTOFF_UNBOUNDED).isEmpty() && get(DEADLOCK).isEmpty() && get(DEAD).isEmpty();	
		isWeakSound = get(CUTOFF_UNBOUNDED).isEmpty() && get(DEADLOCK).isEmpty();
		
		/* Calcolo il tempo del plugin */
		time = (System.currentTimeMillis() - startTime) ;
		
		String out = "<html><h1 style=\"color:red;\">Diagnosis on BPMN graph</h1>";
		
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
						out += "The graph does not contain cutoff points<BR><BR>";
					else	{	
						String temp = "";
						int element = 0;
						for(Transition t: get(key))
							if (!(t.getLabel().equals("reset") || t.getLabel().equals("to") || t.getLabel().equals("ti"))){
								element++;
								BPMNNode fromReverseMap = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t);
								String l= "";
								if (fromReverseMap !=null){
									l = fromReverseMap.getLabel();} 
								else
									{l = "missing";}
								temp += "<li>" + l + "</li>";
								}							
						if (element == 0){
							out += "The graph does not contain cutoff points<BR><BR>";
						} else {
							switch(element){
							case 0:{out += "The graph does not contain cutoff points<BR><BR>"; break;}
							case 1:{out += "The graph contains " + element + " cutoff point:<ol>"; 
							temp += "</ol><BR>";out += temp; break;}
							default:{out += "The graph contains " + element + " cutoff points:<ol>";
							temp += "</ol><BR>";out += temp; break;}	
							}
						}	
					}
					break;
				}
				case CUTOFF_UNBOUNDED:
				{
					if(get(key).isEmpty())
						out += "The graph does not contain the cutoff points that make the unbounded graph<BR><BR>";
					else
					{	
						String temp = "";
						int element = 0;
						for(Transition t: get(key))
							if (!(t.getLabel().equals("reset") || t.getLabel().equals("to") || t.getLabel().equals("ti"))){
								element++;
								String l = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t).getLabel();
								temp += "<li>" + l + "</li>";
								}											
						switch(element){
						case 0:{out += "The graph does not contain the cutoff points that make the unbounded graph<BR><BR>"; break;}
						case 1:{out += "The graph contains " + element + " cutoff point that make the unbounded graph:<ol>"; 
						temp += "</ol><BR>";out += temp; break;}
						default:{out += "The graph contains " + element + " cutoff points that make the unbounded graph::<ol>";
						temp += "</ol><BR>";out += temp; break;}	
						}

					}
					break;
				}
				
				case DEADLOCK:
				{
					if(get(key).isEmpty())
						out += "The graph does not contain the deadlock points<BR><BR>";
					else	{	
							String temp = "";
							int element = 0;
							for(Transition t: get(key))
								if (!(t.getLabel().equals("reset") || t.getLabel().equals("to") || t.getLabel().equals("ti"))){
									element++;
									String l = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t).getLabel();
									temp += "<li>" + l + "</li>";
									}							
							switch(element){
								case 0:{out += "The graph does not contain deadlock points<BR><BR>"; break;}
								case 1:{out += "The graph contains " + element + " deadlock point:<ol>"; 
								temp += "</ol><BR>"; out += temp; break;}
								default:{out += "The graph contains " + element + " deadlock points:<ol>";
								temp += "</ol><BR>";out += temp; break;}	
								}
							
					}
						break;	
				}
				
			case DEAD:
				{
					if(get(key).isEmpty())
						out += "The graph does not contain dead nodes<BR><BR>";
					else	{	
						String temp = "";
						int element = 0;
						for(Transition t: get(key))
							if (!(t.getLabel().equals("reset") || t.getLabel().equals("to") || t.getLabel().equals("ti"))){
								element++;
								BPMNNode bnode = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t);
								if(bnode!=null){
									String l = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t).getLabel();
									temp += "<li>" + l + "</li>";
									}else{
									System.out.print("");
								}
								}							
						switch(element){
						case 0:{out += "The graph does not contain dead nodes<BR><BR>"; break;}
						case 1:{out += "The graph contains " + element + " dead node:<ol>"; 
						temp += "</ol><BR>"; out += temp; break;}
						default:{out += "The graph contains " + element + " dead nodes:<ol>";
						temp += "</ol><BR>"; out += temp; break;}	
						}

					}
					break;
				}
			}
		}
		
		/* Carico le altre statistiche della rete */
		out += "<h2>Other statistics:</h2><ul type=\"disc\">";			
		out += "<li>Number of arcs: " + nArcsBPMN + "</li>";
		out += "<li>Number of gateway: " + nGateway + "</li>";
		out += "<li>Number of activity: " + nActivity + "</li>";
		out += "<li>Number of events: " + nEvents + "</li>";
		out += "<li>Number of message flow: " + nMessageFlow + "</li>";
		out += "<li>Number of pool: " + nPool + "</li>";
		out += "<li>Soundness: " + isSound + "</li>";
		out += "<li>Weak soundness: " + isWeakSound + "</li></ul></html>";

		return out;
	}
	
}