package org.editice.KVtest;

import java.util.ArrayList;

import org.editice.sail.KVrdf.KVStore;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;

public class KVStoreConnectionTest {
	
	public static ArrayList<String> testQuery(Repository repo) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		System.out.println("testQuery");
		ArrayList<String> test=new ArrayList<String>();
		RepositoryConnection con = repo.getConnection();
//		String queryString = LubmQueryStatics.q12;
		String queryString = LubmChanged.q5;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				queryString);
		long timea=System.currentTimeMillis();
		
		System.out.println("evaluate begins ....zj");
		// System.out.println("--> tupleQuery.evaluate()");
		TupleQueryResult result = tupleQuery.evaluate();
		long timeb=System.currentTimeMillis();
		System.out.println(result.getBindingNames());
		int count = 0;

		
		System.out.println("begin to print");
		while (result.hasNext()  ) {
			count++;
			BindingSet res = result.next();
//			System.out.println(res.toString() + "\t"
//					+ res.getClass().getSimpleName());
			
		}
		long timec=System.currentTimeMillis();
		System.out.println("time cost in evaluate:"+(timeb-timea)+"ms");
		System.out.println("time cost in search first 100 result:"+(timec-timeb)+"ms");
		System.out.println("search number:"+count);
		result.close();
		con.close();
		return test;
	}

	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		// initial variable
		String real = "gr_final_SPO_1,gr_final_POS_1,gr_final_OSP_1";
		String hash = "gr_final_Hash_1";
		
		KVStore kvStore = new KVStore(real, hash);
		Repository kvRepository = new SailRepository(kvStore);
		kvRepository.initialize();
		
		testQuery(kvRepository);
		
		
	}
}
