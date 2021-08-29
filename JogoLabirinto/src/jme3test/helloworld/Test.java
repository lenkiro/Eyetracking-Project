package jme3test.helloworld;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class Test {
	
	// This class simulates a fake gaze tracker, generating
	// gaze data that corresponds to the actual mouse position.
	
	// the main purpose of this class is to test the filtering
	// implementation in the GazeTrackerClient class.
	
	// the main method instantiates a GazeTrackerClient object
	// that consumes and filters the fake gaze tracker data. 	
	
	public static void main(String [] args) throws IOException, InterruptedException{
		
		JFrame frame = new MyFrame();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIgnoreRepaint(true);
		frame.setVisible(true);
		frame.createBufferStrategy(2);
				
		GazeTrackerClient client = new GazeTrackerClient("localhost", 3000, true);
		
		while(frame.isVisible()){
			
			GazeData data = client.readGazeData();
			Point p = new Point(data.getX(), data.getY());			
			Point m = frame.getMousePosition();
			Point w = frame.getLocationOnScreen();
		
			Graphics g = frame.getBufferStrategy().getDrawGraphics();
			
			g.clearRect(0, 0, frame.getWidth(), frame.getHeight());
			g.setColor(Color.RED);
			g.drawLine(0, 0, frame.getWidth(), frame.getHeight());
			g.drawLine(frame.getWidth(), 0, 0, frame.getHeight());
			
			
			if(m != null){		
		
				g.setColor(Color.GREEN);
				g.drawLine(m.x - 10, m.y - 10, m.x + 10, m.y + 10);
				g.drawLine(m.x - 10, m.y + 10, m.x + 10, m.y - 10);
				g.drawArc(m.x - 64, m.y - 64, 128, 128, 0, 360);
			}
			
			if(p != null){

				p.x = p.x - w.x;
				p.y = p.y - w.y;
				
				g.setColor(Color.BLUE);
				g.drawLine(p.x - 10, p.y, p.x + 10, p.y);
				g.drawLine(p.x, p.y - 10, p.x, p.y + 10);
			}
				

			g.dispose();
			frame.getBufferStrategy().show();
			
			Thread.sleep(5);
		}
	}
	
}

@SuppressWarnings("serial")
class MyFrame extends JFrame{
	
	public void paint(Graphics g){
	
		g.setColor(Color.BLUE);
		g.drawLine(0, 0, getWidth(), getHeight());
		g.drawLine(getWidth(), 0, 0, getHeight());
	}
}
