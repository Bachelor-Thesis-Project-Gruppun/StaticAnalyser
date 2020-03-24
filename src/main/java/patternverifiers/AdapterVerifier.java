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
        CompilationUnit adapterCompUnit = patternParts.get(ADAPTER_ADAPTER).get(0);
        CompilationUnit adapteeCompUnit = patternParts.get(ADAPTER_ADAPTEE).get(0);
        List<CompilationUnit> interfacesCu = patternParts.get(ADAPTER_INTERFACE);
        List<ClassOrInterfaceDeclaration> interfaces = new ArrayList<>();
        for (CompilationUnit cu : interfacesCu) {
            interfaces.addAll(cu.findAll(ClassOrInterfaceDeclaration.class));
        }

        // Maybe we don't need this? Three variables, one for adaptor, one for adaptee and one
        // for superclass/interface might be enough

        /*
        Tuple<ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration> adapterInterface =
            new Tuple<>();
        Tuple<ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration> adapteeInterface =
            new Tuple<>();

        adapteeInterface.setFirst(
            adapteeCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));

        adapterInterface.setFirst(
            adapterCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));

        if (isClassSuperClassOf(adapterInterface.getFirst(), adapteeInterface.getFirst())) {
            adapterInterface.setSecond(adapteeInterface.getFirst());
        } else {
            adapterInterface.setSecond(getClassInterface(adapterInterface.getFirst(), interfaces));
        }
        adapteeInterface.setSecond(getClassInterface(adapteeInterface.getFirst(), interfaces));
         */
        //adapteeInterface.setSecond(
        //    getWrapee(adapteeInterface.getFirst(), adapterInterface.getFirst(), interfaces));

        // Verify if the parts are a coherent pattern

        // return verifyInterfaces(adapterInterface, adapteeInterface);

        return verifyadapter(
            adapterCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0),
            adapteeCompUnit.findAll(ClassOrInterfaceDeclaration.class).get(0));
    }

    /**
     * A method
     *
     * @return
     */
    private Feedback verifyadapter(
        ClassOrInterfaceDeclaration adapter, ClassOrInterfaceDeclaration adaptee) {

        // TODO if statement to get adaptee class instead of adaptee.getSecond() if extends case
        List<Feedback> feedbackList = adapter.accept(new Visitor(), adaptee);
        for (Feedback f : feedbackList) {
            if (f.getValue()) {
                return new Feedback(true);
            }
        }

        return new Feedback(false, "You are bad and should feel bad. \nGet your shit " +
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

        return new Feedback(
            adapterInterfaceExists && adapteeInterfaceExists && isNotSameInterface,
            "adapter and" + " adaptee does not " + "implement the correct interfaces");
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




