package org.processmining.plugins.converters.bpmn2pn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventTrigger;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Convert a model BPMN in a Worflow System
 *
 * @author Daniele Cicciarella
 */
public class BPMN2WorkflowSystemConverter 
{	
	protected BPMNDiagram bpmn;
	protected Petrinet net;
	protected Marking m;
	
	protected List<String> warnings = new ArrayList<String>();
	protected List<String> errors = new ArrayList<String>();
	
	/* Contains the inputs and outputs of the processes */
	private Collection <Place> input = new ArrayList <Place> (), output  = new ArrayList <Place> ();
	
	/* Maps each start event to an arraylist of place */
	private Map <NodeID, ArrayList <Place>> startEventMap = new HashMap <NodeID, ArrayList <Place>> ();
	
	/* Maps each end event to an arraylist of place */
	private Map <NodeID, ArrayList <Place>> endEventMap = new HashMap <NodeID, ArrayList <Place>> ();
	
	/* Maps each BPMN control-flow edge to a place */
	private Map<BPMNEdge<BPMNNode, BPMNNode>, Place> flowMap = new HashMap<BPMNEdge<BPMNNode, BPMNNode>, Place>();
	
	/* Maps each BPMN node to a set of Petri net nodes (transitions and places) */
	private Map<BPMNNode, Set<PetrinetNode>> nodeMap = new HashMap<BPMNNode, Set<PetrinetNode>>();
		
	public BPMN2WorkflowSystemConverter(BPMNDiagram bpmn) 
	{
		this.bpmn = bpmn;	
		this.net = PetrinetFactory.newPetrinet("Petri net from " + bpmn.getLabel());
		this.m = new Marking();
		
		/* Initialize each nodeID with his arraylist */
		for (Swimlane s : bpmn.getPools()) 
		{
			startEventMap.put(s.getId(), new ArrayList <Place>());
			endEventMap.put(s.getId(), new ArrayList <Place>());
		}
	}
	
	public boolean convert() 
	{
		/* Traslate BPMN to Petri net */
		translateEdges();
		translateMessageFlows();
		translateEvents();
		translateActivities();
		translateSubProcesses();
		translateGateways();
		translateWorkflowSystem();

		return errors.size() == 0;
	}
	
	/**
	 * Translate edges of the BPMN diagram to places
	 */
	private void translateEdges()
	{
		for (Flow f : bpmn.getFlows()) 
		{
			Place p = net.addPlace(f.getSource().getLabel()+"_"+f.getTarget().getLabel()+"_"+f.getLabel());
			flowMap.put(f, p);
		}
	}
	
	/**
	 * Translate message flows of the BPMN diagram to places
	 */
	private void translateMessageFlows() 
	{
		for (MessageFlow f : bpmn.getMessageFlows()) 
		{
			Place p = net.addPlace("interface_" + f.getSource().getLabel()+"_"+f.getTarget().getLabel()+"_"+f.getLabel());
			flowMap.put(f, p);
		}
	}	
	
	/**
	 * Translate events to Petri net patterns
	 */
	private void translateEvents() 
	{
		for (Event e : bpmn.getEvents()) 
		{
			switch (e.getEventType()) 
			{
				case START:
					translateStartEvent(e);
					break;
				case END:
					translateEndEvent(e);
					break;
				case INTERMEDIATE:
					translateIntermediateEvent(e);
					break;
				default:
					warnings.add("Unknown event type " + e.getEventType() + " for " + e.getId() + " (" + e.getLabel() + ")");
					break;
			}
		}
	}
	
	/**
	 * Translate start event to Petri net
	 * 
	 * @param e the event
	 */
	private void translateStartEvent(Event e) 
	{
		Place p = net.addPlace("p_start_" + e.getLabel());
		Transition t = net.addTransition("t_start_"+e.getLabel());
		net.addArc(p, t);
		
		/* Save each pool its input */
		startEventMap.get(e.getParentPool().getId()).add(p);
		
		// Connect transition to place of outgoing edge
		for (BPMNEdge<?, ?> f : bpmn.getOutEdges(e)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(t, flowMap.get(f));
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(t, flowMap.get(f));
			}
		}
		
		// Connect transition to place of ingoing edge
		for (BPMNEdge<?, ?> f : bpmn.getInEdges(e))
		{
			if (f instanceof Flow) 
			{
				net.addArc(flowMap.get(f), t);
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(flowMap.get(f), t);
			}
		}
		setNodeMapFor(nodeMap, e, p, t);
	}
	
	/**
	 * Translate end event to Petri net
	 * 
	 * @param e the event
	 */
	private void translateEndEvent(Event e) {
		Place p = net.addPlace("p_end_"+e.getLabel());
		Transition t = net.addTransition("t_end_"+e.getLabel());
		net.addArc(t, p);
		
		/* Save each pool its output */
		endEventMap.get(e.getParentPool().getId()).add(p);

		// Connect transition to place of outgoing edge
		for (BPMNEdge<?, ?> f : bpmn.getOutEdges(e)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(t, flowMap.get(f));
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(t, flowMap.get(f));
			}
		}
		
		// Connect transition to place of ingoing edge
		for (BPMNEdge<?, ?> f : bpmn.getInEdges(e)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(flowMap.get(f), t);
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(flowMap.get(f), t);
			}
		}
		setNodeMapFor(nodeMap, e, p, t);
	}
	
	/**
	 * Translate intermediate event to Petri net
	 * 
	 * @param e the event
	 */
	private void translateIntermediateEvent(Event e) 
	{
		if (e.getEventTrigger() == EventTrigger.COMPENSATION)
			warnings.add("This translation does not support compensation events and does not preserve compensation semantics.\n The resulting Petri net should not be used for soundness chedcking.");
		
		String attachedActivity = (e.getBoundingNode() != null ? "_"+e.getBoundingNode().getLabel() : "");
		Transition t = net.addTransition("t_ev_"+e.getEventTrigger().name()+"_"+e.getLabel()+attachedActivity);
		
		// Connect transition to place of outgoing edge
		for (BPMNEdge<?, ?> f : bpmn.getOutEdges(e)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(t, flowMap.get(f));
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(t, flowMap.get(f));
			}
		}
		
		// Connect transition to place of ingoing edge
		for (BPMNEdge<?, ?> f : bpmn.getInEdges(e)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(flowMap.get(f), t);
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(flowMap.get(f), t);
			}
		}
		setNodeMapFor(nodeMap, e, t);
	}
	
	/**
	 * Translate activities to Petri net patterns. Depending on the attributes
	 * of the activity, it is translated as atomic (single transition), or with
	 * activity life-cycle (in case of multi-instance loop behavior or attached
	 * events)
	 * 
	 * updates {@link #nodeMap} and reads {@link #flowMap}
	 */
	private void translateActivities() 
	{
		for (Activity a : bpmn.getActivities())
			translateActivity(a);
	}
	
	/**
	 * Traslate activity to Petri net
	 * 
	 * @param a the activity
	 */
	private void translateActivity(Activity a) 
	{
		Set<PetrinetNode> nodeSet = new HashSet<PetrinetNode>();
		nodeMap.put(a, nodeSet);

		Transition t_act = net.addTransition("t_act_" + a.getLabel());
		Transition t_start = null;
		Transition t_end = null;
		Place p_ready = null;
		Place p_finished = null;

		// Create atomic or structured activity
		boolean model_structured = !translateActivityAtomic(a);
		if (model_structured) 
		{
			t_start = net.addTransition("t_act_"+a.getLabel()+"_start");
			t_end = net.addTransition("t_act_"+a.getLabel()+"_complete");
			p_ready = net.addPlace("p_act_"+a.getLabel()+"_ready");
			p_finished = net.addPlace("p_act_"+a.getLabel()+"_finished");

			// Connect start/end with activity
			net.addArc(t_start, p_ready);
			net.addArc(p_ready, t_act);
			net.addArc(t_act, p_finished);
			net.addArc(p_finished, t_end);

		} 
		else 
		{
			t_start = t_act;	// When connecting to the incoming/outgoing places
			t_end = t_act;
		}
		
		// There are different reasons for a structured activity
		if (model_structured) 
		{
			
			// Is a looped multi-instance activity
			if (a.isBLooped()) 
			{
				Transition t_repeat = net.addTransition("t_act_"+a.getLabel()+"_repeat");
				
				// Loop back
				net.addArc(p_finished, t_repeat);
				net.addArc(t_repeat, p_ready);
				
				nodeSet.add(t_act);
				nodeSet.add(t_start);
				nodeSet.add(t_end);
				nodeSet.add(t_repeat);
				nodeSet.add(p_ready);
				nodeSet.add(p_finished);
			}

			// Has attached events
			Event compensationEvent = null;
			for (Event e : getBoundaryEvents(a)) 
			{	
				if (e.getEventTrigger() == EventTrigger.COMPENSATION) 
				{
					compensationEvent = e;
					continue;
				}
				
				// For each boundary event: retrieve the transition representing the event and add an arc from the activity start to the event
				PetrinetNode e_nodes[] = nodeMap.get(e).toArray(new PetrinetNode[nodeMap.get(e).size()]);
				net.addArc(p_ready, (Transition)e_nodes[0]);
			}

			// Compensation events are translated differently
			if (compensationEvent != null)
			{
				// Compensation events are translated not as exclusive choice, but as parallel activation
				PetrinetNode e_nodes[] = nodeMap.get(compensationEvent).toArray(new PetrinetNode[nodeMap.get(compensationEvent).size()]);
				
				// Remember when the activity has been executed and enable compensation event correspondingly
				Place p_act_wasExecuted = net.addPlace("t_act_"+a.getLabel()+"_wasExecuted");
				net.addArc(t_act, p_act_wasExecuted);
				net.addArc(p_act_wasExecuted, (Transition)e_nodes[0]);		
				nodeSet.add(p_act_wasExecuted);
			}
			
		} 
		else 
		{
			// Default case of atomic task
			nodeSet.add(t_act);
		}
		
		// Connect transition to place of incoming edge
		for (BPMNEdge<?, ?> f : bpmn.getInEdges(a)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(flowMap.get(f), t_start);
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(flowMap.get(f), t_start);
			}
		}
		
		// Connect transition to place of outgoing edge
		for (BPMNEdge<?, ?> f : bpmn.getOutEdges(a)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(t_end, flowMap.get(f));
			}
			if (f instanceof MessageFlow) 
			{
				net.addArc(t_end, flowMap.get(f));
			}
		}
	}
	
	/**
	 * Translate subprocesses to Petri net patterns. Currently only as atomic tasks.
	 * 
	 * updates {@link #nodeMap} and reads {@link #flowMap}
	 */
	private void translateSubProcesses() 
	{
		for (SubProcess s : bpmn.getSubProcesses()) 
		{
			warnings.add("Subprocess '"+s.getLabel()+"' has been translated as activity; inner details are not considered.");
			translateActivity(s);
		}
	}
	
	/**
	 * @param a
	 * @return true iff the activity can be translated as a single atomic
	 *         transition (i.e., no multi-instance looping behavior, no attached
	 *         events etc.)
	 */
	private boolean translateActivityAtomic(Activity a) 
	{	
		if (a.isBLooped()) 
			return false;
		if (getBoundaryEvents(a).size() > 0) 
			return false;
		return true;
	}
	
	/**
	 * @param a the activity
	 * @return all events that are attached to the given activity
	 */
	private List<Event> getBoundaryEvents(Activity a) {
		List<Event> boundaryEvents = new ArrayList<Event>();
		for (Event e : bpmn.getEvents()) {
			if (e.getBoundingNode() == a) boundaryEvents.add(e);
		}
		return boundaryEvents;
	}
	
	/**
	 * Translates gateways to Petri net patterns, updated {@link #nodeMap}, reads {@link #flowMap}.
	 */
	private void translateGateways() 
	{
		for (Gateway g : bpmn.getGateways()) 
		{
			switch (g.getGatewayType()) 
			{
				case DATABASED:
				case EVENTBASED:
					translateXORGateway(g);
					break;
				case PARALLEL:
					translateANDGateway(g);
					break;
				case COMPLEX:
				case INCLUSIVE:
					translateORGateway(g);
					break;
				default:
					warnings.add("Unknown gateway type "+g.getGatewayType()+" for "+g.getId()+" ("+g.getLabel()+")");
					break;
			}
		}
	}
		
	/**
	 * Traslate xor-gateway to Petri net
	 * 
	 * @param g the gateway
	 */
	private void translateXORGateway(Gateway g) 
	{	
		Place p = net.addPlace("g_xor_"+g.getLabel());
		setNodeMapFor(nodeMap, g, p);
		
		// Connect transition to place of incoming edge
		for (BPMNEdge<?, ?> f : bpmn.getInEdges(g)) 
		{
			if (f instanceof Flow) 
			{
				Transition t = net.addTransition(f.getSource().getLabel()+"_merge_"+g.getLabel());
				net.addArc(t, p);
				net.addArc(flowMap.get(f), t);
				nodeMap.get(g).add(t);
			}
		}
			
		// Connect transition to place of outgoing edge
		for (BPMNEdge<?, ?> f : bpmn.getOutEdges(g)) 
		{
			if (f instanceof Flow) 
			{
				Transition t = net.addTransition(f.getTarget().getLabel()+"_split_"+g.getLabel());
				net.addArc(p, t);
				net.addArc(t, flowMap.get(f));
				nodeMap.get(g).add(t);
			}
		}
	}
	
	/**
	 * Traslate and-gateway to Petri net
	 * 
	 * @param g the gateway
	 */
	private void translateANDGateway(Gateway g) 
	{
		Transition t = net.addTransition("g_and_"+g.getLabel());
		setNodeMapFor(nodeMap, g, t);
		
		// Connect transition to place of incoming edge
		for (BPMNEdge<?, ?> f : bpmn.getInEdges(g)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(flowMap.get(f), t);
			}
		}
		
		// Connect transition to place of outgoing edge
		for (BPMNEdge<?, ?> f : bpmn.getOutEdges(g)) 
		{
			if (f instanceof Flow) 
			{
				net.addArc(t, flowMap.get(f));
			}
		}
	}
	
	/**
	 * Traslate or-gateway to Petri net
	 * 
	 * @param g the gateway
	 */
	private void translateORGateway(Gateway g) 
	{	
		Set<PetrinetNode> nodeSet = new HashSet<PetrinetNode>();
		nodeMap.put(g, nodeSet);
		
		// OR-join
		if (bpmn.getInEdges(g).size() > 1 && bpmn.getOutEdges(g).size() == 1) 
		{
			warnings.add("Cannot translate Inclusive-OR-Join to standard Petri nets. Translation of gateway "+g.getId()+" ("+g.getLabel()+") does not preserve the semantics.");
			
			BPMNEdge<?, ?> outEdge = (BPMNEdge<?, ?>)bpmn.getOutEdges(g).toArray()[0]; 
			if (!(outEdge instanceof Flow)) 
			{
				warnings.add("Cannot translate Inclusive-OR-Join to standard Petri nets. Gateway "+g.getId()+" ("+g.getLabel()+") has no outgoing control-flow edge.");
				return;
			}
			Place p_out = flowMap.get(outEdge);
			
			// Generate a transition for each non-empty subset of the outgoing edges
			
			// Get the set of all places representing outgoing edges
			Place p_ins[] = new Place[bpmn.getInEdges(g).size()];
			int ik=0;
			for (BPMNEdge<?, ?> f : bpmn.getInEdges(g)) 
			{
				if (f instanceof Flow) 
				{
					p_ins[ik] = flowMap.get(f);
					ik++;
				}
			}
			int n = p_ins.length;
			if (n == 0) {
				warnings.add("Cannot translate Inclusive-OR-Join to standard Petri nets. Gateway "+g.getId()+" ("+g.getLabel()+") has no incoming control-flow edge.");
				return;
			}
			
			// Then compute all subsets by counting to 2^n and using the bitmask of the number to tell which places to include in the subset
			for(int i = 0; i < (1<<n); i++){
				List<Place> p_subset = new ArrayList<Place>();
			    for(int j = 0; j < n; j++){
			        if( ((i>>j) & 1) == 1) { 	// bit j is on
			        	p_subset.add(p_ins[j]);// add place
			        }
			    }
			    // Not for the emtpy subset
			    if (p_subset.isEmpty()) continue;
			    
			    // Create transition for this subset and connect it to the post-places in the subset
			    Transition t = net.addTransition("g_ior_join_"+g.getLabel()+"_"+i);
			    nodeSet.add(t);
			    for (Place p_in : p_subset) 
			    {
			    	net.addArc(p_in, t);
			    }
			    net.addArc(t, p_out);
			}
			
		// OR-split with one incoming edge
		} 
		else 
		{
			BPMNEdge<?, ?> inEdge = (BPMNEdge<?, ?>)bpmn.getInEdges(g).toArray()[0]; 
			if (!(inEdge instanceof Flow)) 
			{
				warnings.add("Cannot translate Inclusive-OR-Join to standard Petri nets. Gateway "+g.getId()+" ("+g.getLabel()+") has no incoming control-flow edge.");
				return;
			}			
			Place p_in = flowMap.get(inEdge);
			
			// Generate a transition for each non-empty subset of the outgoing edges
			
			// Get the set of all places representing outgoing edges
			Place p_outs[] = new Place[bpmn.getOutEdges(g).size()];
			int ik=0;
			for (BPMNEdge<?, ?> f : bpmn.getOutEdges(g)) {
				p_outs[ik] = flowMap.get(f);
				ik++;
			}
			int n = p_outs.length;
			if (n == 0) {
				warnings.add("Cannot translate Inclusive-OR-Join to standard Petri nets. Gateway "+g.getId()+" ("+g.getLabel()+") has no outgoing control-flow edge.");
				return;
			}
			
			// Then compute all subsets by counting to 2^n and using the bitmask of the number to tell which places to include in the subset
			for(int i = 0; i < (1<<n); i++)
			{
				List<Place> p_subset = new ArrayList<Place>();
			    for(int j = 0; j < n; j++)
			    {
			        if( ((i>>j) & 1) == 1) 
			        { 	// Bit j is on
			        	p_subset.add(p_outs[j]);// add place
			        }
			    }
			    // Not for the emtpy subset
			    if (p_subset.isEmpty()) continue;
			    
			    // Create transition for this subset and connect it to the post-places in the subset
			    Transition t = net.addTransition("g_ior_split_"+g.getLabel()+"_"+i);
			    nodeSet.add(t);
			    for (Place p_out : p_subset) 
			    {
			    	net.addArc(t, p_out);
			    }
			    net.addArc(p_in, t);
			}
		}
	}
	
	/**
	 * Traslate the net in a workflow system
	 */
	private void translateWorkflowSystem() 
	{
		int pool = 0;
		
		/* Create for every pool a single place of input and/or output if he has more than one */
		for(NodeID nodeID : startEventMap.keySet()) 
		{				
			/* INPUT. Create XOR-split */
			if(startEventMap.get(nodeID).size() > 1) 
			{
				Place p = net.addPlace("pi_" + nodeID);
				for(int i = 0; i < startEventMap.get(nodeID).size(); i++)
				{
					Transition t = net.addTransition("ti_pool_" + pool + "_" + i);
					net.addArc(t, startEventMap.get(nodeID).get(i));
					net.addArc(p, t);
				}
				input.add(p);
			}
			else if(startEventMap.get(nodeID).size() == 1)
				input.add(startEventMap.get(nodeID).get(0));
							
			/* OUTPUT. Create XOR-join */
			if(endEventMap.get(nodeID).size() > 1) 
			{
				Place p = net.addPlace("po_" + nodeID);
				for(int i = 0; i < endEventMap.get(nodeID).size(); i++)
				{
					Transition t = net.addTransition("to_pool_" + pool + "_" + i);
					net.addArc(endEventMap.get(nodeID).get(i), t);
					net.addArc(t, p);
				}
				output.add(p);
			}
			else if(endEventMap.get(nodeID).size() == 1)
				output.add(endEventMap.get(nodeID).get(0));
			
			pool++;
		}
		
		/* Create workflow system */
		Place i = net.addPlace("i");
		Place o = net.addPlace("o");
		Transition ti = net.addTransition("ti");
		Transition to = net.addTransition("to");
		net.addArc(i, ti);
		net.addArc(to,o);
		m.add(i);
		
		// link the places and transitions
		for (Place pi : input)
			net.addArc(ti, pi);
		for (Place po : output)
			net.addArc(po, to);
	}

	/**
	 * Extend nodeMap with a new entry for n containing all nodes
	 * 
	 * @param nodeMap
	 * @param n
	 * @param nodes
	 */
	private void setNodeMapFor(Map<BPMNNode, Set<PetrinetNode>> nodeMap, BPMNNode n, PetrinetNode ...nodes) 
	{
		Set<PetrinetNode> nodeSet = new HashSet<PetrinetNode>();
		for (PetrinetNode n2 : nodes)
			nodeSet.add(n2);
		nodeMap.put(n, nodeSet);
	}

	/**
	 * @return resulting Petri net
	 */
	public Petrinet getPetriNet() 
	{
		return net;
	}

	/**
	 * @return initial marking of the resulting Petri net
	 */
	public Marking getMarking() 
	{
		return m;
	}
	
	public List<String> getWarnings() 
	{
		return warnings;
	}
	
	public List<String> getErrors() 
	{
		return errors;
	}
	
	public InfoConversionBP2PN getInfoConversionBP2PN(){
		InfoConversionBP2PN info = new InfoConversionBP2PN(startEventMap, endEventMap, flowMap,nodeMap);
		return info;
	}
	
}
