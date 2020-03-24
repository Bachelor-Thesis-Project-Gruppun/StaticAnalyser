package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static base.Pattern.ADAPTOR_ADAPTEE;
import static base.Pattern.ADAPTOR_ADAPTOR;
import static base.Pattern.ADAPTOR_INTERFACE;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import base.Pattern;

/**
 * A verifier for the adaptor pattern.
 */
public class AdaptorVerifier implements IPatternGroupVerifier {

    public AdaptorVerifier() {
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
        CompilationUnit adaptorCompUnit = patternParts.get(ADAPTOR_ADAPTOR).get(0);
        CompilationUnit adapteeCompUnit = patternParts.get(ADAPTOR_ADAPTEE).get(0);
        List<CompilationUnit> interfacesCu = patternParts.get(ADAPTOR_INTERFACE);
        List<ClassOrInterfaceDeclaration> interfaces = new ArrayList<>();
        for (CompilationUnit cu : interfacesCu) {
            interfaces.addAll(cu.findAll(ClassOrInterfaceDeclaration.class));
        }

        // Maybe we don't need this? Three variables, one for adaptor, one for adaptee and one
        // for superclass/interface might be enough

        /*
        Tuple<ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration> adaptorInterface =
            new Tuple<>();
        Tuple<ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration> adapteeInterface =
            new Tuple<>();

        adapteeInterface.setFirst(
            adapteeCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));

        adaptorInterface.setFirst(
            adaptorCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));

        if (isClassSuperClassOf(adaptorInterface.getFirst(), adapteeInterface.getFirst())) {
            adaptorInterface.setSecond(adapteeInterface.getFirst());
        } else {
            adaptorInterface.setSecond(getClassInterface(adaptorInterface.getFirst(), interfaces));
        }
        adapteeInterface.setSecond(getClassInterface(adapteeInterface.getFirst(), interfaces));
         */
        //adapteeInterface.setSecond(
        //    getWrapee(adapteeInterface.getFirst(), adaptorInterface.getFirst(), interfaces));

        // Verify if the parts are a coherent pattern

        // return verifyInterfaces(adaptorInterface, adapteeInterface);
        System.out.println(adapteeCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));
        return verifyAdaptor(
            adaptorCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0),
            adapteeCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));
    }

    /**
     * A method
     *
     * @return
     */
    private Feedback verifyAdaptor(
        ClassOrInterfaceDeclaration adaptor, ClassOrInterfaceDeclaration adaptee) {

        // TODO if statement to get adaptee class instead of adaptee.getSecond() if extends case
        List<Feedback> feedbackList = adaptor.accept(new Visitor(), adaptee);
        for (Feedback f : feedbackList) {
            if (f.getValue()) {
                return new Feedback(true);
            }
        }

        return new Feedback(false, "You are bad and should feel bad. \nGet your shit " +
                                   "together before you even try to run me again");
    }

    /**
     * A method that verifieadaptorInterfaces that the adaptor and the adaptee implements the
     * correct interfaces, the two interfaces should be specified by the annotations, and the
     * interfaces should be different from each other.
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

        return new Feedback(
            adaptorInterfaceExists && adapteeInterfaceExists && isNotSameInterface,
            "Adaptor and" + " adaptee does not " + "implement the correct interfaces");
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
    private class Visitor extends GenericListVisitorAdapter<Feedback, ClassOrInterfaceDeclaration> {

        /**
         * @param method
         * @param adaptee
         *
         * @return
         */
        @Override
        public List<Feedback> visit(
            MethodDeclaration method, ClassOrInterfaceDeclaration adaptee) {
            List<Feedback> feedbackList = super.visit(method, adaptee);

            for (AnnotationExpr annotation : method.getAnnotations()) {
                if (annotation.getNameAsString().equalsIgnoreCase("override")) {
                    if (isWrapper(method, adaptee)) {
                        feedbackList.add(new Feedback(true));
                        return feedbackList;
                    }
                }
            }
            feedbackList.add(new Feedback(
                false, "You are bad and should feel bad. \nGet your shit " +
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
        public List<Boolean> visit(MethodCallExpr n, ClassOrInterfaceDeclaration adaptee) {
            List<Boolean> boolList = super.visit(n, adaptee);

            //System.out.println(adapteeInterface.getNameAsString() + " ######################");

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




