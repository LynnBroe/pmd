package net.sourceforge.pmd.lang.apex.ast;

import apex.jorje.semantic.tester.TestNode;

public class ASTTestNode extends AbstractApexNode<TestNode> {

    public ASTTestNode(TestNode testNode) {
        super(testNode);
    }

    public Object jjtAccept(ApexParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
