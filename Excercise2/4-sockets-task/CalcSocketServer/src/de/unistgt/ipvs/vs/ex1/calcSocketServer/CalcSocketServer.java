package de.unistgt.ipvs.vs.ex1.calcSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Extend the run-method of this class as necessary to complete the assignment.
 * You may also add some fields, methods, or further classes.
 */
public class CalcSocketServer extends Thread{
	private ServerSocket srvSocket;
	private int port;
	clientHandler server;
	
	public CalcSocketServer(int port) {
		this.srvSocket = null;
		this.port      = port;
	}
	
	@Override
	public void interrupt() {
		try {
			if (srvSocket != null) srvSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Start listening server socket ..
		try {
			srvSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port:"+port+".");
            System.exit(-1);
        }
		// wait for client connection
		while(true) {
			clientHandler server;
			try{
				server = new clientHandler(srvSocket.accept(), srvSocket);
				server.start();
			} catch (IOException e) {
				System.out.println("Accept failed: "+port+".");
			    System.exit(-1);
			}
		}
	}
	
	@Override
	protected void finalize(){
		// cleanup
		try{
			srvSocket.close();
		} catch (IOException e) {
			System.out.println("Could not close socket");
		    System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		CalcSocketServer calcSocketServer = new CalcSocketServer(12345);
		calcSocketServer.start();
	}

	class clientHandler extends Thread {
	
		private ServerSocket srvSocket;
		private Socket clientSocket = null;
		private BufferedReader in;
		private PrintWriter out;
		private String input, output;
	
		public clientHandler(Socket clientSocket, ServerSocket srvSocket) {
			this.srvSocket = srvSocket;
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void interrupt() {
			try {
				if (srvSocket != null) srvSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		@Override
		public void run() {
			// Read from the streams
			try{
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				System.out.println("Couldn't read from client");
				System.exit(-1);
			}  
			while(true){
				try{
					// TODO read clients msg, parse it
					input = in.readLine();
			        // TODO give correct answer
			        out.println(output);
				} catch (IOException e) {
					System.out.println("Read failed");
			        System.exit(-1);
				}
			}
		}		
	}
}
