package de.unistgt.ipvs.vs.ex1.calcSocketClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Implement the connectTo-, disconnect-, and calculate-method of this class
 * as necessary to complete the assignment. You may also add some fields or methods.
 */
public class CalcSocketClient {
	private int    rcvdOKs;		// --> Number of   valid message contents
	private int    rcvdErs;		// --> Number of invalid message contents
	private int    calcRes;		// --> Calculation result (cf. 'RES')
	private Socket socket;
	private BufferedReader sockin;
	private PrintWriter sockout;

	public CalcSocketClient() {
		this.rcvdOKs   = 0;
		this.rcvdErs   = 0;
		this.calcRes   = 0;
		this.socket = null;
	}
	
	// Do not change this method ..
	public int getRcvdOKs() {
		return rcvdOKs;
	}

	// Do not change this method ..
	public int getRcvdErs() {
		return rcvdErs;
	}

	// Do not change this method ..
	public int getCalcRes() {
		return calcRes;
	}

	public boolean connectTo(String srvIP, int srvPort) {
		String messageFromServer;
		try {
			socket = new Socket("CalcSocketServer", 12345);
			sockin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sockout = new PrintWriter(socket.getOutputStream(), true);
			// checks if connection to server is successful by checking if first message from server is the 'RDY' message
			messageFromServer = sockin.readLine();
			if (messageFromServer.equals("<08:RDY>")) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("Could not connect to server!");
			System.err.println(e);
		}
		return false;
	}

	public boolean disconnect() {
		try {
			if (socket != null) {
				sockin.close();
				sockout.close();
				socket.close();
				return true;
			}
		} catch (IOException e) {
			System.out.println("Could not disconnect!");
			System.err.println(e);
		}
		return false;
	}

	public boolean calculate(String request) {
		String answer;
		try {
			sockout.println(request);
			while ((answer = sockin.readLine()) != null) {
				// if server returns an "RES" the current result is returned
				if (answer.contains("RES")) {
					// replaces all non-digits with blanks: the remaining string contains only digits
					calcRes = Integer.parseInt(answer.replaceAll("[\\D]", ""));
					// if server returns an "ERR" the corresponding variable is incremented
				}  else if (answer.contains("ERR")) {
					rcvdErs++;
					// if ">" is reached the result is returned, so only then the calculation is successful
				} else if (answer.equals("FIN")) {
					return true;
					// for each received message the server sends a response with the content "OK"
				} else if (answer.contains("OK")) {
					rcvdOKs++;
				}
			}
		} catch (Exception e) {
			System.out.println("Could not interact with server!");
			System.err.println(e);
		}
		return false;
	}
}
