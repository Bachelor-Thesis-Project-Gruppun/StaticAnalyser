package patternverifiers;

import java.util.ArrayList;
import java.util.List;

import base.VariableReader;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class SingletonVerifier implements IPatternVerifier {

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit cu) {

        //throw new UnsupportedOperationException();
        return hasStaticInstance(cu) && hasGetInstanceMethod(cu);
    }

    /**
     * Method for declaring if a java class holds a field variable of a static
     * instance https://stackoverflow
     * .com/questions/53300710/how-to-parse-inner-class-from-java-source-code
     * might help solving a check for inner classes
     *
     * @param cu The CompilationUnit representing the java class to look at
     *
     * @return True iff the java class holds a field variable with a static
     *     modifier of the same type as the class itself (eg. static
     *     SingletonVerifier sv;)
     */
    public boolean hasStaticInstance(CompilationUnit cu) {
        boolean stat = false;
        boolean priv = false;
        for (FieldDeclaration bd : VariableReader.readVariables(
            cu)) {      // For each FieldDeclaration in the java file
            if (bd.getVariables().get(0).getType().toString().equals(
                cu.getType(0)
                  .getNameAsString())) {    // If there is a field of the
                // same type as the file itself, probaply needs to check for
                // several different classes in the same file, can have inner
                // classes etc not sure how javaparser handles that.
                for (Modifier md : bd
                    .getModifiers()) {     // For each modifier on that field
                    if (md.getKeyword().asString().equals(
                        "static")) {  // If that modifier is static
                        stat = true;
                    } else if (md.getKeyword().asString().equals(
                        "private")) { // Else if that modifier is private
                        priv = true;
                    }
                }
            }
        }
        return stat && priv;
    }

    /**
     * Method for declaring if a java class has a getInstance() method which
     * returns an instance of the Singleton class. Currently does not support
     * private getInstace methods that are called somewhere else, or that it
     * checks for a null reference of the instance variable.
     *
     * @param cu The CompilationUnit representing the java class to look at
     *
     * @return
     */
    public boolean hasGetInstanceMethod(CompilationUnit cu) {
        boolean instanceMethod = false;
        List<MethodDeclaration> methods = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            methods.add(methodDeclaration);
        });
        for (MethodDeclaration declaration : methods) {
            if (declaration.isStatic()) {
                System.out.println(declaration.getTypeAsString() + " " +
                                   cu.getPrimaryTypeName().get());
                if (declaration.getTypeAsString().equals(
                    cu.getPrimaryTypeName().get())) {
                    instanceMethod = true;
                }
            }
        }
        //System.out.println(methods.toString());
        //throw new UnsupportedOperationException();
        return instanceMethod;
    }

}
