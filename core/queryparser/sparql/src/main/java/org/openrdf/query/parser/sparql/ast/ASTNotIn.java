/* Generated By:JJTree: Do not edit this line. ASTNotIn.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.sparql.ast;

public class ASTNotIn extends SimpleNode {
	public ASTNotIn(int id) {
		super(id);
	}

	public ASTNotIn(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
			throws VisitorException {
		return visitor.visit(this, data);
	}
}
/*
 * JavaCC - OriginalChecksum=5d8a10320d8247584f45e03f5c6cfd32 (do not edit this
 * line)
 */
