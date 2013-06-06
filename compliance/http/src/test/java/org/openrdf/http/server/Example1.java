package org.openrdf.http.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

public class Example1 {

	// this is the fastedst type of repository that can be used, which just
	// stores RDF data in main memory
	public void createMemStoreRpository() throws RepositoryException {
		File dataDir = new File("/home/hadoop/zj/temp");
		MemoryStore memStore = new MemoryStore(dataDir);
		memStore.setSyncDelay(1000L);
		Repository myRepository = new SailRepository(memStore);
		// it will lost when object is garbage collected or when the program is
		// shutdown
		myRepository.initialize();
	}

	public Repository createNativeRepository() throws RepositoryException {

		File dataDir = new File(
				"/home/hadoop/.aduna/openrdf-sesame-console/repositories/zj");

		String indexes = "spoc,posc,cosp";
		Repository myRepository = new SailRepository(new NativeStore(dataDir,
				indexes));
		System.out.println(myRepository.getDataDir());
		myRepository.initialize();
		return myRepository;

	}

	public Repository accessHTTPRepository(String sesameServer)
			throws RepositoryException {
		String repositoryID = "zjHttp";
		Repository myRepository = new HTTPRepository(sesameServer, repositoryID);
		myRepository.initialize();
		return myRepository;
	}

	public void addFileToLocalRepository(Repository repo, String repoPath,
			String filePath, String baseURI) throws RepositoryException,
			RDFParseException, IOException {
		RepositoryConnection con = repo.getConnection();
		File file = new File(filePath);

		// need to del the file in this filefolder !!
		File[] fileList = new File(repoPath).listFiles();
		for (int i = 0; i < fileList.length; i++) {
			fileList[i].delete();
		}

		// to find if filepath is dir?
		if (file.isDirectory()) {
			File[] dirArr = file.listFiles();
			for (int i = 0; i < dirArr.length; i++) {
				con.add(dirArr[i], baseURI, RDFFormat.RDFXML);
			}
		} else {
			con.add(file, baseURI, RDFFormat.RDFXML);
		}

		con.close();

	}

	// here has problem
	public void addFileToHttpRepository(Repository repo, String filePath,
			String baseURI, String urlString) throws RepositoryException,
			RDFParseException, IOException {
		RepositoryConnection con = repo.getConnection();
		File file = new File(filePath);

		// to find if filepath is dir?
		if (file.isDirectory()) {
			File[] dirArr = file.listFiles();
			for (int i = 0; i < dirArr.length; i++) {
				con.add(dirArr[i], baseURI, RDFFormat.RDFXML);
			}
		} else {
			con.add(file, baseURI, RDFFormat.RDFXML);
		}

		RepositoryResult<Statement> result = con.getStatements(null, null,
				null, true);
		int count = 0;
		while (result.hasNext()) {
			count++;
			result.next();
		}
		System.out.println("the number of statements:" + count);

		con.close();
	}

	public void EvaluateTupleQuery(Repository repo) throws RepositoryException,
			QueryEvaluationException, MalformedQueryException {
		RepositoryConnection con = repo.getConnection();
		String queryString = LubmQueryStatics.test3;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				queryString);

		// System.out.println("--> tupleQuery.evaluate()");
		TupleQueryResult result = tupleQuery.evaluate();

		int count = 0;
		while (result.hasNext() && count++ < 5) {
			BindingSet res = result.next();
			System.out.println(res.toString() + "\t"
					+ res.getClass().getSimpleName());
		}
		result.close();
		con.close();
	}

	public void evaluateQueryToFile(Repository repo, String outDir)
			throws RepositoryException, QueryEvaluationException,
			TupleQueryResultHandlerException, MalformedQueryException,
			IOException {
		FileOutputStream out = new FileOutputStream(outDir);
		SPARQLResultsXMLWriter sparqlWriter = new SPARQLResultsXMLWriter(out);
		RepositoryConnection con = repo.getConnection();

		// String query="SELECT x, y FROM {x} p {y}";
		String query1 = LubmQueryStatics.test1;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				query1);
		tupleQuery.evaluate(sparqlWriter);

		// RepositoryResult<Statement> result=con.getStatements(null, null,
		// null, true, (Resource)null);
		// int count=0;
		// while(result.hasNext()){
		// count++;
		// result.next();
		// }
		// System.out.println("the number of statements that do not have associated context:"+count);

		RepositoryResult<Statement> result = con.getStatements(null, null,
				null, true);
		int count = 0;
		while (result.hasNext()) {
			count++;
			result.next();
		}
		System.out.println("the number of statements:" + count);

		con.close();
		out.close();
	}

	public static void main(String[] args) throws RepositoryException,
			RDFParseException, IOException, QueryEvaluationException,
			MalformedQueryException, TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		Example1 ex = new Example1();
		// ex.createMemStoreRpository();
		// ex.createNativeRepository();

		// String filePath="/home/hadoop/zj/dataDir/RDF/univ-bench.owl";
		String fullFilePath = "/home/hadoop/zj/dataDir/semanticWeb";
		String baseURI = "http://114.212.190.95:8080/openrdf-sesame";
		// String out="/home/hadoop/zj/dataDir/out/result.srx";
		String repoPath = "/home/hadoop/zj/temp";
		//		
		Repository repo = ex.createNativeRepository();
		// ex.addFileToLocalRepository(repo,repoPath,fullFilePath,baseURI);
		ex.EvaluateTupleQuery(repo);

		// Repository repo=ex.accessHTTPRepository(baseURI);
		// ex.addFileToHttpRepository(repo, fullFilePath, baseURI, "");
		// ex.EvaluateTupleQuery(repo);

		// ex.evaluateQueryToFile(repo, out);
	}

}
