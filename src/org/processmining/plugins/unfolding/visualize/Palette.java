package org.processmining.plugins.unfolding.visualize;


import java.awt.Color;

public class Palette{

	
	private Color cutColor;
	private Color deadColor;
	private Color bothCutoffDead;
	private Color LocalConfigurationColor;
	private Color arcLabelColor; 
	private Color arcDead;
	private Color arcDeadLabel;
	private Color deadNodeColor;
	


	public Palette(){
		cutColor = new Color(0,255,255); //Cyan
		deadColor = new Color(255,77,77); // light red
		bothCutoffDead = new Color(138,43,226); //violet
		LocalConfigurationColor = Color.GREEN;
		arcLabelColor = Color.RED; 
		arcDead = Color.RED;
		arcDeadLabel = Color.BLACK;
		deadNodeColor = Color.ORANGE;
	}
	

	public Color getCutColor(){
		return cutColor;
	}

	public Color getDeadColor() {
		return deadColor;
	}

	public Color getBothCutoffDead() {
		return bothCutoffDead;
	}
	
	public Color getLocalConfigurationColor(){
		return LocalConfigurationColor;
	}
	
	public Color getArcLabelColor() {
		return arcLabelColor;
	}

	public Color getArcDead() {
		return arcDead;
	}

	public Color getArcDeadLabel() {
		return arcDeadLabel;
	}

	public Color getDeadNodeColor() {
		return deadNodeColor;
	}

}