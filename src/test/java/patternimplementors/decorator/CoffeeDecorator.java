package patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public abstract class CoffeeDecorator implements IDecoratable {

    private int cost;
    private IDecoratable component;

    @Override
    public int getCost() {
        return cost;
    }
}
