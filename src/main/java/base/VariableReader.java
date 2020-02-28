package base;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for reading variables from a given java class (CompilationUnit)
 */
public class VariableReader {

    /**
     * Reads all variables from a given Java file and returns them in a List of VariableDeclarators
     * @param cu The CompilationUnit describing the Java file to be read
     * @return  A List<VariableDeclarator containing all variables in the Java file
     */
    public static List<FieldDeclaration> readVariables(CompilationUnit cu) {
        List<FieldDeclaration> variables = new ArrayList<>();
        for (TypeDeclaration<?> typeDec : cu.getTypes()) {
            for (BodyDeclaration<?> member : typeDec.getMembers()) {
                member.toFieldDeclaration().ifPresent(field -> {
                    for (VariableDeclarator variable : field.getVariables()) {
                        //Print the field's class type and name, Prints then as [private, static ] String name "value"
                        variable.getParentNode();
                        System.out.println((member.asFieldDeclaration().getModifiers() + " " + variable.getType() + " " + variable.getName() + " " + variable.getInitializer().get()));
                        variables.add(member.asFieldDeclaration());
                    }
                });
            }
        }
        return variables;
    }
}