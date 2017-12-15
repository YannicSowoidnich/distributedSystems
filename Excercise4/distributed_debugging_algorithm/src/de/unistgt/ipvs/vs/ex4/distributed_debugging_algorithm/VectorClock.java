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

	// E4 a)
	/**
	 * Increments the local clock component.
	 */
	public void increment() {
		vectorClock[processId] += 1;
	}

	// E4 a)
	/**
	 * Returns the Vector Clock.
	 * 
	 * @return vectorClock
	 */
	public int[] get() {
		return vectorClock;
	}

	// E4 a)
	/**
	 * Updates the local Vector Clock with information from another Vector
	 * Clock.
	 * 
	 * @param other
	 *            Vector Clock
	 */
	public void update(VectorClock other) {
		for (int i = 0; i < numberOfProcesses; i++) {
			if (vectorClock[i] < other.get()[i])
				vectorClock[i] = other.get()[i];
		}
	}

	// E4 a)
	/**
	 * Checks if a state is consistent regarding two vector clocks.
	 * 
	 * @param otherProcessId
	 * @param other
	 *            Vector Clock
	 * @return consistency
	 */
	public boolean checkConsistency(int otherProcessId, VectorClock other) {
		if (vectorClock[processId] < other.get()[processId]
				|| vectorClock[otherProcessId] > other.get()[otherProcessId])
			return false;
		else
			return true;
	}

}
