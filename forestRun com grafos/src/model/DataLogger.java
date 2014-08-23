package model;

import java.util.ArrayList;
import java.io.*;

import uchicago.src.sim.analysis.Plot;

public class DataLogger {

	private ArrayList<Long> goalTimes;
	static int numOfAgentsToTrack;
	public static boolean debugMode;
	public static int mapFile;
	public int numRobots;
	public int numSoldiers;
	public int numCaptains;
	public int radioCommInterval;
	public int radioMaxRange;
	public int visionRange;
	public int robotMaxPower;
	public int radioMaxPower;
	public int cellMaxPower;
	public boolean efficientComm;
	public boolean efficientHuman;
	public boolean withGraphics;
	public boolean useIcons;
	
	private static Plot aPlot;


	public DataLogger(int n, int numRobots,int numSoldiers,
			int numCaptains,int radioCommInterval,int radioMaxRange,int visionRange,
			int robotMaxPower,int radioMaxPower,int cellMaxPower,
			boolean efficientComm,boolean efficientHuman,
			boolean withGraphics,boolean useIcons) {
		goalTimes = new ArrayList<Long>();
		numOfAgentsToTrack = n;
		this.numRobots=numRobots;
		this.numSoldiers=numSoldiers;
		this.numCaptains=numCaptains;
		this.radioCommInterval=radioCommInterval;
		this.radioMaxRange=radioMaxRange;
		this.visionRange=visionRange;
		this.robotMaxPower=robotMaxPower;
		this.radioMaxPower=radioMaxPower;
		this.cellMaxPower=cellMaxPower;
		this.efficientComm=efficientComm;
		this.efficientHuman=efficientHuman;
		this.withGraphics=withGraphics;
		this.useIcons=useIcons;
	}

	public void addGoalTime(long time) {
		goalTimes.add(time);
		numOfAgentsToTrack--;
		if(debugMode&&numOfAgentsToTrack>=0)
			System.out.println("Number of Agents Left: "+numOfAgentsToTrack);
		if (numOfAgentsToTrack == 0) {
			int sum = 0;
			for (Long l : goalTimes) {
				sum += l;
			}
			sum = sum/goalTimes.size();
			System.out.println("Average time out: " + sum);
			saveToFile(sum);
			System.out.println("Saved log to file");
		}
	}

	public void saveToFile(int sum) {
		String numRep=readIterationNumOfFile();
		int IterationNum=1;
		if(numRep=="")
		{
			writeIterationNumOfFile(1);
		}
		else{
			IterationNum=Integer.valueOf(numRep);
			System.out.println(IterationNum);
			IterationNum++;
			System.out.println(IterationNum);
			writeIterationNumOfFile(IterationNum);

		}
		try{
			// Create file 
			FileWriter fstream = new FileWriter("log.txt", true); //true == append
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("\n--------------------------------------------------------\n");
			out.write("Iteration#"+IterationNum+":\n");
			out.write(sum + "\n");
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	private String readIterationNumOfFile(){
		String lineData="";
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream("log.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			//Read File Line By Line
			lineData=br.readLine();
			lineData=br.readLine();
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			return lineData;
		}
		return lineData;
	}

	private String readRestOfFile(){
		String lineData="";
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream("log.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int inc=0;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				if(inc>17) lineData=lineData+strLine+"\n";
				else
					inc++;
			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			return lineData;
		}
		return lineData;
	}

	private void writeIterationNumOfFile(int num){
		// Create file 
		try{
			String rest=readRestOfFile();
			FileWriter fstream = new FileWriter("log.txt", false); //true == append
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Number of Iterations\n");
			out.write(num+"\n");
			out.write("Parameters:\n");
			out.write("\tRadioCommInterval: "+radioCommInterval+"\n");
			out.write("\tMapFile: "+mapFile+"\n");
			out.write("\tNumCaptains: "+numCaptains+"\n");
			out.write("\tRadioMaxRange: "+radioMaxRange+"\n");
			out.write("\tNumRobots: "+numRobots+"\n");
			out.write("\tRobotMaxPower: "+robotMaxPower+"\n");
			out.write("\tRadioMaxPower: "+radioMaxPower+"\n");
			out.write("\tVisionRange: "+visionRange+"\n");
			out.write("\tEfficientHuman: "+efficientHuman+"\n");
			out.write("\tEfficientComm: "+efficientComm+"\n");
			out.write("\tUseIcons: "+useIcons+"\n");
			out.write("\tNumSoldiers: "+numSoldiers+"\n");
			out.write("\tCellMaxPower: "+cellMaxPower+"\n");
			out.write("\tDebugMode: "+debugMode+"\n");
			out.write("\tWithGraphics: "+withGraphics+"\n");
			out.write(rest);
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void makeChart() {
		aPlot = new Plot("Forest Run");
		aPlot.setXRange(0, 3000);
		aPlot.setYRange(0, 80);
		aPlot.display();
		aPlot.setConnected(true);
	}
	
	public static void updateChart() {
		aPlot.plotPoint(ForestModel.tick, numOfAgentsToTrack, 1);
		aPlot.updateGraph();
		//aPlot.fillPlot();
	}
}
