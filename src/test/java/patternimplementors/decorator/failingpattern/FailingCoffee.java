package patternimplementors.decorator.failingpattern;

public class FailingCoffee implements IFailingBeverageComponent {

    private int cost = 5;

    public int getCost() {
        return cost;
    }
}
