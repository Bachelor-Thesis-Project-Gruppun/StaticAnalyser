package patternimplementors.decorator;

public abstract class FailingCoffeeDecorator implements IBeverageComponent {

    private int cost;

    public FailingCoffeeDecorator(IBeverageComponent component) {
    }

    @Override
    public int getCost() {
        return cost;
    }
}
