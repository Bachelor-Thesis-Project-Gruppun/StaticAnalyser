package patternimplementors.decorator.failingpattern;

/**
 * Should fail predicate because the class does not set it's component at any point
 */
public class FailingCream extends FailingCoffeeDecorator {

    public int cost = 3;
    private IFailingBeverageComponent component;

    @Override
    public int getCost() {
        return this.cost;
    }
}
