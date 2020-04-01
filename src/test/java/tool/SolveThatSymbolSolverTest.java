package tool;

import java.io.FileNotFoundException;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.jupiter.api.Test;
import utilities.TestHelper;

public class SolveThatSymbolSolverTest {

    @Test
    public void testGetConfig() throws FileNotFoundException {
        ClassOrInterfaceDeclaration classOrI = TestHelper.getMockClassOrI("symbolsolver",
                                                                          "SymbolSolverMock");
        classOrI.resolve();
    }
}
