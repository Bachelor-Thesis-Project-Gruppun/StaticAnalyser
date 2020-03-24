package tool.designpatterns.verifiers.singleclassverifiers;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.gradle.internal.impldep.junit.framework.TestCase.assertTrue;
import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;

import com.github.javaparser.ast.CompilationUnit;

import org.junit.jupiter.api.Test;
import tool.designpatterns.verifiers.singleclassverifiers.immutable.ImmutableVerifier;
import utilities.TestHelper;

/**
 * Pattern verifier for the immutable pattern. The following predicates will be Every variable needs
 * to be static, final or it needs to be private and no method changes it.
 */
class ImmutableVerifierTest {

    @Test
        // Test when a variable is reassigned.
    void testVerifyReassignment() throws IOException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit(
            "immutable", "ImmutableClassFailReassignment");
        assertFalse(new ImmutableVerifier().verify(compUnit).getValue());
    }

    @Test
        // Test when no variables are reassigned but a local variable with the same name as a
        // class variable is assigned.
    void testVerifyLocalVariable() throws IOException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit(
            "immutable", "ImmutableClassSuccessLocalVariable");
        assertTrue(new ImmutableVerifier().verify(compUnit).getValue());
    }

    @Test
        // Normal immutable class.
    void testVerifyImmutableClass() throws IOException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit("immutable", "ImmutableClass");
        assertTrue(new ImmutableVerifier().verify(compUnit).getValue());
    }

    @Test
        // Public variable.
    void testVerifyImmutablePublic() throws FileNotFoundException {
        CompilationUnit compUnit = TestHelper.getMockCompUnit("immutable", "ImmutableClassPublic");
        assertFalse(new ImmutableVerifier().verify(compUnit).getValue());
    }
}