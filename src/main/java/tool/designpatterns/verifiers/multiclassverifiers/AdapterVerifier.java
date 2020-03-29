package tool.designpatterns.verifiers.multiclassverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static tool.designpatterns.Pattern.ADAPTER_ADAPTEE;
import static tool.designpatterns.Pattern.ADAPTER_ADAPTER;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;
import tool.feedback.PatternGroupFeedback;

/**
 * A verifier for the adapter pattern.
 */
public class AdapterVerifier implements IPatternGrouper {

    public AdapterVerifier() {
    }

    /**
     * A method for verifying one or more instances of the adapter pattern in a project.
     *
     * @param patternParts The classes that are marked with a adapter annotation
     * @return a Feedback with true or false regarding if the pattern is implemented successfully.
     */
    @Override
    public PatternGroupFeedback verifyGroup(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternParts) {
        List<Feedback> feedbacks = new ArrayList<>();

        Feedback partsFeedback = verifyParts(patternParts);
        if (partsFeedback.getIsError()) {
            feedbacks.add(partsFeedback);
        } else {
            List<ClassOrInterfaceDeclaration> adaptees = patternParts.get(ADAPTER_ADAPTEE);

            for (ClassOrInterfaceDeclaration adapter : patternParts.get(ADAPTER_ADAPTER)) {
                feedbacks.add(verifyAdapter(adapter, adaptees));
            }
        }
        return new PatternGroupFeedback(PatternGroup.ADAPTER, feedbacks);
    }

    private Feedback verifyParts(Map<Pattern,List<ClassOrInterfaceDeclaration>> patternParts) {
        StringBuilder stringBuilder = new StringBuilder(62);
        boolean allParts = true;
        if (!patternParts.containsKey(ADAPTER_ADAPTER) || patternParts.get(ADAPTER_ADAPTER)
            .isEmpty()) {
            allParts = false;
            stringBuilder.append("There is no annotated adapter, ");
        }

        if (!patternParts.containsKey(ADAPTER_ADAPTEE) || patternParts.get(ADAPTER_ADAPTEE)
            .isEmpty()) {
            allParts = false;
            stringBuilder.append("There is no annotated adaptee, ");
        }

        if (allParts) {
            return Feedback.getSuccessfulFeedback();
        } else {
            return Feedback.getPatternInstanceNoChildFeedback(stringBuilder.delete(
                stringBuilder.length() - 2, stringBuilder.length()).toString());
        }
    }

    /**
     * A method to verify the implementation of a single adapter pattern.
     *
     * @param adapter  The adapter classOrInterfaceDeclaration for the specific pattern instance
     * @param adaptees A list of all possible classOrInterfaceDeclaration adaptees
     * @return A feedback with the result of the verification
     */
    private Feedback verifyAdapter(
        ClassOrInterfaceDeclaration adapter, List<ClassOrInterfaceDeclaration> adaptees) {
        MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor(adapter);

        for (ClassOrInterfaceDeclaration adaptee : adaptees) {
            for (Boolean methodWraps : adapter.accept(methodVisitor, adaptee)) {
                if (methodWraps) {
                    return verifyInterfaces(adapter, adaptee);
                }
            }

        }
        return Feedback.getNoChildFeedback(
            "Adapter does not wrap the adaptee", new FeedbackTrace(adapter));
    }

    /**
     * A method for verifying that the adaptor does not implement the interface of the adaptee or
     * extend the adaptee.
     *
     * @param adapter The ClassOrInterfaceDeclaration for the adapter
     * @param adaptee The ClassOrInterfaceDeclaration of the interface or superclass to be adapted
     * @return Boolean regarding if the implementation is correct or not.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // The only instantiated objects in
    // loops are in return statements
    private Feedback verifyInterfaces(
        ClassOrInterfaceDeclaration adapter, ClassOrInterfaceDeclaration adaptee) {
        for (ClassOrInterfaceType coit : adapter.getImplementedTypes()) {
            if (adaptee.isInterface()) {
                if (coit.getNameAsString().equalsIgnoreCase(adaptee.getNameAsString())) {
                    return Feedback.getNoChildFeedback("The adapter implements the adaptee",
                        new FeedbackTrace(adapter));
                }
            } else {
                for (ClassOrInterfaceType coi : adaptee.getImplementedTypes()) {
                    if (coit.getNameAsString().equalsIgnoreCase(coi.getNameAsString())) {
                        return Feedback.getNoChildFeedback(
                            "The adapter implements the same interface as the adaptee",
                            new FeedbackTrace(adapter));
                    }
                }
            }
        }

        return Feedback.getSuccessfulFeedback();
    }

    /**
     * A class used to visit nodes in a AST created by JavaParser.
     */

    private static class MethodDeclarationVisitor
        extends GenericListVisitorAdapter<Boolean, ClassOrInterfaceDeclaration> {

        private final ClassOrInterfaceDeclaration currentClass;

        public MethodDeclarationVisitor(ClassOrInterfaceDeclaration currentClass) {
            super();
            this.currentClass = currentClass;
        }

        /**
         * A method for visiting MethodDeclarations in ClassOrInterfaceDeclarations. Visits the
         * MethodDeclarations and checks if they are overridden. If they are a true feedback is
         * added to the list, otherwise a false is added.
         *
         * @param method  The MethodDeclaration of the method to be checked.
         * @param adaptee The ClassOrInterfaceDeclaration of the adaptees method to be wrapped.
         * @return a list of booleans
         */
        @Override
        public List<Boolean> visit(
            MethodDeclaration method, ClassOrInterfaceDeclaration adaptee) {
            List<Boolean> resultList = super.visit(method, adaptee);
            for (AnnotationExpr annotation : method.getAnnotations()) {
                if (annotation.getNameAsString().equalsIgnoreCase("override")) {
                    resultList.add(isWrapper(method, currentClass, adaptee));
                    return resultList;
                }
            }
            resultList.add(Boolean.FALSE);
            return resultList;
        }

        /**
         * A method that checks if a method is called from within another method, i.e. if it is
         * wrapped.
         *
         * @param method  The MethodDeclaration fo the wrapping method.
         * @param adaptee The ClassOrInterfaceDeclaration to look for the wrapped method in.
         * @return a boolean, true if it is wrapped, otherwise false.
         */
        private Boolean isWrapper(
            MethodDeclaration method, ClassOrInterfaceDeclaration currentClass,
            ClassOrInterfaceDeclaration adaptee) {
            List<Boolean> list = method.accept(new MethodCallVisitor(currentClass), adaptee);

            return list.stream().anyMatch(e -> e);
        }
    }


    /**
     * A class used to visit every method call expression node in a ClassOrInterfaceDeclaration.
     */
    private static class MethodCallVisitor
        extends GenericListVisitorAdapter<Boolean, ClassOrInterfaceDeclaration> {

        private final ClassOrInterfaceDeclaration currentClass;

        public MethodCallVisitor(ClassOrInterfaceDeclaration currentClass) {
            super();
            this.currentClass = currentClass;
        }

        /**
         * Checks if the adaptee is an interface or a superclass, and checks whether the method call
         * wraps a method call from the adaptee, if it does it adds true to the list otherwise it
         * returns false.
         *
         * @param methodCallExpr the type of node to be visited
         * @param adaptee        the ClassOrInterfaceDeclaration nodes are being visited in.
         * @return a list of Booleans, true if wrapped method call otherwise false.
         */
        @Override
        public List<Boolean> visit(
            MethodCallExpr methodCallExpr, ClassOrInterfaceDeclaration adaptee) {
            List<Boolean> boolList = super.visit(methodCallExpr, adaptee);

            if (adaptee.isInterface()) {
                for (FieldDeclaration field : currentClass.getFields()) {
                    if (field.getCommonType().toString().equals(adaptee.getNameAsString())) {
                        boolList.add(Boolean.TRUE);
                        return boolList;
                    }
                }
            } else if (methodCallExpr.getScope().get().toString().equalsIgnoreCase("super")) {
                boolList.add(Boolean.TRUE);
                return boolList;
            }

            boolList.add(Boolean.FALSE);
            return boolList;
        }
    }


}



