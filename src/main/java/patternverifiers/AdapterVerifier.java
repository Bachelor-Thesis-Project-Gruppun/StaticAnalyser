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
        for (Pattern p : patternParts.keySet()) {
            System.out.println(p);
        }
        System.out.println(patternParts.get(ADAPTER_ADAPTER).size());
        for (CompilationUnit adapter : patternParts.get(ADAPTER_ADAPTER)) {
            System.out.println("ADAPTER");
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
        ClassOrInterfaceDeclaration adapter, List<ClassOrInterfaceDeclaration> adaptees) {

        // TODO if statement to get adaptee class instead of adaptee.getSecond() if extends case
        List<Feedback> feedbackList = new ArrayList<>();
        for (ClassOrInterfaceDeclaration adaptee : adaptees) {
            for (Feedback f : adapter.accept(new Visitor(), adaptee)) {
                if (f.getValue()) {
                    return verifyInterfaces(adapter, adaptee);
                }
            }
        }

        return new Feedback(false, "You are bad and should feel bad. \nGet your shit " +
                                   "together before you even try to run me again");
    }

    /**
     * @param adaptee
     * @param adapter
     *
     * @return
     */
    private Feedback verifyInterfaces(
        ClassOrInterfaceDeclaration adapter, ClassOrInterfaceDeclaration adaptee) {

        System.out.println(adaptee.getNameAsString() + " " + adapter.getNameAsString());
        for (ClassOrInterfaceType coit : adapter.getImplementedTypes()) {
            if (adaptee.isInterface()) {
                if (coit.getNameAsString().equalsIgnoreCase(adaptee.getNameAsString())) {
                    return new Feedback(false, "The adapter implements the adaptee");
                }
            } else {
                for (ClassOrInterfaceType coi : adaptee.getImplementedTypes()) {
                    if (coit.getNameAsString().equalsIgnoreCase(coi.getNameAsString())) {
                        return new Feedback(false, "The adapter implements the same interface as " +
                                                   "the adaptee");
                    }
                }
            }
        }

        return new Feedback(true);
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
     * A class used to visit nodes in a AST created by JavaParser.
     */
    private class Visitor extends GenericListVisitorAdapter<Feedback, ClassOrInterfaceDeclaration> {

        /**
         * A method for visiting MethodDeclarations in ClassOrInterfaceDeclarations. Visits the
         * MethodDeclarations and checks if they are overridden. If they are a true feedback is
         * added to the list, otherwise a false is added.
         *
         * @param method
         * @param adaptee
         *
         * @return a list of feedback
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
         * A method that checks if a method is called from within another method, i.e. it if it
         * is wrapped.
         *
         * @param method The MethodDeclaration fo the wrapping method.
         * @param adaptee The ClassOrInterfaceDeclaration to look for the wrapped method in.
         *
         * @return a boolean, true if it is wrapped, otherwise false.
         */
        private boolean isWrapper(
            MethodDeclaration method, ClassOrInterfaceDeclaration adaptee) {
            List<Boolean> list = method.accept(new MethodCallVisitor(), adaptee);

            for (Boolean bool : list) {
                if (bool) {
                    return true;
                }
            }
            return false;
        }
    }


    /**
     * A class used to visit every method call expression node in a ClassOrInterfaceDeclaration.
     */
    class MethodCallVisitor
        extends GenericListVisitorAdapter<Boolean, ClassOrInterfaceDeclaration> {

        /**
         * Checks if the adaptee is an interface or a superclass, and checks whether the method
         * call wraps a method call from the adaptee, if it does it adds true to the list
         * otherwise it returns false.
         *
         * @param n the type of node to be visited
         * @param adaptee the ClassOrInterfaceDeclaration nodes are being visited in.
         *
         * @return a list of Booleans, true if wrapped method call otherwise false.
         */
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

            boolList.add(Boolean.FALSE);
            return boolList;
        }
    }

}




