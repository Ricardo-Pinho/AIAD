package agent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import model.ForestElement;
import model.ForestModel;
import model.ForestTree;
import model.Node;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


public class Robot extends Agent{
	
	public int power;
	public static String imgPath = "robot.png";
	private static BufferedImage img;

	public Robot(int x, int y, int maxpower, Object2DGrid space, ArrayList<Node> graph, int id) {
		super(x, y, space, graph, id);
		this.power = maxpower;
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
			g.drawFastRect(Color.WHITE);
		}
	}
	
	@Override
	public void step() {
		if (power-- > 0) //Is it dead?
			gotoBestPath();
			
	}
	
	public boolean pathBlocked() {
		int nextY = y + ((direction == south)?  1: (direction == north)? -1: 0);
		int nextX = x + ((direction == east)?  1: (direction == west)? -1: 0);
		if(nextY==-1||nextX==-1||nextY==space.getSizeY()||nextX==space.getSizeX())
			return true;
		return space.getObjectAt(nextX, nextY) != null &&
			space.getObjectAt(nextX, nextY) instanceof ForestTree;
	}
	
	/**
	 * Choose the best direction to take next
	 * when it meets an intersection
	 */
	public void gotoBestPath()
	{
		int numTreesVonNeuman = 0;
		Vector<ForestElement> neighbors = getNeighbours();
		boolean atExit=false;
		ArrayList<Integer> emptyDirections = new ArrayList<Integer>();
		int dir = west;
		for (ForestElement n : neighbors) {
			if (n != null && n instanceof ForestTree){
				numTreesVonNeuman++;
			} else {
				int nextY = y + ((dir == south)?  1: (dir == north)? -1: 0);
				int nextX = x + ((dir == east)?  1: (dir == west)? -1: 0);
				if(nextX>-1&&nextX<space.getSizeX()&&nextY>-1&&nextY <space.getSizeY())
						emptyDirections.add(dir);
				else{
					if(dir==0)
						direction=1;
					if(dir==1)
						direction=0;
					if(dir==2)
						direction=3;
					if(dir==3)
						direction=2;
					atExit=true;
				}
			}
			dir++;
		}
		
		// System.out.println(numTreesVonNeuman);
		
		if(numTreesVonNeuman < 2		// It's an intersection or...
				|| direction == -1		// direction is undifined or...
				|| pathBlocked()) {		// 
			if(!atExit)
				moveRandom(emptyDirections);
		}
		
		space.putObjectAt(x, y, null);
		switch (direction) {
		case west:
				x--;
			break;
		case east:
				x++;
			break;
		case north:
				y--;
			break;
		case south:
				y++;
			break;

		default:
			break;
		}
		space.putObjectAt(x, y, this);
		incCurrentLocation();
	}

	/**
	 * Increments the current agent's location on it's viewmap
	 */
	public void incCurrentLocation()
	{
			int heat=viewmap.forestmap.get(x + y * space.getSizeX()).heat;
			viewmap.forestmap.get(x + y * space.getSizeX()).heat=heat+2;
			if(x==0||y==0||(x==space.getSizeX()-1)||(x==space.getSizeY()-1))
				viewmap.forestmap.get(x + y * space.getSizeX()).heat=-3;
	}
	
	public static Node getNode(int xget, int yget)
	{
		for(int i=0; i<graph.size(); i++)
		{
			Node nthis = graph.get(i);
			if( nthis.x==xget && nthis.y==yget)
				return nthis;
			
		}
		throw new RuntimeException("Node with "+xget+"-"+yget+" not found.");
	}
	
	@Override
	public String toString() {
		return "Robot" + super.toString();
	}
}
