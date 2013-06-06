package org.editice.KVtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLog4j {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger=LoggerFactory.getLogger(TestLog4j.class);
		logger.debug("test debug");
		logger.warn("test warn");
		logger.trace("test trace");
		logger.info("test info");
		logger.error("test error");
	}

}
