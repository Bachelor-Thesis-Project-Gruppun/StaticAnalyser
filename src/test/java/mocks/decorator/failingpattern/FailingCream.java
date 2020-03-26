package mocks.decorator.failingpattern;

/**
 * Should fail predicate because the class does not set it's component at any point
 */
public class FailingCream extends FailingCoffeeDecorator {

    public int cost = 3;
    private IFailingBeverageComponent component;

    public FailingCream(IFailingBeverageComponent component) {
        this.component = component;
    }

    public FailingCream() {

    }

    @Override
    public int getCost() {
        return this.cost;
    }
}
