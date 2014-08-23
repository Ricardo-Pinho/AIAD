package model;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;



public class ForestTree extends ForestElement{

	private static Image img;
	
	public ForestTree(int x, int y, Object2DGrid space) {
		super(x,y,space);
	}
	
	@Override
	public void step(){
		// imma tree.
		// chirp chirp.
	}

	public static void setIcon(BufferedImage icon) {
		img = icon;
	}
	
	@Override
	public void draw(SimGraphics g) {
		if (img != null) {
			g.drawImageToFit(img);
		} else {
			g.drawFastCircle(new Color(65,88,43));
		}
	}
}
