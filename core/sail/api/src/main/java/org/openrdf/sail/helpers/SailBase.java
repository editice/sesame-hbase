/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * SailBase is an abstract Sail implementation that takes care of common sail
 * tasks, including proper closing of active connections and a grace period for
 * active connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class SailBase implements Sail {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Default connection timeout on shutdown: 20,000 milliseconds.
	 */
	protected final static long DEFAULT_CONNECTION_TIMEOUT = 20000L;

	// Note: the following variable and method are package protected so that
	// they
	// can be removed when open connections no longer block other connections
	// and
	// they can be closed silently (just like in JDBC).
	static final String DEBUG_PROP = "org.openrdf.repository.debug";

	protected static boolean debugEnabled() {
		try {
			String value = System.getProperty(DEBUG_PROP);
			return value != null && !value.equals("false");
		} catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example
			// when
			// running in applets
			return false;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Directory to store information related to this sail in (if any).
	 */
	private volatile File dataDir;

	/**
	 * Flag indicating whether the Sail has been initialized. Sails are
	 * initialized from {@link #initialize() initialization} until
	 * {@link #shutDown() shutdown}.
	 */
	private volatile boolean initialized = false;

	/**
	 * Lock used to synchronize the initialization state of a sail.
	 * <ul>
	 * <li>write lock: initialize(), shutDown()
	 * <li>read lock: getConnection()
	 * </ul>
	 */
	protected final ReentrantReadWriteLock initializationLock = new ReentrantReadWriteLock();

	/**
	 * Connection timeout on shutdown (in ms). Defaults to
	 * {@link #DEFAULT_CONNECTION_TIMEOUT}.
	 */
	protected volatile long connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;

	/**
	 * Map used to track active connections and where these were acquired. The
	 * Throwable value may be null in case debugging was disable at the time the
	 * connection was acquired.
	 */
	private final Map<SailConnection, Throwable> activeConnections = new IdentityHashMap<SailConnection, Throwable>();

	/*---------*
	 * Methods *
	 *---------*/

	public void setDataDir(File dataDir) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		this.dataDir = dataDir;
	}

	public File getDataDir() {
		return dataDir;
	}

	@Override
	public String toString() {
		if (dataDir == null) {
			return super.toString();
		} else {
			return dataDir.toString();
		}
	}

	/**
	 * Checks whether the Sail has been initialized. Sails are initialized from
	 * {@link #initialize() initialization} until {@link #shutDown() shutdown}.
	 * 
	 * @return <tt>true</tt> if the Sail has been initialized, <tt>false</tt>
	 *         otherwise.
	 */
	protected boolean isInitialized() {
		return initialized;
	}

	public void initialize() throws SailException {
		initializationLock.writeLock().lock();
		try {
			if (isInitialized()) {
				throw new IllegalStateException(
						"sail has already been intialized");
			}

			initializeInternal();

			initialized = true;
		} finally {
			initializationLock.writeLock().unlock();
		}
	}

	/**
	 * Do store-specific operations to initialize the store. The default
	 * implementation of this method does nothing.
	 */
	protected void initializeInternal() throws SailException {
	}

	public void shutDown() throws SailException {
		initializationLock.writeLock().lock();
		try {
			if (!isInitialized()) {
				return;
			}

			Map<SailConnection, Throwable> activeConnectionsCopy;

			synchronized (activeConnections) {
				// Check if any active connections exist. If so, wait for a
				// grace
				// period for them to finish.
				if (!activeConnections.isEmpty()) {
					logger
							.debug("Waiting for active connections to close before shutting down...");
					try {
						activeConnections.wait(DEFAULT_CONNECTION_TIMEOUT);
					} catch (InterruptedException e) {
						// ignore and continue
					}
				}

				// Copy the current contents of the map so that we don't have to
				// synchronize on activeConnections. This prevents a potential
				// deadlock with concurrent calls to connectionClosed()
				activeConnectionsCopy = new IdentityHashMap<SailConnection, Throwable>(
						activeConnections);
			}

			// Forcefully close any connections that are still open
			for (Map.Entry<SailConnection, Throwable> entry : activeConnectionsCopy
					.entrySet()) {
				SailConnection con = entry.getKey();
				Throwable stackTrace = entry.getValue();

				if (stackTrace == null) {
					logger
							.warn(
									"Closing active connection due to shut down; consider setting the {} system property",
									DEBUG_PROP);
				} else {
					logger
							.warn(
									"Closing active connection due to shut down, connection was acquired in",
									stackTrace);
				}

				try {
					con.close();
				} catch (SailException e) {
					logger.error("Failed to close connection", e);
				}
			}

			// All connections should be closed now
			synchronized (activeConnections) {
				activeConnections.clear();
			}

			shutDownInternal();
		} finally {
			initialized = false;
			initializationLock.writeLock().unlock();
		}
	}

	/**
	 * Do store-specific operations to ensure proper shutdown of the store.
	 */
	protected abstract void shutDownInternal() throws SailException;

	public SailConnection getConnection() throws SailException {
		initializationLock.readLock().lock();
		try {
			if (!isInitialized()) {
				throw new IllegalStateException(
						"Sail is not initialized or has been shut down");
			}

			SailConnection connection = getConnectionInternal();

			Throwable stackTrace = debugEnabled() ? new Throwable() : null;
			synchronized (activeConnections) {
				activeConnections.put(connection, stackTrace);
			}

			return connection;
		} finally {
			initializationLock.readLock().unlock();
		}
	}

	/**
	 * Returns a store-specific SailConnection object.
	 * 
	 * @return A connection to the store.
	 */
	protected abstract SailConnection getConnectionInternal()
			throws SailException;

	/**
	 * Signals to the store that the supplied connection has been closed; called
	 * by {@link SailConnectionBase#close()}.
	 * 
	 * @param connection
	 *            The connection that has been closed.
	 */
	protected void connectionClosed(SailConnection connection) {
		synchronized (activeConnections) {
			if (activeConnections.containsKey(connection)) {
				activeConnections.remove(connection);

				if (activeConnections.isEmpty()) {
					// only notify waiting threads if all active connections
					// have
					// been closed.
					activeConnections.notifyAll();
				}
			} else {
				logger
						.warn("tried to remove unknown connection object from store.");
			}
		}
	}
}
