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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import base.Pattern;

/**
 * A verifier for the adapter pattern.
 */
public class AdapterVerifier implements IPatternGroupVerifier {

    List<NodeList> coi = new ArrayList<>();

    public AdapterVerifier() {
    }

    @Override
    public Feedback verifyGroup(Map<Pattern, List<CompilationUnit>> patternParts) {

        /* MATCH PATTERN PARTS LATER */

        /* VERIFICATION */
        CompilationUnit adaptorCompUnit = patternParts.get(ADAPTER_ADAPTER).get(0);
        CompilationUnit adapteeCompUnit = patternParts.get(ADAPTER_ADAPTEE).get(0);
        CompilationUnit clientCompUnit = patternParts.get(ADAPTER_CLIENT).get(0);
        List<CompilationUnit> interfaces = patternParts.get(ADAPTER_INTERFACE);

        System.out.println(interfaces.size());

        // Verify if the parts are a coherent pattern

        return verifyInterfaces(adapteeCompUnit, adaptorCompUnit, interfaces);
    }

    /**
     * A method that verifies that the adaptor and the adaptee implements the correct interfaces,
     * the two interfaces should be specified by the annotations, and the interfaces should be
     * different from each other.
     *
     * @param adaptee    The compilationUnit representing the adaptee
     * @param adaptor    The compilationUnit representing the adaptor
     * @param interfaces The list of compilationUnits representing the interfaces
     *
     * @return Feedback with the result and message
     */
    private Feedback verifyInterfaces(
        CompilationUnit adaptee, CompilationUnit adaptor, List<CompilationUnit> interfaces) {

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
            if (adapteeImplements.get(i).toString().equals(interface1) && adaptorImplements.get(i)
                                                                                           .toString()
                                                                                           .equals(
                                                                                               interface2)) {
                return new Feedback(true);
            } else if (adapteeImplements.get(i).toString().equals(interface2) &&
                       adaptorImplements.get(i).toString().equals(interface1)) {
                return new Feedback(true);
            }
        }
        return new Feedback(false, "Adaptor and adaptee does not implement the correct interfaces");
    }

    private class Visitor extends VoidVisitorAdapter<List<NodeList>> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, List<NodeList> arg) {
            super.visit(n, arg);

            // Start with not considering inner classes.
            arg.add(n.getImplementedTypes());

        }
    }

}




