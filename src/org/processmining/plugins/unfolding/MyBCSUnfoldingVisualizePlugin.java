package org.processmining.plugins.unfolding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.converters.bpmn2pn.InfoConversionBP2PN;
import org.processmining.plugins.unfolding.visualize.StringPanel;
import org.processmining.plugins.unfolding.visualize.TabTraceUnfodingPanel;
import org.processmining.support.unfolding.LegendBCSUnfolding;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.StatisticMap;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class MyBCSUnfoldingVisualizePlugin {

	private StatisticMap output;

	private UIPluginContext context;
	private InfoConversionBP2PN info = null;
	private BPMNDiagram bpmn= null;
	private Petrinet unfolding = null;
	private JPanel panel;
	private Map<PetrinetNode,BPMNNode> reverseMap;

	@Plugin
	(
			name = "Updated Visualization BCS Unfolding Statistics", 
			returnLabels = { "Visualize BCS Unfolding Statistics" }, 
			parameterLabels = { "Visualize BCS Unfolding Statistics" }, 
			returnTypes = { JComponent.class }, 
			userAccessible = true,
			help = "Visualize BCS Unfolding Statistics"
			)
	@UITopiaVariant
	(
			affiliation = "University of Pisa", 
			author = "Francesco Boscia", 
			email = "francesco.boscia@gmail.com"
			)
	@Visualizer
	public JComponent runUI(UIPluginContext context, StatisticMap output) 
	{
		panel = new JPanel();
		Petrinet petrinet;
		this.output = output;
		this.context = context;
		reverseMap = output.getReverseMap();
		try 
		{	
			/* Carico le reti utilizzando la connessione creata in precedenza */
			BCSUnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(BCSUnfoldingConnection.class, context, output);
			petrinet = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.PETRINET);
			unfolding = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.UNFOLDING);
			try{
				bpmn = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.BPMN);
				info = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.InfoCBP2PN);
			}catch (Exception e) {
				bpmn = null;
				info = null;
			}

		repaint(new ArrayList<PetrinetNode>(), true);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return panel;
	}
	
	public void repaint(Collection<PetrinetNode> collection, boolean flag) {
		try{
			double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL,TableLayoutConstants.FILL}};
			panel.setLayout(new TableLayout(size));

			BPMNDiagram bpmncopia;
			bpmncopia= insertDefect(bpmn,output);
			/*
			if(collection!=null)
				if(collection.isEmpty())
					bpmnw = insertDefect(bpmn,output, info);
			*/
			
			/*Costruisco il pannello del BPMN e il ViewInteraction Panel della legenda*/
			ProMJGraphPanel bpmnPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,bpmncopia);
			LegendBCSUnfolding legendPanelB = new LegendBCSUnfolding(bpmnPanel, "Legend");
			bpmnPanel.addViewInteractionPanel(legendPanelB, SwingConstants.EAST);
			panel.add(bpmnPanel, "0,0");
			
			/*Sostituire la history con la localConfiguration*/
			//HistoryUnfolding hu = new HistoryUnfolding(unfolding);

			/*costruzione del widget inspector*/
			/*
			if(flag){
				TabTraceUnfodingPanel tabunf = new TabTraceUnfodingPanel(context, bpmnPanel, "History Unfolding", hu, output, this, bpmn, info);
			}
			*/
			/*Costruisco il pannello dell'Unfolding*/
			ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
			LegendBCSUnfolding legendPanelP = new LegendBCSUnfolding(unfoldingPanel, "Legend");
			unfoldingPanel.addViewInteractionPanel(legendPanelP, SwingConstants.EAST);
			panel.add(unfoldingPanel, "0,1");
			
			/*Costruisco il ViewInteraction pannello delle statistiche*/
			StringPanel sp = new StringPanel(unfoldingPanel, "Statistic Unfolding", output.getStatistic());
			unfoldingPanel.addViewInteractionPanel(sp, SwingConstants.SOUTH);
			panel.revalidate();
			panel.repaint();
		}catch (Exception e) 
		{
			e.printStackTrace();
		}

	}
/**
 * Ritorna il BPMNNode corrispondente al PetriNode
 * @param PetriNode pn
 * @return BPMNNode
 */
	public BPMNNode getBPMNNodeFromReverseMap(PetrinetNode pn){
		return reverseMap.get(pn);
	}
	
	private BPMNNode getNodeinClone(BPMNDiagram bpmn,BPMNNode node){
		for(BPMNNode nodeclone: bpmn.getNodes()){
			if(nodeclone.getLabel()!=null)
				if(nodeclone.getLabel().equals(node.getLabel())){
					return nodeclone;
			}
		}
		return null;
	}

	
	BPMNNode getNodefromCopia(BPMNDiagram copia, BPMNNode bpmnnode){
	
	return bpmnnode;
	}
	
private BPMNDiagram insertDefect(BPMNDiagram bpmnoriginal, StatisticMap map) {
		//Clono il BPMN diagram
		BPMNDiagram bpmncopia = BPMNDiagramFactory.cloneBPMNDiagram(bpmnoriginal);
		
		for( Transition t: map.getCutoff()){
			BPMNNode bpnode = getBPMNNodeFromReverseMap(t);
			if(bpnode!=null){
				Set<BPMNNode> E = bpmncopia.getNodes();
				for (BPMNNode d:E){
					if (d.equals(bpnode)){
						d.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.YELLOW);
					}
				}
			}
		}
		for( Transition t: map.getDeadlock()){
			BPMNNode bpnode = getBPMNNodeFromReverseMap(t);
			if(bpnode!=null){
				Set<BPMNNode> E = bpmncopia.getNodes();
				for (BPMNNode d:E){
					if (d.equals(bpnode)){
						d.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.YELLOW);
					}
				}
			}
		}

		for( Transition t: map.getCutoffUnbounded()){
			BPMNNode bpnode = getBPMNNodeFromReverseMap(t);
			if(bpnode!=null){
				Set<BPMNNode> E = bpmncopia.getNodes();
				for (BPMNNode d:E){
					if (d.equals(bpnode)){
						d.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.YELLOW);
					}
				}
			}
		}


		return bpmn;
	
}
}
