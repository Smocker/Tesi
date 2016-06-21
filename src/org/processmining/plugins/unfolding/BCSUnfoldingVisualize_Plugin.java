package org.processmining.plugins.unfolding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.UIPluginContext;
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

import com.fluxicon.slickerbox.factory.SlickerDecorator;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * Visualizzatore delle reti di petri originale e unfoldata con le statistiche di quest'ultima 
 * 
 * @author cicciarella
 */
public class BCSUnfoldingVisualize_Plugin 
{
	private StatisticMap output;
	private UIPluginContext context;
	private InfoConversionBP2PN info = null;
	private BPMNDiagram bpmn= null;
	private Petrinet unfolding = null;
	private JPanel panel;
	/*@Plugin
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
	@Visualizer*/
	public JComponent runUI(UIPluginContext context, StatisticMap output) 
	{
		panel = new JPanel();
		Petrinet petrinet;


		this.output = output;
		this.context = context;
		try 
		{	
			/* Carico le reti utilizzando la connessione creata in precedenza */
			BCSUnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(BCSUnfoldingConnection.class, context, output);
			petrinet = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.PETRINET);
			unfolding = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.UNFOLDING);
			try{
				bpmn = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.BPMN);
				info = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.InfoCBP2PN);
			}catch (Exception e) 
			{
				bpmn = null;
				info = null;
			}
			if(bpmn==null || info == null){
				/* Creo i pannelli per la visualizzazione */
				double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL}};
				panel.setLayout(new TableLayout(size));
				ProMJGraphPanel petrinetPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
				panel.add(petrinetPanel, "0,0");
				ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
				LegendBCSUnfolding legendPanel = new LegendBCSUnfolding(unfoldingPanel, "Legend");
				unfoldingPanel.addViewInteractionPanel(legendPanel, SwingConstants.EAST);
				panel.add(unfoldingPanel, "0,1");
				JLabel statisticsPanel = new JLabel(output.getStatistic());
				statisticsPanel.setBackground(Color.WHITE);
				JScrollPane scrollStatisticsPanel = new JScrollPane(statisticsPanel);
				panel.add(scrollStatisticsPanel, "0,2");
			}else{

				repaint(new ArrayList<PetrinetNode>(), true);

			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return panel;
	}



	public static JComponent visualizestring( String tovisualize) {
		JScrollPane sp = new JScrollPane();
		sp.setOpaque(false);
		sp.getViewport().setOpaque(false);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setViewportBorder(BorderFactory.createLineBorder(new Color(10, 10, 10), 2));
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(sp.getVerticalScrollBar(), new Color(0, 0, 0, 0),
				new Color (140, 140, 140), new Color(80, 80, 80));
		sp.getVerticalScrollBar().setOpaque(false);


		JLabel l = new JLabel(tovisualize);
		sp.setViewportView(l);

		return sp;
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

	private BPMNNode getNodeinClone(BPMNDiagram bpmn,BPMNNode node){

		for(BPMNNode nodeclone: bpmn.getNodes()){
			if(nodeclone.getLabel()!=null)
			if(nodeclone.getLabel().equals(node.getLabel())){
				return nodeclone;
			}
		}


		return null;
	}

	private BPMNDiagram insertDefect(BPMNDiagram bpmnoriginal, StatisticMap map, InfoConversionBP2PN info) {
		// clona bpmn
		BPMNDiagram bpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmnoriginal);

		for( Transition t: map.getCutoff()){
			BPMNNode node = getNodefromTransition(info,t);
			if(node!=null){
				BPMNNode nodeclone =  getNodeinClone(bpmn, node);
				if(nodeclone!=null)
				nodeclone.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.YELLOW);
			}
		}

		for( Transition t: map.getDeadlock()){
			BPMNNode node = getNodefromTransition(info,t);
			if(node!=null){
				BPMNNode nodeclone =  getNodeinClone(bpmn, node);
				if(nodeclone!=null)
				nodeclone.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.RED);


			}

		}

		for( Transition t: map.getCutoffUnbounded()){
			BPMNNode node = getNodefromTransition(info,t);
			if(node!=null){
				BPMNNode nodeclone =  getNodeinClone(bpmn, node);
				nodeclone.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.ORANGE);


			}

		}

		return bpmn;
	}

	public void repaint(Collection<PetrinetNode> collection, boolean flag) {
		//JPanel panel = new JPanel();


		try{
			double size [] [] = {{TableLayoutConstants.FILL} , {/*TableLayoutConstants.FILL, TableLayoutConstants.FILL,*/ TableLayoutConstants.FILL,TableLayoutConstants.FILL}};
			panel.setLayout(new TableLayout(size));

			BPMNDiagram bpmnw;

			bpmnw= insertDefect(bpmn,collection);
			if(collection!=null)
				if(collection.isEmpty())
					bpmnw = insertDefect(bpmn,output, info);
			ProMJGraphPanel bpmnPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,bpmnw);
			LegendBCSUnfolding legendPanelB = new LegendBCSUnfolding(bpmnPanel, "Legend");
			bpmnPanel.addViewInteractionPanel(legendPanelB, SwingConstants.EAST);
			panel.add(bpmnPanel, "0,0");

			HistoryUnfolding hu = new HistoryUnfolding(unfolding);


			if(flag){
				TabTraceUnfodingPanel tabunf = new TabTraceUnfodingPanel(context, bpmnPanel, "History Unfolding", hu, output, this, bpmn, info);
			}
			//TabTraceUnfodingPanel tabunf = new TabTraceUnfodingPanel(context, bpmnPanel, "History Unfolding", hu, output, this);
			//bpmnPanel.addViewInteractionPanel(tabunf, SwingConstants.SOUTH);
			/*ProMJGraphPanel petrinetPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
		panel.add(petrinetPanel, "0,1");*/
			ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
			LegendBCSUnfolding legendPanelP = new LegendBCSUnfolding(unfoldingPanel, "Legend");
			unfoldingPanel.addViewInteractionPanel(legendPanelP, SwingConstants.EAST);
			panel.add(unfoldingPanel, "0,1");

			//panel.add(visualizestring(output.getStatistic()), "0,2");
			StringPanel sp = new StringPanel(unfoldingPanel, "Statistic Unfolding", output.getStatistic());
			unfoldingPanel.addViewInteractionPanel(sp, SwingConstants.SOUTH);
			panel.revalidate();
			panel.repaint();
		}catch (Exception e) 
		{
			e.printStackTrace();
		}

	}

	private BPMNDiagram insertDefect(BPMNDiagram bpmnoriginal,
			Collection<PetrinetNode> collection) {
		if(collection!=null){
			BPMNDiagram bpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmnoriginal);

			for( PetrinetNode pnnode: collection){
				if(pnnode instanceof Transition){
					Transition t = (Transition) pnnode;
					BPMNNode node = getNodefromTransition(info,t);
					if(node!=null){
						BPMNNode nodeclone =  getNodeinClone(bpmn, node);
						nodeclone.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.ORANGE);
					}
				}
			}

			return bpmn;
		}else
			return bpmnoriginal;
	}
}