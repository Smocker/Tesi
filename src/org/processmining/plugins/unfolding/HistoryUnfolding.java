package org.processmining.plugins.unfolding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class HistoryUnfolding {
	
	private Petrinet petri;
	private ArrayList<Collection<PetrinetNode>> cct;
	
	
	public HistoryUnfolding(Petrinet p){
		this.petri=p;
		
	}

	
	public ArrayList<Collection<PetrinetNode>> createHistory(){
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
				search(node,ct);
			}

		}


		return cct;
	}


	private void search(PetrinetNode node, ArrayList<PetrinetNode> ct) {
		int index = 0;
		ArrayList<PetrinetNode> at1 = (ArrayList<PetrinetNode>) ct.clone();
		if(node.getGraph().getOutEdges(node).isEmpty()){
			ct.add(node);
		}
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :node.getGraph().getOutEdges(node)){
			if(index==0){
				
					ct.add(node);
					
					PetrinetNode nodetarget = edge.getTarget();
					search(nodetarget,ct);
				
			}else{

				
					cct.add(at1);
					at1.add(node);
					PetrinetNode nodetarget = edge.getTarget();
					search(nodetarget,at1);
				
			}
			index++;
		}
		
	}
	
}
