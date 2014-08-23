package model;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.xml.crypto.Data;

import agent.*;
import model.Node;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.CheckBoxListener;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.SimUtilities;

public class ForestModel extends SimModelImpl {
	public static ArrayList<Agent> agentList;
	private ArrayList<ForestElement> displayList;
	private Object2DDisplay heatDisplay;
	private Schedule schedule;
	private  DisplaySurface dsurf;
	private DisplaySurface dsurf2;
	private boolean justRobots=false;
	public static ArrayList<Node> graph = new ArrayList<Node>();
	public static Object2DGrid space;
	public static Object2DGrid HeatSpace;
	public static JList agentJList;
	public static JFrame agentJFrame;


	public static DataLogger log;

	//public static String mapName = "map1.txt";

	public static long tick = 0;

	/**
	 * Parameter defaults
	 */
	public int numRobots = 15,
			numSoldiers = 15,
			numCaptains = 15,
			radioCommInterval = 40,
			visionRange = 4,
			robotMaxPower = 350,
			radioMaxRange = 40,
			radioMaxPower = 400,
			cellMaxPower = 150;
	public Boolean useIcons = false,
			   efficientComm = true,
			   efficientHuman = true,
			   debugMode = false,
			   withGraphics = true;
	public Boolean getWithGraphics() {
		return withGraphics;
	}

	public void setWithGraphics(Boolean withGraphics) {
		this.withGraphics = withGraphics;
	}

	public int mapFile = 3;

	public String[] getInitParam() {
		return new String[] { "numRobots",
				"numSoldiers",
				"numCaptains",
				"radioCommInterval",
				"cellCommInterval",
				"radioMaxRange",
				"visionRange",
				"robotMaxPower",
				"radioMaxPower",
				"useIcons",
				"cellMaxPower",
				"efficientComm",
				"efficientHuman",
				"debugMode",
				"mapFile",
				"withGraphics"};
	}

	public void setup() {
		
		schedule = new Schedule();
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "Forest Display");
		registerDisplaySurface("Forest Display", dsurf);

		if (dsurf2 != null) dsurf2.dispose();
		dsurf2 = new DisplaySurface(this, "Forest Display");
		registerDisplaySurface("Forest Display", dsurf2);
		
		DataLogger.makeChart();
	}

	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();
	}

	public void buildModel() {

		agentList = new ArrayList<Agent>();
		displayList = new ArrayList<ForestElement>();

		ArrayList<Node> prevgraph = new ArrayList<Node>();
		ForestMap map = new ForestMap("map" + mapFile + ".txt");
		//map.print();
		space = new Object2DGrid(map.getHeight(), map.getWidth());
		HeatSpace = new Object2DGrid(map.getHeight(), map.getWidth());

		// Generate forest map and heat map
		for (int i = 0; i < space.getSizeX(); i++) {
			for (int j = 0; j < space.getSizeY(); j++) {
				if (map.getMap()[i][j]!=1) {
					//forest map
					ForestTree newtree = new ForestTree(i,j, space);
					displayList.add(newtree);
					space.putObjectAt(i, j, newtree);
					//heat map
				}
				else
				{
					Node n = new Node(i,j);
					prevgraph.add(n);
					ForestPath newpath = new ForestPath(i,j, space);
					displayList.add(newpath);
				}
				ForestHeat fh = new ForestHeat(i,j);
				HeatSpace.putObjectAt(i, j, fh);
			}
		}
		//printprevGraph(prevgraph);0
		createGraph(prevgraph);
		//printGraph();
		//clearVisitedGraph();

		//Get empty spots
		ArrayList<Pair<Integer>> emptySpots = new ArrayList<Pair<Integer>>();
		for (int i = 0; i < space.getSizeX(); i++) {
			for (int j = 0; j < space.getSizeY(); j++) {
				if (space.getObjectAt(i, j) == null &&
						i != space.getSizeX()-1 &&
						j != space.getSizeY()-1 &&
						i != 0 && j != 0) {
					emptySpots.add(new Pair<Integer>(i,j));
				}
			}
		}

		deployAgents(emptySpots);

		log = new DataLogger(agentList.size()-numRobots,numRobots,numSoldiers,numCaptains,radioCommInterval,radioMaxRange,visionRange,robotMaxPower,radioMaxPower,cellMaxPower,efficientComm,efficientHuman,withGraphics,useIcons);
		Human.commRange = radioMaxRange;
		Human.radioCommInterval = radioCommInterval;
		Human.visionRange = visionRange;
		Human.debugMode = debugMode;
		DataLogger.debugMode = debugMode;
		DataLogger.mapFile=mapFile;
		Human.radioMaxPower = radioMaxPower;
		Human.efficientComm = efficientComm;
		Human.efficientHuman = efficientHuman;
		Captain.cellMaxPower = cellMaxPower;
		tick=0;

		if(agentList.size()<(radioCommInterval*8))
			Human.searchbyAgents=true;
		else
			Human.searchbyAgents=false;

		if(useIcons){
			loadIcons();
		} else {
			unloadIcons();
		}
	}

	/**
	 * Places agents on the empty spots of the map.
	 * @param emptySpots
	 */
	private void deployAgents(ArrayList<Pair<Integer>> emptySpots) {

		Random r = new Random();
		int x, y, id;
		if(numSoldiers==0 && numCaptains==0) justRobots=true;
		for (id = 0; id < numRobots; id++) {
			int rand = r.nextInt(emptySpots.size());
			x = emptySpots.get(rand).getFirst();
			y = emptySpots.get(rand).getLast();
			emptySpots.remove(rand);

			Robot robot = new Robot(x, y, robotMaxPower, space, graph, id);
			displayList.add(robot);
			agentList.add(robot);
			space.putObjectAt(x, y, robot);

			if (emptySpots.size() < 1)
				return;
		}

		for (id = 0; id < numSoldiers; id++) {
			int rand = r.nextInt(emptySpots.size());
			x = emptySpots.get(rand).getFirst();
			y = emptySpots.get(rand).getLast();
			emptySpots.remove(rand);

			Soldier soldier = new Soldier(x, y, space, graph, id);
			displayList.add(soldier);
			agentList.add(soldier);
			space.putObjectAt(x, y, soldier);

			if (emptySpots.size() < 1)
				return;
		}

		for (id = 0; id < numCaptains; id++) {
			int rand = r.nextInt(emptySpots.size());
			x = emptySpots.get(rand).getFirst();
			y = emptySpots.get(rand).getLast();
			emptySpots.remove(rand);

			Captain captain = new Captain(x, y, space, graph, id);
			displayList.add(captain);
			agentList.add(captain);
			space.putObjectAt(x, y, captain);

			if (emptySpots.size() < 1)
				return;
		}
		return;
	}

	public static void createGraph(ArrayList<Node> prevgraph){
		ForestElement neighbor;
		for (Node n : prevgraph) {
			if(n.x>0){
				neighbor=(ForestElement)space.getObjectAt(n.x-1,n.y);
				if (!(neighbor instanceof ForestTree)){
					Node adjacentNode = getNodefromPrevgraph(n.x-1, n.y,prevgraph);
					n.adjacentNodes.add(adjacentNode);
				}
			}
			if(n.x<space.getSizeX()-1){
				neighbor=(ForestElement)space.getObjectAt(n.x+1,n.y);
				if (!(neighbor instanceof ForestTree)){
					Node adjacentNode = getNodefromPrevgraph(n.x+1, n.y,prevgraph);
					n.adjacentNodes.add(adjacentNode);
				}
			}
			if(n.y>0){
				neighbor=(ForestElement)space.getObjectAt(n.x,n.y-1);
				if (!(neighbor instanceof ForestTree)){
					Node adjacentNode = getNodefromPrevgraph(n.x, n.y-1,prevgraph);
					n.adjacentNodes.add(adjacentNode);
				}
			}
			if(n.y<space.getSizeY()-1){
				neighbor=(ForestElement)space.getObjectAt(n.x,n.y+1);
				if (!(neighbor instanceof ForestTree)){
					Node adjacentNode = getNodefromPrevgraph(n.x, n.y+1,prevgraph);
					n.adjacentNodes.add(adjacentNode);
				}
			}
			graph.add(n);
		}
	}

	public static Node getNodefromPrevgraph(int xget, int yget,ArrayList<Node> prevgraph)
	{
		for(int i=0; i<prevgraph.size(); i++)
		{
			Node nthis = prevgraph.get(i);
			if( nthis.x==xget && nthis.y==yget)
				return nthis;

		}
		throw new RuntimeException("Node with "+xget+"-"+yget+" not found.");
	}


	public static void printGraph()
	{

		for (int i = 0; i < graph.size(); i++) {
			System.out.println("|"+graph.get(i).x+","+graph.get(i).y+"|");
			for(Node adj : graph.get(i).adjacentNodes){
				System.out.println("	|"+adj.x+","+adj.y+"|");
			}
		}
	}

	public static void printprevGraph(ArrayList<Node> prevgraph)
	{

		for (int i = 0; i < prevgraph.size(); i++) {
			System.out.println("|"+prevgraph.get(i).x+","+prevgraph.get(i).y+"|");

		}
	}

	public static void clearVisitedGraph()
	{

		for (int i = 0; i < graph.size(); i++) {
			graph.get(i).visited=false;
		}
	}

	public static void loadIcons(){

		try {
			ForestTree.setIcon(ImageIO.read(new File("tree.png")));
			ForestPath.setIcon(ImageIO.read(new File("path.png")));
			Robot.setIcon(ImageIO.read(new File("robot.png")));
			Soldier.setIcon(ImageIO.read(new File("soldier.png")));
			Captain.setIcon(ImageIO.read(new File("captain.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void unloadIcons(){
		ForestTree.setIcon(null);
		ForestPath.setIcon(null);
		Robot.setIcon(null);
		Soldier.setIcon(null);
		Captain.setIcon(null);
	}

	private void buildDisplay() {
		// space and display surface
		Object2DDisplay display = new Object2DDisplay(space);
		display.setObjectList(displayList);
		heatDisplay = new Object2DDisplay(HeatSpace);

		dsurf2.addDisplayable(heatDisplay, "Agent View");
		dsurf2.setBackground(Color.WHITE);
		dsurf2.setSize(50,50);
		dsurf2.display();
		dsurf.addDisplayableProbeable(display, "Agents Space");
		dsurf.setBackground(new Color(50,100,50));
		dsurf.setSize(100, 100);
		dsurf.display();

		agentJList = new JList(agentList.toArray());
		agentJFrame = new JFrame();
		JScrollPane scroll = new JScrollPane(agentJList);
		agentJFrame.add(scroll);
		agentJFrame.setSize(150, 350);
		agentJFrame.setVisible(true);
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int index = agentJList.locationToIndex(e.getPoint());
				System.out.println("Showing map from agent #" + index);
				heatDisplay.setObjectList(agentList.get(index).viewmap.forestmap);

			}
		};
		agentJList.addMouseListener(mouseListener);
		
		if (!withGraphics) {
			dsurf.setVisible(false);
			dsurf2.setVisible(false);
			agentJFrame.setVisible(false);
		}
	}

	private void buildSchedule() {
		//schedule.scheduleActionBeginning(0, new MainAction());
		schedule.scheduleActionBeginning (1, this, "step");
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, dsurf2, "updateDisplay", Schedule.LAST);
	}

	public void step () {
		boolean endGame=true;
		if(justRobots)
			if(tick<robotMaxPower) endGame=false;
			for (int i = 0; i < agentList.size(); i++) {
				if(agentList.get(i).x!=-1)
				{
					if(!justRobots&&!(agentList.get(i) instanceof Robot))
						endGame=false;
					agentList.get(i).step();
				}
		}
		tick++;
		if(endGame){
			this.stop();
			agentJFrame.dispose();
		} else {
			agentJFrame.repaint();
			DataLogger.updateChart();
		}
	}


	class MainAction extends BasicAction {

		public void execute() {
			// shuffle agents
			SimUtilities.shuffle(agentList);

			// iterate through all agents
			for(int i = 0; i < agentList.size(); i++) {
				agentList.get(i).step();
			}
		}
	}


	public int getRadioCommInterval() {
		return radioCommInterval;
	}

	public void setRadioCommInterval(int radioCommInterval) {
		this.radioCommInterval = radioCommInterval;
	}

	public int getVisionRange() {
		return visionRange;
	}

	public void setVisionRange(int visionRange) {
		this.visionRange = visionRange;
	}

	public int getRobotMaxPower() {
		return robotMaxPower;
	}

	public void setRobotMaxPower(int robotMaxPower) {
		this.robotMaxPower = robotMaxPower;
	}

	public int getRadioMaxRange() {
		return radioMaxRange;
	}

	public void setRadioMaxRange(int radioMaxRange) {
		this.radioMaxRange = radioMaxRange;
	}

	public int getNumRobots() {
		return numRobots;
	}

	public void setNumRobots(int numRobots) {
		this.numRobots = numRobots;
	}

	public int getNumSoldiers() {
		return numSoldiers;
	}

	public void setNumSoldiers(int numSoldiers) {
		this.numSoldiers = numSoldiers;
	}

	public int getNumCaptains() {
		return numCaptains;
	}

	public void setNumCaptains(int numCaptains) {
		this.numCaptains = numCaptains;
	}

	public int getNumAgents() {
		return numCaptains + numRobots + numSoldiers;
	}

	public ForestModel() {
	}

	public String getName() {
		return "Forest";
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getRadioMaxPower() {
		return radioMaxPower;
	}


	public void setRadioMaxPower(int radioMaxPower) {
		this.radioMaxPower = radioMaxPower;
	}
	
	public int getCellMaxPower() {
		return cellMaxPower;
	}

	public void setCellMaxPower(int cellMaxPower) {
		this.cellMaxPower = cellMaxPower;
	}
	
	public Boolean getUseIcons() {
		return useIcons;
	}

	public void setUseIcons(Boolean b) {
		useIcons = b;
	}

	public Boolean getEfficientComm() {
		return efficientComm;
	}

	public void setEfficientComm(Boolean efficientComm) {
		this.efficientComm = efficientComm;
	}

	public Boolean getEfficientHuman() {
		return efficientHuman;
	}

	public void setEfficientHuman(Boolean efficientHuman) {
		this.efficientHuman = efficientHuman;
	}

	public Boolean getDebugMode() {
		return debugMode;
	}

	public void setDebugMode(Boolean debugMode) {
		this.debugMode = debugMode;
	}

	public int getMapFile() {
		return mapFile;
	}

	public void setMapFile(int mapFile) {
		this.mapFile = mapFile;
	}

	public static void main(String[] args) {

		SimInit init = new SimInit();
		init.loadModel(new ForestModel(), null, false);
	}


}
