package agent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Vector;

import model.ForestElement;
import model.ForestModel;
import model.ForestTree;
import model.Node;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


public class Soldier extends Human{

	public static int commRange=3;
	private static BufferedImage img;
	
	
	public Soldier(int x, int y, Object2DGrid space, ArrayList<Node> graph, int id) {
		super(x, y, space,graph, id);
	}

	
//	public void step(){
//		space.putObjectAt(x, x, null);
//		
//		//TODO: reset x and y
//		space.putObjectAt(x, y, this);
//	}
	
	public static void setIcon(BufferedImage icon) {
		img = icon;
	}
	
	@Override
	public void draw(SimGraphics g) {
		if (img != null) {
			g.drawImageToFit(img);
		} else {
			g.drawFastRect(Color.orange);
		}
	}

	@Override
	public String toString() {
		return "Soldier" + super.toString();
	}
	
	

}
