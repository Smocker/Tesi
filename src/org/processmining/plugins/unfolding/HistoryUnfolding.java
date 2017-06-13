package org.processmining.plugins.unfolding;

import java.awt.Color;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.converters.bpmn2pn.InfoConversionBP2PN;

public class HistoryUnfolding {

	private Petrinet petri;
	private ArrayList<Collection<PetrinetNode>> cct;


	public HistoryUnfolding(Petrinet p){
		this.petri=p;

	}

	public ArrayList<Collection<BPMNNode>> HistoryonBP( InfoConversionBP2PN info, BPMNDiagram bpmn){
		ArrayList<Collection<BPMNNode>> bpmnnodes = new ArrayList<Collection<BPMNNode>>();
		for(Collection<PetrinetNode> collection: cct){
			Collection<BPMNNode> bpmnnode = new ArrayList<BPMNNode>();
			bpmnnodes.add(bpmnnode);
			for( PetrinetNode pnnode: collection){
				if(pnnode instanceof Transition){
					Transition t = (Transition) pnnode;
					BPMNNode node = getNodefromTransition(info,t);
					if(node!=null){
						bpmnnode.add(node);
						//BPMNNode nodeclone =  getNodeinClone(bpmn, node);
						//nodeclone.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.ORANGE);
					}
				}
			}

		}
		return bpmnnodes;
	}

	private BPMNNode getNodefromTransition( InfoConversionBP2PN info,  Transition t ){
		Map<BPMNNode, Set<PetrinetNode>> nodemap = info.getNodeMap();
		for(BPMNNode node :nodemap.keySet()){
			Set<PetrinetNode> petrinetnodes = nodemap.get(node);
			for(PetrinetNode petrinetnode: petrinetnodes){
				if(petrinetnode instanceof Transition)
					if(petrinetnode.getLabel().equals(t.getLabel())){
						return node;
					}
			}
		}
		return null;
	}


	public ArrayList<Collection<PetrinetNode>> createHistoryBFS(){
		Set<PetrinetNode> nodes = petri.getNodes();
		PetrinetNode fnode = null;
		for(PetrinetNode nod : nodes){

			if(nod.getGraph().getInEdges(nod).isEmpty()){
				fnode = nod;
				break;
			}
		}

		return searchBFS(nodes, fnode);
	}

	// breadth-first search from multiple sources

	public ArrayList<Collection<PetrinetNode>> searchBFS(Set<PetrinetNode> Graph, PetrinetNode fnode){
		cct = new ArrayList<Collection<PetrinetNode>>();
		Queue<Pair<ArrayList<PetrinetNode>,PetrinetNode>> q = new LinkedBlockingQueue<Pair<ArrayList<PetrinetNode>,PetrinetNode>>();

		
		ArrayList<PetrinetNode> ct = new ArrayList<PetrinetNode>();
		q.add(new Pair<ArrayList<PetrinetNode>, PetrinetNode>(ct, fnode));
		cct.add(ct);
		while (!q.isEmpty()) {
			Pair<ArrayList<PetrinetNode>, PetrinetNode> v = q.poll();
			PetrinetNode node = v.getSecond();
			ct = v.getFirst();
			ct.add(node);
			int i = 0;
			boolean flag= true;
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edgesin = node.getGraph().getInEdges(node);
			if(edgesin.size()>1){
				flag = false;
				boolean preset = false;
				for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>  edge : edgesin){
					PetrinetNode nodesource = edge.getSource();
					if(ct.contains(nodesource)){
						preset = true;
					}else{
						preset = false;
					}
				}
				if(preset){
					flag= true;
				}
			}
			if(flag)
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :node.getGraph().getOutEdges(node)){
				
				if(node instanceof Place){
					Place p = (Place) node;
					if(p.getGraph().getOutEdges(p).size()>1 && i==1){
						ct = (ArrayList<PetrinetNode>) ct.clone();
						cct.add(ct);
					}
				}
				
				
				PetrinetNode nodet = edge.getTarget();
				q.add(new Pair<ArrayList<PetrinetNode>, PetrinetNode>(ct, nodet));
				i++;
			}
			
		}

		return cct;

	}
	
	public void searcDFS(PetrinetNode node, ArrayList<PetrinetNode> ct,Queue<PetrinetNode> q  ){
		int i=0;
		
	}
	
	

	public ArrayList<Collection<PetrinetNode>> createHistoryDFS(){
		Set<PetrinetNode> nodes = petri.getNodes();
		PetrinetNode fistnode = null;
		for(PetrinetNode nod : nodes){

			if(nod.getGraph().getInEdges(nod).isEmpty()){
				fistnode = nod;
				break;
			}
		}


		cct = new ArrayList<Collection<PetrinetNode>>();
		if(fistnode!=null){
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :fistnode.getGraph().getOutEdges(fistnode)){
				ArrayList<PetrinetNode> ct = new ArrayList<PetrinetNode>();

				cct.add(ct);

				ct.add(fistnode);
				PetrinetNode node = edge.getTarget();
				searchDFS(node,ct);
			}

		}


		return cct;
	}


	private void searchDFS(PetrinetNode node, ArrayList<PetrinetNode> ct) {
		int index = 0;
		boolean flag = true;
		ArrayList<PetrinetNode> at1 = (ArrayList<PetrinetNode>) ct.clone();
		if(node.getGraph().getOutEdges(node).isEmpty()){
			ct.add(node);
		}
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edgesin = node.getGraph().getInEdges(node);
		if(edgesin.size()>1){
			flag = false;
			boolean preset = false;
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>  edge : edgesin){
				PetrinetNode nodesource = edge.getSource();
				if(ct.contains(nodesource)){
					preset = true;
				}else{
					preset = false;
				}
			}
			if(preset){
				flag= true;
			}
		}
		if(flag)
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :node.getGraph().getOutEdges(node)){
				if(index==0){

					ct.add(node);

					PetrinetNode nodetarget = edge.getTarget();
					searchDFS(nodetarget,ct);

				}else{
					if(node instanceof Transition){
						ct.add(node);

						PetrinetNode nodetarget = edge.getTarget();
						searchDFS(nodetarget,ct);

					}else{
						cct.add(at1);
						at1.add(node);
						PetrinetNode nodetarget = edge.getTarget();
						searchDFS(nodetarget,at1);
					}

				}
				index++;
			}

	}

}
