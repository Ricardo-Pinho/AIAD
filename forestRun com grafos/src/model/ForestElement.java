package model;
import uchicago.src.sim.engine.Stepable;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Represents any agent in the forest (soldier, captain or robot)or an obstacle (trees).
 *
 */
public class ForestElement implements Drawable, Stepable {

	/** Current position of the element.
	 * X: Horizontal;
	 * Y: Vertical
	 */
	public int x, y;
	protected static Object2DGrid space;
	
	public ForestElement(int xpos, int ypos, Object2DGrid s) {
		space = s;
		this.x = xpos;
		this.y = ypos;
	}
	
	@Override
	public void draw(SimGraphics g) {}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
	
	public void step(){}

}
