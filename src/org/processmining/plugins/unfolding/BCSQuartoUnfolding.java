package org.processmining.plugins.unfolding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.converters.bpmn2pn.ClonePetrinet;
import org.processmining.support.localconfiguration.LocalConfiguration;
import org.processmining.support.unfolding.Pair;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.Utility;

public class BCSQuartoUnfolding {

	protected PluginContext context;

	/* Reti di petri */
	protected Petrinet petrinet, unfolding;

	/* Variabili per la trasformazione della rete di Petri in N* */
	protected Place i, o;
	protected Transition reset;

	protected LinkedBlockingQueue<LocalConfiguration> queue = new LinkedBlockingQueue<LocalConfiguration>();
	// protected LocalConfigurationMap localConfigurationMap = new
	// LocalConfigurationMap();
	protected StatisticMap statisticMap = new StatisticMap();

	/*
	 * Mappa ogni nodo della rete di Petri a un uno o più nodi della rete di
	 * unfolding
	 */
	protected Map<PetrinetNode, ArrayList<PetrinetNode>> petri2UnfMap = new HashMap<PetrinetNode, ArrayList<PetrinetNode>>();
	/* Mappa ogni nodo della rete di unfolding a un nodo della rete di Petri */
	protected Map<PetrinetNode, PetrinetNode> unf2PetriMap = new HashMap<PetrinetNode, PetrinetNode>();
	protected Map<PetrinetNode, ArrayList<PetrinetNode>> markingMap = new HashMap<PetrinetNode, ArrayList<PetrinetNode>>();

	protected ArrayList<Transition> transitions = new ArrayList<Transition>();

	/**
	 * Costruttore
	 * 
	 * @param context
	 *            contesto di ProM
	 * @param petrinet
	 *            rete di petri originale
	 */
	BCSQuartoUnfolding(PluginContext context, Petrinet petrinet) {
		this.context = context;
		this.petrinet = PetrinetFactory.clonePetrinet(petrinet);
		ClonePetrinet pnc = new ClonePetrinet(petrinet.getLabel());
		pnc.cloneFrom(petrinet, true, true, true, false, false);
		this.petrinet = pnc;
		this.unfolding = PetrinetFactory.newPetrinet("Unfolding from Petrinet");
	}

	/**
	 * Converte una rete di Petri in una rete BCS unfolding
	 * 
	 * @return la rete BCS unfolding e le sue statistiche
	 * @throws InterruptedException
	 */
	public Object[] convert() throws InterruptedException {
		i = Utility.getStartNode(petrinet);
		o = Utility.getEndNode(petrinet);

		/* Inizio la costruzione della rete inserendo la piazza iniziale i1 */
		Place i1 = unfolding.addPlace(i.getLabel());
		refreshCorrispondence(i, i1);

		/* Trasformo la rete di Petri N in N* */
		reset = petrinet.addTransition("reset");
		petrinet.addArc(o, reset);
		petrinet.addArc(reset, i);
		System.out.println("UNFOLDING BCS");

		/* Inizializzo e visito la coda */
		initQueue(i, i1);
		visitQueue();

		/* Estraggo i deadlock ed effettuo le statistiche della rete */
		writeLog(context, "Extraction of the dealock points...");
		getStatistics();
		System.out.println("FINE");
		return new Object[] { unfolding, statisticMap };
	}

	private void initQueue(Place p, Place p1) throws InterruptedException {
		/*
		 * Per tutte le transizioni t della rete di petri attaccate alla piazza
		 * iniziale p
		 */
		for (DirectedGraphEdge<?, ?> a1 : petrinet.getGraph().getOutEdges(p)) {
			/* Creo una transizione t1 nell'unfolding e attacco p1 con t1 */
			Transition t = (Transition) a1.getTarget();
			String id = "";
			try {
				id = t.getAttributeMap().get("Original id").toString();
			} catch (NullPointerException e) {
				id = "_not_present";
			}
			Transition t1 = unfolding.addTransition(t.getLabel());
			t1.getAttributeMap().put("Original id", id);
			unfolding.addArc(p1, t1);

			/* Per tutti i place u delle rete di petri attaccate a t */
			for (DirectedGraphEdge<?, ?> a2 : petrinet.getGraph().getOutEdges(t)) {
				// Creo un place u1 nell'unfolding e attacco t1 con u1
				Place u = (Place) a2.getTarget();
				Place u1 = unfolding.addPlace(u.getLabel());
				u1.getAttributeMap().put("Original id", u.getAttributeMap().get("Original id"));
				unfolding.addArc(t1, u1);
				refreshCorrispondence((PetrinetNode) u, u1);
			}

			/* Aggiorno tutte le strutture globali e la coda */
			refreshCorrispondence(t, t1);
			LocalConfiguration lc = new LocalConfiguration();
			lc.set(unfolding, t1);
			lc.setMarking(Utility.getMarking(unfolding, lc));
			queue.put(lc);
		}
	}

	private void visitQueue() throws InterruptedException {
		int d = 0;
		while (!queue.isEmpty()) {//d < 150000) {
			d++;
			LocalConfiguration localConfig = queue.poll();
			System.out.println("LocalConfiguration " + localConfig);

			ArrayList<PetrinetNode> markingUnfolding = localConfig.getMarking(); // Utility.getMarking(unfolding,
																					// localConfig);
			ArrayList<PetrinetNode> markingPetrinet = new ArrayList<PetrinetNode>();// petrinet

			System.out.println("markingUnf " + markingUnfolding);

			for (PetrinetNode tlcm : markingUnfolding) {
				markingPetrinet.add(unf2PetriMap.get(tlcm));
			}

			for (PetrinetNode t1 : petrinet.getTransitions()) {// petrinet
				List<PetrinetNode> presetAbilitato = Utility.isEnabledFromMarking(markingPetrinet, t1, petrinet);// petrinet
				if (!presetAbilitato.isEmpty() && !markingUnfolding.isEmpty()) {
					System.out.println("marking " + markingPetrinet);
					System.out.println(t1 + " è abilitata");

					PetrinetNode nodoStessaHistory = null;
					boolean stessaHistory = false;
					List<PetrinetNode> phistory = new ArrayList<PetrinetNode>();
					// controllo la history del nodo
					if (petri2UnfMap.containsKey(t1)) {

						ArrayList<PetrinetNode> t1Unf = petri2UnfMap.get(t1);

						System.out.println("markingUnf " + markingUnfolding);
						for (int j = 0; j < t1Unf.size() && !stessaHistory; j++) {
							nodoStessaHistory = t1Unf.get(j);
							if (!Utility.isEnabledFromMarking(markingUnfolding, nodoStessaHistory, unfolding)
									.isEmpty()) {
								ArrayList<Place> historyT1 = Utility.getHistoryPlace(unfolding, t1Unf.get(j));
								System.out.println("historyT1 " + t1Unf.get(j) + historyT1);
								// for (int x = 0; x < markingUnfolding.size()
								// && !stessaHistory; x++) {

								/*
								 * ArrayList<Place> historyPlace =
								 * Utility.getHistoryPlace(unfolding,
								 * presetAbilitato, petri2UnfMap,
								 * markingUnfolding); System.out.println(
								 * "historyPlace " + historyPlace);
								 * 
								 * if (historyT1.containsAll(historyPlace)) {
								 * stessaHistory = true;
								 * System.out.println("stessaHistory");
								 * 
								 * }
								 */
								
								for (PetrinetNode nodoAbilitato : presetAbilitato) {
									ArrayList<PetrinetNode> nodoAbilitatoArray = petri2UnfMap.get(nodoAbilitato);

									for (PetrinetNode nodoAbilitatoUnf : nodoAbilitatoArray) {
										if (markingUnfolding.contains(nodoAbilitatoUnf)) {

											Transition temp2 = unfolding.addTransition(t1.getLabel());

											unfolding.addArc((Place) nodoAbilitatoUnf, temp2);
											ArrayList<Place> historytemp2 = Utility.getHistoryPlace(unfolding, temp2);

											if (historyT1.containsAll(historytemp2)) {
												stessaHistory = true;
												System.out.println("stessaHistory");

											} else {
												stessaHistory = false;
												phistory.add(nodoAbilitatoUnf);
											}
											unfolding.removeArc(nodoAbilitatoUnf, temp2);
											unfolding.removeTransition(temp2);

										}

									}
									
									
									

								}
							}
						}
					}

					if (!stessaHistory) {

						String id = "";
						try {
							id = t1.getAttributeMap().get("Original id").toString();
						} catch (NullPointerException e) {
							id = "_not_present";
						}

						Transition t2 = unfolding.addTransition(t1.getLabel());
						t2.getAttributeMap().put("Original id", id);
						System.out.println("addTransition " + t1);
						// petri2UnfMap

						for (PetrinetNode nodoAbilitato : presetAbilitato) {
							ArrayList<PetrinetNode> nodoAbilitatoArray = petri2UnfMap.get(nodoAbilitato);
							for (PetrinetNode nodoAbilitatoUnf : nodoAbilitatoArray) {
								if (markingUnfolding.contains(nodoAbilitatoUnf)) {
									if(phistory.isEmpty() || phistory.contains(nodoAbilitatoUnf)){
									System.out.println("unfolding.addArc(p, t) " + nodoAbilitatoUnf + " " + t2);
									unfolding.addArc((Place) nodoAbilitatoUnf, t2);
									}
								}
							}
						}
						refreshCorrispondence(t1, t2);

						if (t1.equals(reset)) {
							if (markingMap.get(t2).size() == 0)
								statisticMap.addCutoff((Transition) t2);
							else
								statisticMap.addCutoffUnbounded((Transition) t2);
						} else {
							boolean isCutoff = false;
							List<PetrinetNode> postset = Utility.getPostset(petrinet, t1);

							// Verifico se una piazza finale di t2 è
							// condivisa
							// da altre transizioni e se provoca cutoff
							for (int i = 0; i < postset.size() && !isCutoff; i++)
								isCutoff = isCutoff(t2, postset.get(i));

							if (isCutoff == false) {
								for (PetrinetNode post : Utility.getPostset(petrinet, t1)) {
									Place p1 = unfolding.addPlace(post.getLabel());
									p1.getAttributeMap().put("Original id", post.getAttributeMap().get("Original id"));
									unfolding.addArc(t2, p1);
									System.out.println("unfolding.addArc(t, p) " + t2 + " " + p1);
									refreshCorrispondence(post, p1);
								}
								LocalConfiguration localConfigNew = localConfig.clone();
								localConfigNew.addAll(t2);
								localConfigNew.setMarking(Utility.getMarking(unfolding, localConfigNew));
								System.out.println("non è cutoff LocalConfiguration " + localConfigNew);
								queue.put(localConfigNew);
							}
						}
					} else {
						// if
						// (!statisticMap.getCutoff().contains(nodoStessaHistory))
						// {
						LocalConfiguration ll = localConfig.clone();
						ll.addAll((Transition) nodoStessaHistory);
						ll.setMarking(Utility.getMarking(unfolding, ll));

						System.out.println("stessa history " + localConfig);
						System.out.println("stessa history " + ll);
						queue.put(ll);
						// }
					}

				}
			}
		}
	}

	/**
	 * Aggiorna le corrispondenze delle map
	 * 
	 * @param pn
	 *            nodo della rete di Petri
	 * @param pn1
	 *            nodo della rete di unfolding
	 */
	private void refreshCorrispondence(PetrinetNode pn, PetrinetNode pn1) {
		/* Aggiorno le map delle corrispondenze */
		if (!petri2UnfMap.containsKey(pn))
			petri2UnfMap.put(pn, new ArrayList<PetrinetNode>());
		petri2UnfMap.get(pn).add(pn1);
		unf2PetriMap.put(pn1, pn);

		/* Se è una transizione aggiornare le altre map */

		if (pn1 instanceof Transition) {
			LocalConfiguration lc = new LocalConfiguration();
			lc.set(unfolding, pn1);
			markingMap.put(pn1, Utility.getMarking(petrinet, lc, unf2PetriMap));
			// xorMap.put(pn1,Utility.getHistoryXOR(unfolding, pn1, null));
		}

	}

	private boolean isCutoff(Transition t, PetrinetNode place) {
		int isBounded;

		// Controllo se place è stato inserito nell'unfolding
		if (petri2UnfMap.containsKey(place)) {
			ArrayList<PetrinetNode> markingT = markingMap.get(t);

			// Se nella storia dei place di t esiste place allora è un ciclo
			for (Place h : Utility.getHistoryPlace(unfolding, t)) {
				if (unf2PetriMap.get(h).equals(place)) {
					for (DirectedGraphEdge<?, ?> a : unfolding.getGraph().getInEdges(h)) {
						isBounded = Utility.isBounded(markingT, markingMap.get(a.getSource()));
						if (isBounded == 0) {
							statisticMap.addCutoff(t);
							return true;
						} else if (isBounded > 0) {
							statisticMap.addCutoffUnbounded(t);
							return true;
						}
					}
				}
			}
			return false;
		} else
			return false;
	}

	// confronto la label delle transition e NON VA BENE
	// t2 petrinet, tunf unfolding, lc2 petrinet
	private boolean isCutoff(LocalConfiguration lc2, Transition t2, Transition tUnf) {
		ArrayList<PetrinetNode> markingT2 = Utility.getMarking(petrinet, lc2);
		for (Transition t1 : lc2.get()) {
			System.out.println("isCutoff? " + t1 + " " + t2);
			if (t2.getLabel() != t1.getLabel()) {
				LocalConfiguration lc1 = new LocalConfiguration();
				lc1.set(unfolding, t1);
				ArrayList<PetrinetNode> markingT1 = Utility.getMarking(petrinet, lc1);
				System.out.println("lc1 " + lc1 + "markingT1 " + markingT1);
				System.out.println("lc2 " + lc2 + "markingT2 " + markingT2);
				if (markingT2.size() == 0 || t2.getLabel() == "reset"
						|| (markingT2.size() == markingT1.size() && markingT2.containsAll(markingT1))) {
					// cutoff
					statisticMap.addCutoff(tUnf);
					System.out.println("cutoff " + tUnf);
					return true;
				} else if (markingT2.containsAll(markingT1)) {
					// cutoff unbounded
					statisticMap.addCutoffUnbounded(tUnf);
					System.out.println("cutoff unbounded " + tUnf);
					return true;
				}
			}
		}
		System.out.println("not Cutoff " + t2);
		return false;
	}

	private void getStatistics() {
		/* Inserisco i livelock trovati in un lista */
		ArrayList<Transition> cutoff = new ArrayList<Transition>(
				statisticMap.getCutoff().size() + statisticMap.getCutoffUnbounded().size());
		for (int i = 0; i < statisticMap.getCutoff().size(); i++)
			cutoff.add(statisticMap.getCutoff().get(i));
		for (int i = 0; i < statisticMap.getCutoffUnbounded().size(); i++)
			cutoff.add(statisticMap.getCutoffUnbounded().get(i));

		/* Filtro i punti di cutoff per ottenere un primo insieme di spoilers */
		ArrayList<Transition> spoilers = filterCutoff(cutoff);

		/* Individuo i deadlock */
		ArrayList<Transition> deadlock = getDeadlock(cutoff, spoilers);
		if (deadlock != null)
			statisticMap.setDeadlock(deadlock);

		/* Inserisco le altre statistiche */
		statisticMap.setStatistic(petrinet, unfolding, petri2UnfMap, null);

	}

	/**
	 * @param cutoff
	 * @return
	 */
	private ArrayList<Transition> filterCutoff(ArrayList<Transition> cutoff) {
		ArrayList<Transition> cutoffHistory = new ArrayList<Transition>(), filter = new ArrayList<Transition>();

		/* */
		for (Transition v : cutoff) {
			cutoffHistory = new ArrayList<Transition>();
			LocalConfiguration lc = new LocalConfiguration();
			for (Transition u : lc.set(petrinet, v))
				if (!cutoffHistory.contains(u))
					cutoffHistory.add(u);

			/* */
			for (Place p : unfolding.getPlaces()) {
				if (unfolding.getGraph().getOutEdges(p).size() > 1) {
					for (DirectedGraphEdge<?, ?> a : unfolding.getGraph().getOutEdges(p)) {
						Arc arc = (Arc) a;
						Transition t = (Transition) arc.getTarget();
						if (!filter.contains(t) && !cutoffHistory.contains(t) && !(cutoff.contains(t)))
							filter.add(t);
					}
				}
			}
		}
		return filter;
	}

	/**
	 * Estrae i punti di deadlock
	 * 
	 * @param cutoff:
	 *            arraylist contenente i punti di cutoff
	 * @return arraylist contenente i punti di deadlock
	 */
	private ArrayList<Transition> getDeadlock(ArrayList<Transition> cutoff, ArrayList<Transition> spoilers) {
		Transition s = null;
		ArrayList<Transition> deadlock = null, cutoff1 = null, spoilers2 = null;

		if (!cutoff.isEmpty()) {
			Transition t = cutoff.get(0);
			ArrayList<Transition> spoilers1 = getSpoilers(t, spoilers);
			while (!spoilers1.isEmpty() && deadlock == null) {
				s = spoilers1.remove(0);
				cutoff1 = removeConflict(cutoff, s);
				spoilers2 = removeConflict(spoilers, s);
				if (cutoff1.isEmpty()) {
					deadlock = new ArrayList<Transition>();
					deadlock.add(s);
				} else {
					deadlock = getDeadlock(cutoff1, spoilers2);
					if (deadlock != null)
						deadlock.add(s);
				}
			}
			return deadlock;
		} else
			return null;
	}

	/**
	 * Scelto come nuovo insieme quelle che non sono in conflitto con lo spoiler
	 * 
	 * @param cutoff:
	 *            insieme corrente di cutoff
	 * @param spoiler:
	 *            spoiler corrente
	 * @return cutoff1: arraylist di transizioni contenente la nuova lista di
	 *         cutoff
	 */
	private ArrayList<Transition> removeConflict(ArrayList<Transition> cutoff, Transition spoiler) {
		/* Se è vuota ritorna lista vuota */
		if (cutoff.isEmpty())
			return new ArrayList<Transition>();
		else {
			ArrayList<Transition> cutoff1 = new ArrayList<Transition>();
			ArrayList<Pair> xorSpoiler = Utility.getHistoryXOR(unfolding, spoiler, null);// xorMap.get(spoiler);

			/*
			 * Se le transizioni del cutoff non sono in conflitto con lo spoiler
			 * le aggiungo alla nuova lista
			 */
			for (Transition t : cutoff)
				if (t != spoiler && !Utility.isConflict(Utility.getHistoryXOR(unfolding, t,
						null)/* xorMap.get(t) */, xorSpoiler))
					cutoff1.add(t);
			return cutoff1;
		}
	}

	/**
	 * Prendo tutte le transizioni che sono in conflitto con il cutoff
	 * 
	 * @param t:
	 *            cutoff scelto
	 * @return spoilers: arraylist di transizioni contenenti tutte le
	 *         transizioni in conflitto con il cutoff
	 */
	private ArrayList<Transition> getSpoilers(Transition t, ArrayList<Transition> set) {
		/* Se è vuota ritorna lista vuota */
		if (set.isEmpty())
			return new ArrayList<Transition>();
		else {
			ArrayList<Transition> spoilers = new ArrayList<Transition>();
			ArrayList<Pair> xorT = Utility.getHistoryXOR(unfolding, t, null);// xorMap.get(t);

			/* Se sono in conflitto le aggiungo alla nuova lista */
			for (Transition t1 : set)
				if (Utility.isConflict(xorT, Utility.getHistoryXOR(unfolding, t1,
						null)/* xorMap.get(t1) */))
					spoilers.add(t1);
			return spoilers;
		}
	}

	private void writeLog(PluginContext context, String log) {
		context.log(log);
		context.getProgress().inc();
	}
}
