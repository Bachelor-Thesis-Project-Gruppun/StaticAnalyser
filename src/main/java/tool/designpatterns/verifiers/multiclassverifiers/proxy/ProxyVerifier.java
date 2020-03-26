package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.Feedback;
import tool.feedback.PatternGroupFeedback;

public class ProxyVerifier implements IPatternGrouper {

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {

        List<Feedback> feedbacks = new ArrayList<>();
        // Gå igenom allt:
        // 1. Hitta en interface / abstrakt klass       --
        // 2. Kolla om denna har en (abstrakt) metod.   --
        // 3. Hitta 2 klasser som implementerar denna metod.  --
        // 4. Kolla om en av dessa (klasser) har en variable av typ den andra. --
        // 5. Kolla om samma (klass) kallar på den givna metoden i den andra klassen. --

        return new PatternGroupFeedback(PatternGroup.PROXY, feedbacks);
    }
}
