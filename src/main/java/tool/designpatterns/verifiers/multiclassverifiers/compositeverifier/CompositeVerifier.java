package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.PatternGroupFeedback;

public class CompositeVerifier implements IPatternGrouper {

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        return null;
    }
}
