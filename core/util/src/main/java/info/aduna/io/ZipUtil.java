/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Zip-related utilities.
 */
public class ZipUtil {

	/**
	 * Magic number for ZIP files (4 bytes: <tt>0x04034b50</tt>).
	 */
	private final static byte MAGIC_NUMBER[] = { (byte) 0x50, (byte) 0x4B,
			(byte) 0x03, (byte) 0x04 };

	public static boolean isZipStream(InputStream in) throws IOException {
		in.mark(MAGIC_NUMBER.length);
		byte[] fileHeader = IOUtil.readBytes(in, MAGIC_NUMBER.length);
		in.reset();
		return Arrays.equals(MAGIC_NUMBER, fileHeader);
	}

	/**
	 * Extract the contents of a zipfile to a directory.
	 * 
	 * @param zipFile
	 *            the zip file to extract
	 * @param destDir
	 *            the destination directory
	 * @throws IOException
	 *             when something untowards happens during the extraction
	 *             process
	 */
	public static void extract(File zipFile, File destDir) throws IOException {
		ZipFile zf = new ZipFile(zipFile);
		try {
			extract(zf, destDir);
		} finally {
			zf.close();
		}
	}

	/**
	 * Extract the contents of a zipfile to a directory.
	 * 
	 * @param zipFile
	 *            the zip file to extract
	 * @param destDir
	 *            the destination directory
	 * @throws IOException
	 *             when something untowards happens during the extraction
	 *             process
	 */
	public static void extract(ZipFile zipFile, File destDir)
			throws IOException {
		assert destDir.isDirectory();

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			writeEntry(zipFile, entry, destDir);
		}
	}

	/**
	 * Write an entry to a zip file.
	 * 
	 * @param zipFile
	 *            the zip file to read from
	 * @param entry
	 *            the entry to process
	 * @param destDir
	 *            the file to write to
	 * @throws IOException
	 *             if the entry could not be processed
	 */
	public static void writeEntry(ZipFile zipFile, ZipEntry entry, File destDir)
			throws IOException {
		File outFile = new File(destDir, entry.getName());

		if (entry.isDirectory()) {
			outFile.mkdirs();
		} else {
			outFile.getParentFile().mkdirs();

			InputStream in = zipFile.getInputStream(entry);
			try {
				IOUtil.writeStream(in, outFile);
			} finally {
				in.close();
			}
		}
	}
}
