package mocks.symbolsolver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Is a mock class")
public class SymbolSolverMock {

    private Symbol symbol;

    public void testSymbol() {
        symbol = new Symbol();
    }

}
