package org.editice.sail.KVrdf;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.algebra.evaluation.TripleSource;


public class KVTripleSource implements TripleSource {
	
	/*-----------*
	 * Constants *
	 *-----------*/

	protected final KVStore kvStore;

	protected final boolean includeInferred;

	protected final boolean readTransaction;
	
	private List<Statement> checkList=new ArrayList<Statement>();
	
	private boolean justStoreStatement=true;

	/*--------------*
	 * Constructors *
	 *--------------*/


	public KVTripleSource(KVStore kvStore, boolean includeInferred,
			boolean transactionActive) {
		this.kvStore=kvStore;
		this.includeInferred=includeInferred;
		this.readTransaction=transactionActive;
	}

	/**
	 * actually the variable 'includeInferred' and 'readTransaction' will not work
	 */
	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
			Resource subj, URI pred, Value obj, Resource... contexts)
			throws QueryEvaluationException {
		try {
//			if(subj!=null && pred!=null && obj!=null){
//				justStoreStatement=true;
//				Statement st=new StatementImpl(subj,pred,obj);
//				checkList.add(st);
//				return checkStatementsList(checkList);
////				checkList.
//			}else{
//				justStoreStatement=false;
			return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
					kvStore.createStatementIterator(subj, pred, obj,
							includeInferred, readTransaction, contexts)) {

				@Override
				protected QueryEvaluationException convert(Exception e) {
					if (e instanceof ClosedByInterruptException) {
						return new QueryInterruptedException(e);
					} else if (e instanceof IOException) {
						return new QueryEvaluationException(e);
					} else if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					} else if (e == null) {
						throw new IllegalArgumentException("e must not be null");
					} else {
						throw new IllegalArgumentException(
								"Unexpected exception type: " + e.getClass());
					}
				}
			};
//		}
		} catch (IOException e) {
			throw new QueryEvaluationException("Unable to get statements", e);
		}
	}

	public CloseableIteration<? extends Statement, QueryEvaluationException> checkStatementsList(
			List<Statement> checklist){
//		if(checklist.size()==0){
//			return null;
//		}else{
//			if(justStoreStatement){
//				//do nothing
//				
//			}else{
				//means now need to do List<get>
				return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
						kvStore.createStatementLitIterator(checkList,
								includeInferred, readTransaction)) {

					@Override
					protected QueryEvaluationException convert(Exception e) {
						if (e instanceof ClosedByInterruptException) {
							return new QueryInterruptedException(e);
						} else if (e instanceof IOException) {
							return new QueryEvaluationException(e);
						} else if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						} else if (e == null) {
							throw new IllegalArgumentException("e must not be null");
						} else {
							throw new IllegalArgumentException(
									"Unexpected exception type: " + e.getClass());
						}
					}
				};
//				//clear list 
//				justStoreStatement=true;
//				checklist.clear();
//			}
		}
		
	
	@Override
	public ValueFactory getValueFactory() {
		return kvStore.getValueFactory();
	}

}
