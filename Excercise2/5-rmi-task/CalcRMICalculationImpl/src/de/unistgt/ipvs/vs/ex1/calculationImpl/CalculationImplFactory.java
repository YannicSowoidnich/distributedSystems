package de.unistgt.ipvs.vs.ex1.calculationImpl;

import de.unistgt.ipvs.vs.ex1.calculation.ICalculation;
import de.unistgt.ipvs.vs.ex1.calculation.ICalculationFactory;

/**
 * Change this class (implementation/signature/...) as necessary to complete the assignment.
 * You may also add some fields or methods.
 */
public class CalculationImplFactory implements ICalculationFactory {

    public ICalculation getSession() {
        try {
            ICalculation calcObj = new CalculationImpl();
            return calcObj;
        } catch (Exception e) {
            System.err.println("CalculationImplFactory exception:");
            e.printStackTrace();
        }
        return null;
    }
}