package patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public class Coffee implements IDecoratable {

    private int cost = 5;

    @Override
    public int getCost() {
        return cost;
    }
}
