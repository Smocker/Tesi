package org.processmining.plugins.unfolding;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.LegendBCSUnfolding;
import org.processmining.support.unfolding.LegendPetrinet;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * Visualizzatore delle reti di petri originale e unfoldata con le statistiche di quest'ultima 
 * 
 * @author cicciarella
 */
public class BCSUnfoldingVisualize_Plugin 
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

		try 
		{	
			/* Carico le reti utilizzando la connessione creata in precedenza */
			BCSUnfoldingConnection unfoldingConnection = context.getConnectionManager().getFirstConnection(BCSUnfoldingConnection.class, context, output);
			petrinet = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.PETRINET);
		 	unfolding = unfoldingConnection.getObjectWithRole(BCSUnfoldingConnection.UNFOLDING);
			 
		 	/* Creo i pannelli per la visualizzazione */
		 	double size [] [] = {{TableLayoutConstants.FILL} , {TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL}};
			panel.setLayout(new TableLayout(size));
			ProMJGraphPanel petrinetPanel = ProMJGraphVisualizer.instance().visualizeGraph(context,petrinet);
			LegendPetrinet legendPetrinet = new LegendPetrinet(petrinetPanel, "Legend");
			petrinetPanel.addViewInteractionPanel(legendPetrinet, SwingConstants.EAST);
			panel.add(petrinetPanel, "0,0");
			ProMJGraphPanel unfoldingPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, unfolding);
			LegendBCSUnfolding legendPanel = new LegendBCSUnfolding(unfoldingPanel, "Legend");
			unfoldingPanel.addViewInteractionPanel(legendPanel, SwingConstants.EAST);
			panel.add(unfoldingPanel, "0,1");
			JLabel statisticsPanel = new JLabel(output.getStatistic());
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