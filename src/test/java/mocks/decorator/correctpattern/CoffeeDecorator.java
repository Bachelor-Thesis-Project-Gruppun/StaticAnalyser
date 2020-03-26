package mocks.decorator.correctpattern;

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
