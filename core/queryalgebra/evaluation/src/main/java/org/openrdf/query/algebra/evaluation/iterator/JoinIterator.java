/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinIterator extends
		LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	
	private final EvaluationStrategy strategy;

	private final Join join;

	private final CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private volatile CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public JoinIterator(EvaluationStrategy strategy, Join join,
			BindingSet bindings) throws QueryEvaluationException {
		this.strategy = strategy;
		this.join = join;
		logger.trace("<joinIterator>left join--> "+join.getLeftArg());
		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
		logger.trace("after create leftIter-- bindings.names:  "+bindings.getBindingNames());
		// Initialize with empty iteration so that var is never null
		rightIter = new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement() throws QueryEvaluationException {
		try {
			while (rightIter.hasNext() || leftIter.hasNext()) {
				if (rightIter.hasNext()) {
					return rightIter.next();
				}

				// Right iteration exhausted
				rightIter.close();

				if (leftIter.hasNext()) {
					rightIter = strategy.evaluate(join.getRightArg(), leftIter
							.next());
				}
			}
		} catch (NoSuchElementException ignore) {
			// probably, one of the iterations has been closed concurrently in
			// handleClose()
		}

		return null;
	}

	@Override
	protected void handleClose() throws QueryEvaluationException {
		super.handleClose();

		leftIter.close();
		rightIter.close();
	}
}
