package de.unistgt.ipvs.vs.ex4.distributed_debugging_algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//you are not allowed to change this class structure. However, you can add local functions!
public class Monitor implements Runnable {

	/**
	 * The state consists on vector timestamp and local variables of each process.
	 * In this class, a state is represented by messages (events) indices of each process.
	 * The message contains a local variable and vector timestamp, see Message class.
	 * E.g. if state.processesMessagesCurrentIndex contains {1, 2}, it means that the state contains the second message (event)
	 * from process1 and the third message (event) from process2  
	 */
	private class State {
		//Message indices of each process
		private int[] processesMessagesCurrentIndex;

		public State(int numberOfProcesses) {
			processesMessagesCurrentIndex = new int[numberOfProcesses];
		}

		public State(int[] processesMessagesCurrentIndex) {
			this.processesMessagesCurrentIndex = processesMessagesCurrentIndex;
		}

		{
			processesMessagesCurrentIndex = new int[numberOfProcesses];
		}

		public int[] getProcessesMessagesCurrentIndex() {
			return processesMessagesCurrentIndex;
		}

		public int getProcessMessageCurrentIndex(int processId) {
			return this.processesMessagesCurrentIndex[processId];
		}

		@Override
		public boolean equals(Object other) {
			State otherState = (State) other;

			// Iterate over processesMessagesCurrentIndex array
			for (int i = 0; i < numberOfProcesses; i++)
				if (this.processesMessagesCurrentIndex[i] != otherState.processesMessagesCurrentIndex[i])
					return false;

			return true;
		}
	}

	private int numberOfProcesses;
	private final int numberOfPredicates = 4;

	//Count of still running processes. The monitor starts to check predicates (build lattice) whenever runningProcesses equals zero.
	private AtomicInteger runningProcesses;
	
	/* 
	 * Q1, Q2, ..., Qn
	 * It represents the processes' queue. See distributed debugging algorithm from global state lecture!
	 */
	 private List<List<Message>> processesMessages;

	//list of states
	private LinkedList<State> states;

	//The predicates checking results
	private boolean[] possiblyTruePredicatesIndex;
	private boolean[] definitelyTruePredicatesIndex;

	public Monitor(int numberOfProcesses) {
		this.numberOfProcesses = numberOfProcesses;

		runningProcesses = new AtomicInteger();
		runningProcesses.set(numberOfProcesses);

		processesMessages = new ArrayList<>(numberOfProcesses);
		for (int i = 0; i < numberOfProcesses; i++) {
			List<Message> tempList = new ArrayList<>();
			processesMessages.add(i, tempList);
		}

		states = new LinkedList<>();
	
		// there are three predicates
		possiblyTruePredicatesIndex = new boolean[numberOfPredicates];
		for (int i = 0; i < numberOfPredicates; i++)
			possiblyTruePredicatesIndex[i] = false;

		definitelyTruePredicatesIndex = new boolean[numberOfPredicates];
		for (int i = 0; i < numberOfPredicates; i++)
			definitelyTruePredicatesIndex[i] = false;
	}

	/**
	 * receive messages (events) from processes
	 * 
	 * @param processId
	 * @param message
	 */
	public void receiveMessage(int processId, Message message) {
		synchronized (processesMessages) {
			processesMessages.get(processId).add(message);
		}
	}

	/**
	 * Whenever a process terminates, it notifies the Monitor. Monitor only starts to
	 * build lattice and check predicates when all processes terminate
	 * 
	 * @param processId
	 */
	public void processTerminated(int processId) {
		runningProcesses.decrementAndGet();
	}

	public boolean[] getPossiblyTruePredicatesIndex() {
		return possiblyTruePredicatesIndex;
	}

	public boolean[] getDefinitelyTruePredicatesIndex() {
		return definitelyTruePredicatesIndex;
	}

	@Override
	public void run() {
		// wait till all processes terminate
		while (runningProcesses.get() != 0)
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		// create initial state (S00)
		State initialState = new State(numberOfProcesses);

		// check predicates for part (b)
		for (int predicateNo = 0; predicateNo < 3; predicateNo++) {
			states.add(initialState); // add the initial state to states list
			buildLattice(predicateNo, 0, 1);
			states.clear();

		}

		if (numberOfProcesses > 2) {
			int predicateNo = 3;
			states.add(initialState); // add the initial state to states list
			buildLattice(predicateNo, 0, 2);
			states.clear();
		}

	}

	public void buildLattice(int predicateNo, int process_i_id, int process_j_id) {
		// TODO
		/*
		 * - implement this function to build the lattice of consistent states.
		 * - The goal of building the lattice is to check a predicate if it is
		 * possibly or/and definitely True. Thus your function should stop
		 * whenever the predicate evaluates to both possibly and definitely
		 * True. 
		 * NOTE1: this function should call findReachableStates and checkPredicate functions. 
		 * NOTE2: predicateNo, process_i_id and process_j_id are described in checkPredicate function.
		 */
	}

	/**
	 * find all reachable states starting from a given state
	 * 
	 * @param state
	 * @return list of all reachable states
	 */
	private LinkedList<State> findReachableStates(State state) {
		// TODO
		/*
		 * Given a state, implement a code that find all reachable states. The
		 * function should return a list of all reachable states
		 * 
		 */
	}

	/**
	 * - check a predicate and return true if the predicate is **definitely** True.
	 * - To simplify the code, we check the predicates only on local variables of two processes.
	 *   Therefore, process_i_Id and process_j_id refer to the processes that have the local variables in the predicate.
	 *   The predicate0, predicate1 and predicate2 contain the local variables from process1 and process2. whilst the predicate3
	 *   contains the local variables from process1 and process3.
	 * 
	 * @param predicateNo: which predicate to validate
	 * @return true if predicate is definitely true else return false
	 */
	private boolean checkPredicate(int predicateNo, int process_i_Id, int process_j_id) {
		// TODO
		/*
		 * - check if a predicate is possibly and/or definitely true. 
		 * - iterate over all reachable states to check the predicates.
		 * NOTE: you can use the following code 
		 * switch (predicateNo) 
		 * {
		 *  case 0: predicate = Predicate.predicate0(process_i_Message, process_j_Message);
		 *   break;
		 * case 1: ... 
		 * }
		 */

	}

}
