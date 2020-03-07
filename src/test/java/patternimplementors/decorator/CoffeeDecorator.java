package patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public abstract class CoffeeDecorator implements IBeverageComponent {

    private int cost;
    private IBeverageComponent component;

    @Override
    public int getCost() {
        return cost;
    }
}
