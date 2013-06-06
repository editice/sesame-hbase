/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.datetime;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} MONTH, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-month">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Month implements Function {

	public String getURI() {
		return FN.MONTH_FROM_DATETIME.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {
		if (args.length != 1) {
			throw new ValueExprEvaluationException(
					"MONTH requires 1 argument, got " + args.length);
		}

		Value argValue = args[0];
		if (argValue instanceof Literal) {
			Literal literal = (Literal) argValue;

			URI datatype = literal.getDatatype();

			if (datatype != null
					&& XMLDatatypeUtil.isCalendarDatatype(datatype)) {
				try {
					XMLGregorianCalendar calValue = literal.calendarValue();

					int month = calValue.getMonth();
					if (DatatypeConstants.FIELD_UNDEFINED != month) {
						return valueFactory.createLiteral(
								String.valueOf(month), XMLSchema.INTEGER);
					} else {
						throw new ValueExprEvaluationException(
								"can not determine month from value: "
										+ argValue);
					}
				} catch (IllegalArgumentException e) {
					throw new ValueExprEvaluationException(
							"illegal calendar value: " + argValue);
				}
			} else {
				throw new ValueExprEvaluationException(
						"unexpected input value for function: " + argValue);
			}
		} else {
			throw new ValueExprEvaluationException(
					"unexpected input value for function: " + args[0]);
		}
	}

}
