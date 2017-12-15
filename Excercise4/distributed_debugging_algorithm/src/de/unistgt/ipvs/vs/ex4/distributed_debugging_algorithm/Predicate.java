package de.unistgt.ipvs.vs.ex4.distributed_debugging_algorithm;

//you are not allowed to change this class structure
public class Predicate {

	static public boolean predicate0(Message process_i_Message,
			Message process_j_Message) {
		if (Math.abs(3 * process_i_Message.getLocalVariable() - 4
				* process_j_Message.getLocalVariable()) == 25)
			return true;
		else
			return false;
	}

	// E4 b)
	/**
	 * Implementation of predicate1 x1 - x2 = 15.
	 * 
	 * @param process_i_Message
	 * @param process_j_Message
	 * @return evaluation of predicate1
	 */
	static public boolean predicate1(Message process_i_Message,
			Message process_j_Message) {
		return (process_i_Message.getLocalVariable()
				- process_j_Message.getLocalVariable() == 15);
	}

	// E4 b)
	/**
	 * Implementation of predicate2 x1 + x2 = 30.
	 * 
	 * @param process_i_Message
	 * @param process_j_Message
	 * @return evaluation of predicate2
	 */
	static public boolean predicate2(Message process_i_Message,
			Message process_j_Message) {
		return (process_i_Message.getLocalVariable()
				+ process_j_Message.getLocalVariable() == 30);
	}

	// E4 c)
	/**
	 * Implementation of predicate3 x1 - x3 = 8.
	 * 
	 * @param process_i_Message
	 * @param process_j_Message
	 * @return evaluation of predicate3
	 */
	static public boolean predicate3(Message process_i_Message,
			Message process_j_Message) {
		return (process_i_Message.getLocalVariable()
				- process_j_Message.getLocalVariable() == 8);
	}
}
