package patternverifiers.patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public class Coffee implements IDecoratable {

    public int cost = 5;

    public int getCost() {
        return cost;
    }
}
