package mocks.decorator.failingpattern;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * Should fail since class does not have an IFailingBeverageComponent field.
 */
@DesignPattern(pattern = {Pattern.DECORATOR_ABSTRACT_DECORATOR})
public abstract class FailingCoffeeDecorator implements IFailingBeverageComponent {

    private int cost;

    public int getCost() {
        return cost;
    }
}
