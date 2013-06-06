/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.File;

import junit.framework.TestCase;

import info.aduna.io.FileUtil;
import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

public class StoreSerializationTest extends TestCase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dataDir;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dataDir = FileUtil.createTempDir("memorystore");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		FileUtil.deleteDir(dataDir);
	}

	public void testSerialization() throws Exception {
		MemoryStore store = new MemoryStore(dataDir);
		store.initialize();

		ValueFactory factory = store.getValueFactory();
		URI foo = factory.createURI("http://www.foo.example/foo");
		URI bar = factory.createURI("http://www.foo.example/bar");

		SailConnection con = store.getConnection();
		con.addStatement(foo, RDF.TYPE, bar);
		con.commit();

		ParsedTupleQuery query = QueryParserUtil.parseTupleQuery(
				QueryLanguage.SERQL, "SELECT X, P, Y FROM {X} P {Y}", null);
		TupleExpr tupleExpr = query.getTupleExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> iter = con
				.evaluate(tupleExpr, null, EmptyBindingSet.getInstance(), false);

		BindingSet bindingSet = iter.next();

		assertEquals(bindingSet.getValue("X"), foo);
		assertEquals(bindingSet.getValue("P"), RDF.TYPE);
		assertEquals(bindingSet.getValue("Y"), bar);
		iter.close();
		con.close();

		store.shutDown();

		store = new MemoryStore(dataDir);
		store.initialize();

		factory = store.getValueFactory();
		foo = factory.createURI("http://www.foo.example/foo");
		bar = factory.createURI("http://www.foo.example/bar");

		con = store.getConnection();

		iter = con.evaluate(tupleExpr, null, EmptyBindingSet.getInstance(),
				false);

		bindingSet = iter.next();

		assertEquals(bindingSet.getValue("X"), foo);
		assertEquals(bindingSet.getValue("P"), RDF.TYPE);
		assertEquals(bindingSet.getValue("Y"), bar);

		iter.close();
		con.addStatement(bar, RDF.TYPE, foo);
		con.commit();
		con.close();

		store.shutDown();
	}

	public void testLongLiterals() throws Exception {
		MemoryStore store = new MemoryStore(dataDir);
		store.initialize();

		ValueFactory factory = store.getValueFactory();
		URI foo = factory.createURI("http://www.foo.example/foo");

		StringBuilder sb = new StringBuilder(66000);
		for (int i = 0; i < 66000; i++) {
			sb.append('a');
		}

		Literal longLiteral = factory.createLiteral(sb.toString());

		SailConnection con = store.getConnection();
		con.addStatement(foo, RDF.TYPE, longLiteral);
		con.commit();

		con.close();
		store.shutDown();

		store = new MemoryStore(dataDir);
		store.initialize();

		con = store.getConnection();

		CloseableIteration<? extends Statement, SailException> iter = con
				.getStatements(foo, RDF.TYPE, null, false);
		assertTrue(iter.hasNext());
		iter.next();
		iter.close();

		con.close();
		store.shutDown();
	}
}
