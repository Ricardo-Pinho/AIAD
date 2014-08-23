package model;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

public class ForestHeat implements Drawable{
	public int heat;
	public int x,y;
	
	public ForestHeat(int heat, int xpos, int ypos){
		this.heat = heat;
		x = xpos;
		y = ypos;
	}
	
	public ForestHeat(int xpos, int ypos){
		this.heat = -1;
		x = xpos;
		y = ypos;
	}

	@Override
	public void draw(SimGraphics g) {
		if(heat>0)
			g.drawFastRect( new Color((heat > 9)? 255 : heat*25, 0, 0) );
		else if (heat==0)
			g.drawFastRect( new Color(10,50,100));
		else if (heat==-2)
			g.drawFastRect( Color.gray);
		else if (heat ==-3)
			g.drawFastRect( Color.YELLOW);
		else
			g.drawFastRect( Color.white);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
}
