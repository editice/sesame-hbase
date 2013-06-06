/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.platform.support.MacOSXPlatform;
import info.aduna.platform.support.PosixGnomePlatform;
import info.aduna.platform.support.PosixKDEPlatform;
import info.aduna.platform.support.PosixPlatform;
import info.aduna.platform.support.WindowsPlatform;

/**
 * PlatformFactory creates a Platform instance corresponding with the current
 * platform.
 */
public class PlatformFactory {

	private static PlatformFactory sharedInstance;

	/**
	 * Returns the Platform instance corresponding with the current platform.
	 */
	public static PlatformFactory getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new PlatformFactory();
		}
		return sharedInstance;
	}

	/**
	 * Returns the Platform instance corresponding with the current platform.
	 */
	public static Platform getPlatform() {
		return getInstance().platform;
	}

	/*-----------*
	 * Constants *
	 *-----------*/

	public final Platform platform;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*--------------*
	 * Constructors *
	 *--------------*/

	private PlatformFactory() {
		platform = createPlatform();
	}

	/**
	 * Tries to determine the platform we're running on based on Java system
	 * properties and/or environment variables. See
	 * http://lopica.sourceforge.net/os.html for an overview.
	 */
	private Platform createPlatform() {
		Platform platform;

		try {
			String osName = System.getProperty("os.name");

			if (osName != null) {
				osName = osName.toLowerCase();

				logger.debug("os.name = {}", osName);

				if (osName.contains("windows")) {
					logger.debug("Detected Windows platform");
					platform = new WindowsPlatform();
				} else if (osName.contains("solaris")
						|| osName.contains("sunos") || osName.contains("linux")
						|| osName.contains("hp-ux")) {
					// Try to detect specific window managers
					if (isGnome()) {
						logger
								.debug("Detected Gnome window manager on Posix platform");
						platform = new PosixGnomePlatform();
					} else if (isKDE()) {
						logger
								.debug("Detected KDE window manager on Posix platform");
						platform = new PosixKDEPlatform();
					} else {
						logger.debug("Detected Posix platform");
						platform = new PosixPlatform();
					}
				} else if (osName.contains("mac os x")
						|| osName.contains("macos")
						|| osName.contains("darwin")
						|| System.getProperty("mrj.version") != null) {
					logger.debug("Detected Mac OS X platform");
					platform = new MacOSXPlatform();
				} else {
					logger
							.warn(
									"Unrecognized operating system: '{}', falling back to default platform",
									osName);
					platform = new DefaultPlatform();
				}
			} else {
				logger
						.warn("System property 'os.name' is null, falling back to default platform");
				platform = new DefaultPlatform();
			}
		} catch (SecurityException e) {
			logger
					.warn(
							"Not allowed to read system property 'os.name', falling back to default platform",
							e);
			platform = new DefaultPlatform();
		}

		return platform;
	}

	/**
	 * Detect gnome environments.
	 */
	private boolean isGnome() {
		// check gdm session
		String gdmSession = getSystemEnv("GDMSESSION");
		if (gdmSession != null && gdmSession.toLowerCase().contains("gnome")) {
			return true;
		}

		// check desktop session
		String desktopSession = getSystemEnv("DESKTOP_SESSION");
		if (desktopSession != null
				&& desktopSession.toLowerCase().contains("gnome")) {
			return true;
		}

		// check gnome desktop id
		String gnomeDesktopSessionId = getSystemEnv("GNOME_DESKTOP_SESSION_ID");
		if (gnomeDesktopSessionId != null
				&& gnomeDesktopSessionId.trim().length() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Detect KDE environments.
	 */
	private boolean isKDE() {
		// check gdm session
		String gdmSession = getSystemEnv("GDMSESSION");
		if (gdmSession != null && gdmSession.toLowerCase().contains("kde")) {
			return true;
		}

		// check desktop session
		String desktopSession = getSystemEnv("DESKTOP_SESSION");
		if (desktopSession != null
				&& desktopSession.toLowerCase().contains("kde")) {
			return true;
		}

		// check window manager
		String windowManager = getSystemEnv("WINDOW_MANAGER");
		if (windowManager != null
				&& windowManager.trim().toLowerCase().endsWith("kde")) {
			return true;
		}

		return false;
	}

	private String getSystemEnv(String propertyName) {
		try {
			return System.getenv(propertyName);
		} catch (SecurityException e) {
			logger.warn("Not allowed to read environment variable '"
					+ propertyName + "'", e);
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(getPlatform().getApplicationDataDir(
				"My Application: Test").getAbsolutePath());
	}
}
