/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.app.config;

import java.io.IOException;

public interface Configuration {

	public static final String DIR = "conf";

	public static final String RESOURCES_LOCATION = "/info/aduna/app/config/";

	public static final String DEFAULT_RESOURCES_LOCATION = RESOURCES_LOCATION
			+ "defaults/";

	/**
	 * Initialize the configuration settings.
	 * 
	 * @throws IOException
	 *             if the configuration settings could not be initialized
	 *             because of an I/O problem.
	 */
	public void init() throws IOException;

	/**
	 * Load the configuration settings.
	 * 
	 * Settings will be loaded from a user and application specific location
	 * first. If no such settings exists, an attempt will be made to retrieve
	 * settings from a resource on the classpath. If no such settings exist
	 * either, settings will be loaded from a default resource on the classpath.
	 * 
	 * @throws IOException
	 *             if the configuration settings could not be loaded due to an
	 *             I/O problem.
	 */
	public void load() throws IOException;

	/**
	 * Store configuration settings.
	 * 
	 * Settings will be stored in a user and application specific location.
	 * 
	 * @throws IOException
	 *             if the configuration settings could not be saved due to an
	 *             I/O problem.
	 */
	public void save() throws IOException;

	/**
	 * Clean up configuration resources.
	 * 
	 * @throws IOException
	 *             if one or more resources could not be cleaned up.
	 *             Implementations should attempt to clean up as many resources
	 *             as possible before returning or throwing an exception.
	 */
	public void destroy() throws IOException;
}
