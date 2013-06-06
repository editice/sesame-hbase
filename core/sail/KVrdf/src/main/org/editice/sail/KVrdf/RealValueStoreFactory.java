package org.editice.sail.KVrdf;

import java.io.IOException;

/**
 * used to create the RealValueStore which stores the byteArray value of encoded string
 * @author zj
 *
 */
public interface RealValueStoreFactory {

	public void close();
	
	public ResultIterator getTriples(byte[] subj, byte[] pred, byte[] obj) throws IOException;
	
	public boolean storeTriple(byte[] subjarr, byte[] predarr, byte[] objarr)throws IOException;
	
	/**
	 * used to remove one triple in KVStore, and returns true if the operation is done successfully
	 * @param subj
	 * @param pred
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public boolean removeTriple(String subj, String pred, String obj) throws IOException;
	
	/**
	 * used to remove some triples and returns the number of the triples which has been deleted
	 * @param iter
	 * @return
	 */
	public int removeTriples(ResultIterator iter);
	
	/**
	 * if all the data and the table has been removed, return true
	 * @return
	 */
	public boolean delAll();
}
