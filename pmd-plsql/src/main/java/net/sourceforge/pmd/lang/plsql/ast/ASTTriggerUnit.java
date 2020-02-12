/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/* Generated By:JJTree: Do not edit this line. ASTTriggerUnit.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */

package net.sourceforge.pmd.lang.plsql.ast;

public class ASTTriggerUnit extends AbstractPLSQLNode implements ExecutableCode, OracleObject {
    public ASTTriggerUnit(int id) {
        super(id);
    }

    public ASTTriggerUnit(PLSQLParser p, int id) {
        super(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(PLSQLParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    /**
     * Gets the name of the trigger.
     *
     * @return a String representing the name of the trigger
     */
    @Override
    public String getMethodName() {
        return getImage();
    }

    public String getName() {
        return getMethodName();
    }

    /**
     * Gets the name of the Oracle Object.
     *
     * @return a String representing the name of the Oracle Object
     */
    @Override
    public String getObjectName() {
        return getImage();
    }
}
/*
 * JavaCC - OriginalChecksum=07e71d00050b4945c960cb84ed1cad6c (do not edit this
 * line)
 */
