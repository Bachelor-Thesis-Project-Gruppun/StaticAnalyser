package mocks.decorator.correctpattern;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.DECORATOR_CONCRETE_COMPONENT})

public class Coffee implements IBeverageComponent {

    private int cost = 5;

    @Override
    public int getCost() {
        return cost;
    }
}
