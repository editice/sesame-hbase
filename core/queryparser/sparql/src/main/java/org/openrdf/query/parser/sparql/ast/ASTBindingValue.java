/* Generated By:JJTree: Do not edit this line. ASTBindingValue.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.sparql.ast;

public class ASTBindingValue extends SimpleNode {
	public ASTBindingValue(int id) {
		super(id);
	}

	public ASTBindingValue(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
			throws VisitorException {
		return visitor.visit(this, data);
	}
}
/*
 * JavaCC - OriginalChecksum=d22ce857724478601c721fa3cc8487ab (do not edit this
 * line)
 */