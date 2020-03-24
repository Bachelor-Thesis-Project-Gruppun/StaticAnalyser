package mocks.immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock class")
// Should fail as 'a' is reassigned
public class ImmutableClassFailReassignment {

    private int a = 0;
    private int b = 1;

    public void asd() {
        a = 2;
    }
}
