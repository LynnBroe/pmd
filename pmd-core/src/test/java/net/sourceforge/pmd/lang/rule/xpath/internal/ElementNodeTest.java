/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.rule.xpath.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sourceforge.pmd.lang.DummyLanguageModule;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.DummyNode.DummyRootNode;

import net.sf.saxon.Configuration;
import net.sf.saxon.type.Type;

public class ElementNodeTest {


    @Test
    public void testCompareOrder() {
        DummyRootNode root = DummyLanguageModule.parse(
            "(#foo)"
                + "(#foo)"
        );

        DummyNode c0 = root.getChild(0);
        DummyNode c1 = root.getChild(1);

        Configuration configuration = Configuration.newConfiguration();

        AstTreeInfo treeInfo = new AstTreeInfo(root, configuration);
        assertSame(root, treeInfo.getRootNode().getUnderlyingNode());
        assertEquals(Type.DOCUMENT, treeInfo.getRootNode().getNodeKind());

        AstElementNode rootElt = treeInfo.getRootNode().getRootElement();
        assertSame(root, rootElt.getUnderlyingNode());
        assertEquals(Type.ELEMENT, rootElt.getNodeKind());
        assertSame(rootElt, treeInfo.findWrapperFor(root));

        AstElementNode elementFoo0 = rootElt.getChildren().get(0);
        assertSame(c0, elementFoo0.getUnderlyingNode());
        assertSame(elementFoo0, treeInfo.findWrapperFor(c0));

        AstElementNode elementFoo1 = rootElt.getChildren().get(1);
        assertSame(c1, elementFoo1.getUnderlyingNode());
        assertSame(elementFoo1, treeInfo.findWrapperFor(c1));

        assertFalse(elementFoo0.isSameNodeInfo(elementFoo1));
        assertFalse(elementFoo1.isSameNodeInfo(elementFoo0));
        assertTrue(elementFoo0.compareOrder(elementFoo1) < 0);
        assertTrue(elementFoo1.compareOrder(elementFoo0) > 0);
        assertEquals(0, elementFoo0.compareOrder(elementFoo0));
        assertEquals(0, elementFoo1.compareOrder(elementFoo1));

    }

    @Test
    public void verifyTextNodeType() {
        DummyRootNode root = DummyLanguageModule.parse("(foo)(#text)");

        DummyNode c0 = root.getChild(0);
        DummyNode c1 = root.getChild(1);

        Configuration configuration = Configuration.newConfiguration();
        AstTreeInfo treeInfo = new AstTreeInfo(root, configuration);

        AstElementNode rootElt = treeInfo.getRootNode().getRootElement();
        assertSame(root, rootElt.getUnderlyingNode());
        assertEquals(Type.ELEMENT, rootElt.getNodeKind());
        assertSame(rootElt, treeInfo.findWrapperFor(root));

        AstElementNode elementFoo0 = rootElt.getChildren().get(0);
        assertEquals(Type.ELEMENT, elementFoo0.getNodeKind());
        assertSame(c0, elementFoo0.getUnderlyingNode());
        assertSame(elementFoo0, treeInfo.findWrapperFor(c0));

        AstElementNode elementText1 = rootElt.getChildren().get(1);
        assertEquals(Type.TEXT, elementText1.getNodeKind());
        assertSame(c1, elementText1.getUnderlyingNode());
        assertSame(elementText1, treeInfo.findWrapperFor(c1));
    }

    @Test
    public void verifyCommentNodeType() {
        DummyRootNode root = DummyLanguageModule.parse("(#comment)");

        DummyNode c1 = root.getChild(0);

        Configuration configuration = Configuration.newConfiguration();
        AstTreeInfo treeInfo = new AstTreeInfo(root, configuration);
        AstElementNode rootElt = treeInfo.getRootNode().getRootElement();

        AstElementNode elementComment = rootElt.getChildren().get(0);
        assertEquals("#comment", c1.getXPathNodeName());
        assertEquals(Type.COMMENT, elementComment.getNodeKind());
        assertSame(c1, elementComment.getUnderlyingNode());
        assertSame(elementComment, treeInfo.findWrapperFor(c1));
    }

}
