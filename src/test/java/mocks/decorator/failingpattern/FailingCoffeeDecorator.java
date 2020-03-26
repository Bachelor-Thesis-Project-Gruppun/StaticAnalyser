package mocks.decorator.failingpattern;

/**
 * Should fail since class does not take an IFailingBeverageComponent in its constructor to set its
 * component variable to.
 */
public abstract class FailingCoffeeDecorator implements IFailingBeverageComponent {

    private int cost;

    public int getCost() {
        return cost;
    }
}
