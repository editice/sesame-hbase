/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} REPLACE, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-substr">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class Replace implements Function {

	public String getURI() {
		return FN.REPLACE.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {
		if (args.length < 3 || args.length > 4) {
			throw new ValueExprEvaluationException(
					"Incorrect number of arguments for REPLACE: " + args.length);
		}

		try {
			Literal arg = (Literal) args[0];
			Literal pattern = (Literal) args[1];
			Literal replacement = (Literal) args[2];
			Literal flags = null;
			if (args.length == 4) {
				flags = (Literal) args[3];
			}

			if (!QueryEvaluationUtil.isStringLiteral(arg)) {
				throw new ValueExprEvaluationException(
						"incompatible operand for REPLACE: " + arg);
			}

			if (!QueryEvaluationUtil.isSimpleLiteral(pattern)) {
				throw new ValueExprEvaluationException(
						"incompatible operand for REPLACE: " + pattern);
			}

			if (!QueryEvaluationUtil.isSimpleLiteral(replacement)) {
				throw new ValueExprEvaluationException(
						"incompatible operand for REPLACE: " + replacement);
			}

			String flagString = null;
			if (flags != null) {
				if (!QueryEvaluationUtil.isSimpleLiteral(flags)) {
					throw new ValueExprEvaluationException(
							"incompatible operand for REPLACE: " + flags);
				}
				flagString = flags.getLabel();
			}

			String argString = arg.getLabel();
			String patternString = pattern.getLabel();
			String replacementString = replacement.getLabel();

			int f = 0;
			if (flagString != null) {
				for (char c : flagString.toCharArray()) {
					switch (c) {
					case 's':
						f |= Pattern.DOTALL;
						break;
					case 'm':
						f |= Pattern.MULTILINE;
						break;
					case 'i':
						f |= Pattern.CASE_INSENSITIVE;
						break;
					case 'x':
						f |= Pattern.COMMENTS;
						break;
					case 'd':
						f |= Pattern.UNIX_LINES;
						break;
					case 'u':
						f |= Pattern.UNICODE_CASE;
						break;
					default:
						throw new ValueExprEvaluationException(flagString);
					}
				}
			}

			Pattern p = Pattern.compile(patternString, f);
			String result = p.matcher(argString).replaceAll(replacementString);

			String lang = arg.getLanguage();
			URI dt = arg.getDatatype();

			if (lang != null) {
				return valueFactory.createLiteral(result, lang);
			} else if (dt != null) {
				return valueFactory.createLiteral(result, dt);
			} else {
				return valueFactory.createLiteral(result);
			}
		} catch (ClassCastException e) {
			throw new ValueExprEvaluationException("literal operands expected",
					e);
		}

	}
}
