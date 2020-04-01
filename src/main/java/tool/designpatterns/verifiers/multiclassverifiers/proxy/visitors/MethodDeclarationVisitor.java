package tool.designpatterns.verifiers.multiclassverifiers.proxy.visitors;


import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

/**
 * A class used to visit nodes in a AST created by JavaParser.
 */
public class MethodDeclarationVisitor
    extends GenericListVisitorAdapter<MethodDeclaration, ClassOrInterfaceDeclaration> {

    public MethodDeclarationVisitor() {
        super();
    }

    /**
     * Visit all MethodDeclarations in a ClassOrInterfaceDeclaration and add them to a list.
     *
     * @param method the MethodDeclaration currently being visited.
     * @param classOrI the ClassOrInterfaceDeclaration the method is declared in.
     *
     * @return a list of MethodDeclarations present in the ClassOrInterfaceDeclaration.
     */
    @Override
    public List<MethodDeclaration> visit(
        MethodDeclaration method, ClassOrInterfaceDeclaration classOrI) {
        List<MethodDeclaration> resultList = super.visit(method, classOrI);
        resultList.add(method);
        return resultList;
    }

}