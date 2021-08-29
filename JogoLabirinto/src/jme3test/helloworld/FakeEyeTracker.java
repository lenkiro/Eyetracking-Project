package jme3test.helloworld;

import java.io.*;
import java.net.*;
import java.awt.*;

public class FakeEyeTracker implements Runnable {

	public void run(){

		try {
			ServerSocket serverSocket = new ServerSocket(3000);
			Socket clientSocket = serverSocket.accept();
			
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			
			for(int count  = 0; ; count++){
				
				Point mouse = MouseInfo.getPointerInfo().getLocation();

				mouse.x += (int) (-64 + 128 * Math.random());
				mouse.y += (int) (-64 + 128 * Math.random());
				
				if(mouse != null){
									
					out.writeBoolean(true);
					out.writeLong(System.currentTimeMillis());					
					out.writeInt(mouse.x);
					out.writeInt(mouse.y);
				}
				
				Thread.sleep(1);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
        
        void exit(){
            System.exit(0);
        }

	public static void main(String [] args){

		new Thread(new FakeEyeTracker()).start();
	}
}
