package org.editice.sail.KVrdf;

import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KVEvaluationStatistics extends EvaluationStatistics{
	
	private final Logger logger=LoggerFactory.getLogger(this.getClass());
	
	private final KVStore kvStore;
	
	public KVEvaluationStatistics(KVStore kvStore){
		this.kvStore=kvStore;
	}
	
	@Override
	protected CardinalityCalculator createCardinalityCalculator() {
		return new KVCardinalityCalculator();
	}

	protected class KVCardinalityCalculator extends CardinalityCalculator{
		@Override
		protected double getCardinality(StatementPattern sp) {
			logger.info("get cardinality");
			try {
				Value subj = getConstantValue(sp.getSubjectVar());
				if (!(subj instanceof Resource)) {
					// can happen when a previous optimizer has inlined a
					// comparison operator.
					// this can cause, for example, the subject variable to be
					// equated to a literal value.
					// See SES-970
					subj = null;
				}
				Value pred = getConstantValue(sp.getPredicateVar());
				if (!(pred instanceof URI)) {
					// can happen when a previous optimizer has inlined a
					// comparison operator. See SES-970
					pred = null;
				}
				Value obj = getConstantValue(sp.getObjectVar());
				Value context = getConstantValue(sp.getContextVar());
				if (!(context instanceof Resource)) {
					// can happen when a previous optimizer has inlined a
					// comparison operator. See SES-970
					context = null;
				}
//				System.out.println("begin to do kvStore.cardinalitty");
				return kvStore.cardinality((Resource) subj, (URI) pred,
						obj, (Resource) context);
			} catch (Exception e) {
				logger
						.error(
								"Failed to estimate statement pattern cardinality, falling back to generic implementation",
								e);
				return super.getCardinality(sp);
			}
			
			
		}

		private Value getConstantValue(Var var) {
			return (var != null) ? var.getValue() : null;
		}
	}

}
