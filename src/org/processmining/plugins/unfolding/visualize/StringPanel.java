package org.processmining.plugins.unfolding.visualize;

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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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
public class StringPanel extends JPanel implements MouseListener, MouseMotionListener, ViewInteractionPanel 
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
    public StringPanel(ScalableViewPanel panel, String panelName, String text) 
    {
    	/* Si setta il layout della legenda */
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setOpaque(true);
        this.setSize(new Dimension(220, 640));
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.panelName = panelName;
        panel.getViewport();
       
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
		
		
		JLabel l = new JLabel(text);
		sp.setViewportView(l);
		sp.setSize(new Dimension(220, 640));
		this.add(sp);

        
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
        return 220;
    }

    public double getWidthInView() 
    {
        return 450;
    }

    public void willChangeVisibility(boolean to) 
    {}

    public void setSize(int width, int height) 
    {
        super.setSize(width, height);
    }
}