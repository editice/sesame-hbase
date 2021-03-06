/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for unary value operators which, by definition, has
 * one argument.
 */
public abstract class UnaryValueOperator extends QueryModelNodeBase implements
		ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected ValueExpr arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new empty unary value operator.
	 */
	public UnaryValueOperator() {
	}

	/**
	 * Creates a new unary value operator.
	 * 
	 * @param arg
	 *            The operator's argument, must not be <tt>null</tt>.
	 */
	public UnaryValueOperator(ValueExpr arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getArg() {
		return arg;
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *            The (new) argument for this operator, must not be
	 *            <tt>null</tt>.
	 */
	public void setArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
			throws X {
		if (arg != null) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current,
			QueryModelNode replacement) {
		if (arg == current) {
			setArg((ValueExpr) replacement);
		} else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UnaryValueOperator) {
			UnaryValueOperator o = (UnaryValueOperator) other;
			return (arg == null && o.getArg() == null)
					|| (arg != null && arg.equals(o.getArg()));
		}

		return false;
	}

	@Override
	public int hashCode() {
		return arg.hashCode();
	}

	@Override
	public UnaryValueOperator clone() {
		UnaryValueOperator clone = (UnaryValueOperator) super.clone();
		if (getArg() != null) {
			clone.setArg(getArg().clone());
		}
		return clone;
	}
}
