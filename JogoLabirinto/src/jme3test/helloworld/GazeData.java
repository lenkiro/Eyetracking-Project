package jme3test.helloworld;

public class GazeData {

	private boolean valid;
	private long timestamp;
	private int x, y;
			
	public GazeData(boolean valid, long time, int x, int y) {

		this.valid = true;
		this.timestamp = time;		
		this.x = x;
		this.y = y;
	}

	public boolean isValid(){

		return valid;
	}
		
	public long getTimestamp() {

		return timestamp;
	}
	
	public int getX() {

		return x;
	}

	public int getY(){

		return y;
	}
			
	public GazeData getCopy() {
	
		return new GazeData(isValid(), getTimestamp(), getX(), getY());
	}
}
