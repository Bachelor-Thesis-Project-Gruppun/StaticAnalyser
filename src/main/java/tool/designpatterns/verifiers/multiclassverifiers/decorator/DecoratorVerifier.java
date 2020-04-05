package tool.designpatterns.verifiers.multiclassverifiers.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.VerifierUtils;
import tool.feedback.Feedback;
import tool.feedback.PatternGroupFeedback;

/**
 * A verifier for the decorator pattern.
 */
@SuppressWarnings("PMD.CommentSize")
public class DecoratorVerifier implements IPatternGrouper {

    /**
     * Constructor.
     */
    public DecoratorVerifier() {
    }

    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        List<DecoratorPatternInstance> patternInstances =
            DecoratorPatternInstance.getPatternInstances(map);
        List<Feedback> results = new ArrayList<>();
        patternInstances.forEach(patternInstance -> {
            results.add(verify(patternInstance));
        });

        return new PatternGroupFeedback(PatternGroup.DECORATOR, results);
    }

    /**
     * <p>Verifies that a correct implementation of the decorator interface has
     * been found.</p>
     * <p>Any valid implementation must fulfill the following requirements:
     *      <ol>
     *          <li>Any given pattern instance must contain:
     *              <ul>
     *             <li>Exactly one component interface</li>
     *             <li>At least one concrete component</li>
     *             <li>At least one abstract decorator</li>
     *             <li>At least one concrete decorator</li>
     *              </ul>
     *          </li>
     *          <li>The component interface must contain at least one
     *          method</li>
     *
     *         <li>Every abstract decorator must fulfill the following:
     *             <ul>
     *                  <li>It must house a component field</li>
     *                  <li>The component field needs to be initialized
     *                  during construction</li>
     *             </ul>
     *         </li>
     *         <li>
     *             Every concrete decorator must extend an existing abstract
     *             decorator in the
     *             current the pattern instance (implicitly done when calling
     *             getPatternInstances())
     *         </li>
     *         <li>
     *             Every concrete component must extend the interface
     *             component (implicitly done
     *             when calling getPatternInstances())
     *         </li>
     *      </ol>
     * </p>
     *
     * @param patternInstance An instance of the decorator pattern to be verified
     *
     * @return A {@link Feedback} object that contains the result and information regarding whether
     *     ther ther or not the instance of the pattern was valid
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    private Feedback verify(DecoratorPatternInstance patternInstance) {
        List<Feedback> childFeedbacks = new ArrayList<>();
        ClassOrInterfaceDeclaration interfaceComponent = patternInstance.getInterfaceComponent();
        List<ClassOrInterfaceDeclaration> abstractDecorators =
            patternInstance.getAbstractDecorators();
        List<ClassOrInterfaceDeclaration> concreteDecorators =
            patternInstance.getConcreteDecorators();

        Feedback hasAllElements = patternInstance.hasAllElements();
        childFeedbacks.add(hasAllElements);
        if (!hasAllElements.getIsError()) {
            childFeedbacks.add(VerifierUtils.hasAtLeastOnePublicMethod(interfaceComponent));
            abstractDecorators.forEach(decorator -> {
                childFeedbacks.add(VerifierUtils.hasFieldOfType(decorator, interfaceComponent));
                childFeedbacks.add(
                    VerifierUtils.initializesTypeInAllConstructors(decorator, interfaceComponent));
            });
            concreteDecorators.forEach(decorator -> {
                childFeedbacks.add(
                    VerifierUtils.initializesTypeInAllConstructors(decorator, interfaceComponent));
            });
        }

        return Feedback.getPatternInstanceFeedback(childFeedbacks);
    }
}

