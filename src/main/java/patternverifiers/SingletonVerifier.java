package patternverifiers;

import base.VariableReader;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;

public class SingletonVerifier implements IPatternVerifier {
    int i = 0;

    public SingletonVerifier() {
    }

    @Override
    public boolean verify(CompilationUnit cu) {

        throw new UnsupportedOperationException();
        //return hasStaticInstance(cu);
    }

    /**
     * Method for declaring if a java class holds a field variable of a static instance
     * @param cu    The CompilationUnit representing the java class to look at
     * @return  True iff the java class holds a field variable with a static modifier of the same type as the class itself (eg. static SingletonVerifier sv;)
     */
    public boolean hasStaticInstance(CompilationUnit cu) {
        Boolean stat = false;
        Boolean priv = false;
        for (FieldDeclaration bd : VariableReader.readVariables(cu)) {      // For each FieldDeclaration in the java file
            if (bd.getVariables().get(0).getType().toString().equals(cu.getType(0).getNameAsString())) {    // If there is a field of the same type as the file itself, probaply needs to check for several different classes in the same file, can have inner classes etc not sure how javaparser handles that.
                for (Modifier md : bd.getModifiers()) {     // For each modifier on that field
                    if (md.getKeyword().asString().equals("static")) {  // If that modifier is static
                        stat = true;
                    }else if(md.getKeyword().asString().equals("private")){ // Else if that modifier is private
                        priv = true;
                    }
                }
            }
        }
        return stat && priv;
    }
}
