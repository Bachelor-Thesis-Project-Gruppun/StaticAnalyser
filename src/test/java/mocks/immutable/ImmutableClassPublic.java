package mocks.immutable;

public class ImmutableClassPublic {
    public int a;
    public final int b;
    private String g;

    public ImmutableClassPublic(int a, int b, String g) {
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
