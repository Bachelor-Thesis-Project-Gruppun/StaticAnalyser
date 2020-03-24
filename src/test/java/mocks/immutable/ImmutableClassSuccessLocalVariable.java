package mocks.immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
// Is immutable, a local variable named 'b' assigned but never the class variable 'b'.
public class ImmutableClassSuccessLocalVariable {

    private int a = 0;
    private int b = 1;

    public ImmutableClassSuccessLocalVariable(int a) {
        this.a = a;
        this.b = a * 2;
    }

    public String asd(String g) {
        String b = "aaa";

        b = b + g;

        return b;
    }
}
