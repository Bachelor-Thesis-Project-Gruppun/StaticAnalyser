package patternverifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static base.Pattern.*;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
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

        List<CompilationUnit> adaptorCompUnits = patternParts.get(ADAPTER_ADAPTER);


        for ( CompilationUnit compUnit : patternParts.get(ADAPTER_ADAPTER)) {
           compUnit.accept(v, coi);
        }

        // Verify if the parts are a coherent pattern

        return false;
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




