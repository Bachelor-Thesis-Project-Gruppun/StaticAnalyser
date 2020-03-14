package mocks.immutable;

public class ImmutableClass {
    private int a;
    public final int b;
    private String g;

    public ImmutableClass(int a, int b, String g) {
        this.a = a;
        this.b = b;
        this.g = g;
    }

    public int getA() {
        return a;
    }

    public String getG() {
        return g;
    }
}
