package org.editice.KVtest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.nativerdf.NativeStore;

public class ImportSearch {

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

	public static void addFileToLocalRepository(Repository repo,
			String repoPath, String filePath, String baseURI)
			throws RepositoryException, RDFParseException, IOException {
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
				if (i % 10 == 0) {
					System.out.println("" + i + "/" + dirArr.length);
				}
				con.add(dirArr[i], baseURI, RDFFormat.RDFXML);
			}
		} else {
			con.add(file, baseURI, RDFFormat.RDFXML);
		}

		con.close();

	}

	public void EvaluateTupleQuery(Repository repo)
			throws RepositoryException, QueryEvaluationException,
			MalformedQueryException {
		RepositoryConnection con = repo.getConnection();
		String queryString = LubmChanged.q13;
		// String queryString= LubmQueryStatics.test6;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				queryString);

		TupleQueryResult result = tupleQuery.evaluate();

		int count = 0;
		long timea = System.currentTimeMillis();

		while (result.hasNext()) {
			count++;
			BindingSet res = result.next();
			System.out.println(res);
		}
		long timeb = System.currentTimeMillis();
		System.out.println("time cost in search:" + (timeb - timea) + "ms");
		System.out.println("search number:" + count);
		result.close();
		con.close();
	}

	public static void main(String[] args) throws RepositoryException,
			RDFParseException, IOException {
		if (args.length != 2) {
			System.err.println("USAGE:<RepoDir> <NativeSourceDir> ");
		}
		// String path=args[0];

		String repoPath = args[0];
		String filePath = args[1];
		String baseURI = "http://114.212.190.95:8080/openrdf-sesame";

		Repository repo = createNativeRepository(repoPath);

		addFileToLocalRepository(repo, repoPath, filePath, baseURI);

	}

}
