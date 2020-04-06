package tool.designpatterns.verifiers.multiclassverifiers.compositeverifier;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;

import tool.designpatterns.verifiers.VerifierUtils;
import tool.feedback.Feedback;
import tool.feedback.FeedbackTrace;

/**
 * This Visitor finds all loops and then accepts the Visitor {@link ComponentMethodVisitor}.
 */
final class LoopVisitor extends GenericVisitorAdapter<Feedback, MethodDeclaration> {

    private int nbrInteratingBlocks;

    /* default */ LoopVisitor() {
        super();
        nbrInteratingBlocks = 0;
    }

    @Override
    public Feedback visit(
        ForStmt loopStmt, MethodDeclaration parentMethod) {
        super.visit(loopStmt, parentMethod);
        nbrInteratingBlocks++;
        return verifyLoop(loopStmt.getBody(), parentMethod);
    }

    @Override
    public Feedback visit(
        WhileStmt loopStmt, MethodDeclaration parentMethod) {
        super.visit(loopStmt, parentMethod);
        nbrInteratingBlocks++;
        return verifyLoop(loopStmt.getBody(), parentMethod);
    }

    @Override
    public Feedback visit(
        DoStmt loopStmt, MethodDeclaration parentMethod) {
        super.visit(loopStmt, parentMethod);
        nbrInteratingBlocks++;
        return verifyLoop(loopStmt.getBody(), parentMethod);
    }

    @Override
    public Feedback visit(
        ForEachStmt loopStmt, MethodDeclaration parentMethod) {
        super.visit(loopStmt, parentMethod);
        nbrInteratingBlocks++;
        return verifyLoop(loopStmt.getBody(), parentMethod);
    }

    @Override
    public Feedback visit(
        MethodCallExpr loopFunc, MethodDeclaration parentMethod) {
        super.visit(loopFunc, parentMethod);
        if (loopFunc.getNameAsString().equalsIgnoreCase("forEach")) {
            nbrInteratingBlocks++;
            return verifyLoop(loopFunc.getArgument(0), parentMethod);
        }
        return Feedback.getSuccessfulFeedback();
    }

    public void resetIteratingBlocks() {
        nbrInteratingBlocks = 0;
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Feedback hasIteratingBlock(MethodDeclaration methodInContainer) {
        if (nbrInteratingBlocks == 0) {
            return Feedback.getNoChildFeedback("The method has no iterating block",
                                               new FeedbackTrace(methodInContainer));
        } else {
            return Feedback.getSuccessfulFeedback();
        }

    }

    private Feedback verifyLoop(Visitable loop, MethodDeclaration parentMethod) {
        ComponentMethodVisitor visitor = new ComponentMethodVisitor();
        loop.accept(visitor, parentMethod);
        if (visitor.getDoesDelegate().stream().anyMatch(e -> e)) {
            return Feedback.getSuccessfulFeedback();
        } else {
            return Feedback.getNoChildFeedback("Container method does not delegate method call",
                                               new FeedbackTrace(parentMethod));
        }
    }


    /**
     * This visitor is used in The Class LoopVisitor and checks the method passed is ever called.
     */
    private static class ComponentMethodVisitor extends VoidVisitorAdapter<MethodDeclaration> {

        private final List<Boolean> doesDelegate;

        private ComponentMethodVisitor() {
            super();
            this.doesDelegate = new ArrayList<>();
        }

        private List<Boolean> getDoesDelegate() {
            return doesDelegate;
        }

        @Override
        public void visit(
            MethodCallExpr methodCall, MethodDeclaration parentMethod) {
            super.visit(methodCall, parentMethod);
            try {
                JavaParserMethodDeclaration methodDeclaration =
                    (JavaParserMethodDeclaration) methodCall.resolve();
                doesDelegate.add(
                    VerifierUtils.isSameMethod(methodDeclaration.getWrappedNode(), parentMethod));
            } catch (ClassCastException exception) {
                doesDelegate.add(Boolean.FALSE);
            }
        }

        @Override
        public void visit(
            MethodReferenceExpr methodRefernce, MethodDeclaration parentMethod) {
            super.visit(methodRefernce, parentMethod);
            try {
                JavaParserMethodDeclaration methodDeclaration =
                    (JavaParserMethodDeclaration) methodRefernce.resolve();
                doesDelegate.add(
                    VerifierUtils.isSameMethod(methodDeclaration.getWrappedNode(), parentMethod));
            } catch (ClassCastException | UnsolvedSymbolException exception) {
                doesDelegate.add(Boolean.FALSE);
            }
        }

    }


}

