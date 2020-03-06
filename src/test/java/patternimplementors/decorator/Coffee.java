package patternimplementors.decorator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public class Coffee implements IBeverageComponent {

    private int cost = 5;

    @Override
    public int getCost() {
        return cost;
    }
}
