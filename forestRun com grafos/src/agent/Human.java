package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import uchicago.src.sim.space.Object2DGrid;
import model.ForestElement;
import model.ForestMap;
import model.ForestModel;
import model.ForestTree;
import model.Node;
import model.ViewMap;

public class Human extends Agent{


	//These fields only belong to Soldier and Captain. 
	public static int visionRange=2;
	public static int commRange=2;
	public static int radioCommInterval=5;
	public static boolean efficientComm=true;
	public static int robotCheckInterval=40;
	public static boolean searchbyAgents=true;
	public static int radioMaxPower=100;
	public static boolean debugMode=false;
	public static boolean efficientHuman=true;
	public boolean foundExit = false, pathtoGoal=false;
	public ArrayList<String> shortPath;
	public Node questNode= new Node(0,0);
	public int broadcastCounter=0,searchGrid=Integer.MAX_VALUE;


	public Human(int xpos, int ypos, Object2DGrid space, ArrayList<Node> g, int Idget) {
		super(xpos, ypos, space, g, Idget);
		graph = g;
		agentID=Idget;
		// Initialize the agent's personal map.
		viewmap = new ViewMap(space.getSizeX(),space.getSizeY());
	}


	/**
	 * Fills the matrix that represents
	 * the agent's field of view.
	 * 	
	 * checks if the exit has been found
	 * if it has changes its value
	 *
	 */
	public void fillGrid()
	{
		if(dirChange) dirChange=false;
		searchGrid=0;
		for (int xi = -visionRange; xi <= visionRange; xi++) {
			for (int yi = -visionRange; yi <= visionRange; yi++) {
				if(x+xi<0 || x+xi >=space.getSizeX() || y+yi<0 || y+yi >=space.getSizeY()) {}
				else if(! (space.getObjectAt(x+xi, y+yi) instanceof ForestTree) )
				{
					if((space.getObjectAt(x+xi, y+yi) instanceof Robot))
					{	
						Agent robotcaster=((Agent)space.getObjectAt(x+xi, y+yi));
						robotBroadcastGet(robotcaster);
					}
					if(getHeatAt(x+xi, y+yi) == -1)
						setHeatAt(x+xi, y+yi, 0);
					if(x+xi==0||y+yi==0 ||x+xi==space.getSizeX()-1 || y+yi == space.getSizeY()-1)
					{
						setHeatAt(x+xi, y+yi, -3);
						int exitx=x+xi;
						int exity=y+yi;
						questNode=getNode(exitx, exity);
						foundExit=true;
					}
				}
				else
					setHeatAt(x+xi, y+yi, -2);
			}
		}
		if(foundExit&&!pathtoGoal){
			getPathToGoal();
		}
	}

	public void getPathToGoal()
	{
		Node startNode=getNode(x,y);
		if(possibleRoute(questNode, startNode))
		{
			shortPath=getDirections(questNode, startNode);
			if(debugMode)
				System.out.println("Soldier#"+this.agentID+" Found exit at "+questNode.x+"-"+questNode.y+" with heat at "+getHeatAt(questNode.x,questNode.y));
			int ticksPassed = (int)ForestModel.tick;
			if(searchbyAgents&&ticksPassed<=radioMaxPower)
				effBroadcastbyAgent();
			else if(ticksPassed<=radioMaxPower)
				effBroadcast();
			broadcastCounter=0;
			//			for (int i = 0; i < shortPath.size(); i++) {
			//				System.out.println(shortPath.get(i));
			//			}
			pathtoGoal=true;
		}

	}

	public void broadcastCheck()
	{
		for (int xi = -commRange; xi <= commRange; xi++) {
			for (int yi = -commRange; yi <= commRange; yi++) {
				if(x+xi<0 || x+xi >=space.getSizeX() || y+yi<0 || y+yi >=space.getSizeY()) {}
				else if((space.getObjectAt(x+xi, y+yi) instanceof Captain) || (space.getObjectAt(x+xi, y+yi) instanceof Soldier) )
				{
					if(((Human)(space.getObjectAt(x+xi, y+yi))).broadcasting)
					{
						//DEBUG
						getBroadcast=true;
						Human broadcaster = ((Human)space.getObjectAt(x+xi, y+yi));
						getDataBroadcast(broadcaster);
					}
				}
			}
		}
	}

	public void broadcastCheckbyAgent()
	{
		int xmin=x-commRange;
		int xmax=x+commRange;
		int ymin=y-commRange;
		int ymax=y+commRange;
		for (int i = 0; i < ForestModel.agentList.size(); i++) {
			if(ForestModel.agentList.get(i).x>xmin&&ForestModel.agentList.get(i).x<xmax&&ForestModel.agentList.get(i).y>ymin&&ForestModel.agentList.get(i).y<ymax)
			{
				//System.out.println("hearing from agent "+(ForestModel.agentList.get(i).agentID));
				if(ForestModel.agentList.get(i).broadcasting)
				{
					getBroadcast=true;
					Agent broadcaster = (ForestModel.agentList.get(i));
					getDataBroadcast(broadcaster);
				}
			}
		}
	}

	public void effBroadcast()
	{		
		long ticksPassed = ForestModel.tick;
		for (int xi = -commRange; xi <= commRange; xi++) {
			for (int yi = -commRange; yi <= commRange; yi++) {
				if(x+xi<0 || x+xi >=space.getSizeX() || y+yi<0 || y+yi >=space.getSizeY()) {}
				else if((space.getObjectAt(x+xi, y+yi) instanceof Captain) || (space.getObjectAt(x+xi, y+yi) instanceof Soldier) )
				{
					ViewMap agentMap;
					if(((Human)space.getObjectAt(x+xi, y+yi)).agentID!=this.agentID && !agentsDiscussed.containsKey(((Human)space.getObjectAt(x+xi, y+yi)).agentID))
					{
						//System.out.println((x+xi)+"-"+(y+yi)+" ->"+((Agent)space.getObjectAt(x+xi, y+yi)).agentID+" as opposed to "+this.agentID);
						broadcasting=true;
						agentMap=viewmap;
						agentMap.tick=(int)ticksPassed;
						agentsDiscussed.put(((Human)space.getObjectAt(x+xi, y+yi)).agentID, agentMap);
					}
					else if(agentsDiscussed.containsKey(((Human)space.getObjectAt(x+xi, y+yi)).agentID))
					{
						agentMap=agentsDiscussed.get(((Human)space.getObjectAt(x+xi, y+yi)).agentID);
						if((agentMap.tick+radioCommInterval)<=ticksPassed)
						{
							broadcasting=true;
							agentMap=viewmap;
							agentMap.tick=(int)ticksPassed;
							agentsDiscussed.put(((Human)space.getObjectAt(x+xi, y+yi)).agentID, agentMap);
						}
					}
				}
			}
		}
		broadcastCounter=0;
	}

	public void effBroadcastbyAgent()
	{		
		long ticksPassed = ForestModel.tick;
		int xmin=x-commRange;
		int xmax=x+commRange;
		int ymin=y-commRange;
		int ymax=y+commRange;
		for (int i = 0; i < ForestModel.agentList.size(); i++) {
			if(ForestModel.agentList.get(i).x>xmin&&ForestModel.agentList.get(i).x<xmax&&ForestModel.agentList.get(i).y>ymin&&ForestModel.agentList.get(i).y<ymax)
			{
				ViewMap agentMap;
				if(ForestModel.agentList.get(i).agentID!=this.agentID && !agentsDiscussed.containsKey(ForestModel.agentList.get(i).agentID))
				{
					//System.out.println((x+xi)+"-"+(y+yi)+" ->"+((Agent)space.getObjectAt(x+xi, y+yi)).agentID+" as opposed to "+this.agentID);
					broadcasting=true;
					agentMap=viewmap;
					agentMap.tick=(int)ticksPassed;
					agentsDiscussed.put(ForestModel.agentList.get(i).agentID, agentMap);
				}
				else if(agentsDiscussed.containsKey(ForestModel.agentList.get(i).agentID))
				{
					agentMap=agentsDiscussed.get(ForestModel.agentList.get(i).agentID);
					if((agentMap.tick+radioCommInterval)<=ticksPassed)
					{
						broadcasting=true;
						agentMap=viewmap;
						agentMap.tick=(int)ticksPassed;
						agentsDiscussed.put(ForestModel.agentList.get(i).agentID, agentMap);
					}
				}
			}
		}
	}

	public void robotBroadcastGet(Agent robotcaster)
	{
		long ticksPassed = ForestModel.tick;
		ViewMap agentMap;
		if(!robotcaster.agentsDiscussed.containsKey(this.agentID))
		{
			agentMap=robotcaster.viewmap;
			agentMap.tick=(int)ticksPassed;
			robotcaster.agentsDiscussed.put(this.agentID, agentMap);
			getDataBroadcast(robotcaster);
		}
		else
		{
			agentMap=robotcaster.agentsDiscussed.get(this.agentID);
			if((agentMap.tick+robotCheckInterval)<=ticksPassed)
			{
				agentMap=robotcaster.viewmap;
				agentMap.tick=(int)ticksPassed;
				robotcaster.agentsDiscussed.put(this.agentID, agentMap);
				getDataBroadcast(robotcaster);
			}
		}
	}


	/**
	 * FIXME: Most parts of this code only belong to Captain and Soldier
	 */
	@Override
	public void step() {
		int ticksPassed = (int)ForestModel.tick;
		if(broadcasting) broadcasting=false;
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


	public void getDataBroadcast(Agent broadcaster)
	{
		if(debugMode){
			if (broadcaster instanceof Soldier) System.out.println("Soldier#"+agentID+" getting data from Soldier#"+broadcaster.agentID);
			else if (broadcaster instanceof Captain) System.out.println("Soldier#"+agentID+" getting data from Captain#"+broadcaster.agentID);
			else if (broadcaster instanceof Robot) System.out.println("Soldier#"+agentID+" getting data from Robot#"+broadcaster.agentID);	
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

		if(foundExit&&!pathtoGoal){
			Node startNode=getNode(x,y);
			if(possibleRoute(questNode, startNode))
			{
				shortPath=getDirections(questNode, startNode);
				int ticksPassed = (int)ForestModel.tick;
				if(ticksPassed<=radioMaxPower)
					broadcasting=true;
				/*for (int i = 0; i < shortPath.size(); i++) {
					System.out.println(shortPath.get(i));
				}*/
				pathtoGoal=true;
				if(debugMode)
					System.out.println("Soldier#"+this.agentID+" Found exit at "+questNode.x+"-"+questNode.y+" with heat at "+getHeatAt(questNode.x,questNode.y));
			}
		}
	}

	public void goalPath()
	{
		space.putObjectAt(x, y, null);
		if(x!=questNode.x||y!=questNode.y){
			String [] coordinates = shortPath.get(0).split("-");
			if(Integer.parseInt(coordinates[0])==x && Integer.parseInt(coordinates[1])==y)
			{
				shortPath.remove(0);
				coordinates = shortPath.get(0).split("-");
			}
			x= Integer.parseInt(coordinates[0]);
			y= Integer.parseInt(coordinates[1]);
			shortPath.remove(0);
			space.putObjectAt(x, y, this);
			broadcastCounter++;
			incCurrentLocation();
		}
		else
		{
			ForestModel.log.addGoalTime(ForestModel.tick);
			this.x=-1;
			this.y=-1;
		}
	}


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
					System.out.println("here");
					atExit=true;
				}
			}
			dir++;
		}

		// System.out.println(numTreesVonNeuman);

		if(numTreesVonNeuman < 2		// It's an intersection or...
				|| direction == -1		// direction is undifined or...
				|| pathBlocked()) {		// 
			/*if(!unexploredNeighbor()&&efficientHuman)
			{
				Node startNode=getNode(x,y);
				String getPath="";
				getPath=getClosestUnexplored(startNode);
				if(getPath!="")
				{
					String [] coordinates = getPath.split("-");	
					if(Integer.valueOf(coordinates[0])<x) direction = west;
					else if(Integer.valueOf(coordinates[0])>x) direction = east;
					else if(Integer.valueOf(coordinates[1])<y) direction = north;
					else if(Integer.valueOf(coordinates[1])>y) direction = south;
				}
			}
			else
			{
				if(!atExit)	*/
					moveRandom(emptyDirections);
			//}
			//moveRandom(emptyDirections);
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
		broadcastCounter++;
		incCurrentLocation();
	}

	public void incCurrentLocation()
	{
		if(!foundExit || x!=questNode.x || y!= questNode.y)
			viewmap.forestmap.get(x + y * space.getSizeX()).heat++;
		else
			viewmap.forestmap.get(x + y * space.getSizeX()).heat=255;
	}

	/**
	 * Returns a list with the 4 neighbors on the for directions in this order:
	 * [WEST, EAST, SOUTH, NORTH]
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Vector<ForestElement> getNeighbours(){
		return space.getVonNeumannNeighbors(getX(), getY(), true); // true: return nulls if empty
	}


	public String mapToString(){
		String str = "";

		for (int i = 0; i < space.getSizeX(); i++) {
			for (int j = 0; j < space.getSizeY(); j++) {
				if (getHeatAt(i, j) == -1) {
					str += 0;
				} else {
					str += getHeatAt(i, j);
				}
			}
			str += "\n";
		}

		return str;
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

	public boolean possibleRoute(Node sourceNode, Node destinationNode) {
		//Initialization.
		Node currentNode = sourceNode;
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(currentNode);

		Set<Node> visitedNodes = new HashSet<Node>();
		visitedNodes.add(currentNode);
		//Search.
		while (!queue.isEmpty()) {
			currentNode = queue.remove();
			if (currentNode.x==destinationNode.x && currentNode.y==destinationNode.y) {
				return true;
			} else {
				for (Node nextNode : currentNode.adjacentNodes) {
					if (!visitedNodes.contains(nextNode) && getHeatAt(nextNode.x,nextNode.y)>=0) {
						queue.add(nextNode);
					}
					visitedNodes.add(nextNode);
				}
			}
		}

		//If all nodes are explored and the destination node hasn't been found.
		return false;
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
					if (!visitedNodes.containsKey(nextNode) && getHeatAt(nextNode.x,nextNode.y)>=0) {
						queue.add(nextNode);
						visitedNodes.put(nextNode,currentNode);
						//System.out.println("  " + nextNode.x + " - " + nextNode.y);
					}
				}
	        }
	    }

	    //If all nodes are explored and the destination node hasn't been found.
	        return null;
	}


//	public ArrayList<ForestHeat> mapToList(Object2DGrid space) {
//		ArrayList<ForestHeat> mTL = new ArrayList<ForestHeat>();
//		
//		for (int i = 0; i < viewmap.length; i++) {
//			for (int j = 0; j < viewmap[i].length; j++) {
//				if (viewmap[i][j] == Integer.MAX_VALUE) {
//					((ForestHeat) space.getObjectAt(i, j)).heat = 0;
//					mTL.add( (ForestHeat) space.getObjectAt(i, j) );
//				} else {
//					((ForestHeat) space.getObjectAt(i, j)).heat = viewmap[i][j];
//					mTL.add( (ForestHeat) space.getObjectAt(i, j) );
//				}
//				
//				int heat = viewmap[i][j];
//				if (true || heat!=0 && heat != Integer.MAX_VALUE) {
//					System.out.println(heat);
//				}
//			}
//		}
//		
//		return mTL;
//	}
}
