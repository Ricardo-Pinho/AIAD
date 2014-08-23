package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import model.ForestElement;
import model.ForestTree;
import model.Node;
import model.ViewMap;
import uchicago.src.sim.space.Object2DGrid;

public class Agent extends ForestElement {

	/** Current direction of the agent
	 * 0 = WEST
	 * 1 = EAST
	 * 2 = NORTH
	 * 3 = SOUTH
	 */
	protected final static int west = 0;
	protected final static int east = 1;
	protected final static int north = 2;
	protected final static int south = 3;
	
	protected static ArrayList<Node> graph = new ArrayList<Node>();
	
	public int agentID;
	public String agentName;
	protected int direction = -1, prevDirection = -1;
	public ViewMap viewmap;
	public boolean broadcasting=false, getBroadcast=false,dirChange=false;
	public HashMap<Integer,ViewMap> agentsDiscussed = new HashMap<Integer,ViewMap>();
	
	public Agent(int xpos, int ypos, Object2DGrid space, ArrayList<Node> g, int Idget) {
		super(xpos, ypos, space);
		graph = g;
		agentID=Idget;
		// Initialize the agent's personal map.
		viewmap = new ViewMap(space.getSizeX(),space.getSizeY());
	}
	
	protected int getHeatAt(int i, int j) {
		return viewmap.forestmap.get(i + j* space.getSizeX()).heat;
	}
	
	protected void setHeatAt(int i, int j, int heat) {
		viewmap.forestmap.get(i + j* space.getSizeX()).heat = heat;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<ForestElement> getNeighbours(){
		return space.getVonNeumannNeighbors(getX(), getY(), true); // true: return nulls if empty
	}
	
	/**
	 * Returns whether there path ahead is free
	 * @return True: path is blocked. False otherwise.
	 */
	public boolean pathBlocked() {
		int nextY = y + ((direction == south)?  1: (direction == north)? -1: 0);
		int nextX = x + ((direction == east)?  1: (direction == west)? -1: 0);
		return space.getObjectAt(nextX, nextY) != null &&
			space.getObjectAt(nextX, nextY) instanceof ForestTree;
	}
	
	@Override
	public String toString() {
		return "#" + agentID + " [" + x + ";" + y + "]";
	}
	
	public void moveRandom(ArrayList<Integer> emptyDirections)
	{
		// choose another direction
					//Random r = new Random();
					//int newdir = r.nextInt(emptyDirections.size());
					int bestoption=Integer.MAX_VALUE;
					for( int i=0; i<emptyDirections.size(); i++)
					{
						switch (emptyDirections.get(i))
						{
						// FIXME: when there are multiple equally good options,
						// pick one randomly
						
							case(west): {
								if(getHeatAt(x-1, y) < bestoption){
									bestoption = getHeatAt(x-1, y);
									direction = west;
								}
								break;
							}
							case(east): {
								if(getHeatAt(x+1, y) < bestoption){
									bestoption = getHeatAt(x+1, y);
									direction = east;
								}
								break;
							}
							case(north): {
								if(getHeatAt(x, y-1) < bestoption){
									bestoption = getHeatAt(x, y-1);
									direction = north;
								}
								break;
							}
							case(south): {
								if(getHeatAt(x, y+1) < bestoption){
									bestoption = getHeatAt(x, y+1);
									direction = south;
								}
								break;
							}
					}
				}
	}
	
	public void moveRandomly(ArrayList<Integer> emptyDirections)
	{
		// choose another direction
		int bestoption=Integer.MAX_VALUE;
		int heatoption=Integer.MAX_VALUE;
		Vector<Integer> dHeat= new Vector<Integer>();
		Vector<Integer> possDirections= new Vector<Integer>();
		if(emptyDirections.size()==1)
			direction=emptyDirections.get(0);
		else{
			for( int i=0; i<emptyDirections.size(); i++)
			{
				switch (emptyDirections.get(i))
				{
				// FIXME: when there are multiple equally good options,
				// pick one randomly
					case(west): {
							heatoption = getHeatAt(x-1, y);
							if(heatoption<bestoption)
								bestoption=heatoption;
							dHeat.add(heatoption);
							possDirections.add(west);
						break;
					}
					case(east): {
							heatoption = getHeatAt(x+1, y);
							if(heatoption<bestoption)
								bestoption=heatoption;
							dHeat.add(heatoption);
							possDirections.add(east);
						break;
					}
					case(north): {
							heatoption = getHeatAt(x, y-1);
							if(heatoption<bestoption)
								bestoption=heatoption;
							dHeat.add(heatoption);
							possDirections.add(north);
						break;
					}
					case(south): {
							heatoption = getHeatAt(x, y+1);
							if(heatoption<bestoption)
								bestoption=heatoption;
							dHeat.add(heatoption);
							possDirections.add(south);
						break;
					}				}
			}
			if(possDirections.size()==1)
				direction=possDirections.get(0);
			else{
				for (int i = 0; i < dHeat.size(); i++) {
					if(dHeat.get(i)>bestoption)
					{
						dHeat.remove(i);
						possDirections.remove(i);
						i--;
					}
				}
				if(possDirections.size()==1)
					direction=possDirections.get(0);
				else{
					Random r = new Random();
					int solution=r.nextInt(possDirections.size());
					direction=possDirections.get(solution);
				}
			}
		}
	//	direction = emptyDirections.get(newdir);
		//dirChange=true;
		//searchGrid=0;
	}
	
	public String getClosestUnexplored(Node sourceNode) {
	    //Initialization.
	    Node currentNode = sourceNode;
	    Queue<Node> queue = new LinkedList<Node>();
	    queue.add(currentNode);
	    HashMap<Node,Node> visitedNodes = new HashMap<Node,Node>();
	    //Search.
	    while (!queue.isEmpty()) {
	        currentNode = queue.remove();
	        //System.out.println(currentNode.x + " - " + currentNode.y);
	        if (getHeatAt(currentNode.x,currentNode.y)==0&&currentNode!=sourceNode) {
	        	String result = "";
	        	Node cNode=currentNode;
	    	    for(;visitedNodes.get(cNode)!=sourceNode;)
	    	    {
	    	    	//System.out.println(visitedNodes.get(cNode).x+"-"+visitedNodes.get(cNode).y);
	    	    	cNode=visitedNodes.get(cNode);
	    	    }
	    	    result=cNode.x+"-"+cNode.y;
	        	return result;
	        } else {
	            for (Node nextNode : currentNode.adjacentNodes) {
	                if (!visitedNodes.containsKey(nextNode) && getHeatAt(nextNode.x,nextNode.y)>=0) {
	                    queue.add(nextNode);
	                    visitedNodes.put(nextNode,currentNode);
	                    //System.out.println("  " + nextNode.x + " - " + nextNode.y);
	                }
	            }
	        }
	    }

	    //If all nodes are explored and the destination node hasn't been found.
	        return "";
	}
	
	public boolean unexploredNeighbor()
	{
		return (getHeatAt(x-1,y)==0||getHeatAt(x+1,y)==0||getHeatAt(x,y-1)==0||getHeatAt(x,y+1)==0);
	}

}
