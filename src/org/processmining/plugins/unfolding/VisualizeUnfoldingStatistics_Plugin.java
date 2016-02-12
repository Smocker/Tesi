package org.processmining.plugins.unfolding;

import java.awt.Color;
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
import org.processmining.plugins.bpmn.diagram.BpmnDiagram;
import org.processmining.plugins.converters.bpmn2pn.InfoConversionBP2PN;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.LegendPanel;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * Visualizzatore delle reti di petri originale e unfoldata con le statistiche di quest'ultima 
 * 
 * @author cicciarella
 */
public class VisualizeUnfoldingStatistics_Plugin 
{
	@Plugin
	(
			name = "Visualize BCS Unfolding Statistics", 
			returnLabels = { "Visualize BCS Unfolding Statistics" }, 
			parameterLabels = { "Visualize BCS Unfolding Statistics" }, 
			returnTypes = { JComponent.class }, 
			userAccessible = true,
			help = "Visualize BCS Unfolding Statistics"
			)
	@UITopiaVariant
	(
			affiliation = "University of Pisa", 
			author = "Daniele Cicciarella", 
			email = "cicciarellad@gmail.com"
			)
	@Visualizer
	public JComponent runUI(UIPluginContext context, StatisticMap output) 
	{
		JPanel panel = new JPanel();
		Petrinet petrinet, unfolding;
		BPMNDiagram bpmn= null;
		InfoConversionBP2PN info = null;
		try 
		{	
			/* Carico le reti utilizzando la connessione creata in precedenza */
			UnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(UnfoldingConnection.class, context, output);
			petrinet = unfoldingConnection.getObjectWithRole(UnfoldingConnection.PETRINET);
			unfolding = unfoldingConnection.getObjectWithRole(UnfoldingConnection.UNFOLDING);

			bpmn = unfoldingConnection.getObjectWithRole(UnfoldingConnection.BPMN);
			info = unfoldingConnection.getObjectWithRole(UnfoldingConnection.InfoCBP2PN);
			if(bpmn==null || info == null){
				/* Creo i pannelli per la visualizzazione */
				double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL}};
				panel.setLayout(new TableLayout(size));
				ProMJGraphPanel petrinetPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
				panel.add(petrinetPanel, "0,0");
				ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
				LegendPanel legendPanel = new LegendPanel(unfoldingPanel, "Legend");
				unfoldingPanel.addViewInteractionPanel(legendPanel, SwingConstants.EAST);
				panel.add(unfoldingPanel, "0,1");
				JLabel statisticsPanel = new JLabel(output.getStatistic());
				statisticsPanel.setBackground(Color.WHITE);
				JScrollPane scrollStatisticsPanel = new JScrollPane(statisticsPanel);
				panel.add(scrollStatisticsPanel, "0,2");
			}else{
				
				double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL, TableLayoutConstants.FILL,TableLayoutConstants.FILL}};
				panel.setLayout(new TableLayout(size));
				insertDefect(bpmn,output, info);
				ProMJGraphPanel bpmnPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,bpmn);
				panel.add(bpmnPanel, "0,0");
				ProMJGraphPanel petrinetPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
				panel.add(petrinetPanel, "0,1");
				ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
				LegendPanel legendPanel = new LegendPanel(unfoldingPanel, "Legend");
				unfoldingPanel.addViewInteractionPanel(legendPanel, SwingConstants.EAST);
				panel.add(unfoldingPanel, "0,2");
				JLabel statisticsPanel = new JLabel(output.getStatistic());
				statisticsPanel.setBackground(Color.WHITE);
				JScrollPane scrollStatisticsPanel = new JScrollPane(statisticsPanel);
				panel.add(scrollStatisticsPanel, "0,3");
				
				
				
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return panel;
	}
	
	private BPMNNode getNodefromTransition( InfoConversionBP2PN info,  Transition t ){
		Map<BPMNNode, Set<PetrinetNode>> nodemap = info.getNodeMap();
		for(BPMNNode node :nodemap.keySet()){
			Set<PetrinetNode> petrinetnodes = nodemap.get(node);
			for(PetrinetNode petrinetnode: petrinetnodes){
				if(petrinetnode instanceof Transition)
				if(petrinetnode.equals(t)){
					return node;
				}
			}
		}
		return null;
	}
	
	private BPMNNode getNodeinClone(BPMNDiagram bpmn,BPMNNode node){
		
		for(BPMNNode nodeclone: bpmn.getNodes()){
			if(nodeclone.getId().equals(node.getId())){
				return nodeclone;
			}
		}
		
		
		return null;
	}

	private void insertDefect(BPMNDiagram bpmnoriginal, StatisticMap map, InfoConversionBP2PN info) {
		// clona bpmn
		BPMNDiagram bpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmnoriginal);
		
		for( Transition t: map.getCutoff()){
			BPMNNode node = getNodefromTransition(info,t);
			BPMNNode nodeclone =  getNodeinClone(bpmn, node);
			nodeclone.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.YELLOW);
		}
		
		for( Transition t: map.getDeadlock()){
			BPMNNode node = getNodefromTransition(info,t);
			BPMNNode nodeclone =  getNodeinClone(bpmn, node);
			nodeclone.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.RED);
		}
		
		//activity.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.RED);
		//String label = "<html>"+ unsoundallert + "<html>";
		
		//f.getAttributeMap().remove(AttributeMap.TOOLTIP);

		//f.getAttributeMap().put(AttributeMap.TOOLTIP, flowerr);
		//f.getAttributeMap().remove(AttributeMap.SHOWLABEL);
		//f.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
		//f.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.RED);

		
	}
}