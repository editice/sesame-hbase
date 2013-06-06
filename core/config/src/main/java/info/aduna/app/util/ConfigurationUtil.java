/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.app.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import info.aduna.app.config.Configuration;
import info.aduna.io.IOUtil;
import info.aduna.io.ResourceUtil;

public class ConfigurationUtil {

	/**
	 * Load configuration settings from the specified file.
	 * 
	 * @param file
	 *            the file to load from
	 * @return the contents of the file as a String, or null if the file did not
	 *         exist
	 * @throws IOException
	 *             if the contents of the file could not be read due to an I/O
	 *             problem
	 */
	public static String loadConfigurationContents(File file)
			throws IOException {
		String result = null;
		if (file.exists()) {
			result = IOUtil.readString(file);
		}
		return result;
	}

	/**
	 * Load configuration settings from a resource on the classpath.
	 * 
	 * @param resourceName
	 *            the name of the resource
	 * @return the contents of the resources as a String, or null if the
	 *         resource, nor its default, could be found
	 * @throws IOException
	 *             if the resource could not be read due to an I/O problem
	 */
	public static String loadConfigurationContents(String resourceName)
			throws IOException {
		String result = null;
		InputStream in = ResourceUtil
				.getInputStream(getResourceName(resourceName));
		if (in == null) {
			in = ResourceUtil
					.getInputStream(getDefaultResourceName(resourceName));
		}
		if (in != null) {
			result = IOUtil.readString(in);
		}
		return result;
	}

	/**
	 * Load configuration properties from the specified file.
	 * 
	 * @param file
	 *            the file to load from
	 * @return the contents of the file as Properties, or null if the file did
	 *         not exist
	 * @throws IOException
	 *             if the contents of the file could not be read due to an I/O
	 *             problem
	 */
	public static Properties loadConfigurationProperties(File file,
			Properties defaults) throws IOException {
		Properties result = null;
		if (file.exists()) {
			result = IOUtil.readProperties(file, defaults);
		} else {
			result = new Properties(defaults);
		}
		return result;
	}

	/**
	 * Load configuration properties from a resource on the classpath.
	 * 
	 * @param resourceName
	 *            the name of the resource
	 * @return the contents of the resource as Properties
	 * @throws IOException
	 *             if the resource could not be read due to an I/O problem
	 */
	public static Properties loadConfigurationProperties(String resourceName,
			Properties defaults) throws IOException {
		Properties result = null;

		String defaultResourceName = getDefaultResourceName(resourceName);

		Properties defaultResult = null;
		InputStream in = ResourceUtil.getInputStream(defaultResourceName);
		if (in != null) {
			defaultResult = IOUtil.readProperties(in, defaults);
		} else {
			defaultResult = new Properties(defaults);
		}

		// load application-specific overrides
		in = ResourceUtil.getInputStream(getResourceName(resourceName));
		if (in != null) {
			result = IOUtil.readProperties(in, defaultResult);
		} else {
			result = new Properties(defaultResult);
		}

		return result;
	}

	private static String getResourceName(String resourceName) {
		StringBuilder result = new StringBuilder(
				Configuration.RESOURCES_LOCATION);
		if (resourceName.startsWith("/")) {
			resourceName = resourceName.substring(1);
		}
		result.append(resourceName);
		return result.toString();
	}

	private static String getDefaultResourceName(String resourceName) {
		StringBuilder result = new StringBuilder(
				Configuration.DEFAULT_RESOURCES_LOCATION);
		if (resourceName.startsWith("/")) {
			resourceName = resourceName.substring(1);
		}
		result.append(resourceName);
		return result.toString();
	}

	/**
	 * Save configuration settings to a file.
	 * 
	 * @param contents
	 *            the configuration settings
	 * @param file
	 *            the file to write to
	 * @throws IOException
	 *             if the settings could not be saved because of an I/O problem
	 */
	public static void saveConfigurationContents(String contents, File file)
			throws IOException {
		if (file.getParentFile().mkdirs() || file.getParentFile().canWrite()) {
			IOUtil.writeString(contents, file);
		}
	}

	/**
	 * Save configuration properties to a file.
	 * 
	 * @param props
	 *            the configuration properties
	 * @param file
	 *            the file to write to
	 * @throws IOException
	 *             if the settings could not be saved because of an I/O problem
	 */
	public static void saveConfigurationProperties(Properties props, File file,
			boolean includeDefaults) throws IOException {
		if (file.getParentFile().mkdirs() || file.getParentFile().canWrite()) {
			IOUtil.writeProperties(props, file, includeDefaults);
		}
	}
}
