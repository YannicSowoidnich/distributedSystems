package de.unistgt.ipvs.vs.ex1.calcRMIclient;

import de.unistgt.ipvs.vs.ex1.calculation.ICalculation;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;

/**
 * Implement the getCalcRes-, init-, and calculate-method of this class as
 * necessary to complete the assignment. You may also add some fields or methods.
 */
public class CalcRmiClient {
	private ICalculation calc = null;

	public CalcRmiClient() {
		this.calc = null;
	}

	public int getCalcRes() {
		try {
			calc.getResult();
		} catch (Exception e) {
			System.err.println("getCalcRes exception:");
			e.printStackTrace();
		}
		return 0;
	}

	public boolean init(String url) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			Registry registry = LocateRegistry.getRegistry();
			calc = (ICalculation) registry.lookup(url);
			return true;
		} catch (Exception e) {
			System.err.println("init exception:");
			e.printStackTrace();
		}
		return false;
	}

	public boolean calculate(CalculationMode calcMode, Collection<Integer> numbers) {
		try {
			switch (calcMode) {
				case ADD:
					for (Integer number : numbers) {
						calc.add(number);
					}
					break;
				case SUB:
					for (Integer number : numbers) {
						calc.subtract(number);
					}
					break;
				case MUL:
					for (Integer number : numbers) {
						calc.multiply(number);
					}
					break;
				default:
					return false;
			}
		} catch (Exception e) {
			System.err.println("calculate exception:");
			e.printStackTrace();
		}
		return true;
	}
}
