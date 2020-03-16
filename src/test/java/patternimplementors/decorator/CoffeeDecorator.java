package patternimplementors.decorator;

public abstract class CoffeeDecorator implements IBeverageComponent {

    private int cost;
    private IBeverageComponent component;

    public CoffeeDecorator(IBeverageComponent component) {
        this.component = component;
    }

    @Override
    public int getCost() {
        return component.getCost() + cost;
    }
}
