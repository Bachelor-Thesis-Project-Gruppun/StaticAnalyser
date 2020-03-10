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
public class AdapterVerifier /* REMEMBER TO IMPLEMENT THE INTERFACE */ {

    List<NodeList> coi = new ArrayList<>();

    public AdapterVerifier() {
    }

    public boolean verify(Map<Pattern, List<CompilationUnit>> patternParts) {

        /* MATCH PATTERN PARTS LATER */

        /* VERIFICATION */

        CompilationUnit adaptorCompUnit = patternParts.get(ADAPTER_ADAPTER).get(0);
        CompilationUnit adapteeCompUnit = patternParts.get(ADAPTER_ADAPTEE).get(0);
        CompilationUnit clientCompUnit = patternParts.get(ADAPTER_CLIENT).get(0);
        List<CompilationUnit> interfaces = patternParts.get(ADAPTER_INTERFACE);

        // Verify if the parts are a coherent pattern

        return false;
    }

    private boolean verifyInterfaces(
        CompilationUnit adaptee, CompilationUnit client, List<CompilationUnit> interfaces) {
        //        ConcurrentSkipListSet<ClassOrInterfaceDeclaration> adapteeCOI = new
        //        ConcurrentSkipListSet<>(
        //            adaptee.findAll(ClassOrInterfaceDeclaration.class));
        //
        //        ConcurrentSkipListSet<ClassOrInterfaceDeclaration> clientCOI = new
        //        ConcurrentSkipListSet<>(
        //            client.findAll(ClassOrInterfaceDeclaration.class));
        //
        //        ConcurrentSkipListSet<ClassOrInterfaceDeclaration> interfaceCOI =
        //            new ConcurrentSkipListSet<>(interfaces.findAll(ClassOrInterfaceDeclaration
        //            .class));

        ClassOrInterfaceDeclaration adapteeClass = adaptee.findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        ClassOrInterfaceDeclaration clientClass = client.findAll(ClassOrInterfaceDeclaration.class)
                                                        .get(0);
        ClassOrInterfaceDeclaration interface1 = interfaces.get(0).findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        ClassOrInterfaceDeclaration interface2 = interfaces.get(1).findAll(
            ClassOrInterfaceDeclaration.class).get(1);

        ClassOrInterfaceDeclaration adapteeImplements = null;
        for (ClassOrInterfaceType type : adapteeClass.getImplementedTypes()) {
            if (type.asString().equals(interfaces.get(0).getPrimaryTypeName().get())) {
                adapteeImplements = interface1;
                break;
            } else if (type.asString().equals(interfaces.get(1).getPrimaryTypeName().get())) {
                adapteeImplements = interface2;
                break;
            }
        }

        if (adapteeImplements == null) {
            return false;
        }

        ClassOrInterfaceDeclaration clientImplements = null;
        for (ClassOrInterfaceType type : clientClass.getImplementedTypes()) {
            if (adapteeImplements.equals(interface2)) {
                if (type.asString().equals(interfaces.get(0).getPrimaryTypeName().get())) {
                    adapteeImplements = interface1;
                    break;
                }
            } else {
                if (type.asString().equals(interfaces.get(1).getPrimaryTypeName().get())) {
                    adapteeImplements = interface2;
                    break;
                }
            }
        }

        return adapteeImplements != null && clientImplements != null;
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




