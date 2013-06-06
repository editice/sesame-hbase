/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;
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
public abstract class NotifyingSailBase extends SailBase implements
		NotifyingSail {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Objects that should be notified of changes to the data in this Sail.
	 */
	private Set<SailChangedListener> sailChangedListeners = new HashSet<SailChangedListener>(
			0);

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public NotifyingSailConnection getConnection() throws SailException {
		return (NotifyingSailConnection) super.getConnection();
	}

	@Override
	protected abstract NotifyingSailConnection getConnectionInternal()
			throws SailException;

	public void addSailChangedListener(SailChangedListener listener) {
		synchronized (sailChangedListeners) {
			sailChangedListeners.add(listener);
		}
	}

	public void removeSailChangedListener(SailChangedListener listener) {
		synchronized (sailChangedListeners) {
			sailChangedListeners.remove(listener);
		}
	}

	/**
	 * Notifies all registered SailChangedListener's of changes to the contents
	 * of this Sail.
	 */
	public void notifySailChanged(SailChangedEvent event) {
		synchronized (sailChangedListeners) {
			for (SailChangedListener l : sailChangedListeners) {
				l.sailChanged(event);
			}
		}
	}
}
