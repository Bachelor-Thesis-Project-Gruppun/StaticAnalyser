package tool.designpatterns.verifiers;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.ast.CompilationUnit;

import org.junit.jupiter.api.Test;
import tool.designpatterns.verifiers.singleclassverifiers.immutable.ImmutableVerifier;
import utilities.TestHelper;

/**
 * Pattern verifier for the immutable pattern. The following predicates will be Every variable needs
 * to be static, final or it needs to be private and no method changes it.
 */
class ImmutableVerifierTest {

    /**
     * Test when a variable is reassigned.
     */
    @Test
    void testVerifyReassignment() throws IOException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit(
            "immutable", "ImmutableClassFailReassignment");
        assertTrue(new ImmutableVerifier().verify(compUnit).getIsError());
    }

    /**
     * Test when no variables are reassigned but a local variable with the same name as a class
     * variable is assigned.
     */
    @Test
    void testVerifyLocalVariable() throws IOException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit(
            "immutable", "ImmutableClassSuccessLocalVariable");
        assertFalse(new ImmutableVerifier().verify(compUnit).getIsError());
    }

    /**
     * Normal immutable class.
     */
    @Test
    void testVerifyImmutableClass() throws IOException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit("immutable", "ImmutableClass");
        assertFalse(new ImmutableVerifier().verify(compUnit).getIsError());
    }

    /**
     * Public variable.
     */
    @Test
    void testVerifyImmutablePublic() throws FileNotFoundException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit("immutable", "ImmutableClassPublic");
        assertTrue(new ImmutableVerifier().verify(compUnit).getIsError());
    }
}