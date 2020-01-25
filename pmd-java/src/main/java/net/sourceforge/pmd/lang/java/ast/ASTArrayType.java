/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.ast.NodeStream;

/**
 * Represents an array type.
 *
 * <pre class="grammar">
 *
 * ArrayType ::= {@link ASTPrimitiveType PrimitiveType} {@link ASTArrayDimensions ArrayDimensions}
 *             | {@link ASTClassOrInterfaceType ClassOrInterfaceType} {@link ASTArrayDimensions ArrayDimensions}
 *
 * </pre>
 */
public final class ASTArrayType extends AbstractJavaTypeNode implements ASTReferenceType {
    ASTArrayType(int id) {
        super(id);
    }


    @Override
    public NodeStream<ASTAnnotation> getDeclaredAnnotations() {
        return getDimensions().getLastChild().getDeclaredAnnotations();
    }

    public ASTArrayDimensions getDimensions() {
        return (ASTArrayDimensions) getChild(1);
    }


    public ASTType getElementType() {
        return (ASTType) getChild(0);
    }

    @Override
    public String getTypeImage() {
        return getElementType().getTypeImage();
    }

    @Override
    public int getArrayDepth() {
        return getDimensions().getSize();
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
