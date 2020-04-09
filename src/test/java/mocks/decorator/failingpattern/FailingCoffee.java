package mocks.decorator.failingpattern;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.DECORATOR_CONCRETE_COMPONENT})
public class FailingCoffee implements IFailingBeverageComponent {

    private int cost = 5;

    public int getCost() {
        return cost;
    }
}
