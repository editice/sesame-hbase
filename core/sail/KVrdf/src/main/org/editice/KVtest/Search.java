package org.editice.KVtest;
import java.io.File;

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
import org.openrdf.sail.nativerdf.NativeStore;

public class Search {

	public static Repository createNativeRepository(String path)
			throws RepositoryException {

		// File dataDir = new File(
		// "/home/hadoop/.aduna/openrdf-sesame-console/repositories/zj");

		// "/home/hadoop/zj/fuck"
		File dataDir = new File(path);

		String indexes = "spoc,posc,cosp";
		Repository myRepository = new SailRepository(new NativeStore(dataDir,
				indexes));
		System.out.println(myRepository.getDataDir());
		myRepository.initialize();
		return myRepository;

	}

	public static void EvaluateTupleQuery(Repository repo,String query)
			throws RepositoryException, QueryEvaluationException,
			MalformedQueryException {
		RepositoryConnection con = repo.getConnection();
//		String queryString = LubmChanged.q13;
		String queryString = query;
		// String queryString= LubmQueryStatics.test6;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				queryString);

		TupleQueryResult result = tupleQuery.evaluate();

		int count = 0;
		long timea = System.currentTimeMillis();

		while (result.hasNext()) {
			count++;
			BindingSet res = result.next();
			// System.out.println(res);
		}
		long timeb = System.currentTimeMillis();
		System.out.println("time cost in search:" + (timeb - timea) + "ms");
		System.out.println("search number:" + count);
		result.close();
		con.close();
	}

	/**
	 * @param args
	 * @throws RepositoryException 
	 * @throws MalformedQueryException 
	 * @throws QueryEvaluationException 
	 */
	public static void main(String[] args) throws RepositoryException, QueryEvaluationException, MalformedQueryException {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			System.err.println("USAGE:<RepoDir> <QueryString: q1,q2...> ");
		}
		
		String repoPath = args[0];
		String choose=args[1];
		String query=LubmChanged.q1;
		Repository repo = createNativeRepository(repoPath);
		EvaluateTupleQuery(repo,query);
	}

}
