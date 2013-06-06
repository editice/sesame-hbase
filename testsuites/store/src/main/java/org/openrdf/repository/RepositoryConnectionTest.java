/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.sail.memory.MemoryStore;

public abstract class RepositoryConnectionTest extends TestCase {

	protected static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

	protected static final String DC_NS = "http://purl.org/dc/elements/1.1/";

	public static final String TEST_DIR_PREFIX = "/testcases/";

	protected Repository testRepository;

	protected RepositoryConnection testCon;

	protected RepositoryConnection testCon2;

	protected ValueFactory vf;

	protected BNode bob;

	protected BNode alice;

	protected BNode alexander;

	protected URI name;

	protected URI mbox;

	protected URI publisher;

	protected URI unknownContext;

	protected URI context1;

	protected URI context2;

	protected Literal nameAlice;

	protected Literal nameBob;

	protected Literal mboxAlice;

	protected Literal mboxBob;

	protected Literal Александър;

	public RepositoryConnectionTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		testRepository = createRepository();
		testRepository.initialize();

		testCon = testRepository.getConnection();
		testCon.clear();
		testCon.clearNamespaces();

		testCon2 = testRepository.getConnection();

		vf = testRepository.getValueFactory();

		// Initialize values
		bob = vf.createBNode();
		alice = vf.createBNode();
		alexander = vf.createBNode();

		name = vf.createURI(FOAF_NS + "name");
		mbox = vf.createURI(FOAF_NS + "mbox");

		publisher = vf.createURI(DC_NS + "publisher");

		nameAlice = vf.createLiteral("Alice");
		nameBob = vf.createLiteral("Bob");

		mboxAlice = vf.createLiteral("alice@example.org");
		mboxBob = vf.createLiteral("bob@example.org");

		Александър = vf.createLiteral("Александър");

		unknownContext = new URIImpl("urn:unknownContext");

		context1 = vf.createURI("urn:x-local:graph1");
		context2 = vf.createURI("urn:x-local:graph2");
	}

	@Override
	protected void tearDown() throws Exception {
		testCon2.close();
		testCon2 = null;

		testCon.close();
		testCon = null;

		testRepository.shutDown();
		testRepository = null;

		vf = null;
	}

	/**
	 * Gets an (uninitialized) instance of the repository that should be tested.
	 * 
	 * @return an uninitialized repository.
	 */
	protected abstract Repository createRepository() throws Exception;

	public void testAddStatement() throws Exception {
		testCon.add(bob, name, nameBob);

		assertTrue("Repository should contain newly added statement", testCon
				.hasStatement(bob, name, nameBob, false));

		Statement statement = vf.createStatement(alice, name, nameAlice);
		testCon.add(statement);

		assertTrue("Repository should contain newly added statement", testCon
				.hasStatement(statement, false));
		assertTrue("Repository should contain newly added statement", testCon
				.hasStatement(alice, name, nameAlice, false));

		Repository tempRep = new SailRepository(new MemoryStore());
		tempRep.initialize();
		RepositoryConnection con = tempRep.getConnection();

		con.add(testCon.getStatements(null, null, null, false));

		assertTrue("Temp Repository should contain newly added statement", con
				.hasStatement(bob, name, nameBob, false));
		con.close();
		tempRep.shutDown();
	}

	public void testAddLiteralWithNewline() throws Exception {
		Literal test = vf.createLiteral("this is a test\n");
		testCon.add(bob, RDFS.LABEL, test);

		assertTrue("Repository should contain newly added statement", testCon
				.hasStatement(bob, RDFS.LABEL, test, false));
	}

	public void testTransactionIsolation() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasStatement(bob, name, nameBob, false));
		assertFalse(testCon2.hasStatement(bob, name, nameBob, false));

		testCon.commit();

		assertTrue(testCon.hasStatement(bob, name, nameBob, false));
		assertTrue(testCon2.hasStatement(bob, name, nameBob, false));
	}

	public void testAddReader() throws Exception {
		InputStream defaultGraphStream = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "default-graph.ttl");
		Reader defaultGraph = new InputStreamReader(defaultGraphStream, "UTF-8");

		testCon.add(defaultGraph, "", RDFFormat.TURTLE);

		defaultGraph.close();

		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameBob, false));
		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameAlice, false));

		// add file graph1.ttl to context1
		InputStream graph1Stream = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "graph1.ttl");
		Reader graph1 = new InputStreamReader(graph1Stream, "UTF-8");

		testCon.add(graph1, "", RDFFormat.TURTLE, context1);

		graph1.close();

		// add file graph2.ttl to context2
		InputStream graph2Stream = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "graph2.ttl");
		Reader graph2 = new InputStreamReader(graph2Stream, "UTF-8");

		testCon.add(graph2, "", RDFFormat.TURTLE, context2);

		graph2.close();

		assertTrue("alice should be known in the store", testCon.hasStatement(
				null, name, nameAlice, false));

		assertFalse("alice should not be known in context1", testCon
				.hasStatement(null, name, nameAlice, false, context1));
		assertTrue("alice should be known in context2", testCon.hasStatement(
				null, name, nameAlice, false, context2));

		assertTrue("bob should be known in the store", testCon.hasStatement(
				null, name, nameBob, false));

		assertFalse("bob should not be known in context2", testCon
				.hasStatement(null, name, nameBob, false, context2));
		assertTrue("bib should be known in context1", testCon.hasStatement(
				null, name, nameBob, false, context1));

	}

	public void testAddInputStream() throws Exception {
		// add file default-graph.ttl to repository, no context
		InputStream defaultGraph = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "default-graph.ttl");

		testCon.add(defaultGraph, "", RDFFormat.TURTLE);

		defaultGraph.close();

		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameBob, false));
		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameAlice, false));

		// add file graph1.ttl to context1
		InputStream graph1 = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "graph1.ttl");

		testCon.add(graph1, "", RDFFormat.TURTLE, context1);

		graph1.close();

		// add file graph2.ttl to context2
		InputStream graph2 = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "graph2.ttl");

		testCon.add(graph2, "", RDFFormat.TURTLE, context2);

		graph2.close();

		assertTrue("alice should be known in the store", testCon.hasStatement(
				null, name, nameAlice, false));

		assertFalse("alice should not be known in context1", testCon
				.hasStatement(null, name, nameAlice, false, context1));
		assertTrue("alice should be known in context2", testCon.hasStatement(
				null, name, nameAlice, false, context2));

		assertTrue("bob should be known in the store", testCon.hasStatement(
				null, name, nameBob, false));

		assertFalse("bob should not be known in context2", testCon
				.hasStatement(null, name, nameBob, false, context2));
		assertTrue("bib should be known in context1", testCon.hasStatement(
				null, name, nameBob, false, context1));

	}

	public void testAddGzipInputStream() throws Exception {
		// add file default-graph.ttl to repository, no context
		InputStream defaultGraph = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "default-graph.ttl.gz");
		try {
			testCon.add(defaultGraph, "", RDFFormat.TURTLE);
		} finally {
			defaultGraph.close();
		}

		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameBob, false));
		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameAlice, false));

	}

	public void testAddZipFile() throws Exception {
		InputStream in = RepositoryConnectionTest.class
				.getResourceAsStream(TEST_DIR_PREFIX + "graphs.zip");

		testCon.add(in, "", RDFFormat.TURTLE);

		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameBob, false));
		assertTrue("Repository should contain newly added statements", testCon
				.hasStatement(null, publisher, nameAlice, false));

		assertTrue("alice should be known in the store", testCon.hasStatement(
				null, name, nameAlice, false));

		assertTrue("bob should be known in the store", testCon.hasStatement(
				null, name, nameBob, false));
	}

	public void testAutoCommit() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(alice, name, nameAlice);

		assertTrue("Uncommitted update should be visible to own connection",
				testCon.hasStatement(alice, name, nameAlice, false));

		testCon.commit();

		assertTrue("Repository should contain statement after commit", testCon
				.hasStatement(alice, name, nameAlice, false));

		testCon.setAutoCommit(true);
	}

	public void testRollback() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(alice, name, nameAlice);

		assertTrue("Uncommitted updates should be visible to own connection",
				testCon.hasStatement(alice, name, nameAlice, false));

		testCon.rollback();

		assertFalse("Repository should not contain statement after rollback",
				testCon.hasStatement(alice, name, nameAlice, false));

		testCon.setAutoCommit(true);
	}

	public void testSimpleTupleQuery() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT name, mbox");
		queryBuilder.append(" FROM {} foaf:name {name};");
		queryBuilder.append("         foaf:mbox {mbox}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		TupleQueryResult result = testCon.prepareTupleQuery(
				QueryLanguage.SERQL, queryBuilder.toString()).evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("name"));
				assertTrue(solution.hasBinding("mbox"));

				Value nameResult = solution.getValue("name");
				Value mboxResult = solution.getValue("mbox");

				assertTrue((nameAlice.equals(nameResult) || nameBob
						.equals(nameResult)));
				assertTrue((mboxAlice.equals(mboxResult) || mboxBob
						.equals(mboxResult)));
			}
		} finally {
			result.close();
		}
	}

	public void testPrepareSeRQLQuery() throws Exception {

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT person");
		queryBuilder.append(" FROM {person} foaf:name {").append(
				Александър.getLabel()).append("}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		try {
			TupleQuery query = (TupleQuery) testCon.prepareQuery(
					QueryLanguage.SERQL, queryBuilder.toString());
		} catch (UnsupportedOperationException e) {
			fail("unsupported operation: " + e.getMessage());
		} catch (ClassCastException e) {
			fail("unexpected query object type: " + e.getMessage());
		}

		queryBuilder = new StringBuilder();
		queryBuilder.append(" (SELECT person");
		queryBuilder.append(" FROM {person} foaf:name {").append(
				Александър.getLabel()).append("}");
		queryBuilder.append(") UNION ");
		queryBuilder.append("(SELECT x FROM {x} p {y} )");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		try {
			TupleQuery query = (TupleQuery) testCon.prepareQuery(
					QueryLanguage.SERQL, queryBuilder.toString());
		} catch (UnsupportedOperationException e) {
			fail("unsupported operation: " + e.getMessage());
		} catch (ClassCastException e) {
			fail("unexpected query object type: " + e.getMessage());
		}
	}

	public void testPrepareSPARQLQuery() throws Exception {

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + ">");
		queryBuilder.append(" SELECT ?person");
		queryBuilder.append(" WHERE { ?person foaf:name ?y . }");

		try {
			TupleQuery query = (TupleQuery) testCon.prepareQuery(
					QueryLanguage.SPARQL, queryBuilder.toString());
		} catch (UnsupportedOperationException e) {
			fail("unsupported operation: " + e.getMessage());
		} catch (ClassCastException e) {
			fail("unexpected query object type: " + e.getMessage());
		}

		queryBuilder = new StringBuilder();
		queryBuilder.append(" BASE <http://base.uri>");
		queryBuilder.append(" PREFIX foaf: <" + FOAF_NS + ">");
		queryBuilder.append(" PREFIX ex: <http://example.org/>");
		queryBuilder.append(" PREFIX : <http://example.org/foo#>");
		queryBuilder.append(" SELECT ?person");
		queryBuilder.append(" WHERE { ?person foaf:name ?y . }");

		try {
			TupleQuery query = (TupleQuery) testCon.prepareQuery(
					QueryLanguage.SPARQL, queryBuilder.toString());
		} catch (UnsupportedOperationException e) {
			fail("unsupported operation: " + e.getMessage());
		} catch (ClassCastException e) {
			fail("unexpected query object type: " + e.getMessage());
		}
	}

	public void testSimpleTupleQueryUnicode() throws Exception {
		testCon.add(alexander, name, Александър);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT person");
		queryBuilder.append(" FROM {person} foaf:name {").append(
				Александър.getLabel()).append("}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		TupleQueryResult result = testCon.prepareTupleQuery(
				QueryLanguage.SERQL, queryBuilder.toString()).evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("person"));
				assertEquals(alexander, solution.getValue("person"));
			}
		} finally {
			result.close();
		}
	}

	public void testPreparedTupleQuery() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT name, mbox");
		queryBuilder.append(" FROM {} foaf:name {name};");
		queryBuilder.append("         foaf:mbox {mbox}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SERQL,
				queryBuilder.toString());
		query.setBinding("name", nameBob);

		TupleQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("name"));
				assertTrue(solution.hasBinding("mbox"));

				Value nameResult = solution.getValue("name");
				Value mboxResult = solution.getValue("mbox");

				assertEquals("unexpected value for name: " + nameResult,
						nameBob, nameResult);
				assertEquals("unexpected value for mbox: " + mboxResult,
						mboxBob, mboxResult);
			}
		} finally {
			result.close();
		}
	}

	public void testPreparedTupleQuery2() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT name, mbox");
		queryBuilder.append(" FROM {p} foaf:name {name};");
		queryBuilder.append("         foaf:mbox {mbox}");
		queryBuilder.append(" WHERE p = VAR");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SERQL,
				queryBuilder.toString());
		query.setBinding("VAR", bob);

		TupleQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("name"));
				assertTrue(solution.hasBinding("mbox"));

				Value nameResult = solution.getValue("name");
				Value mboxResult = solution.getValue("mbox");

				assertEquals("unexpected value for name: " + nameResult,
						nameBob, nameResult);
				assertEquals("unexpected value for mbox: " + mboxResult,
						mboxBob, mboxResult);
			}
		} finally {
			result.close();
		}
	}

	public void testPreparedTupleQueryUnicode() throws Exception {
		testCon.add(alexander, name, Александър);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT person");
		queryBuilder.append(" FROM {person} foaf:name {name}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SERQL,
				queryBuilder.toString());
		query.setBinding("name", Александър);

		TupleQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				BindingSet solution = result.next();
				assertTrue(solution.hasBinding("person"));
				assertEquals(alexander, solution.getValue("person"));
			}
		} finally {
			result.close();
		}
	}

	public void testSimpleGraphQuery() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" CONSTRUCT *");
		queryBuilder.append(" FROM {} foaf:name {name};");
		queryBuilder.append("         foaf:mbox {mbox}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		GraphQueryResult result = testCon.prepareGraphQuery(
				QueryLanguage.SERQL, queryBuilder.toString()).evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				Statement st = result.next();
				if (name.equals(st.getPredicate())) {
					assertTrue(nameAlice.equals(st.getObject())
							|| nameBob.equals(st.getObject()));
				} else {
					assertTrue(mbox.equals(st.getPredicate()));
					assertTrue(mboxAlice.equals(st.getObject())
							|| mboxBob.equals(st.getObject()));
				}
			}
		} finally {
			result.close();
		}
	}

	public void testPreparedGraphQuery() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" CONSTRUCT *");
		queryBuilder.append(" FROM {} foaf:name {name};");
		queryBuilder.append("         foaf:mbox {mbox}");
		queryBuilder.append(" USING NAMESPACE foaf = <" + FOAF_NS + ">");

		GraphQuery query = testCon.prepareGraphQuery(QueryLanguage.SERQL,
				queryBuilder.toString());
		query.setBinding("name", nameBob);

		GraphQueryResult result = query.evaluate();

		try {
			assertTrue(result != null);
			assertTrue(result.hasNext());

			while (result.hasNext()) {
				Statement st = result.next();
				assertTrue(name.equals(st.getPredicate())
						|| mbox.equals(st.getPredicate()));
				if (name.equals(st.getPredicate())) {
					assertTrue("unexpected value for name: " + st.getObject(),
							nameBob.equals(st.getObject()));
				} else {
					assertTrue(mbox.equals(st.getPredicate()));
					assertTrue("unexpected value for mbox: " + st.getObject(),
							mboxBob.equals(st.getObject()));
				}

			}
		} finally {
			result.close();
		}
	}

	public void testSimpleBooleanQuery() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append("ASK ");
		queryBuilder.append("{ ?p foaf:name ?name }");

		boolean exists = testCon.prepareBooleanQuery(QueryLanguage.SPARQL,
				queryBuilder.toString()).evaluate();

		assertTrue(exists);
	}

	public void testPreparedBooleanQuery() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append("ASK ");
		queryBuilder.append("{ ?p foaf:name ?name }");

		BooleanQuery query = testCon.prepareBooleanQuery(QueryLanguage.SPARQL,
				queryBuilder.toString());
		query.setBinding("name", nameBob);

		assertTrue(query.evaluate());
	}

	public void testDataset() throws Exception {
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);

		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append("ASK ");
		queryBuilder.append("{ ?p foaf:name ?name }");

		BooleanQuery query = testCon.prepareBooleanQuery(QueryLanguage.SPARQL,
				queryBuilder.toString());
		query.setBinding("name", nameBob);

		assertTrue(query.evaluate());

		DatasetImpl dataset = new DatasetImpl();

		// default graph: {context1}
		dataset.addDefaultGraph(context1);
		query.setDataset(dataset);
		assertTrue(query.evaluate());

		// default graph: {context1, context2}
		dataset.addDefaultGraph(context2);
		query.setDataset(dataset);
		assertTrue(query.evaluate());

		// default graph: {context2}
		dataset.removeDefaultGraph(context1);
		query.setDataset(dataset);
		assertFalse(query.evaluate());

		queryBuilder.setLength(0);
		queryBuilder.append("PREFIX foaf: <" + FOAF_NS + "> ");
		queryBuilder.append("ASK ");
		queryBuilder.append("{ GRAPH ?g { ?p foaf:name ?name } }");

		query = testCon.prepareBooleanQuery(QueryLanguage.SPARQL, queryBuilder
				.toString());
		query.setBinding("name", nameBob);

		// default graph: {context2}; named graph: {}
		query.setDataset(dataset);
		assertFalse(query.evaluate());

		// default graph: {context1, context2}; named graph: {context2}
		dataset.addDefaultGraph(context1);
		dataset.addNamedGraph(context2);
		query.setDataset(dataset);
		assertFalse(query.evaluate());

		// default graph: {context1, context2}; named graph: {context1,
		// context2}
		dataset.addNamedGraph(context1);
		query.setDataset(dataset);
		assertTrue(query.evaluate());
	}

	public void testGetStatements() throws Exception {
		testCon.add(bob, name, nameBob);

		assertTrue("Repository should contain statement", testCon.hasStatement(
				bob, name, nameBob, false));

		RepositoryResult<Statement> result = testCon.getStatements(null, name,
				null, false);

		try {
			assertTrue("Iterator should not be null", result != null);
			assertTrue("Iterator should not be empty", result.hasNext());

			while (result.hasNext()) {
				Statement st = result.next();
				assertNull("Statement should not be in a context ", st
						.getContext());
				assertTrue("Statement predicate should be equal to name ", st
						.getPredicate().equals(name));
			}
		} finally {
			result.close();
		}

		List<Statement> list = Iterations.addAll(testCon.getStatements(null,
				name, null, false), new ArrayList<Statement>());

		assertTrue("List should not be null", list != null);
		assertFalse("List should not be empty", list.isEmpty());
	}

	public void testGetStatementsInSingleContext() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);

		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);
		testCon.setAutoCommit(true);

		assertTrue("Repository should contain statement", testCon.hasStatement(
				bob, name, nameBob, false));

		assertTrue("Repository should contain statement in context1", testCon
				.hasStatement(bob, name, nameBob, false, context1));

		assertFalse("Repository should not contain statement in context2",
				testCon.hasStatement(bob, name, nameBob, false, context2));

		// Check handling of getStatements without context IDs
		RepositoryResult<Statement> result = testCon.getStatements(bob, name,
				null, false);
		try {
			while (result.hasNext()) {
				Statement st = result.next();
				assertTrue(bob.equals(st.getSubject()));
				assertTrue(name.equals(st.getPredicate()));
				assertTrue(nameBob.equals(st.getObject()));
				assertTrue(context1.equals(st.getContext()));
			}
		} finally {
			result.close();
		}

		// Check handling of getStatements with a known context ID
		result = testCon.getStatements(null, null, null, false, context1);
		try {
			while (result.hasNext()) {
				Statement st = result.next();
				assertTrue(context1.equals(st.getContext()));
			}
		} finally {
			result.close();
		}

		// Check handling of getStatements with an unknown context ID
		result = testCon.getStatements(null, null, null, false, unknownContext);
		try {
			assertTrue(result != null);
			assertFalse(result.hasNext());
		} finally {
			result.close();
		}

		List<Statement> list = Iterations.addAll(testCon.getStatements(null,
				name, null, false, context1), new ArrayList<Statement>());

		assertTrue("List should not be null", list != null);
		assertFalse("List should not be empty", list.isEmpty());
	}

	public void testGetStatementsInMultipleContexts() throws Exception {
		testCon.clear();

		testCon.setAutoCommit(false);
		testCon.add(alice, name, nameAlice, context2);
		testCon.add(alice, mbox, mboxAlice, context2);
		testCon.add(context2, publisher, nameAlice);
		testCon.setAutoCommit(true);

		// get statements with either no context or context2
		CloseableIteration<? extends Statement, RepositoryException> iter = testCon
				.getStatements(null, null, null, false, null, context2);

		try {
			int count = 0;
			while (iter.hasNext()) {
				count++;
				Statement st = iter.next();

				assertTrue(st.getContext() == null
						|| context2.equals(st.getContext()));
			}

			assertEquals("there should be three statements", 3, count);
		} finally {
			iter.close();
		}

		// get all statements with context1 or context2. Note that context1 and
		// context2 are both known
		// in the store because they have been created through the store's own
		// value vf.
		iter = testCon.getStatements(null, null, null, false, context1,
				context2);

		try {
			int count = 0;
			while (iter.hasNext()) {
				count++;
				Statement st = iter.next();
				// we should have _only_ statements from context2
				assertTrue(context2.equals(st.getContext()));
			}
			assertEquals("there should be two statements", 2, count);
		} finally {
			iter.close();
		}

		// get all statements with unknownContext or context2.
		iter = testCon.getStatements(null, null, null, false, unknownContext,
				context2);

		try {
			int count = 0;
			while (iter.hasNext()) {
				count++;
				Statement st = iter.next();
				// we should have _only_ statements from context2
				assertTrue(context2.equals(st.getContext()));
			}
			assertEquals("there should be two statements", 2, count);
		} finally {
			iter.close();
		}

		// add statements to context1
		testCon.setAutoCommit(false);
		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, mbox, mboxBob, context1);
		testCon.add(context1, publisher, nameBob);
		testCon.setAutoCommit(true);

		iter = testCon.getStatements(null, null, null, false, context1);
		try {
			assertTrue(iter != null);
			assertTrue(iter.hasNext());
		} finally {
			iter.close();
		}

		// get statements with either no context or context2
		iter = testCon.getStatements(null, null, null, false, null, context2);
		try {
			int count = 0;
			while (iter.hasNext()) {
				count++;
				Statement st = iter.next();
				// we should have _only_ statements from context2, or without
				// context
				assertTrue(st.getContext() == null
						|| context2.equals(st.getContext()));
			}
			assertEquals("there should be four statements", 4, count);
		} finally {
			iter.close();
		}

		// get all statements with context1 or context2
		iter = testCon.getStatements(null, null, null, false, context1,
				context2);

		try {
			int count = 0;
			while (iter.hasNext()) {
				count++;
				Statement st = iter.next();
				assertTrue(context1.equals(st.getContext())
						|| context2.equals(st.getContext()));
			}
			assertEquals("there should be four statements", 4, count);
		} finally {
			iter.close();
		}
	}

	public void testDuplicateFilter() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(bob, name, nameBob);
		testCon.add(bob, name, nameBob, context1);
		testCon.add(bob, name, nameBob, context2);
		testCon.setAutoCommit(true);

		RepositoryResult<Statement> result = testCon.getStatements(bob, name,
				null, true);
		result.enableDuplicateFilter();

		int count = 0;
		while (result.hasNext()) {
			result.next();
			count++;
		}
		assertEquals(1, count);
	}

	public void testRemoveStatements() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(bob, name, nameBob);
		testCon.add(alice, name, nameAlice);
		testCon.setAutoCommit(true);

		assertTrue(testCon.hasStatement(bob, name, nameBob, false));
		assertTrue(testCon.hasStatement(alice, name, nameAlice, false));

		testCon.remove(bob, name, nameBob);

		assertFalse(testCon.hasStatement(bob, name, nameBob, false));
		assertTrue(testCon.hasStatement(alice, name, nameAlice, false));

		testCon.remove(alice, null, null);
		assertFalse(testCon.hasStatement(alice, name, nameAlice, false));
		assertTrue(testCon.isEmpty());
	}

	public void testRemoveStatementCollection() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(alice, name, nameAlice);
		testCon.add(bob, name, nameBob);
		testCon.setAutoCommit(true);

		assertTrue(testCon.hasStatement(bob, name, nameBob, false));
		assertTrue(testCon.hasStatement(alice, name, nameAlice, false));

		Collection<Statement> c = Iterations.addAll(testCon.getStatements(null,
				null, null, false), new ArrayList<Statement>());

		testCon.remove(c);

		assertFalse(testCon.hasStatement(bob, name, nameBob, false));
		assertFalse(testCon.hasStatement(alice, name, nameAlice, false));
	}

	public void testRemoveStatementIteration() throws Exception {
		testCon.setAutoCommit(false);
		testCon.add(alice, name, nameAlice);
		testCon.add(bob, name, nameBob);
		testCon.setAutoCommit(true);

		assertTrue(testCon.hasStatement(bob, name, nameBob, false));
		assertTrue(testCon.hasStatement(alice, name, nameAlice, false));

		CloseableIteration<? extends Statement, RepositoryException> iter = testCon
				.getStatements(null, null, null, false);

		try {
			testCon.remove(iter);
		} finally {
			iter.close();
		}

		assertFalse(testCon.hasStatement(bob, name, nameBob, false));
		assertFalse(testCon.hasStatement(alice, name, nameAlice, false));

	}

	public void testGetNamespaces() throws Exception {
		StringBuilder rdfFragment = new StringBuilder();
		rdfFragment.append("<rdf:RDF\n");
		rdfFragment.append("    xmlns:example='http://example.org/'\n");
		rdfFragment
				.append("    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n");
		rdfFragment
				.append("    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' >\n");
		rdfFragment
				.append("  <rdf:Description rdf:about='http://example.org/Main'>\n");
		rdfFragment.append("    <rdfs:label>Main Node</rdfs:label>\n");
		rdfFragment.append("  </rdf:Description>\n");
		rdfFragment.append("</rdf:RDF>");

		testCon.add(new StringReader(rdfFragment.toString()), "",
				RDFFormat.RDFXML);

		CloseableIteration<? extends Namespace, RepositoryException> nsIter = testCon
				.getNamespaces();
		try {
			Map<String, String> map = new HashMap<String, String>();
			int nsCount = 0;
			while (nsIter.hasNext()) {
				nsCount++;
				Namespace ns = nsIter.next();
				map.put(ns.getPrefix(), ns.getName());
			}

			assertEquals("There should be exactly three namespaces", 3, nsCount);
			assertTrue("namespace for prefix 'example' should exist", map
					.containsKey("example"));
			assertTrue("namespace for prefix 'rdfs' should exist", map
					.containsKey("rdfs"));
			assertTrue("namespace for prefix 'rdf' should exist", map
					.containsKey("rdf"));

			assertTrue("namespace name for 'example' not well-defined", map
					.get("example").equals("http://example.org/"));
			assertTrue("namespace name for 'rdfs' not well-defined", map.get(
					"rdfs").equals("http://www.w3.org/2000/01/rdf-schema#"));
			assertTrue("namespace name for 'rdf' not well-defined", map.get(
					"rdf")
					.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		} finally {
			nsIter.close();
		}
	}

	public void testClear() throws Exception {
		testCon.add(bob, name, nameBob);
		assertTrue(testCon.hasStatement(null, name, nameBob, false));
		testCon.clear();
		assertFalse(testCon.hasStatement(null, name, nameBob, false));
	}

	public void testRecoverFromParseError() throws RepositoryException,
			IOException {
		String invalidData = "bad";
		String validData = "@prefix foo: <http://example.org/foo#>.\nfoo:a foo:b foo:c.";

		try {
			testCon.add(new StringReader(invalidData), "", RDFFormat.TURTLE);
			fail("Invalid data should result in an exception");
		} catch (RDFParseException e) {
			// Expected behaviour
		}

		try {
			testCon.add(new StringReader(validData), "", RDFFormat.TURTLE);
		} catch (RDFParseException e) {
			fail("Valid data should not result in an exception");
		}

		assertEquals("Repository contains incorrect number of statements", 1,
				testCon.size());
	}

	public void testStatementSerialization() throws Exception {
		testCon.add(bob, name, nameBob);

		Statement st;
		RepositoryResult<Statement> statements = testCon.getStatements(null,
				null, null, true);
		try {
			st = statements.next();
		} finally {
			statements.close();
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(st);
		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		Statement deserializedStatement = (Statement) in.readObject();
		in.close();

		assertTrue(st.equals(deserializedStatement));

		assertTrue(testCon.hasStatement(st, true));
		assertTrue(testCon.hasStatement(deserializedStatement, true));
	}

	public void testBNodeSerialization() throws Exception {
		testCon.add(bob, name, nameBob);

		Statement st;
		RepositoryResult<Statement> statements = testCon.getStatements(null,
				null, null, false);
		try {
			st = statements.next();
		} finally {
			statements.close();
		}

		BNode bnode = (BNode) st.getSubject();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(bnode);
		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		BNode deserializedBNode = (BNode) in.readObject();
		in.close();

		assertTrue(bnode.equals(deserializedBNode));

		assertTrue(testCon.hasStatement(bnode, name, nameBob, true));
		assertTrue(testCon.hasStatement(deserializedBNode, name, nameBob, true));
	}

	public void testURISerialization() throws Exception {
		testCon.add(bob, name, nameBob);

		Statement st;
		RepositoryResult<Statement> statements = testCon.getStatements(null,
				null, null, false);
		try {
			st = statements.next();
		} finally {
			statements.close();
		}

		URI uri = st.getPredicate();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(uri);
		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		URI deserializedURI = (URI) in.readObject();
		in.close();

		assertTrue(uri.equals(deserializedURI));

		assertTrue(testCon.hasStatement(bob, uri, nameBob, true));
		assertTrue(testCon.hasStatement(bob, deserializedURI, nameBob, true));
	}

	public void testLiteralSerialization() throws Exception {
		testCon.add(bob, name, nameBob);

		Statement st;
		RepositoryResult<Statement> statements = testCon.getStatements(null,
				null, null, false);
		try {
			st = statements.next();
		} finally {
			statements.close();
		}

		Literal literal = (Literal) st.getObject();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(literal);
		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		Literal deserializedLiteral = (Literal) in.readObject();
		in.close();

		assertTrue(literal.equals(deserializedLiteral));

		assertTrue(testCon.hasStatement(bob, name, literal, true));
		assertTrue(testCon.hasStatement(bob, name, deserializedLiteral, true));
	}

	public void testGraphSerialization() throws Exception {
		testCon.add(bob, name, nameBob);
		testCon.add(alice, name, nameAlice);

		Graph graph;
		RepositoryResult<Statement> statements = testCon.getStatements(null,
				null, null, true);
		try {
			graph = new GraphImpl(vf, statements.asList());
		} finally {
			statements.close();
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(graph);
		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		Graph deserializedGraph = (Graph) in.readObject();
		in.close();

		assertFalse(deserializedGraph.isEmpty());

		for (Statement st : deserializedGraph) {
			assertTrue(graph.contains(st));
			assertTrue(testCon.hasStatement(st, true));
		}
	}

	public void testEmptyRollback() throws Exception {
		assertTrue(testCon.isEmpty());
		assertTrue(testCon2.isEmpty());
		testCon.setAutoCommit(false);
		testCon.add(vf.createBNode(), vf.createURI("urn:pred"), vf
				.createBNode());
		assertFalse(testCon.isEmpty());
		assertTrue(testCon2.isEmpty());
		testCon.rollback();
		assertTrue(testCon.isEmpty());
		assertTrue(testCon2.isEmpty());
	}

	public void testEmptyCommit() throws Exception {
		assertTrue(testCon.isEmpty());
		assertTrue(testCon2.isEmpty());
		testCon.setAutoCommit(false);
		testCon.add(vf.createBNode(), vf.createURI("urn:pred"), vf
				.createBNode());
		assertFalse(testCon.isEmpty());
		assertTrue(testCon2.isEmpty());
		testCon.commit();
		assertFalse(testCon.isEmpty());
		assertFalse(testCon2.isEmpty());
	}

	public void testOpen() throws Exception {
		assertTrue(testCon.isOpen());
		assertTrue(testCon2.isOpen());
		testCon.close();
		assertFalse(testCon.isOpen());
		assertTrue(testCon2.isOpen());
	}

	public void testSizeRollback() throws Exception {
		assertEquals(0, testCon.size());
		assertEquals(0, testCon2.size());
		testCon.setAutoCommit(false);
		testCon.add(vf.createBNode(), vf.createURI("urn:pred"), vf
				.createBNode());
		assertEquals(1, testCon.size());
		assertEquals(0, testCon2.size());
		testCon.add(vf.createBNode(), vf.createURI("urn:pred"), vf
				.createBNode());
		assertEquals(2, testCon.size());
		assertEquals(0, testCon2.size());
		testCon.rollback();
		assertEquals(0, testCon.size());
		assertEquals(0, testCon2.size());
	}

	public void testSizeCommit() throws Exception {
		assertEquals(0, testCon.size());
		assertEquals(0, testCon2.size());
		testCon.setAutoCommit(false);
		testCon.add(vf.createBNode(), vf.createURI("urn:pred"), vf
				.createBNode());
		assertEquals(1, testCon.size());
		assertEquals(0, testCon2.size());
		testCon.add(vf.createBNode(), vf.createURI("urn:pred"), vf
				.createBNode());
		assertEquals(2, testCon.size());
		assertEquals(0, testCon2.size());
		testCon.commit();
		assertEquals(2, testCon.size());
		assertEquals(2, testCon2.size());
	}

	public void testAddRemove() throws Exception {
		URI FOAF_PERSON = vf.createURI("http://xmlns.com/foaf/0.1/Person");
		final Statement stmt = vf.createStatement(bob, name, nameBob);

		testCon.add(bob, RDF.TYPE, FOAF_PERSON);

		testCon.setAutoCommit(false);
		testCon.add(stmt);
		testCon.remove(stmt);
		testCon.commit();

		testCon.exportStatements(null, null, null, false, new RDFHandlerBase() {

			@Override
			public void handleStatement(Statement st)
					throws RDFHandlerException {
				assertTrue(!stmt.equals(st));
			}
		});
	}

	public void testInferredStatementCount() throws Exception {
		assertTrue(testCon.isEmpty());
		int inferred = getTotalStatementCount(testCon);

		URI root = vf.createURI("urn:root");

		testCon.add(root, RDF.TYPE, RDF.LIST);
		testCon.remove(root, RDF.TYPE, RDF.LIST);

		assertTrue(testCon.isEmpty());
		assertEquals(inferred, getTotalStatementCount(testCon));
	}

	public void testGetContextIDs() throws Exception {
		assertEquals(0, testCon.getContextIDs().asList().size());

		// load data
		testCon.setAutoCommit(false);
		testCon.add(bob, name, nameBob, context1);
		assertEquals(Arrays.asList(context1), testCon.getContextIDs().asList());

		testCon.remove(bob, name, nameBob, context1);
		assertEquals(0, testCon.getContextIDs().asList().size());
		testCon.setAutoCommit(true);

		assertEquals(0, testCon.getContextIDs().asList().size());

		testCon.add(bob, name, nameBob, context2);
		assertEquals(Arrays.asList(context2), testCon.getContextIDs().asList());
	}

	public void testXmlCalendarZ() throws Exception {
		String NS = "http://example.org/rdf/";
		int OFFSET = TimeZone.getDefault().getOffset(
				new Date(2007, Calendar.NOVEMBER, 6).getTime()) / 1000 / 60;
		String SELECT_BY_DATE = "SELECT ?s ?d WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> ?d . FILTER (?d <= ?date) }";
		DatatypeFactory data = DatatypeFactory.newInstance();
		for (int i = 1; i < 5; i++) {
			URI uri = vf.createURI(NS, "date" + i);
			XMLGregorianCalendar xcal = data.newXMLGregorianCalendar();
			xcal.setYear(2000);
			xcal.setMonth(11);
			xcal.setDay(i * 2);
			testCon.add(uri, RDF.VALUE, vf.createLiteral(xcal));
			URI uriz = vf.createURI(NS, "dateZ" + i);
			xcal = data.newXMLGregorianCalendar();
			xcal.setYear(2007);
			xcal.setMonth(11);
			xcal.setDay(i * 2);
			xcal.setTimezone(OFFSET);
			testCon.add(uriz, RDF.VALUE, vf.createLiteral(xcal));
		}
		XMLGregorianCalendar xcal = data.newXMLGregorianCalendar();
		xcal.setYear(2007);
		xcal.setMonth(11);
		xcal.setDay(6);
		xcal.setTimezone(OFFSET);
		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL,
				SELECT_BY_DATE);
		query.setBinding("date", vf.createLiteral(xcal));
		TupleQueryResult result = query.evaluate();
		List<BindingSet> list = new ArrayList<BindingSet>();
		while (result.hasNext()) {
			list.add(result.next());
		}
		assertEquals(7, list.size());
	}

	public void testOptionalFilter() throws Exception {
		String optional = "{ ?s :p1 ?v1 OPTIONAL {?s :p2 ?v2 FILTER(?v1<3) } }";
		URI s = vf.createURI("urn:test:s");
		URI p1 = vf.createURI("urn:test:p1");
		URI p2 = vf.createURI("urn:test:p2");
		Literal v1 = vf.createLiteral(1);
		Literal v2 = vf.createLiteral(2);
		Literal v3 = vf.createLiteral(3);
		testCon.add(s, p1, v1);
		testCon.add(s, p2, v2);
		testCon.add(s, p1, v3);
		String qry = "PREFIX :<urn:test:> SELECT ?s ?v1 ?v2 WHERE " + optional;
		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL, qry);
		TupleQueryResult result = query.evaluate();
		Set<List<Value>> set = new HashSet<List<Value>>();
		while (result.hasNext()) {
			BindingSet bindings = result.next();
			set.add(Arrays.asList(bindings.getValue("v1"), bindings
					.getValue("v2")));
		}
		result.close();
		assertTrue(set.contains(Arrays.asList(v1, v2)));
		assertTrue(set.contains(Arrays.asList(v3, null)));
	}

	public void testOrPredicate() throws Exception {
		String union = "{ :s ?p :o FILTER (?p = :p1 || ?p = :p2) }";
		URI s = vf.createURI("urn:test:s");
		URI p1 = vf.createURI("urn:test:p1");
		URI p2 = vf.createURI("urn:test:p2");
		URI o = vf.createURI("urn:test:o");
		testCon.add(s, p1, o);
		testCon.add(s, p2, o);
		String qry = "PREFIX :<urn:test:> SELECT ?p WHERE " + union;
		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL, qry);
		TupleQueryResult result = query.evaluate();
		List<Value> list = new ArrayList<Value>();
		while (result.hasNext()) {
			BindingSet bindings = result.next();
			list.add(bindings.getValue("p"));
		}
		result.close();
		assertTrue(list.contains(p1));
		assertTrue(list.contains(p2));
	}

	public void testSES713() throws Exception {
		String queryString = "SELECT * { ?sub ?pred ?obj . FILTER ( 'not a number' + 1 = ?obj )}";

		TupleQuery query = testCon.prepareTupleQuery(QueryLanguage.SPARQL,
				queryString);
		TupleQueryResult tqr = query.evaluate();
		try {
			assertFalse("Query should not return any results", tqr.hasNext());
		} finally {
			tqr.close();
		}
	}

	public void testOrderByQueriesAreInterruptable() throws Exception {
		testCon.setAutoCommit(false);
		for (int index = 0; index < 512; index++) {
			testCon.add(RDFS.CLASS, RDFS.COMMENT, testCon.getValueFactory()
					.createBNode());
		}
		testCon.setAutoCommit(true);

		TupleQuery query = testCon
				.prepareTupleQuery(
						QueryLanguage.SPARQL,
						"SELECT * WHERE { ?s ?p ?o . ?s1 ?p1 ?o1 . ?s2 ?p2 ?o2 . ?s3 ?p3 ?o3 } ORDER BY ?s1 ?p1 ?o1 LIMIT 1000");
		query.setMaxQueryTime(2);

		TupleQueryResult result = query.evaluate();
		long startTime = System.currentTimeMillis();
		try {
			result.hasNext();
			fail("Query should have been interrupted");
		} catch (QueryInterruptedException e) {
			// Expected
			long duration = System.currentTimeMillis() - startTime;

			assertTrue(
					"Query not interrupted quickly enough, should have been ~2s, but was "
							+ (duration / 1000) + "s", duration < 5000);
		}
	}

	public void testQueryDefaultGraph() throws Exception {
		URI graph = vf.createURI("urn:test:default");
		testCon.add(vf.createURI("urn:test:s1"), vf.createURI("urn:test:p1"),
				vf.createURI("urn:test:o1"));
		assertEquals(0, size(graph));
		testCon.add(vf.createURI("urn:test:s2"), vf.createURI("urn:test:p2"),
				vf.createURI("urn:test:o2"), graph);
		assertEquals(1, size(graph));
	}

	public void testQueryBaseURI() throws Exception {
		testCon.add(vf.createURI("urn:test:s1"), vf.createURI("urn:test:p1"),
				vf.createURI("urn:test:o1"));
		TupleQueryResult rs = testCon.prepareTupleQuery(QueryLanguage.SPARQL,
				"SELECT * { <> ?p ?o }", "urn:test:s1").evaluate();
		try {
			assertTrue(rs.hasNext());
		} finally {
			rs.close();
		}
	}

	public void testUpdateBaseURI() throws Exception {
		testCon.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <> a <> }",
				"urn:test:s1").execute();
		assertEquals(1, testCon.size());
	}

	public void testDeleteDefaultGraph() throws Exception {
		URI g1 = vf.createURI("urn:test:g1");
		URI g2 = vf.createURI("urn:test:g2");
		testCon.add(vf.createURI("urn:test:s1"), vf.createURI("urn:test:p1"),
				vf.createURI("urn:test:o1"), g1);
		testCon.add(vf.createURI("urn:test:s2"), vf.createURI("urn:test:p2"),
				vf.createURI("urn:test:o2"), g2);
		Update up = testCon.prepareUpdate(QueryLanguage.SPARQL,
				"DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }");
		DatasetImpl ds = new DatasetImpl();
		ds.addDefaultGraph(g1);
		ds.addDefaultRemoveGraph(g1);
		up.setDataset(ds);
		up.execute();
		assertEquals(0, size(g1));
		assertEquals(1, size(g2));
	}

	public void testDefaultContext() throws Exception {
		ContextAwareConnection con = new ContextAwareConnection(testCon);
		URI defaultGraph = vf.createURI("urn:test:default");
		con.setReadContexts(defaultGraph);
		con.setInsertContext(defaultGraph);
		con.setRemoveContexts(defaultGraph);
		con.add(vf.createURI("urn:test:s1"), vf.createURI("urn:test:p1"), vf
				.createURI("urn:test:o1"));
		con.prepareUpdate("INSERT DATA { <urn:test:s2> <urn:test:p2> \"l2\" }")
				.execute();
		assertEquals(2, con.getStatements(null, null, null).asList().size());
		assertEquals(2, con.getStatements(null, null, null, defaultGraph)
				.asList().size());
		assertEquals(2, size(defaultGraph));
		con.add(vf.createURI("urn:test:s3"), vf.createURI("urn:test:p3"), vf
				.createURI("urn:test:o3"), (Resource) null);
		con.add(vf.createURI("urn:test:s4"), vf.createURI("urn:test:p4"), vf
				.createURI("urn:test:o4"), vf.createURI("urn:test:other"));
		assertEquals(3, con.getStatements(null, null, null).asList().size());
		assertEquals(4, testCon.getStatements(null, null, null, true).asList()
				.size());
		assertEquals(3, size(defaultGraph));
		assertEquals(1, size(vf.createURI("urn:test:other")));
		con.prepareUpdate("DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }").execute();
		assertEquals(0, con.getStatements(null, null, null).asList().size());
		assertEquals(1, testCon.getStatements(null, null, null, true).asList()
				.size());
		assertEquals(0, size(defaultGraph));
		assertEquals(1, size(vf.createURI("urn:test:other")));
	}

	public void testDefaultInsertContext() throws Exception {
		ContextAwareConnection con = new ContextAwareConnection(testCon);
		URI defaultGraph = vf.createURI("urn:test:default");
		con.setInsertContext(defaultGraph);
		con.add(vf.createURI("urn:test:s1"), vf.createURI("urn:test:p1"), vf
				.createURI("urn:test:o1"));
		con.prepareUpdate("INSERT DATA { <urn:test:s2> <urn:test:p2> \"l2\" }")
				.execute();
		assertEquals(2, con.getStatements(null, null, null).asList().size());
		assertEquals(2, con.getStatements(null, null, null, defaultGraph)
				.asList().size());
		assertEquals(2, size(defaultGraph));
		con.add(vf.createURI("urn:test:s3"), vf.createURI("urn:test:p3"), vf
				.createURI("urn:test:o3"), (Resource) null);
		con.add(vf.createURI("urn:test:s4"), vf.createURI("urn:test:p4"), vf
				.createURI("urn:test:o4"), vf.createURI("urn:test:other"));
		assertEquals(4, con.getStatements(null, null, null).asList().size());
		assertEquals(3, con.getStatements(null, null, null, defaultGraph)
				.asList().size());
		assertEquals(4, testCon.getStatements(null, null, null, true).asList()
				.size());
		assertEquals(3, size(defaultGraph));
		assertEquals(1, size(vf.createURI("urn:test:other")));
		con.prepareUpdate("DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }").execute();
		assertEquals(0, con.getStatements(null, null, null).asList().size());
		assertEquals(0, testCon.getStatements(null, null, null, true).asList()
				.size());
		assertEquals(0, size(defaultGraph));
		assertEquals(0, size(vf.createURI("urn:test:other")));
	}

	public void testExclusiveNullContext() throws Exception {
		ContextAwareConnection con = new ContextAwareConnection(testCon);
		URI defaultGraph = null; // null context
		con.setReadContexts(defaultGraph);
		con.setInsertContext(defaultGraph);
		con.setRemoveContexts(defaultGraph);
		con.add(vf.createURI("urn:test:s1"), vf.createURI("urn:test:p1"), vf
				.createURI("urn:test:o1"));
		con.prepareUpdate("INSERT DATA { <urn:test:s2> <urn:test:p2> \"l2\" }")
				.execute();
		assertEquals(2, con.getStatements(null, null, null).asList().size());
		assertEquals(2, con.getStatements(null, null, null, defaultGraph)
				.asList().size());
		assertEquals(2, size(defaultGraph));
		con.add(vf.createURI("urn:test:s3"), vf.createURI("urn:test:p3"), vf
				.createURI("urn:test:o3"), (Resource) null);
		con.add(vf.createURI("urn:test:s4"), vf.createURI("urn:test:p4"), vf
				.createURI("urn:test:o4"), vf.createURI("urn:test:other"));
		assertEquals(3, con.getStatements(null, null, null).asList().size());
		assertEquals(4, testCon.getStatements(null, null, null, true).asList()
				.size());
		assertEquals(3, size(defaultGraph));
		assertEquals(1, size(vf.createURI("urn:test:other")));
		con.prepareUpdate("DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }").execute();
		assertEquals(0, con.getStatements(null, null, null).asList().size());
		assertEquals(1, testCon.getStatements(null, null, null, true).asList()
				.size());
		assertEquals(0, size(defaultGraph));
		assertEquals(1, size(vf.createURI("urn:test:other")));
	}

	private int size(URI defaultGraph) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		TupleQuery qry = testCon.prepareTupleQuery(QueryLanguage.SPARQL,
				"SELECT * { ?s ?p ?o }");
		DatasetImpl dataset = new DatasetImpl();
		dataset.addDefaultGraph(defaultGraph);
		qry.setDataset(dataset);
		TupleQueryResult result = qry.evaluate();
		try {
			int count = 0;
			while (result.hasNext()) {
				result.next();
				count++;
			}
			return count;
		} finally {
			result.close();
		}
	}

	private int getTotalStatementCount(RepositoryConnection connection)
			throws RepositoryException {
		CloseableIteration<? extends Statement, RepositoryException> iter = connection
				.getStatements(null, null, null, true);

		try {
			int size = 0;

			while (iter.hasNext()) {
				iter.next();
				++size;
			}

			return size;
		} finally {
			iter.close();
		}
	}
}