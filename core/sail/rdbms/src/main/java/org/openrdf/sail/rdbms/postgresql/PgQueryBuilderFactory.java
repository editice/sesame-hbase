/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.SqlExprBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlQueryBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlRegexBuilder;

/**
 * Overrides PostgreSQL specific SQL syntax. Including regular expression
 * operator, LIMIT clause and CROSS JOIN notation.
 * 
 * @author Jeen Broekstra
 * @author James Leigh
 * 
 */
public class PgQueryBuilderFactory extends QueryBuilderFactory {

	@Override
	public SqlQueryBuilder createSqlQueryBuilder() {
		return new PgSqlQueryBuilder(this);
	}

	@Override
	public SqlRegexBuilder createSqlRegexBuilder(SqlExprBuilder where) {
		return new SqlRegexBuilder(where, this) {

			@Override
			protected void appendRegExp(SqlExprBuilder where) {
				appendValue(where);
				where.append(" ~ ");
				appendPattern(where);
			}
		};
	}
}
