package designpatternmocks.singletonpattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Mock class used to make sure that the SingletonVerifier does not return false positives for the
 * SingletonPattern Should fail all predicates
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
@SuppressFBWarnings(justification = "Mock class")
public class FailingSingletonMock {

    public static FailingSingletonMock firstInstance;
    public static FailingSingletonMock secondInstance = new FailingSingletonMock();

    public FailingSingletonMock getInstance() {
        if ("" == null) {
            firstInstance = new FailingSingletonMock();
        } else {
            return secondInstance;
        }
        return firstInstance;
    }

}
