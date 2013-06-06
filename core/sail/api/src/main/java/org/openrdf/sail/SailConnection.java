/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;

/**
 * A connection to an RDF Sail object. A SailConnection is active from the
 * moment it is created until it is closed. Care should be taken to properly
 * close SailConnections as they might block concurrent queries and/or updates
 * on the Sail while active, depending on the Sail-implementation that is being
 * used.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public interface SailConnection {

	/**
	 * Checks whether this SailConnection is open. A SailConnection is open from
	 * the moment it is created until it is closed.
	 * 
	 * @see SailConnection#close
	 */
	public boolean isOpen() throws SailException;

	/**
	 * Closes the connection. Any updates that haven't been committed yet will
	 * be rolled back. The connection can no longer be used once it is closed.
	 */
	public void close() throws SailException;

	/**
	 * Evaluates the supplied TupleExpr on the data contained in this Sail
	 * object, using the (optional) dataset and supplied bindings as input
	 * parameters.
	 * 
	 * @param tupleExpr
	 *            The tuple expression to evaluate.
	 * @param dataset
	 *            The dataset to use for evaluating the query, <tt>null</tt> to
	 *            use the Sail's default dataset.
	 * @param bindings
	 *            A set of input parameters for the query evaluation. The keys
	 *            reference variable names that should be bound to the value
	 *            they map to.
	 * @param includeInferred
	 *            Indicates whether inferred triples are to be considered in the
	 *            query result. If false, no inferred statements are returned;
	 *            if true, inferred statements are returned if available
	 * @return The TupleQueryResult.
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException;

	public void executeUpdate(UpdateExpr updateExpr, Dataset dataset,
			BindingSet bindings, boolean includeInferred) throws SailException;

	/**
	 * Returns the set of all unique context identifiers that are used to store
	 * statements.
	 * 
	 * @return An iterator over the context identifiers, should not contain any
	 *         duplicates.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
			throws SailException;

	/**
	 * Gets all statements from the specified contexts that have a specific
	 * subject, predicate and/or object. All three parameters may be null to
	 * indicate wildcards. The <tt>includeInferred</tt> parameter can be used to
	 * control which statements are fetched: all statements or only the
	 * statements that have been added explicitly.
	 * 
	 * @param subj
	 *            A Resource specifying the subject, or <tt>null</tt> for a
	 *            wildcard.
	 * @param pred
	 *            A URI specifying the predicate, or <tt>null</tt> for a
	 *            wildcard.
	 * @param obj
	 *            A Value specifying the object, or <tt>null</tt> for a
	 *            wildcard.
	 * @param includeInferred
	 *            if false, no inferred statements are returned; if true,
	 *            inferred statements are returned if available
	 * @param contexts
	 *            The context(s) to get the data from. Note that this parameter
	 *            is a vararg and as such is optional. If no contexts are
	 *            specified the method operates on the entire repository. A
	 *            <tt>null</tt> value can be used to match context-less
	 *            statements.
	 * @return The statements matching the specified pattern.
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public CloseableIteration<? extends Statement, SailException> getStatements(
			Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws SailException;

	/**
	 * Returns the number of (explicit) statements in the store, or in specific
	 * contexts.
	 * 
	 * @param contexts
	 *            The context(s) to determine the size of. Note that this
	 *            parameter is a vararg and as such is optional. If no contexts
	 *            are specified the method operates on the entire repository. A
	 *            <tt>null</tt> value can be used to match context-less
	 *            statements.
	 * @return The number of explicit statements in this store, or in the
	 *         specified context(s).
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public long size(Resource... contexts) throws SailException;

	/**
	 * Commits any updates that have been performed since the last time
	 * {@link #commit()} or {@link #rollback()} was called.
	 * 
	 * @throws SailException
	 *             If the SailConnection could not be committed.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void commit() throws SailException;

	/**
	 * Rolls back the SailConnection, discarding any uncommitted changes that
	 * have been made in this SailConnection.
	 * 
	 * @throws SailException
	 *             If the SailConnection could not be rolled back.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void rollback() throws SailException;

	/**
	 * Adds a statement to the store.
	 * 
	 * @param subj
	 *            The subject of the statement to add.
	 * @param pred
	 *            The predicate of the statement to add.
	 * @param obj
	 *            The object of the statement to add.
	 * @param contexts
	 *            The context(s) to add the statement to. Note that this
	 *            parameter is a vararg and as such is optional. If no contexts
	 *            are specified, a context-less statement will be added.
	 * @throws SailException
	 *             If the statement could not be added.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void addStatement(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException;

	/**
	 * Removes all statements matching the specified subject, predicate and
	 * object from the repository. All three parameters may be null to indicate
	 * wildcards.
	 * 
	 * @param subj
	 *            The subject of the statement that should be removed, or
	 *            <tt>null</tt> to indicate a wildcard.
	 * @param pred
	 *            The predicate of the statement that should be removed, or
	 *            <tt>null</tt> to indicate a wildcard.
	 * @param obj
	 *            The object of the statement that should be removed , or
	 *            <tt>null</tt> to indicate a wildcard. *
	 * @param contexts
	 *            The context(s) from which to remove the statement. Note that
	 *            this parameter is a vararg and as such is optional. If no
	 *            contexts are specified the method operates on the entire
	 *            repository. A <tt>null</tt> value can be used to match
	 *            context-less statements.
	 * @throws SailException
	 *             If the statement could not be removed.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void removeStatements(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException;

	/**
	 * Removes all statements from the specified/all contexts. If no contexts
	 * are specified the method operates on the entire repository.
	 * 
	 * @param contexts
	 *            The context(s) from which to remove the statements. Note that
	 *            this parameter is a vararg and as such is optional. If no
	 *            contexts are specified the method operates on the entire
	 *            repository. A <tt>null</tt> value can be used to match
	 *            context-less statements.
	 * @throws SailException
	 *             If the statements could not be removed.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void clear(Resource... contexts) throws SailException;

	/**
	 * Gets the namespaces relevant to the data contained in this Sail object.
	 * 
	 * @return An iterator over the relevant namespaces, should not contain any
	 *         duplicates.
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
			throws SailException;

	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 * 
	 * @param prefix
	 *            A namespace prefix, or an empty string in case of the default
	 *            namespace.
	 * @return The namespace name that is associated with the specified prefix,
	 *         or <tt>null</tt> if there is no such namespace.
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws NullPointerException
	 *             In case <tt>prefix</tt> is <tt>null</tt>.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public String getNamespace(String prefix) throws SailException;

	/**
	 * Sets the prefix for a namespace.
	 * 
	 * @param prefix
	 *            The new prefix, or an empty string in case of the default
	 *            namespace.
	 * @param name
	 *            The namespace name that the prefix maps to.
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws NullPointerException
	 *             In case <tt>prefix</tt> or <tt>name</tt> is <tt>null</tt>.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void setNamespace(String prefix, String name) throws SailException;

	/**
	 * Removes a namespace declaration by removing the association between a
	 * prefix and a namespace name.
	 * 
	 * @param prefix
	 *            The namespace prefix, or an empty string in case of the
	 *            default namespace.
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws NullPointerException
	 *             In case <tt>prefix</tt> is <tt>null</tt>.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void removeNamespace(String prefix) throws SailException;

	/**
	 * Removes all namespace declarations from the repository.
	 * 
	 * @throws SailException
	 *             If the Sail object encountered an error or unexpected
	 *             situation internally.
	 * @throws IllegalStateException
	 *             If the connection has been closed.
	 */
	public void clearNamespaces() throws SailException;

}
