package patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public class Milk extends CoffeeDecorator {

    public int cost = 3;
    private IDecoratable component;

    public Milk(IDecoratable decoratable) {
        this.component = decoratable;
    }

    @Override
    public int getCost() {
        return this.component.getCost() + this.cost;
    }
}
