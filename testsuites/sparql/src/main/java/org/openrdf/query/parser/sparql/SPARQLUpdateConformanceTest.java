/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;
import info.aduna.text.StringUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.repository.contextaware.ContextAwareRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public abstract class SPARQLUpdateConformanceTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	static final Logger logger = LoggerFactory
			.getLogger(SPARQLUpdateConformanceTest.class);

	protected final String testURI;

	protected final String requestFileURL;

	/*-----------*
	 * Variables *
	 *-----------*/

	protected ContextAwareRepository dataRep;

	protected Repository expectedResultRepo;

	private URI inputDefaultGraph;

	private Map<String, URI> inputNamedGraphs;

	private URI resultDefaultGraph;

	private Map<String, URI> resultNamedGraphs;

	protected final Dataset dataset;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLUpdateConformanceTest(String testURI, String name,
			String requestFile, URI defaultGraphURI,
			Map<String, URI> inputNamedGraphs, URI resultDefaultGraphURI,
			Map<String, URI> resultNamedGraphs) {
		super(name);

		this.testURI = testURI;
		this.requestFileURL = requestFile;
		this.inputDefaultGraph = defaultGraphURI;
		this.inputNamedGraphs = inputNamedGraphs;
		this.resultDefaultGraph = resultDefaultGraphURI;
		this.resultNamedGraphs = resultNamedGraphs;

		if (this.inputNamedGraphs.size() > 0) {
			DatasetImpl ds = new DatasetImpl();
			ds.addDefaultGraph(null);
			ds.addDefaultRemoveGraph(null);
			ds.setDefaultInsertGraph(null);

			for (String ng : inputNamedGraphs.keySet()) {
				URI namedGraph = new URIImpl(ng);
				ds.addNamedGraph(namedGraph);
			}
			this.dataset = ds;
		} else {
			this.dataset = null;
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp() throws Exception {
		dataRep = createRepository();

		URL graphURL = null;
		RepositoryConnection conn = dataRep.getConnection();
		try {
			conn.clear();

			if (inputDefaultGraph != null) {
				graphURL = new URL(inputDefaultGraph.stringValue());
				conn.add(graphURL, null, RDFFormat.forFileName(graphURL
						.toString()));
			}

			for (String ng : inputNamedGraphs.keySet()) {
				graphURL = new URL(inputNamedGraphs.get(ng).stringValue());
				conn.add(graphURL, null, RDFFormat.forFileName(graphURL
						.toString()), dataRep.getValueFactory().createURI(ng));
			}
		} finally {
			conn.close();
		}

		expectedResultRepo = createRepository();

		conn = expectedResultRepo.getConnection();

		try {
			conn.clear();

			if (resultDefaultGraph != null) {
				graphURL = new URL(resultDefaultGraph.stringValue());
				conn.add(graphURL, null, RDFFormat.forFileName(graphURL
						.toString()));
			}

			for (String ng : resultNamedGraphs.keySet()) {
				graphURL = new URL(resultNamedGraphs.get(ng).stringValue());
				conn.add(graphURL, null, RDFFormat.forFileName(graphURL
						.toString()), dataRep.getValueFactory().createURI(ng));
			}
		} finally {
			conn.close();
		}

	}

	protected ContextAwareRepository createRepository() throws Exception {
		ContextAwareRepository repo = newRepository();
		repo.initialize();
		RepositoryConnection con = repo.getConnection();
		try {
			con.clear();
			con.clearNamespaces();
		} finally {
			con.close();
		}
		return repo;
	}

	protected abstract ContextAwareRepository newRepository() throws Exception;

	@Override
	protected void tearDown() throws Exception {
		if (dataRep != null) {
			dataRep.shutDown();
			dataRep = null;
		}
	}

	@Override
	protected void runTest() throws Exception {
		ContextAwareConnection con = dataRep.getConnection();
		RepositoryConnection erCon = expectedResultRepo.getConnection();
		try {
			String updateString = readUpdateString();

			con.setReadContexts((URI) null);

			Update update = con.prepareUpdate(QueryLanguage.SPARQL,
					updateString, requestFileURL);
			if (this.dataset != null) {
				update.setDataset(this.dataset);
			}
			update.execute();

			// check default graph
			logger.info("checking default graph");
			compareGraphs(con.getStatements(null, null, null, true,
					(Resource) null).asList(), erCon.getStatements(null, null,
					null, true, (Resource) null).asList());

			for (String namedGraph : inputNamedGraphs.keySet()) {
				logger.info("checking named graph {}", namedGraph);
				URI contextURI = con.getValueFactory().createURI(
						namedGraph.replaceAll("\"", ""));
				compareGraphs(con.getStatements(null, null, null, true,
						contextURI).asList(), erCon.getStatements(null, null,
						null, true, contextURI).asList());
			}
		} finally {
			con.close();
			erCon.close();
		}
	}

	private void compareGraphs(Iterable<? extends Statement> actual,
			Iterable<? extends Statement> expected) throws Exception {
		if (!ModelUtil.equals(expected, actual)) {
			StringBuilder message = new StringBuilder(128);
			message.append("\n============ ");
			message.append(getName());
			message.append(" =======================\n");
			message.append("Expected result: \n");
			for (Statement st : expected) {
				message.append(st.toString());
				message.append("\n");
			}
			message.append("=============");
			StringUtil.appendN('=', getName().length(), message);
			message.append("========================\n");

			message.append("Actual result: \n");
			for (Statement st : actual) {
				message.append(st.toString());
				message.append("\n");
			}
			message.append("=============");
			StringUtil.appendN('=', getName().length(), message);
			message.append("========================\n");

			logger.error(message.toString());
			fail(message.toString());
		}
	}

	private String readUpdateString() throws IOException {
		InputStream stream = new URL(requestFileURL).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		} finally {
			stream.close();
		}
	}

	public interface Factory {

		SPARQLUpdateConformanceTest createSPARQLUpdateConformanceTest(
				String testURI, String name, String requestFile,
				URI defaultGraphURI, Map<String, URI> inputNamedGraphs,
				URI resultDefaultGraphURI, Map<String, URI> resultNamedGraphs);

	}

	public static TestSuite suite(String manifestFileURL, Factory factory)
			throws Exception {
		return suite(manifestFileURL, factory, true);
	}

	public static TestSuite suite(String manifestFileURL, Factory factory,
			boolean approvedOnly) throws Exception {
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite(factory.getClass().getName());

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		ManifestTest.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		suite.setName(getManifestName(manifestRep, con, manifestFileURL));

		// Extract test case information from the manifest file. Note that we
		// only
		// select those test cases that are mentioned in the list.
		StringBuilder query = new StringBuilder(512);
		query
				.append(" SELECT DISTINCT testURI, testName, result, action, requestFile, defaultGraph, resultDefaultGraph ");
		query
				.append(" FROM {} rdf:first {testURI} rdf:type {mf:UpdateEvaluationTest}; ");
		if (approvedOnly) {
			query
					.append("                          dawgt:approval {dawgt:Approved}; ");
		}
		query.append("                             mf:name {testName}; ");
		query
				.append("                             mf:action {action} ut:request {requestFile}; ");
		query
				.append("                                                [ ut:data {defaultGraph} ],  ");
		query
				.append("                   {testURI} mf:result {result} [ut:data {resultDefaultGraph}] ");
		query.append(" USING NAMESPACE ");
		query
				.append("  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, ");
		query
				.append("  dawgt = <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#>, ");
		query
				.append("  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>, ");
		query
				.append("  ut = <http://www.w3.org/2009/sparql/tests/test-update#>, ");
		query
				.append("  sd = <http://www.w3.org/ns/sparql-service-description#>, ");
		query.append("  ent = <http://www.w3.org/ns/entailment/> ");

		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
				query.toString());

		query.setLength(0);
		query.append(" SELECT DISTINCT namedGraphData, namedGraphLabel ");
		query
				.append(" FROM {graphDef} ut:graphData {} ut:graph {namedGraphData} ; ");
		query
				.append("                               rdfs:label {namedGraphLabel} ");
		query.append(" USING NAMESPACE ");
		query
				.append("  ut = <http://www.w3.org/2009/sparql/tests/test-update#> ");

		TupleQuery namedGraphsQuery = con.prepareTupleQuery(
				QueryLanguage.SERQL, query.toString());

		logger.debug("evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			URI testURI = (URI) bindingSet.getValue("testURI");
			String testName = bindingSet.getValue("testName").toString();
			Value result = bindingSet.getValue("result");
			Value action = bindingSet.getValue("action");
			URI requestFile = (URI) bindingSet.getValue("requestFile");
			URI defaultGraphURI = (URI) bindingSet.getValue("defaultGraph");
			URI resultDefaultGraphURI = (URI) bindingSet
					.getValue("resultDefaultGraph");

			logger.debug("found test case : {}", testName);

			// Query input named graphs
			namedGraphsQuery.setBinding("graphDef", action);
			TupleQueryResult inputNamedGraphsResult = namedGraphsQuery
					.evaluate();

			HashMap<String, URI> inputNamedGraphs = new HashMap<String, URI>();

			if (inputNamedGraphsResult.hasNext()) {
				while (inputNamedGraphsResult.hasNext()) {
					BindingSet graphBindings = inputNamedGraphsResult.next();
					URI namedGraphData = (URI) graphBindings
							.getValue("namedGraphData");
					String namedGraphLabel = ((Literal) graphBindings
							.getValue("namedGraphLabel")).getLabel();
					logger.debug(" adding named graph : {}", namedGraphLabel);
					inputNamedGraphs.put(namedGraphLabel, namedGraphData);
				}
			}

			// Query result named graphs
			namedGraphsQuery.setBinding("graphDef", result);
			TupleQueryResult resultNamedGraphsResult = namedGraphsQuery
					.evaluate();

			HashMap<String, URI> resultNamedGraphs = new HashMap<String, URI>();

			if (resultNamedGraphsResult.hasNext()) {
				while (resultNamedGraphsResult.hasNext()) {
					BindingSet graphBindings = resultNamedGraphsResult.next();
					URI namedGraphData = (URI) graphBindings
							.getValue("namedGraphData");
					String namedGraphLabel = ((Literal) graphBindings
							.getValue("namedGraphLabel")).getLabel();
					logger.debug(" adding named graph : {}", namedGraphLabel);
					resultNamedGraphs.put(namedGraphLabel, namedGraphData);
				}
			}

			SPARQLUpdateConformanceTest test = factory
					.createSPARQLUpdateConformanceTest(testURI.toString(),
							testName, requestFile.toString(), defaultGraphURI,
							inputNamedGraphs, resultDefaultGraphURI,
							resultNamedGraphs);

			if (test != null) {
				suite.addTest(test);
			}
		}

		testCases.close();
		con.close();

		manifestRep.shutDown();
		logger.info("Created test suite with " + suite.countTestCases()
				+ " test cases.");
		return suite;
	}

	protected static String getManifestName(Repository manifestRep,
			RepositoryConnection con, String manifestFileURL)
			throws QueryEvaluationException, RepositoryException,
			MalformedQueryException {
		// Try to extract suite name from manifest file
		TupleQuery manifestNameQuery = con
				.prepareTupleQuery(QueryLanguage.SERQL,
						"SELECT ManifestName FROM {ManifestURL} rdfs:label {ManifestName}");
		manifestNameQuery.setBinding("ManifestURL", manifestRep
				.getValueFactory().createURI(manifestFileURL));
		TupleQueryResult manifestNames = manifestNameQuery.evaluate();
		try {
			if (manifestNames.hasNext()) {
				return manifestNames.next().getValue("ManifestName")
						.stringValue();
			}
		} finally {
			manifestNames.close();
		}

		// Derive name from manifest URL
		int lastSlashIdx = manifestFileURL.lastIndexOf('/');
		int secLastSlashIdx = manifestFileURL
				.lastIndexOf('/', lastSlashIdx - 1);
		return manifestFileURL.substring(secLastSlashIdx + 1, lastSlashIdx);
	}
}
