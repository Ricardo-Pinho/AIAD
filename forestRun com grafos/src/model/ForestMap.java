package model;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class ForestMap {
	private int[][] map;
	private String f;
	
	public ForestMap(String filename){
		{
			this.f = filename;
			try{
				FileInputStream fstream = new FileInputStream(f);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				
				strLine = br.readLine();
				String[] dimensions = strLine.split(";");
				map = new int[Integer.valueOf(dimensions[0])][Integer.valueOf(dimensions[1])];
				
				for (int i = 0; i < map.length; i++) {
					strLine = br.readLine();
					if(strLine == null || strLine.length() != map[i].length){
						System.err.println("FILE: Wrong dimensions.");
						break;
					}
					for (int j = 0; j < strLine.length(); j++) {
						map[j][i] = (int) strLine.charAt(j) - 48;
					}
					strLine = null;
				}
				in.close();
			} catch (Exception e){
				System.err.println("FILE STREAM ERROR: " + e.getMessage());
			}
		}
	}
	
	public void print(){
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				System.out.print(map[j][i]);
			}
			System.out.println();
		}
	}

	public int[][] getMap() {
		return map;
	}

	public void setMap(int x, int y, int value) {
		map[x][y] = value;
	}

	public int getHeight() {
		return map.length;
	}

	public int getWidth() {
		return map[0].length;
	}
	
}
