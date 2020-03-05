package patternverifiers.patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public class Milk extends CoffeeDecorator {

    public int cost = 3;
    private IDecoratable

    public Milk(IDecoratable decoratable) {
        this.IDecoratable = decoratable;
    }

    public void getCost() {
        return this.decoratable.getCost() + this.cost;
    }
}
