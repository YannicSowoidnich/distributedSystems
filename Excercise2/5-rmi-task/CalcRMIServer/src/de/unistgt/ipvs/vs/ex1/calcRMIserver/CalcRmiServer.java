package de.unistgt.ipvs.vs.ex1.calcRMIserver;

import de.unistgt.ipvs.vs.ex1.calculation.ICalculation;
import de.unistgt.ipvs.vs.ex1.calculationImpl.CalculationImplFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;

/**
 * Implement the run-method of this class to complete
 * the assignment. You may also add some fields or methods.
 */
public class CalcRmiServer extends Thread {
	private String regHost;
	private String objName;
	private CalculationImplFactory calcFactory;
	
	public CalcRmiServer(String regHost, String objName) {
		this.regHost = regHost;
		this.objName = objName;
		calcFactory = new CalculationImplFactory();
		start();
	}
	
	@Override
	public void run() {
		if (regHost == null || objName == null) {
			System.err.println("<registryHost> and/or <objectName> not set!");
			return;
		}
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			ICalculation calcObject = calcFactory.getSession();
			RemoteServer.setLog(System.out);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(regHost + "/" + objName, calcObject);

			System.out.println("ICalculation bound");
		} catch (Exception e) {
			System.err.println("run exception:");
			e.printStackTrace();
		}
	}

}
