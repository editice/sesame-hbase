/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.ReadPrefReadWriteLockManager;
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.DirectoryLockManager;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementIterator;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;

/**
 * An implementation of the Sail interface that stores its data in main memory
 * and that can use a file for persistent storage. This Sail implementation
 * supports single, isolated transactions. This means that changes to the data
 * are not visible until a transaction is committed and that concurrent
 * transactions are not possible. When another transaction is active, calls to
 * <tt>startTransaction()</tt> will block until the active transaction is
 * committed or rolled back.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class MemoryStore extends NotifyingSailBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected static final String DATA_FILE_NAME = "memorystore.data";

	protected static final String SYNC_FILE_NAME = "memorystore.sync";

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Factory/cache for MemValue objects.
	 */
	private final MemValueFactory valueFactory = new MemValueFactory();

	/**
	 * List containing all available statements.
	 */
	private final MemStatementList statements = new MemStatementList(256);

	/**
	 * Set of all statements that have been affected by a transaction.
	 */
	private volatile IdentityHashMap<MemStatement, MemStatement> txnStatements;

	/**
	 * Identifies the current snapshot.
	 */
	private volatile int currentSnapshot;

	/**
	 * Store for namespace prefix info.
	 */
	private final MemNamespaceStore namespaceStore = new MemNamespaceStore();

	/**
	 * Lock manager used to give the snapshot cleanup thread exclusive access to
	 * the statement list.
	 */
	private final ReadWriteLockManager statementListLockManager = new ReadPrefReadWriteLockManager(
			debugEnabled());

	/**
	 * Lock manager used to prevent concurrent transactions.
	 */
	private final ExclusiveLockManager txnLockManager = new ExclusiveLockManager(
			debugEnabled());

	private volatile boolean persist = false;

	/**
	 * The file used for data persistence, null if this is a volatile RDF store.
	 */
	private volatile File dataFile;

	/**
	 * The file used for serialising data, null if this is a volatile RDF store.
	 */
	private volatile File syncFile;

	/**
	 * The directory lock, null if this is read-only or a volatile RDF store.
	 */
	private volatile Lock dirLock;

	/**
	 * Flag indicating whether the contents of this repository have changed.
	 */
	private volatile boolean contentsChanged;

	/**
	 * The sync delay.
	 * 
	 * @see #setSyncDelay
	 */
	private volatile long syncDelay = 0L;

	/**
	 * Semaphore used to synchronize concurrent access to
	 * {@link #syncWithLock()} .
	 */
	private final Object syncSemaphore = new Object();

	/**
	 * The timer used to trigger file synchronization.
	 */
	private volatile Timer syncTimer;

	/**
	 * The currently scheduled timer task, if any.
	 */
	private volatile TimerTask syncTimerTask;

	/**
	 * Semaphore used to synchronize concurrent access to {@link #syncTimer} and
	 * {@link #syncTimerTask}.
	 */
	private final Object syncTimerSemaphore = new Object();

	/**
	 * Cleanup thread that removes deprecated statements when no other threads
	 * are accessing this list. Seee {@link #scheduleSnapshotCleanup()}.
	 */
	private volatile Thread snapshotCleanupThread;

	/**
	 * Semaphore used to synchronize concurrent access to
	 * {@link #snapshotCleanupThread}.
	 */
	private final Object snapshotCleanupThreadSemaphore = new Object();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemoryStore.
	 */
	public MemoryStore() {
	}

	/**
	 * Creates a new persistent MemoryStore. If the specified data directory
	 * contains an existing store, its contents will be restored upon
	 * initialization.
	 * 
	 * @param dataDir
	 *            the data directory to be used for persistence.
	 */
	public MemoryStore(File dataDir) {
		setDataDir(dataDir);
		setPersist(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setPersist(boolean persist) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		this.persist = persist;
	}

	public boolean getPersist() {
		return persist;
	}

	/**
	 * Sets the time (in milliseconds) to wait after a transaction was commited
	 * before writing the changed data to file. Setting this variable to 0 will
	 * force a file sync immediately after each commit. A negative value will
	 * deactivate file synchronization until the Sail is shut down. A positive
	 * value will postpone the synchronization for at least that amount of
	 * milliseconds. If in the meantime a new transaction is started, the file
	 * synchronization will be rescheduled to wait for another
	 * <tt>syncDelay</tt> ms. This way, bursts of transaction events can be
	 * combined in one file sync.
	 * <p>
	 * The default value for this parameter is <tt>0</tt> (immediate
	 * synchronization).
	 * 
	 * @param syncDelay
	 *            The sync delay in milliseconds.
	 */
	public void setSyncDelay(long syncDelay) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been initialized");
		}

		this.syncDelay = syncDelay;
	}

	/**
	 * Gets the currently configured sync delay.
	 * 
	 * @return syncDelay The sync delay in milliseconds.
	 * @see #setSyncDelay
	 */
	public long getSyncDelay() {
		return syncDelay;
	}

	/**
	 * Initializes this repository. If a persistence file is defined for the
	 * store, the contents will be restored.
	 * 
	 * @throws SailException
	 *             when initialization of the store failed.
	 */
	protected void initializeInternal() throws SailException {
		logger.debug("Initializing MemoryStore...");

		currentSnapshot = 1;

		if (persist) {
			File dataDir = getDataDir();
			DirectoryLockManager locker = new DirectoryLockManager(dataDir);
			dataFile = new File(dataDir, DATA_FILE_NAME);
			syncFile = new File(dataDir, SYNC_FILE_NAME);

			if (dataFile.exists()) {
				logger.debug("Reading data from {}...", dataFile);

				// Initialize persistent store from file
				if (!dataFile.canRead()) {
					logger.error("Data file is not readable: {}", dataFile);
					throw new SailException("Can't read data file: " + dataFile);
				}
				// try to create a lock for later writing
				dirLock = locker.tryLock();
				if (dirLock == null) {
					logger.warn("Failed to lock directory: {}", dataDir);
				}
				// Don't try to read empty files: this will result in an
				// IOException, and the file doesn't contain any data anyway.
				if (dataFile.length() == 0L) {
					logger.warn("Ignoring empty data file: {}", dataFile);
				} else {
					try {
						new FileIO(this).read(dataFile);
						logger.debug("Data file read successfully");
					} catch (IOException e) {
						logger.error("Failed to read data file", e);
						throw new SailException(e);
					}
				}
			} else {
				// file specified that does not exist yet, create it
				try {
					File dir = dataFile.getParentFile();
					if (dir != null && !dir.exists()) {
						logger.debug("Creating directory for data file...");
						if (!dir.mkdirs()) {
							logger
									.debug(
											"Failed to create directory for data file: {}",
											dir);
							throw new SailException(
									"Failed to create directory for data file: "
											+ dir);
						}
					}
					// try to lock directory or fail
					dirLock = locker.lockOrFail();

					logger.debug("Initializing data file...");
					new FileIO(this).write(syncFile, dataFile);
					logger.debug("Data file initialized");
				} catch (IOException e) {
					logger.debug("Failed to initialize data file", e);
					throw new SailException("Failed to initialize data file "
							+ dataFile, e);
				} catch (SailException e) {
					logger.debug("Failed to initialize data file", e);
					throw new SailException("Failed to initialize data file "
							+ dataFile, e);
				}
			}
		}

		contentsChanged = false;

		logger.debug("MemoryStore initialized");
	}

	@Override
	protected void shutDownInternal() throws SailException {
		try {
			Lock stLock = statementListLockManager.getWriteLock();

			try {
				cancelSyncTimer();
				syncWithLock();

				valueFactory.clear();
				statements.clear();
				dataFile = null;
				syncFile = null;
			} finally {
				stLock.release();
				if (dirLock != null) {
					dirLock.release();
				}
			}
		} catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	/**
	 * Checks whether this Sail object is writable. A MemoryStore is not
	 * writable if a read-only data file is used.
	 */
	public boolean isWritable() {
		// Sail is not writable when it has a dataDir but no directory lock
		return !persist || dirLock != null;
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
			throws SailException {
		return new MemoryStoreConnection(this);
	}

	public MemValueFactory getValueFactory() {
		if (valueFactory == null) {
			throw new IllegalStateException("sail not initialized.");
		}

		return valueFactory;
	}

	protected MemNamespaceStore getNamespaceStore() {
		return namespaceStore;
	}

	protected MemStatementList getStatements() {
		return statements;
	}

	protected int getCurrentSnapshot() {
		return currentSnapshot;
	}

	protected Lock getStatementsReadLock() throws SailException {
		try {
			return statementListLockManager.getReadLock();
		} catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected Lock getTransactionLock() throws SailException {
		try {
			return txnLockManager.getExclusiveLock();
		} catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected int size() {
		return statements.size();
	}

	protected boolean hasStatement(Resource subj, URI pred, Value obj,
			boolean explicitOnly, int snapshot, ReadMode readMode,
			Resource... contexts) {
		CloseableIteration<MemStatement, RuntimeException> iter = createStatementIterator(
				RuntimeException.class, subj, pred, obj, explicitOnly,
				snapshot, readMode, contexts);
		try {
			return iter.hasNext();
		} finally {
			iter.close();
		}
	}

	/**
	 * Creates a StatementIterator that contains the statements matching the
	 * specified pattern of subject, predicate, object, context. Inferred
	 * statements are excluded when <tt>explicitOnly</tt> is set to
	 * <tt>true</tt> . Statements from the null context are excluded when
	 * <tt>namedContextsOnly</tt> is set to <tt>true</tt>. The returned
	 * StatementIterator will assume the specified read mode.
	 */
	protected <X extends Exception> CloseableIteration<MemStatement, X> createStatementIterator(
			Class<X> excClass, Resource subj, URI pred, Value obj,
			boolean explicitOnly, int snapshot, ReadMode readMode,
			Resource... contexts) {
		// Perform look-ups for value-equivalents of the specified values
		MemResource memSubj = valueFactory.getMemResource(subj);
		if (subj != null && memSubj == null) {
			// non-existent subject
			return new EmptyIteration<MemStatement, X>();
		}

		MemURI memPred = valueFactory.getMemURI(pred);
		if (pred != null && memPred == null) {
			// non-existent predicate
			return new EmptyIteration<MemStatement, X>();
		}

		MemValue memObj = valueFactory.getMemValue(obj);
		if (obj != null && memObj == null) {
			// non-existent object
			return new EmptyIteration<MemStatement, X>();
		}

		MemResource[] memContexts;
		MemStatementList smallestList;

		if (contexts.length == 0) {
			memContexts = new MemResource[0];
			smallestList = statements;
		} else if (contexts.length == 1 && contexts[0] != null) {
			MemResource memContext = valueFactory.getMemResource(contexts[0]);
			if (memContext == null) {
				// non-existent context
				return new EmptyIteration<MemStatement, X>();
			}

			memContexts = new MemResource[] { memContext };
			smallestList = memContext.getContextStatementList();
		} else {
			Set<MemResource> contextSet = new LinkedHashSet<MemResource>(
					2 * contexts.length);

			for (Resource context : contexts) {
				MemResource memContext = valueFactory.getMemResource(context);
				if (context == null || memContext != null) {
					contextSet.add(memContext);
				}
			}

			if (contextSet.isEmpty()) {
				// no known contexts specified
				return new EmptyIteration<MemStatement, X>();
			}

			memContexts = contextSet
					.toArray(new MemResource[contextSet.size()]);
			smallestList = statements;
		}

		if (memSubj != null) {
			MemStatementList l = memSubj.getSubjectStatementList();
			if (l.size() < smallestList.size()) {
				smallestList = l;
			}
		}

		if (memPred != null) {
			MemStatementList l = memPred.getPredicateStatementList();
			if (l.size() < smallestList.size()) {
				smallestList = l;
			}
		}

		if (memObj != null) {
			MemStatementList l = memObj.getObjectStatementList();
			if (l.size() < smallestList.size()) {
				smallestList = l;
			}
		}

		return new MemStatementIterator<X>(smallestList, memSubj, memPred,
				memObj, explicitOnly, snapshot, readMode, memContexts);
	}

	protected Statement addStatement(Resource subj, URI pred, Value obj,
			Resource context, boolean explicit) throws SailException {
		assert txnStatements != null;

//		System.out.println("use it");
		// Get or create MemValues for the operands
		MemResource memSubj = valueFactory.getOrCreateMemResource(subj);
		MemURI memPred = valueFactory.getOrCreateMemURI(pred);
		MemValue memObj = valueFactory.getOrCreateMemValue(obj);
		MemResource memContext = (context == null) ? null : valueFactory
				.getOrCreateMemResource(context);

		if (memSubj.hasStatements() && memPred.hasStatements()
				&& memObj.hasStatements()
				&& (memContext == null || memContext.hasStatements())) {
			// All values are used in at least one statement. Possibly, the
			// statement is already present. Check this.
			CloseableIteration<MemStatement, SailException> stIter = createStatementIterator(
					SailException.class, memSubj, memPred, memObj, false,
					currentSnapshot + 1, ReadMode.RAW, memContext);

			try {
				if (stIter.hasNext()) {
					// statement is already present, update its transaction
					// status if appropriate
					MemStatement st = stIter.next();

					txnStatements.put(st, st);

					TxnStatus txnStatus = st.getTxnStatus();

					if (txnStatus == TxnStatus.NEUTRAL && !st.isExplicit()
							&& explicit) {
						// Implicit statement is now added explicitly
						st.setTxnStatus(TxnStatus.EXPLICIT);
					} else if (txnStatus == TxnStatus.NEW && !st.isExplicit()
							&& explicit) {
						// Statement was first added implicitly and now
						// explicitly
						st.setExplicit(true);
					} else if (txnStatus == TxnStatus.DEPRECATED) {
						if (st.isExplicit() == explicit) {
							// Statement was removed but is now re-added
							st.setTxnStatus(TxnStatus.NEUTRAL);
						} else if (explicit) {
							// Implicit statement was removed but is now added
							// explicitly
							st.setTxnStatus(TxnStatus.EXPLICIT);
						} else {
							// Explicit statement was removed but can still be
							// inferred
							st.setTxnStatus(TxnStatus.INFERRED);
						}

						return st;
					} else if (txnStatus == TxnStatus.INFERRED
							&& st.isExplicit() && explicit) {
						// Explicit statement was removed but is now re-added
						st.setTxnStatus(TxnStatus.NEUTRAL);
					} else if (txnStatus == TxnStatus.ZOMBIE) {
						// Restore zombie statement
						st.setTxnStatus(TxnStatus.NEW);
						st.setExplicit(explicit);

						return st;
					}

					return null;
				}
			} finally {
				stIter.close();
			}
		}

		// completely new statement
		MemStatement st = new MemStatement(memSubj, memPred, memObj,
				memContext, explicit, currentSnapshot + 1, TxnStatus.NEW);
		statements.add(st);
		st.addToComponentLists();

		txnStatements.put(st, st);

		assert hasStatement(memSubj, memPred, memObj, explicit,
				currentSnapshot + 1, ReadMode.TRANSACTION, memContext);

		return st;
	}

	protected boolean removeStatement(MemStatement st, boolean explicit)
			throws SailException {
		boolean statementsRemoved = false;
		TxnStatus txnStatus = st.getTxnStatus();

		if (txnStatus == TxnStatus.NEUTRAL && st.isExplicit() == explicit) {
			// Remove explicit statement
			st.setTxnStatus(TxnStatus.DEPRECATED);
			statementsRemoved = true;
		} else if (txnStatus == TxnStatus.NEW && st.isExplicit() == explicit) {
			// Statement was added and now removed in the same transaction
			st.setTxnStatus(TxnStatus.ZOMBIE);
			statementsRemoved = true;
		} else if (txnStatus == TxnStatus.INFERRED && st.isExplicit()
				&& !explicit) {
			// Explicit statement was replaced by inferred statement and this
			// inferred statement is now removed
			st.setTxnStatus(TxnStatus.DEPRECATED);
			statementsRemoved = true;
		} else if (txnStatus == TxnStatus.EXPLICIT && !st.isExplicit()
				&& explicit) {
			// Inferred statement was replaced by explicit statement, but this
			// is
			// now undone
			st.setTxnStatus(TxnStatus.NEUTRAL);
		}

		txnStatements.put(st, st);

		return statementsRemoved;
	}

	protected void startTransaction() throws SailException {
		cancelSyncTask();

		assert txnStatements == null;
		txnStatements = new IdentityHashMap<MemStatement, MemStatement>();
	}

	protected void commit() throws SailException {
		assert txnStatements != null;

		boolean statementsAdded = false;
		boolean statementsRemoved = false;
		boolean statementsDeprecated = false;

		int txnSnapshot = currentSnapshot + 1;

		for (MemStatement st : txnStatements.keySet()) {
			TxnStatus txnStatus = st.getTxnStatus();

			if (txnStatus == TxnStatus.NEUTRAL) {
				continue;
			} else if (txnStatus == TxnStatus.NEW) {
				statementsAdded = true;
			} else if (txnStatus == TxnStatus.DEPRECATED) {
				st.setTillSnapshot(txnSnapshot);
				statementsRemoved = true;
			} else if (txnStatus == TxnStatus.ZOMBIE) {
				st.setTillSnapshot(txnSnapshot);
				statementsDeprecated = true;
			} else if (txnStatus == TxnStatus.EXPLICIT
					|| txnStatus == TxnStatus.INFERRED) {
				// Deprecate the existing statement...
				st.setTillSnapshot(txnSnapshot);
				statementsDeprecated = true;

				// ...and add a clone with modified explicit/implicit flag
				MemStatement explSt = new MemStatement(st.getSubject(), st
						.getPredicate(), st.getObject(), st.getContext(),
						txnStatus == TxnStatus.EXPLICIT, txnSnapshot);
				statements.add(explSt);
				explSt.addToComponentLists();
			}

			st.setTxnStatus(TxnStatus.NEUTRAL);
		}

		txnStatements = null;

		if (statementsAdded || statementsRemoved || statementsDeprecated) {
			currentSnapshot = txnSnapshot;
		}

		if (statementsAdded || statementsRemoved) {
			contentsChanged = true;
			scheduleSyncTask();

			DefaultSailChangedEvent event = new DefaultSailChangedEvent(this);
			event.setStatementsAdded(statementsAdded);
			event.setStatementsRemoved(statementsRemoved);
			notifySailChanged(event);
		}

		if (statementsRemoved || statementsDeprecated) {
			scheduleSnapshotCleanup();
		}
	}

	protected void rollback() throws SailException {
		assert txnStatements != null;
		logger.debug("rolling back transaction");

		boolean statementsDeprecated = false;

		for (MemStatement st : txnStatements.keySet()) {
			TxnStatus txnStatus = st.getTxnStatus();
			if (txnStatus == TxnStatus.NEW || txnStatus == TxnStatus.ZOMBIE) {
				// Statement has been added during this transaction and
				// deprecates
				// immediately
				st.setTillSnapshot(currentSnapshot);
				statementsDeprecated = true;
			} else if (txnStatus != TxnStatus.NEUTRAL) {
				// Return statement to neutral status
				st.setTxnStatus(TxnStatus.NEUTRAL);
			}
		}

		txnStatements = null;

		if (statementsDeprecated) {
			scheduleSnapshotCleanup();
		}
	}

	protected void scheduleSyncTask() throws SailException {
		if (!persist) {
			return;
		}

		if (syncDelay == 0L) {
			// Sync immediately
			sync();
		} else if (syncDelay > 0L) {
			synchronized (syncTimerSemaphore) {
				// Sync in syncDelay milliseconds
				if (syncTimer == null) {
					// Create the syncTimer on a deamon thread
					syncTimer = new Timer("MemoryStore synchronization", true);
				}

				if (syncTimerTask != null) {
					logger.error("syncTimerTask is not null");
				}

				syncTimerTask = new TimerTask() {

					@Override
					public void run() {
						try {
							sync();
						} catch (SailException e) {
							logger.warn("Unable to sync on timer", e);
						}
					}
				};

				syncTimer.schedule(syncTimerTask, syncDelay);
			}
		}
	}

	protected void cancelSyncTask() {
		synchronized (syncTimerSemaphore) {
			if (syncTimerTask != null) {
				syncTimerTask.cancel();
				syncTimerTask = null;
			}
		}
	}

	protected void cancelSyncTimer() {
		synchronized (syncTimerSemaphore) {
			if (syncTimer != null) {
				syncTimer.cancel();
				syncTimer = null;
			}
		}
	}

	/**
	 * Synchronizes the contents of this repository with the data that is stored
	 * on disk. Data will only be written when the contents of the repository
	 * and data in the file are out of sync.
	 */
	public void sync() throws SailException {
		// Acquire read lock to guarantee that the statement list
		// doesn't change while writing
		Lock stLock = getStatementsReadLock();
		try {
			syncWithLock();
		} finally {
			stLock.release();
		}
	}

	/**
	 * Synchronizes the contents of this repository with the data that is stored
	 * on disk. Data will only be written when the contents of the repository
	 * and data in the file are out of sync. The calling code must get a read-
	 * or write lock from {@link #statementListLockManager} before calling this
	 * method to prevent modifications to the statements list while the
	 * synchronization is in progress.
	 */
	protected void syncWithLock() throws SailException {
		// syncSemaphore prevents concurrent file synchronizations
		synchronized (syncSemaphore) {
			if (persist && contentsChanged) {
				logger.debug("syncing data to file...");
				try {
					new FileIO(this).write(syncFile, dataFile);
					contentsChanged = false;
					logger.debug("Data synced to file");
				} catch (IOException e) {
					logger.error("Failed to sync to file", e);
					throw new SailException(e);
				}
			}
		}
	}

	/**
	 * Removes statements from old snapshots from the main statement list and
	 * resets the snapshot to 1 for the rest of the statements.
	 * 
	 * @throws InterruptedException
	 */
	protected void cleanSnapshots() throws InterruptedException {
		// System.out.println("cleanSnapshots() starting...");
		// long startTime = System.currentTimeMillis();
		MemStatementList statements = this.statements;

		if (statements == null) {
			// Store has been shut down
			return;
		}

		// Sets used to keep track of which lists have already been processed
		HashSet<MemValue> processedSubjects = new HashSet<MemValue>();
		HashSet<MemValue> processedPredicates = new HashSet<MemValue>();
		HashSet<MemValue> processedObjects = new HashSet<MemValue>();
		HashSet<MemValue> processedContexts = new HashSet<MemValue>();

		Lock stLock = statementListLockManager.getWriteLock();
		try {
			for (int i = statements.size() - 1; i >= 0; i--) {
				MemStatement st = statements.get(i);

				if (st.getTillSnapshot() <= currentSnapshot) {
					MemResource subj = st.getSubject();
					if (processedSubjects.add(subj)) {
						subj
								.cleanSnapshotsFromSubjectStatements(currentSnapshot);
					}

					MemURI pred = st.getPredicate();
					if (processedPredicates.add(pred)) {
						pred
								.cleanSnapshotsFromPredicateStatements(currentSnapshot);
					}

					MemValue obj = st.getObject();
					if (processedObjects.add(obj)) {
						obj.cleanSnapshotsFromObjectStatements(currentSnapshot);
					}

					MemResource context = st.getContext();
					if (context != null && processedContexts.add(context)) {
						context
								.cleanSnapshotsFromContextStatements(currentSnapshot);
					}

					// stale statement
					statements.remove(i);
				} else {
					// Reset snapshot
					st.setSinceSnapshot(1);
				}
			}

			currentSnapshot = 1;
		} finally {
			stLock.release();
		}

		// long endTime = System.currentTimeMillis();
		// System.out.println("cleanSnapshots() took " + (endTime - startTime) +
		// " ms");
	}

	protected void scheduleSnapshotCleanup() {
		synchronized (snapshotCleanupThreadSemaphore) {
			if (snapshotCleanupThread == null
					|| !snapshotCleanupThread.isAlive()) {
				Runnable runnable = new Runnable() {

					public void run() {
						try {
							cleanSnapshots();
						} catch (InterruptedException e) {
							logger.warn("snapshot cleanup interrupted");
						}
					}
				};

				snapshotCleanupThread = new Thread(runnable,
						"MemoryStore snapshot cleanup");
				snapshotCleanupThread.setDaemon(true);
				snapshotCleanupThread.start();
			}
		}
	}
}
