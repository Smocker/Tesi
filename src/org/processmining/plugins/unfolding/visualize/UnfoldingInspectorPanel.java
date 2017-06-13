package org.processmining.plugins.unfolding.visualize;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.Inspector;
import org.processmining.framework.util.ui.widgets.InspectorPanel;

public class UnfoldingInspectorPanel extends InspectorPanel {

	public UnfoldingInspectorPanel(PluginContext context) {
		super(context);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	public void addReset(final String header, final JComponent component) {
		Inspector inspector = this.getInspector();
		JPanel reset = inspector.addTab("Reset");
		inspector.addGroup(reset, header, component);
		
	}
	public void addExport(final String header, final JComponent component) {
		Inspector inspector = this.getInspector();
		JPanel export = inspector.addTab("Export");
		inspector.addGroup(export, header, component);
		
	}

}