/* Generated By:JJTree: Do not edit this line. ASTIsIRI.java */

package org.openrdf.query.parser.sparql.ast;

public class ASTIsIRI extends SimpleNode {

	public ASTIsIRI(int id) {
		super(id);
	}

	public ASTIsIRI(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
			throws VisitorException {
		return visitor.visit(this, data);
	}
}
