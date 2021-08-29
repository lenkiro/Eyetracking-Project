package jme3test.helloworld;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class GazeTrackerClient {

	private static final int HISTORY_MAX_SIZE = 20;
	private static final int DISTANCE_THRESHOLD = 200;
	
	private Socket socket;
	private DataInputStream in;

	private boolean active;
	private boolean filtered;

	private List<GazeData> rawHistory;
	
	public GazeTrackerClient(String host, int port, boolean filtered){

		this.active = true;
		this.filtered = filtered;

		this.rawHistory = new ArrayList<>();
		
		try {
			socket = new Socket(host, port);
			in = new DataInputStream(socket.getInputStream());
		} 
		catch (UnknownHostException e) {

			e.printStackTrace();
			System.out.println("exiting...");
			System.exit(1);
		} 
		catch (IOException e) {

			e.printStackTrace();
			System.out.println("exiting...");
			System.exit(1);
		}
	}

	public GazeData readGazeData() throws IOException {

		return filtered ? readFilteredGazePoint() : readRawGazePoint(); 
	}
	
	private GazeData readRawGazePoint() throws IOException{
			
		return new GazeData(in.readBoolean(), in.readLong(), in.readInt(), in.readInt());
	}
	
	private GazeData getAveragePoint(){
		
		double sumX, sumY;
		
		if(rawHistory.size() == 0) return new GazeData(false, 0, 0, 0);
		
		sumX = sumY = 0;
		
		for(GazeData p : rawHistory){
			sumX += p.getX();
			sumY += p.getY();
		}
		
		sumX = sumX / rawHistory.size();
		sumY = sumY / rawHistory.size();
		
		return new GazeData(true, System.currentTimeMillis(), (int) Math.round(sumX), (int) Math.round(sumY));
	}
	
	private boolean veryFar(GazeData p1, GazeData p2){

		Point a = new Point(p1.getX(), p1.getY());
		Point b = new Point(p2.getX(), p2.getY());
		
		return (a.distance(b) > DISTANCE_THRESHOLD) ? true : false;
	}
	
	private GazeData readFilteredGazePoint() throws IOException{

		GazeData data, lastInHistory, lastAverage; 

		data = readRawGazePoint();
				
		if(data.isValid()){
					
			if(rawHistory.size() > 0){
				
				// we alredy have at least one point in history...
				
				lastInHistory = rawHistory.get(rawHistory.size() - 1);
				lastAverage = getAveragePoint();
			
				if(veryFar(lastAverage, lastInHistory) && veryFar(lastAverage, data)){ 
					
					// last two points are far from the current average, indicating a large eye movement
										
					rawHistory.clear();
					//rawHistory.add(lastAverage); // <--- to make the change not too much abrupt
					rawHistory.add(lastInHistory);
				}
				else{
					// make room for the current gaze sample in the history list
					
					while(rawHistory.size() >= HISTORY_MAX_SIZE){
						rawHistory.remove(0);
					}
				}
			}
			
			rawHistory.add(data);
			return getAveragePoint();
		}

		// if current raw point is null, then we also return null.
		return data;
	}
}
