/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.concurrent.locks;

import java.util.NoSuchElementException;

import info.aduna.iteration.Iteration;
import info.aduna.iteration.IterationWrapper;

/**
 * An Iteration that holds on to a lock until the Iteration is closed. Upon
 * closing, the underlying Iteration is closed before the lock is released. This
 * iterator closes itself as soon as all elements have been read.
 */
public class LockingIteration<E, X extends Exception> extends
		IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lock to release when the Iteration is closed.
	 */
	private final Lock lock;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LockingIteration.
	 * 
	 * @param lock
	 *            The lock to release when the itererator is closed, must not be
	 *            <tt>null</tt>.
	 * @param iter
	 *            The underlying Iteration, must not be <tt>null</tt>.
	 */
	public LockingIteration(Lock lock, Iteration<? extends E, X> iter) {
		super(iter);

		assert lock != null;
		this.lock = lock;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public synchronized boolean hasNext() throws X {
		if (isClosed()) {
			return false;
		}

		if (super.hasNext()) {
			return true;
		}

		close();
		return false;
	}

	@Override
	public synchronized E next() throws X {
		if (isClosed()) {
			throw new NoSuchElementException("Iteration has been closed");
		}

		return super.next();
	}

	@Override
	public synchronized void remove() throws X {
		if (isClosed()) {
			throw new IllegalStateException();
		}

		super.remove();
	}

	@Override
	protected void handleClose() throws X {
		try {
			super.handleClose();
		} finally {
			synchronized (this) {
				lock.release();
			}
		}
	}
}
