/* Generated By:JJTree: Do not edit this line. ASTFunctionCall.java */

package org.openrdf.query.parser.sparql.ast;

public class ASTFunctionCall extends SimpleNode {

	public ASTFunctionCall(int id) {
		super(id);
	}

	public ASTFunctionCall(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
			throws VisitorException {
		return visitor.visit(this, data);
	}
}
