package patternimplementors.decorator.failingpattern;

/**
 * Should fail since the class does not set its component in the constructor.
 */
public class FailingMilk extends FailingCoffeeDecorator {

    public int cost = 3;
    private IFailingBeverageComponent component = new FailingMilk();

    public FailingMilk() {
    }

    @Override
    public int getCost() {
        return this.cost;
    }
}
