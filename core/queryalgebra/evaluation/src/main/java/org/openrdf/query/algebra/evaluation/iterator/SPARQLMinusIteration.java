/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.HashSet;
import java.util.Set;

import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultUtil;

/**
 * An Iteration that returns the results of an Iteration (the left argument)
 * MINUS any results that are compatible with results of another Iteration (the
 * right argument) or that have no shared variables. This iteration uses the
 * formal definition of the SPARQL 1.1 MINUS operator to determine which
 * BindingSets to return.
 * 
 * @see http://www.w3.org/TR/sparql11-query/#sparqlAlgebra
 * @author Jeen
 */
public class SPARQLMinusIteration<X extends Exception> extends
		FilterIteration<BindingSet, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iteration<BindingSet, X> rightArg;

	private final boolean distinct;

	private boolean initialized;

	private Set<BindingSet> excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MinusIteration that returns the results of the left
	 * argument minus the results of the right argument. By default, duplicates
	 * are <em>not</em> filtered from the results.
	 * 
	 * @param leftArg
	 *            An Iteration containing the main set of elements.
	 * @param rightArg
	 *            An Iteration containing the set of elements that should be
	 *            filtered from the main set.
	 */
	public SPARQLMinusIteration(Iteration<BindingSet, X> leftArg,
			Iteration<BindingSet, X> rightArg) {
		this(leftArg, rightArg, false);
	}

	/**
	 * Creates a new MinusIteration that returns the results of the left
	 * argument minus the results of the right argument.
	 * 
	 * @param leftArg
	 *            An Iteration containing the main set of elements.
	 * @param rightArg
	 *            An Iteration containing the set of elements that should be
	 *            filtered from the main set.
	 * @param distinct
	 *            Flag indicating whether duplicate elements should be filtered
	 *            from the result.
	 */
	public SPARQLMinusIteration(Iteration<BindingSet, X> leftArg,
			Iteration<BindingSet, X> rightArg, boolean distinct) {
		super(leftArg);

		assert rightArg != null;

		this.rightArg = rightArg;
		this.distinct = distinct;
		this.initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	// implements LookAheadIteration.getNextElement()
	protected boolean accept(BindingSet object) throws X {
		if (!initialized) {
			// Build set of elements-to-exclude from right argument
			excludeSet = Iterations.addAll(rightArg, new HashSet<BindingSet>());
			initialized = true;
		}

		boolean compatible = false;

		for (BindingSet excluded : excludeSet) {

			// build set of shared variable names
			Set<String> sharedBindingNames = new HashSet<String>(excluded
					.getBindingNames());
			sharedBindingNames.retainAll(object.getBindingNames());

			// two bindingsets that share no variables are compatible by
			// definition, however, the formal
			// definition of SPARQL MINUS indicates that such disjoint sets
			// should be filtered out.
			// See http://www.w3.org/TR/sparql11-query/#sparqlAlgebra
			if (!sharedBindingNames.isEmpty()) {
				if (QueryResultUtil.bindingSetsCompatible(excluded, object)) {
					// at least one compatible bindingset has been found in the
					// exclude set, therefore the object is compatible,
					// therefore it should not be accepted.
					compatible = true;
					break;
				}
			}
		}

		return !compatible;
	}

	@Override
	protected void handleClose() throws X {
		super.handleClose();
		Iterations.closeCloseable(rightArg);
	}
}
