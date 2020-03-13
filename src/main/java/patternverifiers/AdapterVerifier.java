package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static base.Pattern.ADAPTER_ADAPTEE;
import static base.Pattern.ADAPTER_ADAPTER;
import static base.Pattern.ADAPTER_CLIENT;
import static base.Pattern.ADAPTER_INTERFACE;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
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

    List<NodeList> coi = new ArrayList<>();

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
        CompilationUnit adaptorCompUnit = patternParts.get(ADAPTER_ADAPTER).get(0);
        CompilationUnit adapteeCompUnit = patternParts.get(ADAPTER_ADAPTEE).get(0);
        CompilationUnit clientCompUnit = patternParts.get(ADAPTER_CLIENT).get(0);
        List<CompilationUnit> interfaces = patternParts.get(ADAPTER_INTERFACE);

        Tuple<CompilationUnit, ClassOrInterfaceDeclaration> adaptorInterface = getInterfaces(
            adapteeCompUnit, adaptorCompUnit, interfaces).getFirst();
        Tuple<CompilationUnit, ClassOrInterfaceDeclaration> adapteeInterface = getInterfaces(
            adapteeCompUnit, adaptorCompUnit, interfaces).getSecond();

        // Verify if the parts are a coherent pattern

        // return verifyInterfaces(adaptorInterface, adapteeInterface);
        return verifyAdaptor(adaptorInterface, adapteeInterface);
    }

    /**
     * @return
     */
    private Feedback verifyAdaptor(
        Tuple<CompilationUnit, ClassOrInterfaceDeclaration> adaptor,
        Tuple<CompilationUnit, ClassOrInterfaceDeclaration> adaptee) {

        List<Feedback> feedbackList = adaptor.getFirst().accept(new Visitor(), adaptee.getSecond());
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
     * A method that verifies that the adaptor and the adaptee implements the correct interfaces,
     * the two interfaces should be specified by the annotations, and the interfaces should be
     * different from each other.
     *
     * @param adaptee The tuple representing the adaptee and its interface
     * @param adaptor The tuple representing the adaptor and its interface
     *
     * @return Feedback with the result and message
     */
    private Feedback verifyInterfaces(
        Tuple<CompilationUnit, CompilationUnit> adaptor,
        Tuple<CompilationUnit, CompilationUnit> adaptee) {

        boolean adaptorInterfaceExists = false;
        boolean adapteeInterfaceExists = false;
        boolean isNotSameInterface = false;

        if (adaptor.getSecond() != null) {
            adaptorInterfaceExists = true;
        }

        if (adaptee.getSecond() != null) {
            adapteeInterfaceExists = true;
        }

        if (!adaptor.getSecond().equals(adaptee.getSecond())) {
            isNotSameInterface = true;
        }

        return new Feedback(adaptorInterfaceExists && adapteeInterfaceExists && isNotSameInterface,
                            "Adaptor and" + " adaptee does not " +
                            "implement the correct interfaces");
    }

    /**
     * @param adaptee
     * @param adaptor
     * @param interfaces
     *
     * @return
     */
    public Tuple<Tuple<CompilationUnit, ClassOrInterfaceDeclaration>, Tuple<CompilationUnit,
        ClassOrInterfaceDeclaration>> getInterfaces(
        CompilationUnit adaptee, CompilationUnit adaptor, List<CompilationUnit> interfaces) {

        Tuple<CompilationUnit, ClassOrInterfaceDeclaration> adaptorIntPair = new Tuple<>();
        Tuple<CompilationUnit, ClassOrInterfaceDeclaration> adapteeIntPair = new Tuple<>();

        // This is pretty bad, currently assumes the first class in the compilationUnit is the
        // correct one.
        ClassOrInterfaceDeclaration adapteeClass = adaptee.findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        ClassOrInterfaceDeclaration adaptorClass = adaptor.findAll(
            ClassOrInterfaceDeclaration.class).get(0);

        // This assumes the two first interfaces are the correct ones
        String interface1 = interfaces.get(0).getPrimaryTypeName().get();
        String interface2 = interfaces.get(1).getPrimaryTypeName().get();

        NodeList<ClassOrInterfaceType> adapteeImplements = adapteeClass.getImplementedTypes();
        NodeList<ClassOrInterfaceType> adaptorImplements = adaptorClass.getImplementedTypes();

        for (int i = 0; i < adapteeImplements.size(); i++) {
            // These if statements compares strings, witch feels bad, but it does the job for now.
            if (adapteeImplements.get(i).toString().equals(interface1)) {
                adapteeIntPair.setFirst(adaptee);
                adapteeIntPair.setSecond(interfaces.get(0)
                                                   .findAll(ClassOrInterfaceDeclaration.class)
                                                   .get(0));
            } else if (adapteeImplements.get(i).toString().equals(interface2)) {
                adapteeIntPair.setFirst(adaptee);
                adapteeIntPair.setSecond(interfaces.get(1)
                                                   .findAll(ClassOrInterfaceDeclaration.class)
                                                   .get(0));
            }

            if (adaptorImplements.get(i).toString().equals(interface1)) {
                adaptorIntPair.setFirst(adaptor);
                adaptorIntPair.setSecond(interfaces.get(0)
                                                   .findAll(ClassOrInterfaceDeclaration.class)
                                                   .get(0));
            } else if (adaptorImplements.get(i).toString().equals(interface2)) {
                adaptorIntPair.setFirst(adaptor);
                adaptorIntPair.setSecond(interfaces.get(0)
                                                   .findAll(ClassOrInterfaceDeclaration.class)
                                                   .get(1));
            }
        }
        return new Tuple<>(adaptorIntPair, adapteeIntPair);
    }

    /**
     *
     */
    private class Visitor extends GenericListVisitorAdapter<Feedback, ClassOrInterfaceDeclaration> {

        /**
         * HOW TO GET THIS TO GO THROUGH EVERYTHING?
         *
         * @param method
         * @param adapteeInterface
         *
         * @return
         */
        @Override
        public List<Feedback> visit(
            MethodDeclaration method, ClassOrInterfaceDeclaration adapteeInterface) {
            List<Feedback> feedbackList = super.visit(method, adapteeInterface);

            for (AnnotationExpr annotation : method.getAnnotations()) {
                if (annotation.getNameAsString().equalsIgnoreCase("override")) {
                    if (isWrapper(method, adapteeInterface)) {
                        feedbackList.add(new Feedback(true));
                        return feedbackList;
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
     * HOW DO WE GET FALSE FOR THIS?
     */
    class MethodCallVisitor
        extends GenericListVisitorAdapter<Boolean, ClassOrInterfaceDeclaration> {

        @Override
        public List<Boolean> visit(MethodCallExpr n, ClassOrInterfaceDeclaration adapteeInterface) {
            List<Boolean> boolList = super.visit(n, adapteeInterface);
            if (n.getScope().get().toString().equalsIgnoreCase("super")) {
                boolList.add(Boolean.TRUE);
                return boolList;
            }
            boolList.add(Boolean.FALSE);
            return boolList;
        }
    }

}




