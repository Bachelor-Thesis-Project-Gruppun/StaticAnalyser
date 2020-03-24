package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static base.Pattern.ADAPTER_ADAPTEE;
import static base.Pattern.ADAPTER_ADAPTER;
import static base.Pattern.ADAPTER_INTERFACE;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import base.Pattern;

/**
 * A verifier for the adapter pattern.
 */
public class AdapterVerifier implements IPatternGroupVerifier {

    public AdapterVerifier() {
    }

    /**
     * @param patternParts
     *
     * @return
     */
    @Override
    public Feedback verifyGroup(Map<Pattern, List<CompilationUnit>> patternParts) {

        /* MATCH PATTERN PARTS LATER */

        /* VERIFICATION */
        List<ClassOrInterfaceDeclaration> adaptees = new ArrayList<>();
        for (CompilationUnit cu : patternParts.get(ADAPTER_ADAPTEE)) {
            adaptees.add(cu.findAll(ClassOrInterfaceDeclaration.class).get(0));
        }

        List<CompilationUnit> interfacesCu = patternParts.get(ADAPTER_INTERFACE);
        List<ClassOrInterfaceDeclaration> interfaces = new ArrayList<>();
        for (CompilationUnit cu : interfacesCu) {
            interfaces.addAll(cu.findAll(ClassOrInterfaceDeclaration.class));
        }
        List<Feedback> feedbacks = new ArrayList<>();
        for (CompilationUnit adapter : patternParts.get(ADAPTER_ADAPTER)) {
            feedbacks.add(
                verifyadapter(adapter.findAll(ClassOrInterfaceDeclaration.class).get(0), adaptees));
        }

        boolean verifySuccessful = true;
        StringBuilder message = new StringBuilder();
        for (Feedback feedback : feedbacks) {
            if (!feedback.getValue()) {
                verifySuccessful = false;
                message.append('\n');
                message.append(feedback.getMessage());
            }
        }

        return new Feedback(verifySuccessful, message.toString());
    }

    /**
     * A method
     *
     * @return
     */
    private Feedback verifyadapter(
        ClassOrInterfaceDeclaration adapter, List<ClassOrInterfaceDeclaration> adaptee) {

        // TODO if statement to get adaptee class instead of adaptee.getSecond() if extends case
        List<Feedback> feedbackList = adapter.accept(new Visitor(), adaptee);
        for (Feedback f : feedbackList) {
            if (f.getValue()) {
                return new Feedback(true);
            }
        }

        return new Feedback(
            false, "You are bad and should feel bad. \nGet your shit " +
                   "together before you even try to run me again");
    }

    /**
     * A method that verifieadapterInterfaces that the adapter and the adaptee implements the
     * correct interfaces, the two interfaces should be specified by the annotations, and the
     * interfaces should be different from each other.
     *
     * @param adaptee The tuple representing the adaptee and its interface
     * @param adapter The tuple representing the adapter and its interface
     *
     * @return Feedback with the result and message
     */
    private Feedback verifyInterfaces(
        Tuple<CompilationUnit, CompilationUnit> adapter,
        Tuple<CompilationUnit, CompilationUnit> adaptee) {

        boolean adapterInterfaceExists = false;
        boolean adapteeInterfaceExists = false;
        boolean isNotSameInterface = false;

        if (adapter.getSecond() != null) {
            adapterInterfaceExists = true;
        }

        if (adaptee.getSecond() != null) {
            adapteeInterfaceExists = true;
        }

        if (!adapter.getSecond().equals(adaptee.getSecond())) {
            isNotSameInterface = true;
        }

        return new Feedback(adapterInterfaceExists && adapteeInterfaceExists && isNotSameInterface,
                            "adapter and" + " adaptee does not " +
                            "implement the correct interfaces");
    }

    private ClassOrInterfaceDeclaration getClassInterface(
        ClassOrInterfaceDeclaration implementer, List<ClassOrInterfaceDeclaration> interfaces) {
        for (ClassOrInterfaceDeclaration coi : interfaces) {
            for (ClassOrInterfaceType coi2 : implementer.getImplementedTypes()) {
                if (coi.getName().equals(coi2.getName())) {
                    return coi; // Returns the interface of the adaptee
                }
            }
        }
        return null;
    }

    private boolean isClassSuperClassOf(
        ClassOrInterfaceDeclaration subclass, ClassOrInterfaceDeclaration superclass) {
        for (ClassOrInterfaceType coi : subclass.getExtendedTypes()) {
            if (coi.getName().equals(superclass.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    private class Visitor
        extends GenericListVisitorAdapter<Feedback, List<ClassOrInterfaceDeclaration>> {

        /**
         * @param method
         * @param adaptees
         *
         * @return
         */
        @Override
        public List<Feedback> visit(
            MethodDeclaration method, List<ClassOrInterfaceDeclaration> adaptees) {
            List<Feedback> feedbackList = super.visit(method, adaptees);

            for (AnnotationExpr annotation : method.getAnnotations()) {
                if (annotation.getNameAsString().equalsIgnoreCase("override")) {
                    for (ClassOrInterfaceDeclaration adaptee : adaptees) {
                        if (isWrapper(method, adaptee)) {
                            feedbackList.add(new Feedback(true));
                            return feedbackList;
                        }
                    }

                }
            }
            feedbackList.add(new Feedback(false,
                                          "You are bad and should feel bad. \nGet your shit " +
                                          "together before you even try to run me again"));
            return feedbackList;
        }

        /**
         * @param method
         * @param adapteeInterface
         *
         * @return
         */
        private boolean isWrapper(
            MethodDeclaration method, ClassOrInterfaceDeclaration adapteeInterface) {
            List<Boolean> list = method.accept(new MethodCallVisitor(), adapteeInterface);

            for (Boolean bool : list) {
                if (bool) {
                    return true;
                }
            }
            return false;
        }
    }


    /**
     *
     */
    class MethodCallVisitor
        extends GenericListVisitorAdapter<Boolean, ClassOrInterfaceDeclaration> {

        @Override
        public List<Boolean> visit(MethodCallExpr n, ClassOrInterfaceDeclaration adaptee) {
            List<Boolean> boolList = super.visit(n, adaptee);

            if (adaptee.isInterface()) {
                if (n.getScope().get().toString().equalsIgnoreCase(adaptee.getNameAsString())) {
                    boolList.add(Boolean.TRUE);
                    return boolList;
                }
            } else if (n.getScope().get().toString().equalsIgnoreCase("super")) {
                boolList.add(Boolean.TRUE);
                return boolList;
            }
            System.out.println("HEJ");
            boolList.add(Boolean.FALSE);
            return boolList;
        }
    }

}




