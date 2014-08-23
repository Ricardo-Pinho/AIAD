package model;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

public class ForestPath extends ForestElement {
	
	private static Image img;
	
	public ForestPath(int xpos, int ypos, Object2DGrid s) {
		super(xpos,ypos,space);
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
			g.drawFastRect(new Color(191, 164, 112));
		}
	}
}
