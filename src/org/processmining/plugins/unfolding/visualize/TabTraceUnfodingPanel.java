package org.processmining.plugins.unfolding.visualize;




import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.AbstractDocument.Content;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;
import org.processmining.framework.util.ui.widgets.Inspector;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextArea;
//import org.processmining.plugins.log.ui.logdialog.ProcessInstanceView;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.converters.bpmn2pn.InfoConversionBP2PN;
import org.processmining.plugins.unfolding.HistoryUnfolding;
import org.processmining.plugins.unfolding.VisualizeUnfoldingStatistics_Plugin;
import org.processmining.support.unfolding.StatisticMap;

import com.fluxicon.slickerbox.components.AutoFocusButton;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class TabTraceUnfodingPanel extends JPanel implements MouseListener, MouseMotionListener, ViewInteractionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */



	protected SlickerFactory factory = SlickerFactory.instance();
	protected SlickerDecorator decorator = SlickerDecorator.instance();
	private JComponent component;
	private JTable tab;
	private String panelName;
	//private TotalConformanceResult tovisualize;
	//private HistoryUnfolding historyunf;
	private ArrayList<Collection<PetrinetNode>> historyPN;
	private ArrayList<Collection<BPMNNode>> historyBPMN;
	private  UnfInspectorPanel inspector;
	private StatisticMap statistiunf;
	private VisualizeUnfoldingStatistics_Plugin visualizeUnfoldingStatistics_Plugin;

	public TabTraceUnfodingPanel(PluginContext context, ScalableViewPanel panel, String panelName,
			HistoryUnfolding hu, StatisticMap statistiunf, VisualizeUnfoldingStatistics_Plugin visualizeUnfoldingStatistics_Plugin, BPMNDiagram bpmn, InfoConversionBP2PN info){
		super(new BorderLayout());
		this.statistiunf = statistiunf;
		this.visualizeUnfoldingStatistics_Plugin = visualizeUnfoldingStatistics_Plugin;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setOpaque(true);
		this.setSize(new Dimension(160, 260));
		//this.historyunf=hu;
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
	//	historyPN = hu.createHistoryDFS();
		//historyPN =hu.createHistoryBFS();
		historyPN = hu.createHistory();
	//	historyBPMN = hu.HistoryonBP(info, bpmn);
		panel.getViewport();
		this.panelName = panelName;
		/*this.tovisualize=tpr;

		replayRuposPanel=replayPRP;*/
		inspector = new UnfInspectorPanel(context);
		panel.add(inspector);
		painttabtrace();
		widget(this);

	}

	public JPanel widget(JPanel panell){

		// XLogInfo info = null;
		// info = XLogInfoFactory.createLogInfo(log);
		// ProcessInstanceView instanceView = new ProcessInstanceView(log.get(index), info);
		// instanceView.setAlignmentX(LEFT_ALIGNMENT);
		JPanel comprisePanel = new JPanel();
	
		comprisePanel.setAlignmentX(LEFT_ALIGNMENT);
		comprisePanel.setBorder(BorderFactory.createEmptyBorder());
		comprisePanel.setOpaque(false);
		comprisePanel.setLayout(new BoxLayout(comprisePanel, BoxLayout.X_AXIS));
		//	inspector.setSize(new Dimension(160, 260));
		comprisePanel.add(tab);
		comprisePanel.add(Box.createHorizontalGlue());
	
		//	inspector = new InspectorPanel((Frame) null);
		//	inspector.removeInfoAll(0);
		inspector.addInfo("History Unfolding", comprisePanel);
		

		JButton	button2  = new AutoFocusButton("Reset");


		button2.setOpaque(false);


		button2.setFont(new Font("Monospaced", Font.PLAIN, 12));



		button2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//replayRuposPanel.fullrepaint();
				visualizeUnfoldingStatistics_Plugin.repaint(new ArrayList<PetrinetNode>());
			}

		});

		inspector.addReset("Reset", button2);

		return comprisePanel;

		//, "0, 0");

	}

	private void painttabtrace() {

		this.setBackground(new Color(30, 30, 30));

		JPanel legendPanel = new JPanel();
		legendPanel.setBorder(BorderFactory.createEmptyBorder());
		legendPanel.setBackground(new Color(30, 30, 30));


		JPanel jp1 = new JPanel();
		tab = new JTable(new AbstractTableModel() {

			private static final long serialVersionUID = -2176731961693608635L;
			

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return historyPN.get(rowIndex);// tovisualize.getList().get(rowIndex).getTracename()+"+"+tovisualize.getList().get(rowIndex).getConformance();//rowIndex;
			}

			@Override
			public int getRowCount() {

				return historyPN.size();//tovisualize.getList().size();
			}

			@Override
			public int getColumnCount() {

				return 1;
			}

			public String getColumnName(int col) { 

				return "List of history"; 
			}

			public boolean isCellEditable(int row, int col) 
			{ 

				return false; 
			}
		});
		
		tab.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {

			public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
			{

				Component cell = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
				//this.scrollRectToVisible(getBounds());
				 setToolTipText(table.getValueAt(row, column).toString());
				ArrayList<Transition> cuttoff = statistiunf.getCutoffUnbounded();
				ArrayList<Transition> deadlock = statistiunf.getDeadlock();
				boolean flag = search(historyPN.get(row), cuttoff);

				boolean flag2 = search(historyPN.get(row), deadlock);
				if(flag2){
					if(flag){
						cell.setBackground( Color.orange );
					}else
						cell.setBackground( Color.red );
				}else{
					if(flag){
						cell.setBackground( Color.blue );
					}else
						cell.setBackground( Color.gray );
				}

				/*if (tovisualize.getList().get(row).getMissingMarking().isEmpty() &&
							tovisualize.getList().get(row).getMapTransition().isEmpty() &&
							tovisualize.getList().get(row).getConformance()>0.92){
						cell.setBackground( Color.gray );
					}else{

						cell.setBackground( Color.red );
					}*/

				return cell;

			}

			private boolean search(Collection<PetrinetNode> collection,
					ArrayList<Transition> arraylist) {
				for (Transition transition : arraylist) {
					if(collection.contains(transition)){
						return true;
					}
				}

				return false;
			}});




		tab.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				JTable target = (JTable)e.getSource();
				int row = target.getSelectedRow();
				int column = target.getSelectedColumn();
				visualizeUnfoldingStatistics_Plugin.repaint(historyPN.get(row));

			}
		});
		legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
		jp1.setLayout(new BoxLayout(jp1, BoxLayout.X_AXIS));

		JScrollPane scrollpane = new JScrollPane(tab); 
		scrollpane.setOpaque(false);
		scrollpane.getViewport().setOpaque(false);
		scrollpane.setBorder(BorderFactory.createEmptyBorder());
		scrollpane.setViewportBorder(BorderFactory.createLineBorder(new Color(10, 10, 10), 2));
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(scrollpane.getVerticalScrollBar(), new Color(0, 0, 0, 0),
				new Color (140, 140, 140), new Color(80, 80, 80));
		scrollpane.getVerticalScrollBar().setOpaque(false);
		SlickerDecorator.instance().decorate(scrollpane.getHorizontalScrollBar(), new Color(0, 0, 0, 0),
				new Color (140, 140, 140), new Color(80, 80, 80));
		scrollpane.getHorizontalScrollBar().setOpaque(false);



		JButton button  = new AutoFocusButton("Update");
		JButton	button2  = new AutoFocusButton("UpdateAll");

		button.setOpaque(false);
		button2.setOpaque(false);

		button.setFont(new Font("Monospaced", Font.PLAIN, 12));
		button2.setFont(new Font("Monospaced", Font.PLAIN, 12));

		jp1.add(button,BorderLayout.NORTH);
		jp1.add(button2,BorderLayout.WEST);


		button2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//replayRuposPanel.fullrepaint();
				visualizeUnfoldingStatistics_Plugin.repaint(null);
			}

		});




		legendPanel.add(jp1);
		legendPanel.add(scrollpane,BorderLayout.SOUTH);



		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {


				int i=tab.getSelectedRow();
				if(i>=0){
					//replayRuposPanel.onerepaint(i);
					//widget(i);
					visualizeUnfoldingStatistics_Plugin.repaint(historyPN.get(i));
					//	replayPerformanceRuposPanel.fullrepaint(tovisualize.getListperformance().get(i));

				}

			}
		});


		legendPanel.setOpaque(false);
		this.add(legendPanel, BorderLayout.WEST);
		this.setOpaque(false);


	}


	public double getVisWidth() {
		return component.getSize().getWidth();
	}

	public double getVisHeight() {
		return component.getSize().getHeight();
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
	}

	public synchronized void mouseDragged(MouseEvent evt) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public synchronized void mousePressed(MouseEvent e) {

	}

	public synchronized void mouseReleased(MouseEvent e) {

	}

	public void setScalableComponent(ScalableComponent scalable) {
		this.component = scalable.getComponent();
	}

	public void setParent(ScalableViewPanel parent) {

	}

	public JComponent getComponent() {
		return this;
	}

	public int getPosition() {
		return SwingConstants.SOUTH;
	}

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String name) {
		this.panelName = name;
	}

	public void updated() {

	}

	public double getHeightInView() {
		return 160;
	}

	public double getWidthInView() {
		return tab.getWidth()+8;
	}

	public void willChangeVisibility(boolean to) {

	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
	}
}
