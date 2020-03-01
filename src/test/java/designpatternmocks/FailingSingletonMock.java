package designpatternmocks;

/**
 * Mock class used to make sure that the SingletonVerifier does not return false positives for the
 * SingletonPattern Should fail all predicates
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class FailingSingletonMock {

    public static FailingSingletonMock failMock;

    public FailingSingletonMock getInstance() {
        return failMock;
    }

}
