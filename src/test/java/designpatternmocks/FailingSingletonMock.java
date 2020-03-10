package designpatternmocks;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Mock class used to make sure that the SingletonVerifier does not return false positives for the
 * SingletonPattern Should fail all predicates
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
@SuppressFBWarnings(justification = "Mock class")
public class FailingSingletonMock {

    public static FailingSingletonMock failMock;

    public FailingSingletonMock getInstance() {
        if ("" == null) {
            failMock = new FailingSingletonMock();
        }
        return failMock;
    }

}
