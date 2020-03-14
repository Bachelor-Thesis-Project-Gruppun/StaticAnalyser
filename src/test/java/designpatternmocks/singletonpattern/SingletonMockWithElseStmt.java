package designpatternmocks.singletonpattern;

/**
 * Mock class for the SingletonPattern, contains a private static field of SingletonMock, a private
 * constructor aswell as a public static method which calls the constructor, constructor is called
 * regardless of if the instance variable is null or not so the class should fail tests.
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class SingletonMockWithElseStmt {

    //  Private Static field of SingletonMock, should pass first predicate
    private static SingletonMockWithElseStmt instance;

    /**
     * Private constructor, should pass second predicate
     */
    private SingletonMockWithElseStmt() {
        System.out.println("Constructor called");
    }

    /**
     * private static method getInstance which calls the constructor, should fail if called
     *
     * @return The Singleton instance of SingletonMock
     */
    public static SingletonMockWithElseStmt getInstance() {
        if (instance == null) {
            instance = new SingletonMockWithElseStmt();
        } else {
            return new SingletonMockWithElseStmt();
        }
        return instance;
    }

}
