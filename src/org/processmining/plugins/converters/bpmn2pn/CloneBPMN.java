package org.processmining.plugins.converters.bpmn2pn;



import java.util.HashMap;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.CallActivity;
import org.processmining.models.graphbased.directed.bpmn.elements.DataAssociation;
import org.processmining.models.graphbased.directed.bpmn.elements.DataObject;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;

public class CloneBPMN  {

	private static HashMap<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

	
	
	public static BPMNDiagram cloneFrom(
			DirectedGraph<BPMNNode, BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> graph) {
		BPMNDiagram bpmndiagram = (BPMNDiagram) graph;
		BPMNDiagram clonebpmndiagram = BPMNDiagramFactory.newBPMNDiagram("clone_"+bpmndiagram.getLabel());
		boolean newSwimlanes = true;
		while (newSwimlanes) {
			newSwimlanes = false;
			for (Swimlane s : bpmndiagram.getSwimlanes()) {
				// If swimlane has not been added yet
				Swimlane ss = null;
				if (!mapping.containsKey(s)) {
					newSwimlanes = true;
					Swimlane parentSwimlane = s.getParentSwimlane();
					// If there is no parent or parent has been added, add swimlane
					if (parentSwimlane == null) {
						ss = clonebpmndiagram.addSwimlane(s.getLabel(), parentSwimlane, s.getSwimlaneType());
						mapping.put(s, ss);
					} else if (mapping.containsKey(parentSwimlane)) {
						ss = clonebpmndiagram.addSwimlane(s.getLabel(), (Swimlane) mapping.get(parentSwimlane), 
								s.getSwimlaneType());
						mapping.put(s,ss);
					}
					ss.getAttributeMap().put("Original id", s.getAttributeMap().get("Original id"));
				}
			

			}
		}
		boolean newSubprocesses = true;
		while (newSubprocesses) {
			newSubprocesses = false;
			for (SubProcess s : bpmndiagram.getSubProcesses()) {
				// If subprocess has not been added yet
				SubProcess sb = null;
				if (!mapping.containsKey(s)) {
					newSubprocesses = true;
					if (s.getParentSubProcess() != null) {
						if (mapping.containsKey(s.getParentSubProcess())) {
							sb = clonebpmndiagram.addSubProcess(s.getLabel(), s.isBLooped(), s.isBAdhoc(), s.isBCompensation(),
									s.isBMultiinstance(), s.isBCollapsed(),
									(SubProcess) mapping.get(s.getParentSubProcess()));
									mapping.put(s,sb);							
						}
					} else if (s.getParentSwimlane() != null) {
						if (mapping.containsKey(s.getParentSwimlane())) {
							sb  = clonebpmndiagram.addSubProcess(s.getLabel(), s.isBLooped(), s.isBAdhoc(), s.isBCompensation(),
									s.isBMultiinstance(), s.isBCollapsed(),
									(Swimlane) mapping.get(s.getParentSwimlane()));
							mapping.put(s,sb);
						}
					} else{
						sb = clonebpmndiagram.addSubProcess(s.getLabel(), s.isBLooped(), s.isBAdhoc(), s.isBCompensation(),
								s.isBMultiinstance(), s.isBCollapsed());
						mapping.put(s,sb);}
				sb.getAttributeMap().put("Original id", s.getAttributeMap().get("Original id"));
				}

			}
		}
		for (Activity a : bpmndiagram.getActivities()) {
			Activity aa = null; 
			if (a.getParentSubProcess() != null) {
				if (mapping.containsKey(a.getParentSubProcess())) {
				aa= 	clonebpmndiagram.addActivity(a.getLabel(), a.isBLooped(), a.isBAdhoc(), a.isBCompensation(),
						a.isBMultiinstance(), a.isBCollapsed(),
						(SubProcess) mapping.get(a.getParentSubProcess()));
						mapping.put(
							a,
							aa);
				}
			} else if (a.getParentSwimlane() != null) {
				if (mapping.containsKey(a.getParentSwimlane())) {
					aa= 	clonebpmndiagram.addActivity(a.getLabel(), a.isBLooped(), a.isBAdhoc(), a.isBCompensation(),
									a.isBMultiinstance(), a.isBCollapsed(),
									(Swimlane) mapping.get(a.getParentSwimlane()));
					mapping.put(
							a,
							aa);
				}
			} else{
				aa= clonebpmndiagram.addActivity(a.getLabel(), a.isBLooped(), a.isBAdhoc(), a.isBCompensation(),
						a.isBMultiinstance(), a.isBCollapsed());
				mapping.put(
						a,
						aa);
				}
			aa.getAttributeMap().put("Original id", a.getAttributeMap().get("Original id"));
			
		}

        for (CallActivity a : bpmndiagram.getCallActivities()) {
        	CallActivity aa = null;
        	if (a.getParentSubProcess() != null) {
                if (mapping.containsKey(a.getParentSubProcess())) {
                    aa = clonebpmndiagram.addCallActivity(a.getLabel(), a.isBLooped(), a.isBAdhoc(), a.isBCompensation(),
                            a.isBMultiinstance(), a.isBCollapsed(),
                            (SubProcess) mapping.get(a.getParentSubProcess()));
                	mapping.put(a,aa);
                }
            } else if (a.getParentSwimlane() != null) {
                if (mapping.containsKey(a.getParentSwimlane())) {
                    aa = clonebpmndiagram.addCallActivity(a.getLabel(), a.isBLooped(), a.isBAdhoc(), a.isBCompensation(),
                            a.isBMultiinstance(), a.isBCollapsed(),
                            (Swimlane) mapping.get(a.getParentSwimlane()));
                	mapping.put(a,aa);
                }
            } else {
            	aa = clonebpmndiagram.addCallActivity(a.getLabel(), a.isBLooped(), a.isBAdhoc(), a.isBCompensation(),
                        a.isBMultiinstance(), a.isBCollapsed());
                mapping.put(a,aa);
            }
        	aa.getAttributeMap().put("Original id", a.getAttributeMap().get("Original id"));
        	}

		for (Event e : bpmndiagram.getEvents()) {
			Event ee = null; 
			if (e.getParentSubProcess() != null) {
				if (mapping.containsKey(e.getParentSubProcess())) {
					ee =	clonebpmndiagram.addEvent(e.getLabel(), e.getEventType(), e.getEventTrigger(), e.getEventUse(),
							(SubProcess) mapping.get(e.getParentSubProcess()), e.getBoundingNode());
						mapping.put(e,ee);
				}
			} else if (e.getParentSwimlane() != null) {
				if (mapping.containsKey(e.getParentSwimlane())) {
					ee =	clonebpmndiagram.addEvent(e.getLabel(), e.getEventType(), e.getEventTrigger(), e.getEventUse(),
							(Swimlane) mapping.get(e.getParentSwimlane()), e.getBoundingNode());
							mapping.put(e,ee);
				}
			} else{
				ee =	clonebpmndiagram.addEvent(e.getLabel(), e.getEventType(), e.getEventTrigger(), e.getEventUse(),
						e.getBoundingNode());
				mapping.put(e,ee);
			}
			ee.getAttributeMap().put("Original id", e.getAttributeMap().get("Original id"));
		}
		for (Gateway g : bpmndiagram.getGateways()) {
			Gateway gg = null; 
			if (g.getParentSubProcess() != null) {
				if (mapping.containsKey(g.getParentSubProcess())) {
				gg = clonebpmndiagram.addGateway(g.getLabel(), g.getGatewayType(),
						(SubProcess) mapping.get(g.getParentSubProcess()));
				mapping.put(g,gg);
				}
			} else if (g.getParentSwimlane() != null) {
				if (mapping.containsKey(g.getParentSwimlane())) {
					gg = clonebpmndiagram.addGateway(g.getLabel(), g.getGatewayType(), (Swimlane) mapping.get(g.getParentSwimlane()));
					mapping.put(g,gg);
				}
			} else{
				gg = clonebpmndiagram.addGateway(g.getLabel(), g.getGatewayType());
				mapping.put(g, gg);
			}
			
			gg.getAttributeMap().put("Original id", g.getAttributeMap().get("Original id"));
		}
		
		for (DataObject d : bpmndiagram.getDataObjects()) {
				mapping.put(d, clonebpmndiagram.addDataObject(d.getLabel()));
		}

		for (Flow f : bpmndiagram.getFlows()) {
			Flow ff = clonebpmndiagram.addFlow((BPMNNode) mapping.get(f.getSource()), 
					(BPMNNode) mapping.get(f.getTarget()), f.getLabel());
			mapping.put(f, ff);
			ff.getAttributeMap().put("Original id", f.getAttributeMap().get("Original id"));
		}
		for (MessageFlow f : bpmndiagram.getMessageFlows()) {
			MessageFlow mf = clonebpmndiagram.addMessageFlow((BPMNNode) mapping.get(f.getSource()), 
					(BPMNNode) mapping.get(f.getTarget()), f.getLabel());
			mapping.put(f, mf);
			mf.getAttributeMap().put("Original id", f.getAttributeMap().get("Original id"));
		}
		for (DataAssociation a : bpmndiagram.getDataAssociations()) {
			DataAssociation da = clonebpmndiagram.addDataAssociation((BPMNNode) mapping.get(a.getSource()), 
					(BPMNNode) mapping.get(a.getTarget()), a.getLabel()); 
			mapping.put(a, da);
			da.getAttributeMap().put("Original id", a.getAttributeMap().get("Original id"));
		}

		clonebpmndiagram.getAttributeMap().clear();
		AttributeMap map = bpmndiagram.getAttributeMap();
		for (String key : map.keySet()) {
			clonebpmndiagram.getAttributeMap().put(key, map.get(key));
		}
		return clonebpmndiagram;
	}

	public HashMap<DirectedGraphElement, DirectedGraphElement> getMapping() {
		return mapping;
	}


}
