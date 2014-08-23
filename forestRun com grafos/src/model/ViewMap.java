package model;

import java.util.ArrayList;

public class ViewMap {
	public int tick;
	public ArrayList<ForestHeat> forestmap;
	
	public ViewMap(int x, int y){
		forestmap = new ArrayList<ForestHeat>();
		for(int i=0; i<x;i++)
		{
			for(int j=0;j<y;j++)
			{
				forestmap.add(new ForestHeat(j,i));
			}
		}
	}
}
