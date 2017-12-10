package de.unistgt.ipvs.vs.ex4.distributed_debugging_algorithm;

//you are not allowed to change this class structure
public class VectorClock {

	protected int[] vectorClock;
	private int processId;
	private int numberOfProcesses;

	public VectorClock(int processId, int numberOfProcesses) {
		vectorClock = new int[numberOfProcesses];
		this.numberOfProcesses = numberOfProcesses;
		this.processId = processId;
	}

	VectorClock(VectorClock other) {
		vectorClock = other.vectorClock.clone();
		processId = other.processId;
		numberOfProcesses = other.numberOfProcesses;

	}

	public void increment() {
		// TODO
		/*
		 * Complete a code to increment the local clock component  
		 */

	}

	public int[] get() {
		// TODO 
		// Complete a code to return the vectorClock value

	}

	public void update(VectorClock other) {
		// TODO
		/*
		 * Implement Supermum operation
		 */

	}

	public boolean checkConsistency(int otherProcessId, VectorClock other) {
		//TODO
		/*
		 * Implement a code to check if a state is consist regarding two vector clocks (i.e. this and other). 
		 * See slide 41 from global state lecture.
		 */

		
	}

}
