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

        return verifyInterfaces(getInterfaces(adapteeCompUnit, adaptorCompUnit, interfaces).getFirst(),
                                getInterfaces(adapteeCompUnit, adaptorCompUnit, interfaces).getSecond());
    }

    /**
     *
     * @return
     */
    private Feedback verifyAdaptor(Tuple<CompilationUnit,CompilationUnit> adaptor,
                                   Tuple<CompilationUnit,CompilationUnit> adaptee){




        return new Feedback(false);
    }

    /**
     * A method that verifies that the adaptor and the adaptee implements the correct interfaces,
     * the two interfaces should be specified by the annotations, and the interfaces should be
     * different from each other.
     *
     * @param adaptee    The tuple representing the adaptee and its interface
     * @param adaptor    The tuple representing the adaptor and its interface
     *
     * @return Feedback with the result and message
     */
    private Feedback verifyInterfaces(Tuple<CompilationUnit,CompilationUnit> adaptor,
                                      Tuple<CompilationUnit,CompilationUnit> adaptee) {

        boolean adaptorInterfaceExists = false;
        boolean adapteeInterfaceExists = false;
        boolean isNotSameInterface = false;

        if(adaptor.getSecond() != null){
            adaptorInterfaceExists = true;
        }

        if(adaptee.getSecond() != null){
            adapteeInterfaceExists = true;
        }

        if(!adaptor.getSecond().equals(adaptee.getSecond())){
            isNotSameInterface = true;
        }

        return new Feedback(adaptorInterfaceExists &&
                            adapteeInterfaceExists &&
                            isNotSameInterface,
                            "Adaptor and" + " adaptee does not "
                            + "implement the correct interfaces");
    }

    public Tuple<Tuple<CompilationUnit,CompilationUnit>,Tuple<CompilationUnit,CompilationUnit>>
    getInterfaces(CompilationUnit adaptee,
                  CompilationUnit adaptor,
                  List<CompilationUnit> interfaces){

        Tuple<CompilationUnit, CompilationUnit> adaptorIntPair = new Tuple<>();
        Tuple<CompilationUnit, CompilationUnit> adapteeIntPair = new Tuple<>();

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
                adapteeIntPair.setSecond(interfaces.get(0));
            } else if(adapteeImplements.get(i).toString().equals(interface2))  {
                adapteeIntPair.setFirst(adaptee);
                adapteeIntPair.setSecond(interfaces.get(1));
            }

            if (adaptorImplements.get(i).toString().equals(interface1)) {
                adaptorIntPair.setFirst(adaptor);
                adaptorIntPair.setSecond(interfaces.get(0));
            } else if(adaptorImplements.get(i).toString().equals(interface2))  {
                adaptorIntPair.setFirst(adaptor);
                adaptorIntPair.setSecond(interfaces.get(1));
            }

        }

        return new Tuple<>(adaptorIntPair, adapteeIntPair);
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




