package mocks.decorator.failingpattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * Should fail since the class does not set its component in the constructor.
 */
@SuppressFBWarnings("URF_UNREAD_FIELD")
@DesignPattern(pattern = {Pattern.DECORATOR_CONCRETE_DECORATOR})
public class FailingMilk extends FailingCoffeeDecorator {

    public int cost = 3;
    private IFailingBeverageComponent component = new FailingCoffee();

    public FailingMilk() {
    }

    @Override
    public int getCost() {
        return this.cost;
    }
}
