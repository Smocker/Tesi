package org.processmining.plugins.unfolding;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.support.unfolding.IdentificationMap;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class VisualizeUnfoldingStatistics_Plugin 
{
	@Plugin(
			name = "Visualize Unfolding Statistics", 
			returnLabels = { "Visualize Unfolding Statistics" }, 
			parameterLabels = { "Identification Map" }, 
			returnTypes = { JComponent.class }, 
			userAccessible = false,
			help = "Visualize Unfolding Statistics"
			)
    @UITopiaVariant
    	(
    		affiliation = "Università di Pisa", 
    		author = "Daniele Cicciarella", 
    		email = "cicciarellad@gmail.com"
    	)
    @Visualizer
	public JComponent runUI(UIPluginContext context, IdentificationMap output) {
		JPanel p = new JPanel();
		BPMNDiagramImpl d;
		Petrinet ini;
		Petrinet unfold;
		try {
		 UnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(UnfoldingConnection.class, context, output);
		 d=unfoldingConnection.getObjectWithRole(UnfoldingConnection.bpmn);
		 ini=unfoldingConnection.getObjectWithRole(UnfoldingConnection.InputPetriNet);
		 unfold=unfoldingConnection.getObjectWithRole(UnfoldingConnection.unfolded);
		 
		 double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL}};
		 p.setLayout(new TableLayout(size));
		// JComponent bpmnpanel = ProMJGraphVisualizer.instance().visualizeGraph(context, d);
		// p.add(bpmnpanel, "0,0");
		 ProMJGraphPanel initpnpanel = ProMJGraphVisualizer.instance().visualizeGraph(context, ini);
		 p.add(initpnpanel, "0,0");
		 ProMJGraphPanel unfoldpanbel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfold);
		 p.add(unfoldpanbel, "0,1");
		// JComponent unfoldpanbel2 = ProMJGraphVisualizer.instance().visualizeGraph(context, d);
		 p.add(new JLabel("ciao"), "0,2");
		 
		} catch (ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return p;
    }
}