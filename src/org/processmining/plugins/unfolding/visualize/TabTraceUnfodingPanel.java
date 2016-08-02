package org.processmining.plugins.unfolding.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.deckfour.uitopia.ui.UITopiaController;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.converters.bpmn2pn.CloneBPMN;
import org.processmining.plugins.converters.bpmn2pn.EPetrinetNode;
import org.processmining.plugins.unfolding.BCSUnfoldingVisualize_Plugin;
import org.processmining.plugins.utils.ProvidedObjectHelper;
import org.processmining.support.localconfiguration.LocalConfiguration;
import org.processmining.support.localconfiguration.LocalConfigurationMap;
import org.processmining.support.unfolding.StatisticMap;
import org.processmining.support.unfolding.Utility;

import com.fluxicon.slickerbox.components.AutoFocusButton;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

class WrapInt{
	Integer value = 0;

	String print(){
		return value.toString();
	}
}

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
	private LocalConfigurationMap local;
	private UnfoldingInspectorPanel inspector;
	private StatisticMap statistiunf;
	private BCSUnfoldingVisualize_Plugin bcsUnfoldingVisualize_Plugin;
	private Map<LocalConfiguration,Color> mapLocalColor;
	private Map<EPetrinetNode,BPMNNode> reverseMap;
	private ArrayList<LocalConfiguration> list;
	private String elencoBPMN = "";
	private Palette pal = new Palette();
	private UIPluginContext context;
	private BPMNDiagram bpmnoriginal;
	private BPMNDiagram bpmnvisualizzed;

	public TabTraceUnfodingPanel(UIPluginContext context, ScalableViewPanel panel, String panelName,
			StatisticMap statistiunf, BPMNDiagram bpmnoriginal, LocalConfigurationMap local, BCSUnfoldingVisualize_Plugin bcsUnfoldingVisualize_Plugin ){
		super(new BorderLayout());
		mapLocalColor = new HashMap<LocalConfiguration,Color>();
		list = new ArrayList<LocalConfiguration>();
		this.statistiunf = statistiunf;
		this.bpmnoriginal=bpmnoriginal;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setOpaque(true);
		this.setSize(new Dimension(160, 260));
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.local = local; 
		this.reverseMap = statistiunf.getReverseMap();
		this.context = context;
		this.bcsUnfoldingVisualize_Plugin=bcsUnfoldingVisualize_Plugin;
		panel.getViewport();
		init();
		this.panelName = panelName;
		inspector = new UnfoldingInspectorPanel(context);
		panel.add(inspector);
		painttabtrace();
		widget(this);
	}

	public JPanel widget(JPanel panell){

		JPanel comprisePanel = new JPanel();

		comprisePanel.setAlignmentX(LEFT_ALIGNMENT);
		comprisePanel.setBorder(BorderFactory.createEmptyBorder());
		comprisePanel.setOpaque(false);
		comprisePanel.setLayout(new BoxLayout(comprisePanel, BoxLayout.X_AXIS));
		comprisePanel.add(tab);
		comprisePanel.add(Box.createHorizontalGlue());

		inspector.addInfo("Unfolding History", comprisePanel);   

		JButton	button2  = new AutoFocusButton("Reset");
		button2.setOpaque(false);
		button2.setFont(new Font("Monospaced", Font.PLAIN, 12));
		button2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BPMNDiagram bpmncopia= bcsUnfoldingVisualize_Plugin.getBpmncopia();
				bcsUnfoldingVisualize_Plugin.repaint(false, bpmncopia);
			}
		}
				);

		inspector.addReset("Reset", button2);

		JButton button3 = new AutoFocusButton("Export");
		button3.setOpaque(false);
		button3.setFont(new Font("Monospaced", Font.PLAIN, 12));
		button3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {				
				export();



			}
		}
				);

		inspector.addExport("Export", button3);

		return comprisePanel;
	}

	private void export() {

		String name = bpmnvisualizzed.getLabel();
		ProvidedObjectHelper.publish(context, name, bpmnvisualizzed, BPMNDiagram.class, false);

		final GlobalContext gconetxt = context.getGlobalContext();
		if(gconetxt instanceof UIContext){
			UITopiaController controller =((UIContext)gconetxt).getController() ;
			UIContext uicontext = ((UIContext)gconetxt);

			final ProMResource<?> res = uicontext.getResourceManager().getResourceForInstance(bpmnvisualizzed);

			if(!controller.getFrameworkHub().getResourceManager()
					.getExportFilters(res).isEmpty()){
				try {
					controller.getFrameworkHub().getResourceManager().exportResource(res);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void init(){
		ArrayList<Transition> cutoff = statistiunf.getCutoff();
		ArrayList<Transition> cutoffU = statistiunf.getCutoffUnbounded();		
		ArrayList<Transition> dead = statistiunf.getDead();
		ArrayList<Transition> deadL = statistiunf.getDeadlock();

		fillmap(cutoff,pal.getCutColor());
		fillmap(cutoffU,pal.getCutColor());
		fillmap(dead,pal.getDeadColor());
		fillmap(deadL,pal.getDeadColor());		
	}

	public BPMNDiagram paintConf(LocalConfiguration localConfiguration,Map<?,BPMNNode> reverseMap){
		
		CloneBPMN bpmncopia = new CloneBPMN(bpmnoriginal.getLabel());
		bpmncopia.cloneFrom(bpmnoriginal);
		colorPath(localConfiguration, bpmncopia);
		return bpmncopia;	
	}




	private void colorPath(LocalConfiguration localConfiguration, BPMNDiagram diagram) {
		Petrinet unfolding = bcsUnfoldingVisualize_Plugin.getUnfolding();
		ArrayList<Place> listplace = Utility.getHistoryPlace(unfolding,localConfiguration.get().get(0));

		Place start = Utility.getStartNode(unfolding);

		ArrayList<Transition> listt = localConfiguration.get();
		Transition last = listt.get(0);
		List<PetrinetNode> ll  = new ArrayList<PetrinetNode>();

		ll.addAll(listplace);
		ll.addAll(listt);


		WrapInt i = new WrapInt();


		List<BPMNEdge<BPMNNode, BPMNNode>> nodesVisitati  = new ArrayList<BPMNEdge<BPMNNode, BPMNNode>>();
		List<PetrinetNode> nodidiSincronizazione  = new ArrayList<PetrinetNode>();

		path(ll,start,i,diagram,nodesVisitati,nodidiSincronizazione);
		for (int j = 0; j < nodidiSincronizazione.size(); j++) {
			path(ll,nodidiSincronizazione.get(j),i,diagram,nodesVisitati,nodidiSincronizazione);
		}


		Color colorlocal = mapLocalColor.get(localConfiguration);
		if(colorlocal.equals(pal.getDeadColor())){
			colorDeadArc(last,i,diagram);
		}

	}

	private void colorDeadArc(Transition t, WrapInt i,BPMNDiagram diagram){
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = t.getGraph().getOutEdges(t);
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :edges ){
			//Prendo il PetrinetNode bersaglio dell'arco
			PetrinetNode target = edge.getTarget();
			//List<BPMNEdge<BPMNNode, BPMNNode>> noded = statistiunf.getflowMapPNtoBP().get(new EPetrinetNode(target));         
			BPMNEdge<BPMNNode, BPMNNode> arcoBPMN = statistiunf.getflowMapPNtoBP().get(new EPetrinetNode(target));
			if(arcoBPMN!=null){
			//	for (BPMNEdge<BPMNNode, BPMNNode> arcoBPMN :noded){

					BPMNEdge<BPMNNode, BPMNNode> to = UtilitiesforMapping.getArcInClone(diagram, arcoBPMN);

					i.value++;
					to.getAttributeMap().put(AttributeMap.LABEL, i.print());
					to.getAttributeMap().put(AttributeMap.LINEWIDTH, 3.0f);
					to.getAttributeMap().put(AttributeMap.EDGECOLOR, pal.getArcDead());	
			//	}
			}
		}
	}

	private void path(List<PetrinetNode> ll, PetrinetNode start, WrapInt i, BPMNDiagram diagram, List<BPMNEdge<BPMNNode, BPMNNode>> nodesVisitati, List<PetrinetNode> nodidiSincronizazione) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = start.getGraph().getOutEdges(start);
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges){
			PetrinetNode target = edge.getTarget();
			if(ll.contains(target)){
				rec(diagram,target,i,nodesVisitati);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inedges = target.getGraph().getInEdges(target);
				if(inedges.size()<2)
					path(ll,target,i, diagram,nodesVisitati,nodidiSincronizazione);
				else
					nodidiSincronizazione.add(target);
			}
		}

	}

	private void rec (BPMNDiagram diagram, PetrinetNode nod,WrapInt num, List<BPMNEdge<BPMNNode, BPMNNode>> nodesVisitati){
		BPMNNode node = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap,nod);
		BPMNNode clonato = UtilitiesforMapping.getNodeinClone(diagram, node);
		if (clonato != null){
			clonato.getAttributeMap().put(AttributeMap.FILLCOLOR, pal.getLocalConfigurationColor());
		}


		//List<BPMNEdge<BPMNNode, BPMNNode>> noded = statistiunf.getflowMapPNtoBP().get(new EPetrinetNode(nod));
		BPMNEdge<BPMNNode, BPMNNode> noded = statistiunf.getflowMapPNtoBP().get(new EPetrinetNode(nod));

		if(noded!=null){
			//for (  BPMNEdge<BPMNNode, BPMNNode> n :noded){
				if(!nodesVisitati.contains(noded)){
					BPMNEdge<BPMNNode, BPMNNode> to = UtilitiesforMapping.getArcInClone(diagram, noded);

					to.getAttributeMap().put(AttributeMap.EDGECOLOR, pal.getLocalConfigurationColor());
					to.getAttributeMap().put(AttributeMap.LINEWIDTH, 3.0f);
					to.getAttributeMap().put(AttributeMap.LABELCOLOR, pal.getArcLabelColor());
					num.value++;
					to.getAttributeMap().put(AttributeMap.LABEL, num.value.toString());
					nodesVisitati.add(noded);

				}
			//}
		}
	}



	private void fillmap(ArrayList<Transition> elements, Color color) {
		if (!elements.isEmpty()){					
			for (Transition t : elements) {
				if (!(t.getLabel().equals("reset") || t.getLabel().equals("to") || t.getLabel().equals("ti"))){
					LocalConfiguration l =  local.get(t);
					if(l!=null){
						list.add(l);
						mapLocalColor.put(l, color);
					}
				}												
			}
		}

	}

	private void painttabtrace() {

		this.setBackground(new Color(30, 30, 30));

		JPanel legendPanel = new JPanel();
		legendPanel.setBorder(BorderFactory.createEmptyBorder());
		legendPanel.setBackground(new Color(30, 30, 30));


		tab = new JTable(new AbstractTableModel() {

			private static final long serialVersionUID = -2176731961693608635L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				BPMNNode node = null;
				LocalConfiguration runningConf = null; 
				if(list!=null){
					runningConf = list.get(rowIndex);
					ArrayList<Transition> lista = runningConf.get();
					for (Transition t: lista){
						node = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t);							
						elencoBPMN = listing(reverseMap,lista);
						if (node != null){
							return node.getLabel() +" --> " + elencoBPMN; }
					}
				}		
				return 0;	
			}


			@Override
			public int getRowCount() {
				return list.size();
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

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
			{

				Component cell = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);

				LocalConfiguration key = list.get(row);
				Color c = mapLocalColor.get(key);
				setToolTipText(elencoBPMN.toString());
				cell.setBackground(c );
				return cell;

			}
		}
				);

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
				JTable target = (JTable)e.getSource();
				int row = target.getSelectedRow();
				LocalConfiguration localConf = list.get(row);
				switch(e.getClickCount()){
				case 2:	if (e.getClickCount() == 2){
					copyStringToClipboard("BPMNlist: " + elencoBPMN + "; Local Configuration: " + localConf.toString()); //copia la localConf
					break;}
				case 1: {
					if (e.getClickCount()==1){
						 bpmnvisualizzed = paintConf(localConf,reverseMap);
						bcsUnfoldingVisualize_Plugin.repaint(false,bpmnvisualizzed);}
				}
				}	

			}
		});
		legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));

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
		legendPanel.add(scrollpane,BorderLayout.SOUTH);
		legendPanel.setOpaque(false);
		this.add(legendPanel, BorderLayout.WEST);
		this.setOpaque(false);
	}


	public String listing(Map<EPetrinetNode,BPMNNode> reverseMap, ArrayList<Transition> alt){
		elencoBPMN = "";
		BPMNNode node = null;
		ArrayList<BPMNNode> alb = new ArrayList<BPMNNode>();
		for (Transition t: alt){
			//cerco i BPMNNode
			node = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t);
			if (node != null){
				alb.add(node);
			}
		}
		//inverto la stringa 
		for (BPMNNode bp: alb){
			elencoBPMN = bp.toString() + ", " + elencoBPMN;
		}
		//rimuovo l'ultima virgola
		return elencoBPMN.substring(0, (elencoBPMN.length())-2); 
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
		JTable target = (JTable)e.getSource();
		int row = target.getSelectedRow();
		int column = target.getSelectedColumn();
		BPMNDiagram bpmncopia = BPMNDiagramFactory.cloneBPMNDiagram(bpmnoriginal);
		bcsUnfoldingVisualize_Plugin.repaint(false,bpmncopia);

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
	public static void copyStringToClipboard (String str)
	{
		Clipboard clipBoard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
		clipBoard.setContents (new StringSelection (str), null);
	}

	public Object fillWidget(int index){
		BPMNNode node = null;
		LocalConfiguration running = null; 
		if(list!=null){
			running = list.get(index);
			ArrayList<Transition> lista  = running.get();
			for (Transition t: lista){
				node = UtilitiesforMapping.getBPMNNodeFromReverseMap(reverseMap, t);							
				elencoBPMN = listing(reverseMap,lista);
				if (node != null){
					return node.getLabel() +" --> " + elencoBPMN; }
			}
		}
		return 0;				
	}


}

