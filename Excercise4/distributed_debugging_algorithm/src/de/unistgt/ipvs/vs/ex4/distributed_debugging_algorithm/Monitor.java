package de.unistgt.ipvs.vs.ex4.distributed_debugging_algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

//you are not allowed to change this class structure. However, you can add local functions!
public class Monitor implements Runnable {

	/**
	 * The state consists on vector timestamp and local variables of each
	 * process. In this class, a state is represented by messages (events)
	 * indices of each process. The message contains a local variable and vector
	 * timestamp, see Message class. E.g. if state.processesMessagesCurrentIndex
	 * contains {1, 2}, it means that the state contains the second message
	 * (event) from process1 and the third message (event) from process2
	 */
	private class State {
		// Message indices of each process
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

	// Count of still running processes. The monitor starts to check predicates
	// (build lattice) whenever runningProcesses equals zero.
	private AtomicInteger runningProcesses;

	/*
	 * Q1, Q2, ..., Qn It represents the processes' queue. See distributed
	 * debugging algorithm from global state lecture!
	 */
	private List<List<Message>> processesMessages;

	// list of states
	private LinkedList<State> states;

	// The predicates checking results
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
	 * Whenever a process terminates, it notifies the Monitor. Monitor only
	 * starts to build lattice and check predicates when all processes terminate
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

	// E4 b)
	/**
	 * Builds the lattice of consistent states and checks if a predicate is
	 * possibly or/and definitely true.
	 * 
	 * @param predicateNo
	 *            : which predicate to validate
	 * @param process_i_id
	 *            the id of process i
	 * @param process_j_id
	 *            the id of process j
	 */
	public void buildLattice(int predicateNo, int process_i_id, int process_j_id) {
		State root = states.getFirst(); // initial state (S00)
		Queue<State> statesQueue = new LinkedList<State>();
		statesQueue.add(root);
		while (!statesQueue.isEmpty()) {
			// current state
			State cur_s = statesQueue.remove();
			// get states which are reachable from current state
			LinkedList<State> reachables = new LinkedList<State>();
			reachables = findReachableStates(cur_s);
			for (State r : reachables) {
				// check if state is already contained
				boolean contains = false;
				for (State s : states) {
					if (Arrays.equals(r.getProcessesMessagesCurrentIndex(),
							s.getProcessesMessagesCurrentIndex())) {
						contains = true;
					}
				}
				if (!contains) {
					// adding state
					states.add(r); // using "states" to save lattice
					statesQueue.add(r);
				}
			}
		}

		/*
		 * To enable custom console outputs set outputs to (true). To disable
		 * custom console outputs set outputs to (false).
		 */
		// Console Outputs for the Lattice and the Predicates
		boolean outputs = false;
		if (outputs) {
			System.out.println();
			System.out.println("Predicate" + predicateNo);
			System.out.print("Lattice : ");
			for (State s : states) {
				System.out.print("(" + s.getProcessMessageCurrentIndex(0) + ","
						+ s.getProcessMessageCurrentIndex(1) + ","
						+ s.getProcessMessageCurrentIndex(2) + ")");
			}
		}

		// check predicates
		definitelyTruePredicatesIndex[predicateNo] = checkPredicate(
				predicateNo, process_i_id, process_j_id);

		// Console Outputs for the Predicates
		if (outputs) {
			System.out.println("Possibly True : "
					+ possiblyTruePredicatesIndex[predicateNo]);
			System.out.println("Definitely True : "
					+ definitelyTruePredicatesIndex[predicateNo]);
			System.out.println();
		}
	}

	// E4 b)
	/**
	 * Find all reachable states starting from a given state
	 * 
	 * @param state
	 *            from which reachable states are found
	 * @return list of all reachable states
	 */
	private LinkedList<State> findReachableStates(State state) {
		LinkedList<State> reachable = new LinkedList<State>();
		// current and consistent vector clocks
		LinkedList<VectorClock> vClocks = new LinkedList<VectorClock>();
		for (int i = 0; i < numberOfProcesses; i++) {
			int state_i_indx = state.getProcessMessageCurrentIndex(i);
			VectorClock cur_i_vc = processesMessages.get(i).get(state_i_indx)
					.getVectorClock();
			vClocks.add(i, cur_i_vc);
		}
		// try next step with each process
		for (int i = 0; i < numberOfProcesses; i++) {
			// check if process has no more lines
			int next_i_indx = state.getProcessMessageCurrentIndex(i) + 1;
			if (processesMessages.get(i).size() <= next_i_indx) {
				continue;
			}
			// vector clock of new state
			VectorClock next_i_vc = processesMessages.get(i).get(next_i_indx)
					.getVectorClock();
			// check if state will be consistent
			boolean consistent = true;
			for (int j = 0; j < numberOfProcesses; j++) {
				if (i == j) {
					continue;
				} else if (!next_i_vc.checkConsistency(j, vClocks.get(j))) {
					consistent = false;
				}
			}
			if (consistent) {
				// adds reachable state
				int[] new_indizes = state.getProcessesMessagesCurrentIndex()
						.clone();
				new_indizes[i] += 1; // only 1 process is in its next state
				State new_s = new State(new_indizes);
				reachable.add(new_s);
			}
		}
		return reachable;
	}

	// E4 b)
	/**
	 * - check a predicate and return true if the predicate is **definitely**
	 * True. - To simplify the code, we check the predicates only on local
	 * variables of two processes. Therefore, process_i_id and process_j_id
	 * refer to the processes that have the local variables in the predicate.
	 * The predicate0, predicate1 and predicate2 contain the local variables
	 * from process1 and process2. whilst the predicate3 contains the local
	 * variables from process1 and process3.
	 * 
	 * @param predicateNo
	 *            : which predicate to validate
	 * @return true if predicate is definitely true else return false
	 */
	private boolean checkPredicate(int predicateNo, int process_i_id,
			int process_j_id) {
		// Check if predicate is possibly true
		boolean predicate = false;
		for (State s : states) {
			// get messages to validate predicate for state
			int msg_i_indx = s.getProcessMessageCurrentIndex(process_i_id);
			int msg_j_indx = s.getProcessMessageCurrentIndex(process_j_id);
			Message process_i_Message = processesMessages.get(process_i_id)
					.get(msg_i_indx);
			Message process_j_Message = processesMessages.get(process_j_id)
					.get(msg_j_indx);
			// selects predicate
			switch (predicateNo) {
			case 0:
				predicate = Predicate.predicate0(process_i_Message,
						process_j_Message);
				break;
			case 1:
				predicate = Predicate.predicate1(process_i_Message,
						process_j_Message);
				break;
			case 2:
				predicate = Predicate.predicate2(process_i_Message,
						process_j_Message);
				break;
			case 3:
				predicate = Predicate.predicate3(process_i_Message,
						process_j_Message);
				break;
			default:
				System.out.println("predicateNo incorrect!");
			}
			if (predicate) {
				possiblyTruePredicatesIndex[predicateNo] = true;
				break;
			}
		}

		// Check if predicate is definitely true
		predicate = false;
		// states in current level
		LinkedList<State> lvl_States = new LinkedList<State>();
		// Check if predicate is true for initial state
		State init = states.getFirst();
		// get messages to validate predicate for state
		int msg_i_indx = init.getProcessMessageCurrentIndex(process_i_id);
		int msg_j_indx = init.getProcessMessageCurrentIndex(process_j_id);
		Message process_i_Message = processesMessages.get(process_i_id).get(
				msg_i_indx);
		Message process_j_Message = processesMessages.get(process_j_id).get(
				msg_j_indx);
		// selects predicate
		switch (predicateNo) {
		case 0:
			predicate = Predicate.predicate0(process_i_Message,
					process_j_Message);
			break;
		case 1:
			predicate = Predicate.predicate1(process_i_Message,
					process_j_Message);
			break;
		case 2:
			predicate = Predicate.predicate2(process_i_Message,
					process_j_Message);
			break;
		case 3:
			predicate = Predicate.predicate3(process_i_Message,
					process_j_Message);
			break;
		default:
			System.out.println("predicateNo incorrect!");
		}
		if (predicate) {
			return true;
		} else {
			lvl_States.add(init);
		}

		// loops over levels
		while (!lvl_States.isEmpty()) {
			LinkedList<State> reachable = new LinkedList<State>();
			// loops over states within a level
			for (State s : lvl_States) {
				LinkedList<State> s_reachable = findReachableStates(s);
				// loops over newly found states
				for (State new_r : s_reachable) {
					// Check if reachable State is new
					boolean contains = false;
					for (State r : reachable) {
						if (Arrays.equals(
								new_r.getProcessesMessagesCurrentIndex(),
								r.getProcessesMessagesCurrentIndex())) {
							contains = true;
						}
					}
					if (!contains) {
						// Check if state evaluates predicate to false
						predicate = false;
						// get messages to validate predicate for state
						msg_i_indx = new_r
								.getProcessMessageCurrentIndex(process_i_id);
						msg_j_indx = new_r
								.getProcessMessageCurrentIndex(process_j_id);
						process_i_Message = processesMessages.get(process_i_id)
								.get(msg_i_indx);
						process_j_Message = processesMessages.get(process_j_id)
								.get(msg_j_indx);
						// selects predicate
						switch (predicateNo) {
						case 0:
							predicate = Predicate.predicate0(process_i_Message,
									process_j_Message);
							break;
						case 1:
							predicate = Predicate.predicate1(process_i_Message,
									process_j_Message);
							break;
						case 2:
							predicate = Predicate.predicate2(process_i_Message,
									process_j_Message);
							break;
						case 3:
							predicate = Predicate.predicate3(process_i_Message,
									process_j_Message);
							break;
						default:
							System.out.println("predicateNo incorrect!");
						}
						if (!predicate) {
							// Add reachable State
							reachable.add(new_r);
						}
					}
				}
			}
			// update current level
			lvl_States.clear();
			for (State r : reachable) {
				lvl_States.add(r);
			}
		}
		return predicate;
	}
}
