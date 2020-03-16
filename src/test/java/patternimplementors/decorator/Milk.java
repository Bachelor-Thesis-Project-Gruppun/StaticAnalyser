package patternimplementors.decorator;

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
