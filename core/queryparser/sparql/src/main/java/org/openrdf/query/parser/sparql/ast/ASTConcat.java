/* Generated By:JJTree: Do not edit this line. ASTConcat.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.sparql.ast;

public class ASTConcat extends SimpleNode {
	public ASTConcat(int id) {
		super(id);
	}

	public ASTConcat(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
			throws VisitorException {
		return visitor.visit(this, data);
	}
}
/*
 * JavaCC - OriginalChecksum=c159855320d036f6cddd14a000539e2e (do not edit this
 * line)
 */
