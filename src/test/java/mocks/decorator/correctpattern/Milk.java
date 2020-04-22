package mocks.decorator.correctpattern;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.DECORATOR_CONCRETE_DECORATOR})
public class Milk extends CoffeeDecorator {

    public int cost = 3;
    private IBeverageComponent component;

    public Milk(IBeverageComponent decoratable) {
        super(decoratable);
        this.component = decoratable;
    }

    @Override
    public int getCost() {
        return this.component.getCost() + this.cost;
    }
}
