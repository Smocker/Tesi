package org.processmining.support.unfolding;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Costruisce la legenda della rete di unfolding
 * 
 * @author Daniele Cicciarella
 */
public class LegendBCSUnfolding extends JPanel implements MouseListener, MouseMotionListener, ViewInteractionPanel 
{
	/* serialVersionUID */
    private static final long serialVersionUID = 5563202352636336868L;

    protected SlickerFactory factory = SlickerFactory.instance();
    protected SlickerDecorator decorator = SlickerDecorator.instance();
    private JComponent component;
    private String panelName;

    /**
     * Costruttore 
     * 
     * @param panel pannello sulla quale deve essere inserito il pannello
     * @param panelName nome del pannello 
     */
    public LegendBCSUnfolding(ScalableViewPanel panel, String panelName) 
    {
    	/* Si setta il layout della legenda */
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setOpaque(true);
        this.setSize(new Dimension(90, 250));
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.panelName = panelName;
        panel.getViewport();
        paintLegend();
    }

    /**
     * Disegna la legenda
     */
    public synchronized void paintLegend() 
    {
    	/* Altri stili */
        this.setBackground(new Color(30, 30, 30));
        JPanel legendPanel = new JPanel();
        legendPanel.setBorder(BorderFactory.createEmptyBorder());
        legendPanel.setBackground(new Color(30, 30, 30));
        TableLayout layout = new TableLayout(new double[][] { { 0.10, TableLayout.FILL }, {} });
        legendPanel.setLayout(layout);
        
        /* Riga legenda */
        layout.insertRow(0, 0.2);
        int row = 1;
        layout.insertRow(row, TableLayout.PREFERRED);
        JLabel legend = factory.createLabel("LEGEND");
        legend.setForeground(Color.WHITE);
        legendPanel.add(legend, "0,1,1,1,c, c");
        row++;

        /* Riga punti di cutoff */
        layout.insertRow(row, 0.2);
        layout.insertRow(row, TableLayout.PREFERRED);
        JPanel greenPanel = new JPanel();
        greenPanel.setBackground(Color.BLUE);
        legendPanel.add(greenPanel, "0," + row + ",r, c");
        JLabel syncLbl = factory.createLabel(" Points of Cutoff");
        syncLbl.setForeground(Color.WHITE);
        legendPanel.add(syncLbl, "1," + row++ + ",l, c");

        /* Riga punti di deadlock */
        layout.insertRow(row, TableLayout.PREFERRED);
        JPanel yellowPanel = new JPanel();
        yellowPanel.setBackground(Color.RED);
        legendPanel.add(yellowPanel, "0," + row + ",r, c");
        JLabel moveLogLbl = factory.createLabel(" Points of Deadlock");
        moveLogLbl.setForeground(Color.WHITE);
        legendPanel.add(moveLogLbl, "1," + row++ + ",l, c");

        /* Riga vuota */
        layout.insertRow(row, 0.2);
        
        /* Si settano altri stili */
        legendPanel.setOpaque(false);
        this.add(legendPanel, BorderLayout.WEST);
        this.setOpaque(false);
        this.setOpaque(false);
    }

    public double getVisWidth() 
    {
        return component.getSize().getWidth();
    }

    public double getVisHeight() 
    {
        return component.getSize().getHeight();
    }

    @Override
    public void paint(Graphics g) 
    {
    	super.paint(g);
    }

    public synchronized void mouseDragged(MouseEvent evt) 
    {}

    public void mouseClicked(MouseEvent e) 
    {}

    public void mouseEntered(MouseEvent e) 
    {}

    public void mouseExited(MouseEvent e) 
    {}

    public void mouseMoved(MouseEvent e) 
    {}

    public synchronized void mousePressed(MouseEvent e) 
    {}

    public synchronized void mouseReleased(MouseEvent e) 
    {}

    public void setScalableComponent(ScalableComponent scalable) 
    {
            this.component = scalable.getComponent();
    }

    public void setParent(ScalableViewPanel parent) 
    {}

    public JComponent getComponent() 
    {
            return this;
    }

    public int getPosition() 
    {
        return SwingConstants.NORTH;
    }

    public String getPanelName() 
    {
        return panelName;
    }

    public void setPanelName(String name) 
    {
        this.panelName = name;
    }

    public void updated() 
    {}

    public double getHeightInView() 
    {
        return 90;
    }

    public double getWidthInView() 
    {
        return 250;
    }

    public void willChangeVisibility(boolean to) 
    {}

    public void setSize(int width, int height) 
    {
        super.setSize(width, height);
    }
}