/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.query;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Adds more bindings to each of the results.
 * 
 * @author James Leigh
 */
public class InsertBindingSetCursor extends
		IterationWrapper<BindingSet, QueryEvaluationException> {

	private BindingSet bindings;

	public InsertBindingSetCursor(
			CloseableIteration<BindingSet, QueryEvaluationException> delegate,
			BindingSet bindings) {
		super(delegate);
		this.bindings = bindings;
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
		BindingSet next = super.next();
		if (next == null) {
			return null;
		}
		int size = bindings.size() + next.size();
		SPARQLQueryBindingSet set = new SPARQLQueryBindingSet(size);
		set.addAll(bindings);
		for (Binding binding : next) {
			set.setBinding(binding);
		}
		return set;
	}

}
