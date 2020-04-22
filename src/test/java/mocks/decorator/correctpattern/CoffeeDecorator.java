package mocks.decorator.correctpattern;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.DECORATOR_ABSTRACT_DECORATOR})
public abstract class CoffeeDecorator implements IBeverageComponent {

    private IBeverageComponent component;
    private int cost;

    public CoffeeDecorator(IBeverageComponent component) {
        this.component = component;
    }

    @Override
    public int getCost() {
        return component.getCost() + cost;
    }
}
