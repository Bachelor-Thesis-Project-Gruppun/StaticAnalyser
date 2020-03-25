package tool.util;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * Class for reading variables from a given java class (CompilationUnit).
 */

@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class VariableReader {

    private VariableReader() {

    }

    /**
     * Reads all variables from a given Java file and returns them in a List of
     * VariableDeclarations.
     *
     * @param compUnit The CompilationUnit describing the Java file to be read
     *
     * @return A List of VariableDeclarations containing all variables in the Java file
     */
    public static List<FieldDeclaration> readVariables(CompilationUnit compUnit) {
        List<FieldDeclaration> variables = new ArrayList<>();
        for (TypeDeclaration<?> typeDec : compUnit.getTypes()) {
            for (BodyDeclaration<?> member : typeDec.getMembers()) {
                member.toFieldDeclaration().ifPresent(field -> {
                    for (VariableDeclarator variable : field.getVariables()) {
                        //Print the field's class type and name, Prints then
                        // as [private, static ] String name "value"
                        variable.getParentNode();

                        variables.add(member.asFieldDeclaration());
                    }
                });
            }
        }
        return variables;
    }
}