package org.editice.sail.KVrdf;

import info.aduna.iteration.CloseableIteration;

import org.editice.KVevaluate.KVEvaluationStrategyImpl;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionBase;

public class KVStoreConnection extends SailConnectionBase{

	protected final KVStore kvStore;
	
	public KVStoreConnection(KVStore kvstore) {
		super(kvstore);
		this.kvStore=kvstore;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {
		//not consider about context
		addStatement(subj,pred,obj);
		
	}
	
	private boolean addStatement(Resource subj,URI pred,Value obj){
		boolean result=false;
		
		try{
			MappingStore mappingStore=kvStore.getMappingStore();
			byte[] subjarr=mappingStore.storeValue(subj);
			byte[] predarr=mappingStore.storeValue(pred);
			byte[] objarr=mappingStore.storeValue(obj);
			
			boolean wasNew=kvStore.getHbaseStore().storeTriple(subjarr, predarr, objarr);
			result |=wasNew;
			
//			if(wasNew){
//				//the triple was not present in the hbaseStore
//				Statement st;
//				st=mappingStore.createStatement(subj,pred,obj);//here confused
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}

	public boolean totalDelete(){
		if(kvStore.totalDelete()){
			return true;
		}
		return false;
		
	}
	
	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clearNamespacesInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void closeInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void commitInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException {
//		System.out.println("this method was invoked");
//		logger.info("KVStoreConnection.evaluateInternal()-->", tupleExpr);
//		logger.trace("KVStoreConnection.evaluateInternal()-->"+tupleExpr);
		// Clone the tuple expression to allow for more aggressive optimizations
		tupleExpr = tupleExpr.clone();
		// logger.trace(tupleExpr);

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimizers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		try {
			replaceValues(tupleExpr);

			KVTripleSource tripleSource = new KVTripleSource(
					kvStore, includeInferred, transactionActive());
			KVEvaluationStrategyImpl strategy = new KVEvaluationStrategyImpl(
					tripleSource, dataset);

			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset,
					bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset,
					bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset,
					bindings);
			new SameTermFilterOptimizer()
					.optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
//			 new SubSelectJoinOptimizer().optimize(tupleExpr, dataset,
//			 bindings);
			
			//TODO this optimize need i to do something to realize
//			logger.info("begin to do queryJoinOptimizer");
			new QueryJoinOptimizer(new KVEvaluationStatistics(kvStore))
					.optimize(tupleExpr, dataset, bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset,
					bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.info("Optimized query model:\n{}", tupleExpr);
			// logger.trace("optimize:-----");
			// System.err.println(tupleExpr);
			
			return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
		} catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	private void replaceValues(TupleExpr tupleExpr) throws SailException {
		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {

			@Override
			public void meet(Var var) {
				if (var.hasValue()) {
//					System.out.println("replace :"+kvStore.getMappingStore().getNativeValue(
//							var.getValue()));
					var.setValue(kvStore.getMappingStore().getNativeValue(
							var.getValue()));
				}
			}
		});
	}
	
	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
			throws SailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNamespaceInternal(String prefix) throws SailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
			throws SailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
			Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws SailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void removeNamespaceInternal(String prefix) throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void rollbackInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
			throws SailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected long sizeInternal(Resource... contexts) throws SailException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void startTransactionInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}

}
