package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.PatternGroupFeedback;

/**
 * A verifier for the compsite pattern.
 */
public class CompositeVerifier implements IPatternGrouper {

    public CompositeVerifier() {
    }

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        return null;
    }
}
