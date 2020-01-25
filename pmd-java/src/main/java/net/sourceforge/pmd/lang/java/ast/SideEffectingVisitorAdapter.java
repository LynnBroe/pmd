/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Adapter for {@link SideEffectingVisitor}. See {@link JavaParserVisitorAdapter} for why this is needed.
 * Unless visit methods are overridden without calling {@code super.visit}, the visitor performs a full
 * depth-first tree walk.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
public class SideEffectingVisitorAdapter<T> implements SideEffectingVisitor<T> {


    // TODO delegation


    public void visit(ASTAnyTypeDeclaration node, T data) {
        visit((JavaNode) node, data);
    }

    @Override
    public void visit(ASTClassOrInterfaceDeclaration node, T data) {
        visit((ASTAnyTypeDeclaration) node, data);
    }

    // public void visit(ASTAnonymousClassDeclaration node, T data) {visit((ASTAnyTypeDeclaration) node, data);}

    @Override
    public void visit(ASTEnumDeclaration node, T data) {
        visit((ASTAnyTypeDeclaration) node, data);
    }

    @Override
    public void visit(ASTAnnotationTypeDeclaration node, T data) {
        visit((ASTAnyTypeDeclaration) node, data);
    }
}
