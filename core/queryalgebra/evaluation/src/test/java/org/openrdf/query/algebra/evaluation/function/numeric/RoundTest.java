/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class RoundTest {

	private Round round;

	private ValueFactory f = new ValueFactoryImpl();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		round = new Round();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEvaluateDouble() {
		try {
			double dVal = 1.6;
			Literal rounded = round.evaluate(f, f.createLiteral(dVal));

			double roundValue = rounded.doubleValue();

			assertEquals((double) 2.0, roundValue, 0.001d);
		} catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluateInt() {
		try {
			int iVal = 1;
			Literal rounded = round.evaluate(f, f.createLiteral(iVal));

			int roundValue = rounded.intValue();

			assertEquals(iVal, roundValue);
		} catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluateBigDecimal() {
		try {
			BigDecimal bd = new BigDecimal(1234567.567);

			Literal rounded = round.evaluate(f, f.createLiteral(bd
					.toPlainString(), XMLSchema.DECIMAL));

			BigDecimal roundValue = rounded.decimalValue();

			assertEquals(new BigDecimal(1234568.0), roundValue);
		} catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
