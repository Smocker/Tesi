package org.processmining.support.indexnode;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;


/**
 * Map contiene per ogni nodo una lista di indexnode associati
 * 
 * @author Maria Tourbanova
 */

public class IndexNodeMap {

	private Map<PetrinetNode, ArrayList<IndexNode>> map;
	private int maxIndex;

	public IndexNodeMap() {
		super();
		this.map = new ConcurrentHashMap<PetrinetNode, ArrayList<IndexNode>>();
		this.maxIndex = 0;
	}

	public ArrayList<IndexNode> getArrayIndexNode(PetrinetNode p) {
		return map.get(p);
	}

	public synchronized ArrayList<IndexNode> getArrayIndexNodeMinPi(PetrinetNode p, IndexNode pi) {
		ArrayList<IndexNode> arrayIndexNode = new ArrayList<IndexNode>();
		if (map.containsKey(p)) {
			for (IndexNode i : map.get(p)) {
				if (i.getIndex() < pi.getIndex()) {
					arrayIndexNode.add(i);
				}
			}
		}
		return arrayIndexNode;
	}
	
	public synchronized ArrayList<PetrinetNode> getArrayPetrinetNodeMinPi(PetrinetNode p, IndexNode pi) {
		ArrayList<PetrinetNode> arrayIndexNode = new ArrayList<PetrinetNode>();
		if (map.containsKey(p)) {
			for (IndexNode i : map.get(p)) {
				if (i.getIndex() < pi.getIndex()) {
					arrayIndexNode.add(i.getNode());
				}
			}
		}
		return arrayIndexNode;
	}
	

	public synchronized IndexNode insertIndexNode(PetrinetNode p, PetrinetNode p1) {
		maxIndex++;
		IndexNode indexNode = new IndexNode(p1, maxIndex);
		ArrayList<IndexNode> array = null;
		if ((array = map.get(p)) != null) {
			array.add(indexNode);
			System.out.println("indexNode " + indexNode);

		} else {
			array = new ArrayList<IndexNode>();
			array.add(indexNode);
			map.put(p, array);
			System.out.println("indexNode " + indexNode);
		}
		return indexNode;
	}

}
