package org.processmining.plugins.unfolding;

import java.awt.Color;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.converters.bpmn2pn.CloneBPMN;
import org.processmining.plugins.converters.bpmn2pn.EPetrinetNode;
import org.processmining.plugins.unfolding.visualize.LegendBCSUnfolding;
import org.processmining.plugins.unfolding.visualize.LegendPetrinet;
import org.processmining.plugins.unfolding.visualize.Palette;
import org.processmining.plugins.unfolding.visualize.StringPanel;
import org.processmining.plugins.unfolding.visualize.TabTraceUnfodingPanel;
import org.processmining.plugins.unfolding.visualize.UtilitiesforMapping;
import org.processmining.support.localconfiguration.LocalConfigurationMap;
import org.processmining.support.unfolding.StatisticMap;

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
	private StatisticMap statBPMN;
	private UIPluginContext context; 
	private BPMNDiagram bpmn= null;
	private LocalConfigurationMap local;
	private Petrinet petrinet;
	private Petrinet unfolding = null;
	private JPanel panel;
	private Palette pal = new Palette();
	private Map<EPetrinetNode,BPMNNode> reverseMap;

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
			affiliation = "University of Pisa, ISTI CNR Italy", 
			author = "Daniele Cicciarella, Giorgio O. Spagnolo", 
			email = "cicciarellad@gmail.com, spagnolo@isti.cnr.it"
			)
	@Visualizer
	public JComponent runUI(UIPluginContext context, StatisticMap output) 
	{
		panel = new JPanel();

		this.output = output;
		this.context = context;
		this.statBPMN = output;
		reverseMap = output.getReverseMap();
		try 
		{	
			/* Carico le reti utilizzando la connessione creata in precedenza */
			BCSUnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(BCSUnfoldingConnection.class, context, output);
			petrinet = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.PETRINET);
			unfolding = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.UNFOLDING);
			try{
				bpmn = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.BPMN);
				local = output.getLocalConfigurationMap();
				BPMNDiagram bpmncopia= insertDefect(bpmn,output);
				repaint( true,bpmncopia);
			
			}catch (Exception e) {
				bpmn = null;
				paintwithoutbpmn();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return panel;
	}

	private void paintwithoutbpmn() {
		try{
			double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL,TableLayoutConstants.FILL}};
			panel.setLayout(new TableLayout(size));

			
			/*Costruisco il pannello del PN e il ViewInteraction Panel della legenda*/
			ProMJGraphPanel PNPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
			
			
			panel.add(PNPanel, "0,0");

			
			panel.revalidate();
			panel.repaint();
			

			/*Costruisco il pannello dell'Unfolding*/
			ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
			LegendPetrinet legendPanelP = new LegendPetrinet (unfoldingPanel, "Legend");
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

	public void repaint( boolean flag, BPMNDiagram bpmncopia) {
		try{
			double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL,TableLayoutConstants.FILL}};
			panel.setLayout(new TableLayout(size));

			

			/*Costruisco il pannello del BPMN e il ViewInteraction Panel della legenda*/
			ProMJGraphPanel bpmnPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,bpmncopia);
			LegendBCSUnfolding legendPanelB = new LegendBCSUnfolding(bpmnPanel, "Legend");
			bpmnPanel.addViewInteractionPanel(legendPanelB, SwingConstants.EAST);
			panel.add(bpmnPanel, "0,0");

			StringPanel sp1 = new StringPanel(bpmnPanel, "Statistic BPMN", statBPMN.getBPMNStatistic(bpmn));
			bpmnPanel.addViewInteractionPanel(sp1, SwingConstants.SOUTH);
			panel.revalidate();
			panel.repaint();

			/*costruzione del widget inspector*/
			if(flag){
				TabTraceUnfodingPanel tabunf = new TabTraceUnfodingPanel(context, bpmnPanel, "History Unfolding",  output,  bpmn, local,this);
			}

			/*Costruisco il pannello dell'Unfolding*/
			ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
			LegendPetrinet legendPanelP = new LegendPetrinet (unfoldingPanel, "Legend");
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

	public BPMNDiagram getBpmncopia() {


		return insertDefect(bpmn,output);
	}


	public BPMNDiagram getOriginalBpmn(){
		return bpmn;
	}

	public Petrinet getPetriNet(){
		return petrinet;
	}

	

	

	public BPMNDiagram insertDefect(BPMNDiagram bpmnoriginal, StatisticMap map) {
		//Clono il BPMN diagram

		CloneBPMN bpmncopia = new CloneBPMN(bpmnoriginal.getLabel());
		bpmncopia.cloneFrom(bpmnoriginal);
		//BPMNDiagram bpmncopia = CloneBPMN.cloneFrom(bpmnoriginal);
		

		for( Transition t: map.getCutoff()){
			BPMNNode bpnode = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap,t);
			if (bpnode != null){		
				UtilitiesforMapping.getNodeinClone(bpmncopia,bpnode).getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getCutColor());
			}

		}

		for( Transition t: map.getCutoffUnbounded()){
			BPMNNode bpnode = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap,t);
			if (bpnode != null){
				UtilitiesforMapping.getNodeinClone(bpmncopia,bpnode).getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getCutColor());
			}

		}

		for( Transition t: map.getDeadlock()){
			BPMNNode bpnode = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap,t);
			if (bpnode != null){
				BPMNNode bpnod = UtilitiesforMapping.getNodeinClone(bpmncopia,bpnode);
				Color colo = (Color) bpnod.getAttributeMap().get(AttributeMap.FILLCOLOR);
				if(colo != null) {
					if (colo.equals(pal.getCutColor())){
						bpnod.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getBothCutoffDead());
					}
					else{					
						bpnod.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getDeadColor());
					}}
				else{
					bpnod.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getDeadColor());
				}


			}

		}
		
	 	for (Transition t: map.getDead()){
	 		BPMNNode node = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap,t);
	 		BPMNNode clonato = UtilitiesforMapping.getNodeinClone(bpmncopia, node);
	 		if (clonato!= null){
	           clonato.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getDeadNodeColor());
	 		}
	 	}
		

		
		return bpmncopia;

	}
	
	
	
	public Petrinet getUnfolding() {
		return unfolding;
	}
	
}