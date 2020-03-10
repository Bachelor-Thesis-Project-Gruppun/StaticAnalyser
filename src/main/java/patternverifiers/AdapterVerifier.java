package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static base.Pattern.*;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import base.Pattern;

/**
 * A verifier for the adapter pattern.
 */
public class AdapterVerifier /* REMEMBER TO IMPLEMENT THE INTERFACE */{

    private Visitor v = new Visitor();
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

    private boolean verifyInterfaces(CompilationUnit adaptee, CompilationUnit client,
                                     List<CompilationUnit> interfaces){
//        ConcurrentSkipListSet<ClassOrInterfaceDeclaration> adapteeCOI = new ConcurrentSkipListSet<>(
//            adaptee.findAll(ClassOrInterfaceDeclaration.class));
//
//        ConcurrentSkipListSet<ClassOrInterfaceDeclaration> clientCOI = new ConcurrentSkipListSet<>(
//            client.findAll(ClassOrInterfaceDeclaration.class));
//
//        ConcurrentSkipListSet<ClassOrInterfaceDeclaration> interfaceCOI =
//            new ConcurrentSkipListSet<>(interfaces.findAll(ClassOrInterfaceDeclaration.class));

        ClassOrInterfaceDeclaration adapteeClass = new ClassOrInterfaceDeclaration();
        ClassOrInterfaceDeclaration clientClass;
        ClassOrInterfaceDeclaration interface1;
        ClassOrInterfaceDeclaration interface2;

        boolean is = false;

        for (ClassOrInterfaceDeclaration coi :
            adaptee.findAll(ClassOrInterfaceDeclaration.class) ) {
            if(!coi.isInnerClass()){
                adapteeClass = coi;
                break;
            }
        }

        for (ClassOrInterfaceDeclaration coi :
            client.findAll(ClassOrInterfaceDeclaration.class) ) {
            if(!coi.isInnerClass()){
                clientClass = coi;
                break;
            }
        }

        for (ClassOrInterfaceDeclaration coi :
            interfaces.get(0).findAll(ClassOrInterfaceDeclaration.class) ) {
            if(!coi.isInnerClass()){
                interface1 = coi;
                break;
            }
        }

        for (ClassOrInterfaceDeclaration coi :
            interfaces.get(1).findAll(ClassOrInterfaceDeclaration.class) ) {
            if(!coi.isInnerClass()){
                interface2 = coi;
                break;
            }
        }

        for (ClassOrInterfaceType type : adapteeClass.getImplementedTypes()) {

        }



        return is;
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




