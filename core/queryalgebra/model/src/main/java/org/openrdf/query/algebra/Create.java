/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author jeen
 */
public class Create extends QueryModelNodeBase implements UpdateExpr {

	private ValueConstant graph;

	private boolean silent;

	public Create() {
		super();
	}

	public Create(ValueConstant graph) {
		super();
		setGraph(graph);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
			throws X {
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
			throws X {
		if (graph != null) {
			graph.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current,
			QueryModelNode replacement) {
		if (graph == current) {
			setGraph((ValueConstant) replacement);
		} else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Create clone() {
		Create clone = new Create();
		clone.setSilent(isSilent());
		if (getGraph() != null) {
			clone.setGraph(getGraph().clone());
		}
		return clone;
	}

	/**
	 * @param graph
	 *            The graph to set.
	 */
	public void setGraph(ValueConstant graph) {
		this.graph = graph;
	}

	/**
	 * @return Returns the graph.
	 */
	public ValueConstant getGraph() {
		return graph;
	}

	/**
	 * @param silent
	 *            The silent to set.
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/**
	 * @return Returns the silent.
	 */
	public boolean isSilent() {
		return silent;
	}

}
