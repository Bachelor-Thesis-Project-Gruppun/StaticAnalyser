package patternverifiers.patternimplementors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
public class ImmutableClass {

    private int a = 0;
    private int b = 1;

    public void asd() {
        a = 2;

        String b = "asd";
        b += 5;

        b = 3;
    }
}
