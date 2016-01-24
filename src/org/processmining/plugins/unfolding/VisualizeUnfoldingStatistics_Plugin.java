package org.processmining.plugins.unfolding;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.support.unfolding.IdentificationMap;

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
			name = "Visualize Unfolding Statistics", 
			returnLabels = { "Visualize Unfolding Statistics" }, 
			parameterLabels = { "Identification Map" }, 
			returnTypes = { JComponent.class }, 
			userAccessible = true,
			help = "Visualize Unfolding Statistics"
		)
    @UITopiaVariant
    	(
    		affiliation = "Universita di Pisa", 
    		author = "Daniele Cicciarella", 
    		email = "cicciarellad@gmail.com"
    	)
    @Visualizer
	public JComponent runUI(UIPluginContext context, IdentificationMap output) 
	{
		JPanel panel = new JPanel();
		Petrinet petrinet, unfolding;

		try 
		{	
			/* Carico le reti utilizzando la connessione creata in precedenza */
			UnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(UnfoldingConnection.class, context, output);
			petrinet = unfoldingConnection.getObjectWithRole(UnfoldingConnection.PETRINET);
		 	unfolding = unfoldingConnection.getObjectWithRole(UnfoldingConnection.UNFOLDING);
			 
		 	/* Creo i pannelli per la visualizzazione */
		 	double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL}};
			panel.setLayout(new TableLayout(size));
			ProMJGraphPanel petrinetPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
			panel.add(petrinetPanel, "0,0");
			ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
			panel.add(unfoldingPanel, "0,1");
			JLabel statisticsPanel = new JLabel(output.loadStatistics());
			statisticsPanel.setBackground(Color.WHITE);
			JScrollPane scrollStatisticsPanel = new JScrollPane(statisticsPanel);
			panel.add(scrollStatisticsPanel, "0,2");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
        return panel;
    }
}