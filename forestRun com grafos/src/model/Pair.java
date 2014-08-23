package model;

public class Pair<T> {
	
	private T first;
	private T last;
	
	public Pair(T i, T j) {
		first = i;
		last = j;
	}
	public T getFirst() {
		return first;
	}
	public void setFirst(T first) {
		this.first = first;
	}
	public T getLast() {
		return last;
	}
	public void setLast(T last) {
		this.last = last;
	}
	

}
