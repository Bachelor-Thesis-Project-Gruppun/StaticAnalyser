package mocks.decorator.failingpattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * Should fail predicate because the class does not set it's component in one of the constructors
 */
@SuppressFBWarnings("URF_UNREAD_FIELD")
@DesignPattern(pattern = {Pattern.DECORATOR_CONCRETE_DECORATOR})
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
