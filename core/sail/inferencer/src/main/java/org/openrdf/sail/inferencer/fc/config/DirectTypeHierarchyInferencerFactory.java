/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.fc.config;

import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.inferencer.fc.DirectTypeHierarchyInferencer;

/**
 * A {@link SailFactory} that creates {@link DirectTypeHierarchyInferencer}s
 * based on RDF configuration data.
 * 
 * @author Arjohn Kampman
 */
public class DirectTypeHierarchyInferencerFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:DirectTypeHierarchyInferencer";

	/**
	 * Returns the Sail's type: <tt>openrdf:DirectTypeHierarchyInferencer</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new DirectTypeHierarchyInferencerConfig();
	}

	public Sail getSail(SailImplConfig config) throws SailConfigException {
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: "
					+ config.getType());
		}

		return new DirectTypeHierarchyInferencer();
	}
}
