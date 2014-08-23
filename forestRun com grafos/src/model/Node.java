package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

public class Node {

	public int x;
	public int y;
	public boolean visited=false; // flag to track the already visited node
	public LinkedList<Node> adjacentNodes = new LinkedList<Node>(); // adjacency list

	public Node(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Node(Node n){
		this.x = n.x;
		this.y = n.y;
		this.adjacentNodes = n.adjacentNodes;
	}
	
	public Node(){}
	
	public int getX()
	{
		return this.x;
	}
	
	public int getY()
	{
		return this.y;
	}
	
	public void addAdjacentNode(final Node node){
		adjacentNodes.add(node);
	}

	public int getPathLength() {
		return 0;
	}

}
