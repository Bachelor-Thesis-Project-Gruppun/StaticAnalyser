package mocks.decorator.failingpattern;

/**
 * Should fail since class does not have an IFailingBeverageComponent field.
 */
public abstract class FailingCoffeeDecorator implements IFailingBeverageComponent {

    private int cost;

    public int getCost() {
        return cost;
    }
}
