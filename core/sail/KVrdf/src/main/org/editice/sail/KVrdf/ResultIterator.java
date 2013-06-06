package org.editice.sail.KVrdf;

import java.io.IOException;

/**
 * an iterator that iterates over actually results, for example the hbase
 * "Result"
 * 
 * @author zj
 * 
 */
public interface ResultIterator {

	/**
	 * return the next result
	 */
	public byte[] next() throws IOException;

	/**
	 * Replaces the last record returned by next() with the specified result.
	 */
	public void set(byte[] result) throws IOException;

	/**
	 * Closes the iterator, freeing any resources that it uses. Once closed, the
	 * iterator will not return any more records.
	 */
	public void close() throws IOException;
}
