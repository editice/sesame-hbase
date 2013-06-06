package org.editice.KVevaluate;

import info.aduna.iteration.CloseableIteration;

import java.util.concurrent.Callable;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

public class CallableEvaluateJob implements Callable{
	
	CloseableIteration<BindingSet, QueryEvaluationException> iter;
	
	public CallableEvaluateJob(CloseableIteration<BindingSet, QueryEvaluationException> other){
		this.iter=other;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return iter;
	}

}
