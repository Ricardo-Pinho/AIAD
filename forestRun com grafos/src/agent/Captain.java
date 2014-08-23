package agent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import model.ForestElement;
import model.ForestModel;
import model.ForestTree;
import model.Node;
import model.ViewMap;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


public class Captain extends Human{
	
	private static BufferedImage img;
	public static int cellMaxPower=100;
	
	/** Used to trigger cellphone communications
	 */
	public boolean transferingInfo= false;

	public Captain(int x, int y, Object2DGrid space, ArrayList<Node> graph, int id) {
		super(x, y, space, graph, id);
	}
	
	public static void setIcon(BufferedImage icon) {
		img = icon;
	}
	
	@Override
	public void draw(SimGraphics g) {
		if (img != null) {
			g.drawImageToFit(img);
		} else {
			g.drawFastRect(Color.blue);
		}
	}
	
	@Override
	public String toString() {
		return "Captain" + super.toString();
	}
	
	
	public void getPathToGoal()
	{
		Node startNode=getNode(x,y);
		if(possibleRoute(questNode, startNode))
		{
			shortPath=getDirections(questNode, startNode);
			int ticksPassed = (int)ForestModel.tick;
			if(searchbyAgents&&ticksPassed<=radioMaxPower)
				effBroadcastbyAgent();
			else if(ticksPassed<=radioMaxPower)
				effBroadcast();
			if(debugMode)
				System.out.println("Captain#"+this.agentID+" Found exit at "+questNode.x+"-"+questNode.y+" with heat at "+getHeatAt(questNode.x,questNode.y));
			shareInformationwithCaptains();
			pathtoGoal=true;
		}

	}
	
	@Override
	public void step() {
		int ticksPassed = (int)ForestModel.tick;
		if(broadcasting) broadcasting=false;
		else if(transferingInfo) transferingInfo=false;
		else if(!efficientComm&&broadcastCounter==radioCommInterval&&ticksPassed<=radioMaxPower)
			{
				/*if(dirChange||(searchGrid>=(visionRange*2)))*/ fillGrid();
				//else searchGrid++;
				if(!broadcasting)
				{
					if(searchbyAgents)
						broadcastCheckbyAgent();
					else
						broadcastCheck();
				}
				if(getBroadcast) {getBroadcast=false;}
				else
				{
					broadcasting=true;
					broadcastCounter=0;
				}
			}
		else if(!pathtoGoal){
			/*if(dirChange||searchGrid>=(visionRange*2))*/ fillGrid();
			//else searchGrid++;
			if(!broadcasting&&ticksPassed<=radioMaxPower)
				{
					if(searchbyAgents)
						broadcastCheckbyAgent();
					else
						broadcastCheck();
				}
			if(efficientComm&&!getBroadcast)
				{
				if(broadcastCounter>=radioCommInterval&&ticksPassed<=radioMaxPower)	
					{
						if(searchbyAgents)
							effBroadcastbyAgent();
						else 
							effBroadcast();
							broadcastCounter=0;
					}
				}
			if(getBroadcast) {getBroadcast=false;}
			else if (!pathtoGoal)
				gotoBestPath();
		}
		else
		{
			goalPath();
		}
	}
	
	@Override
	public void gotoBestPath()
	{
		int numTreesVonNeuman = 0;
		Vector<ForestElement> neighbors = getNeighbours();
		ArrayList<Integer> emptyDirections = new ArrayList<Integer>();
		int dir = west;
		boolean atExit=false;
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
			// choose another direction
			//Random r = new Random();
			//int newdir = r.nextInt(emptyDirections.size());
			int bestoption=Integer.MAX_VALUE;
			int heatoption=Integer.MAX_VALUE;
			Vector<Integer> dHeat= new Vector<Integer>();
			Vector<Integer> possDirections= new Vector<Integer>();
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
					int closestCapt=Integer.MAX_VALUE;
					Node myNode=getNode(this.x,this.y);
					Node thisCapt;
					int getDist=0;
					boolean newDirection=false;
					boolean moreCaptains=false;
					for (Agent agent : ForestModel.agentList) 
						{
							if(agent instanceof Captain && agent!=this&& agent.x!=-1)
							{
								moreCaptains=true;
								thisCapt=getNode(agent.x,agent.y);
								if(thisCapt==myNode)
									getDist=0;
								else{
									getDist=getDirections(myNode, thisCapt).size();
								}
								if(getDist<closestCapt)
								{
									closestCapt=getDist;
									if(possDirections.contains(east))
										{
											if(agent.x<this.x){
												direction=east;
												newDirection=true;
											}
										}
									if(possDirections.contains(west))
										{
											if(agent.x>this.x){
												direction=west;
												newDirection=true;
											}
										}
									if(possDirections.contains(north))
											{
												if(agent.y>this.y){
													direction=north;
													newDirection=true;
												}
											}
									if(possDirections.contains(south))
										{
											if(agent.y<this.y){
												direction=south;
												newDirection=true;
											}
										}
								}
							}
						}
					if(!moreCaptains||direction==-1||newDirection==false)
						{
							/*if(!unexploredNeighbor()&&efficientHuman)
							{
								Node startNode=getNode(x,y);
								String getPath="";
								getPath=getClosestUnexplored(startNode);
								if(getPath!="")
								{
									//System.out.println("here");
									String [] coordinates = getPath.split("-");	
									if(Integer.valueOf(coordinates[0])<x) direction = west;
									else if(Integer.valueOf(coordinates[0])>x) direction = east;
									else if(Integer.valueOf(coordinates[1])<y) direction = north;
									else if(Integer.valueOf(coordinates[1])>y) direction = south;
								}
							}
							else
							{
								if(!atExit)*/	
									moveRandom(emptyDirections);
							//}
						}
					/*callCaptains();
					int maxcapt=Integer.MAX_VALUE;
					for (int i = 0; i < possDirections.size(); i++) {
						switch (possDirections.get(i))
						{
						case north: if(capt2North<=maxcapt) direction= north; break;
						case south: if(capt2South<=maxcapt) direction= south; break;
						case east: if(capt2East<=maxcapt) direction= east; break;
						case west: if(capt2West<=maxcapt) direction= west; break;
						}
					}*/
					//moveRandom(emptyDirections);
					}
				}
			}
		//	direction = emptyDirections.get(newdir);
			//dirChange=true;
			//searchGrid=0;
		//}
		
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
		broadcastCounter++;
		incCurrentLocation();
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
	
	
	public void getDataBroadcast(Agent broadcaster)
	{
		if(debugMode){
			if (broadcaster instanceof Soldier) System.out.println("Captain#"+agentID+" getting data from Soldier#"+broadcaster.agentID);
			else if (broadcaster instanceof Captain) System.out.println("Captain#"+agentID+" getting data from Captain#"+broadcaster.agentID);
			else if (broadcaster instanceof Robot) System.out.println("Captain#"+agentID+" getting data from Robot#"+broadcaster.agentID);
		}
		ViewMap oldmap;
		if(broadcaster.agentsDiscussed.get(this.agentID)!=null)
			oldmap = broadcaster.agentsDiscussed.get(this.agentID);
		else
			oldmap=broadcaster.viewmap;
		for (int i=0; i<space.getSizeX();i++)
		{
			for (int j=0; j<space.getSizeY();j++)
			{
				//System.out.println(i+"-"+j);
				if( broadcaster.getHeatAt(i,j) != -1  )
				{
					if(broadcaster.getHeatAt(i,j)==-3)
						{
							setHeatAt(i,j,-3);
							int exitx=i;
							int exity=j;
							questNode=getNode(exitx, exity);
							foundExit=true;
						}
					else if(broadcaster.getHeatAt(i,j)==-2)
						setHeatAt(i,j,-2);
					else{
						if(getHeatAt(i,j)==-1)
							setHeatAt(i,j,broadcaster.getHeatAt(i,j));
						else{
							int heat = (broadcaster.getHeatAt(i,j)-oldmap.forestmap.get(i + j* space.getSizeX()).heat)+getHeatAt(i,j);
							setHeatAt(i,j,heat);
						}
					}
				}
			}
		}
		if(!transferingInfo)
			shareInformationwithCaptains();
		
		if(foundExit&&!pathtoGoal){
			Node startNode=getNode(x,y);
			if(possibleRoute(questNode, startNode))
			{
				shortPath=getDirections(questNode, startNode);
				
				/*for (int i = 0; i < shortPath.size(); i++) {
					System.out.println(shortPath.get(i));
				}*/
				broadcasting=true;
				pathtoGoal=true;
				if(debugMode)
					System.out.println("Captain#"+this.agentID+" Found exit at "+questNode.x+"-"+questNode.y+" with heat at "+getHeatAt(questNode.x,questNode.y));
			}
		}
	}
	
	public void shareInformationwithCaptains()
	{
		int ticksPassed = (int)ForestModel.tick;
		if(ticksPassed<=cellMaxPower)
		{
			//System.out.println(ticksPassed+"-"+cellMaxPower);
			for (Agent agent : ForestModel.agentList) 
			{
				if(agent instanceof Captain&& agent!=this)
				{
					((Captain)agent).transferingInfo=true;
					((Captain)agent).getDataBroadcast(((Agent)this));
					ViewMap agentMap;
					agentMap=viewmap;
					agentMap.tick=(int)ticksPassed;
					agentsDiscussed.put(agent.agentID, agentMap);
				}
			}
			transferingInfo=true;
		}
	}
	
	public ArrayList<String> getDirections(Node sourceNode, Node destinationNode) {
	    //Initialization.
	    Node currentNode = sourceNode;
	    Queue<Node> queue = new LinkedList<Node>();
	    queue.add(currentNode);
	    HashMap<Node,Node> visitedNodes = new HashMap<Node,Node>();
	    //Search.
	    while (!queue.isEmpty()) {
	        currentNode = queue.remove();
	        //System.out.println(currentNode.x + " - " + currentNode.y);
	        if (currentNode.x==destinationNode.x && currentNode.y==destinationNode.y) {
	        	ArrayList<String> result = new ArrayList<String>();
	        	Node cNode=currentNode;
	    	    for(;visitedNodes.get(cNode)!=sourceNode;)
	    	    {
	    	    	//System.out.println(visitedNodes.get(cNode).x+"-"+visitedNodes.get(cNode).y);
	    	    	result.add(visitedNodes.get(cNode).x+"-"+visitedNodes.get(cNode).y);
	    	    	cNode=visitedNodes.get(cNode);
	    	    }
	    	    result.add(visitedNodes.get(cNode).x+"-"+visitedNodes.get(cNode).y);
	        	return result;
	        } else {
	            for (Node nextNode : currentNode.adjacentNodes) {
	                if (!visitedNodes.containsKey(nextNode)) {
	                    queue.add(nextNode);
	                    visitedNodes.put(nextNode,currentNode);
	                    //System.out.println("  " + nextNode.x + " - " + nextNode.y);
	                }
//	                else if(visitedNodes.containsKey(nextNode) &&
//	                		visitedNodes.get(nextNode).getPathLength() > currentNode.getPathLength()+1){
//	                	visitedNodes.put(nextNode,currentNode);
//	                	System.out.println("ola");
//	                }
	            }
	        }
	    }

	    //If all nodes are explored and the destination node hasn't been found.
	        return null;
	}
}
