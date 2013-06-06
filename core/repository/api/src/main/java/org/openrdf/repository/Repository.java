/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.io.File;

import org.openrdf.model.ValueFactory;

/**
 * A Sesame repository that contains RDF data that can be queried and updated.
 * Access to the repository can be acquired by openening a connection to it.
 * This connection can then be used to query and/or update the contents of the
 * repository. Depending on the implementation of the repository, it may or may
 * not support multiple concurrent connections.
 * <p>
 * Please note that a repository needs to be initialized before it can be used
 * and that it should be shut down before it is discarded/garbage collected.
 * Forgetting the latter can result in loss of data (depending on the Repository
 * implementation)!
 * 
 * @author Arjohn Kampman
 */
public interface Repository {

	/**
	 * Set the directory where data and logging for this repository is stored.
	 * 
	 * @param dataDir
	 *            the directory where data for this repository is stored
	 */
	public void setDataDir(File dataDir);

	/**
	 * Get the directory where data and logging for this repository is stored.
	 * 
	 * @return the directory where data for this repository is stored.
	 */
	public File getDataDir();

	/**
	 * Initializes this repository. A repository needs to be initialized before
	 * it can be used.
	 * 
	 * @throws RepositoryException
	 *             If the initialization failed.
	 */
	public void initialize() throws RepositoryException;

	/**
	 * Indicates if the Repository has been initialized. Note that the
	 * initialization status may change if the Repository is shut down.
	 * 
	 * @return true iff the repository has been initialized.
	 */
	public boolean isInitialized();

	/**
	 * Shuts the repository down, releasing any resources that it keeps hold of.
	 * Once shut down, the repository can no longer be used until it is
	 * re-initialized.
	 */
	public void shutDown() throws RepositoryException;

	/**
	 * Checks whether this repository is writable, i.e. if the data contained in
	 * this repository can be changed. The writability of the repository is
	 * determined by the writability of the Sail that this repository operates
	 * on.
	 */
	public boolean isWritable() throws RepositoryException;

	/**
	 * Opens a connection to this repository that can be used for querying and
	 * updating the contents of the repository. Created connections need to be
	 * closed to make sure that any resources they keep hold of are released.
	 * The best way to do this is to use a try-finally-block as follows:
	 * 
	 * <pre>
	 * Connection con = repository.getConnection();
	 * try {
	 * 	// perform operations on the connection
	 * } finally {
	 * 	con.close();
	 * }
	 * </pre>
	 * 
	 * @return A connection that allows operations on this repository.
	 * @throws RepositoryException
	 *             If something went wrong during the creation of the
	 *             Connection.
	 */
	public RepositoryConnection getConnection() throws RepositoryException;

	/**
	 * Gets a ValueFactory for this Repository.
	 * 
	 * @return A repository-specific ValueFactory.
	 */
	public ValueFactory getValueFactory();
}
