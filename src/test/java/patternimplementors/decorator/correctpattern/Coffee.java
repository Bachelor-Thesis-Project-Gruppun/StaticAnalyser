package patternimplementors.decorator.correctpattern;

public class Coffee implements IBeverageComponent {

    private int cost = 5;

    @Override
    public int getCost() {
        return cost;
    }
}
