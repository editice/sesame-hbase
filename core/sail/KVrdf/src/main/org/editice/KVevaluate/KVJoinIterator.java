package org.editice.KVevaluate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KVJoinIterator extends
		LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final EvaluationStrategy strategy;

	private final Join join;

	private final CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private volatile CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	/*----------------------------*
	 * Variables  for multi thread*
	 *----------------------------*/
	ExecutorService exec ;

	List<Future<CloseableIteration<BindingSet, QueryEvaluationException>>> result = new ArrayList<Future<CloseableIteration<BindingSet, QueryEvaluationException>>>();

	CallableEvaluateJob job;
	
	List<CloseableIteration<BindingSet, QueryEvaluationException>> end=new ArrayList<CloseableIteration<BindingSet, QueryEvaluationException>>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	// public KVJoinIterator(EvaluationStrategy strategy, Join join,
	// BindingSet bindings) throws QueryEvaluationException {
	// this.strategy = strategy;
	// this.join = join;
	// leftIter = strategy.evaluate(join.getLeftArg(), bindings);
	// rightIter = new EmptyIteration<BindingSet, QueryEvaluationException>();
	// }

	public KVJoinIterator(EvaluationStrategy strategy, Join join,
			BindingSet bindings, boolean isEntrance)
			throws QueryEvaluationException {
		exec= Executors.newFixedThreadPool(8);
		this.strategy = strategy;
		this.join = join;
		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
		while (leftIter.hasNext()) {
			job = new CallableEvaluateJob(strategy.evaluate(join.getRightArg(),
					leftIter.next()));
			result.add(exec.submit(job));
		}
		
		try{
			for(Future<CloseableIteration<BindingSet, QueryEvaluationException>> f:result){
				try{
					CloseableIteration<BindingSet, QueryEvaluationException> iter=f.get();
					end.add(iter);
				}catch(InterruptedException e){
					System.out.println("interrupted Exception");
					e.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					exec.shutdown();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		rightIter = new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public BindingSet getNextElement() throws QueryEvaluationException{
		Iterator<CloseableIteration<BindingSet, QueryEvaluationException>> iter=end.iterator();
		CloseableIteration<BindingSet, QueryEvaluationException> sub;
		while(iter.hasNext()){
			sub=iter.next();
			while(sub.hasNext()){
				return sub.next();
			}
			
		}
//		System.err.println("nothing left");
		return null;
		
		
		
	}

//	@Override
//	protected BindingSet getNextElement() throws QueryEvaluationException {
//		try {
//			while (rightIter.hasNext() || leftIter.hasNext()) {
//				if (rightIter.hasNext()) {
//					return rightIter.next();
//				}
//
//				// Right iteration exhausted
//				rightIter.close();
//
//				if (leftIter.hasNext()) {
//					rightIter = strategy.evaluate(join.getRightArg(), leftIter
//							.next());
//				}
//			}
//		} catch (NoSuchElementException ignore) {
//			// probably, one of the iterations has been closed concurrently in
//			// handleClose()
//		}
//
//		return null;
//	}

	@Override
	protected void handleClose() throws QueryEvaluationException {
		super.handleClose();

		leftIter.close();
		rightIter.close();
	}
}
