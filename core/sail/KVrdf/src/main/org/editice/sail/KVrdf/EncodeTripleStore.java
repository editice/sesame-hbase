package org.editice.sail.KVrdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.editice.KVmodel.KVvalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodeTripleStore implements RealValueStoreFactory{

	/*-----------*
	 * Constants *
	 *-----------*/
	
	static final int MD5Length = KVStoreConfig.hashLen;

	static final int SUBJ_IDX = 0;

	static final int PRED_IDX = MD5Length;

	static final int OBJ_IDX = MD5Length << 1;

	/**
	 * Bit field indicating that a statement has been explicitly added (instead
	 * of being inferred).
	 */
	static final byte EXPLICIT_FLAG = (byte) 0x1; // 0000 0001

	/**
	 * Bit field indicating that a statement has been added in a (currently
	 * active) transaction.
	 */
	static final byte ADDED_FLAG = (byte) 0x2; // 0000 0010

	/**
	 * Bit field indicating that a statement has been removed in a (currently
	 * active) transaction.
	 */
	static final byte REMOVED_FLAG = (byte) 0x4; // 0000 0100

	/**
	 * Bit field indicating that the explicit flag has been toggled (from true
	 * to false, or vice versa) in a (currently active) transaction.
	 */
	static final byte TOGGLE_EXPLICIT_FLAG = (byte) 0x8; // 0000 1000

	// static final int CONTEXT_IDX = 12;

	// static final int FLAG_IDX = 16;

	/*-----------*
	 * Variables *
	 *-----------*/
	/**
	 * the list of IndexStore that are used to store and retrieval
	 */
	private final List<IndexStore> indexStores = new ArrayList<IndexStore>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String familyName = KVStoreConfig.familyName;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public EncodeTripleStore() {

	}

	public EncodeTripleStore(String realValueTableName) {

	}

	public EncodeTripleStore(String SPOTableName, String POSTableName,
			String OSPTableName) {
		this(SPOTableName, POSTableName, OSPTableName, familyName);
	}

	public EncodeTripleStore(String SPOTableName, String POSTableName,
			String OSPTableName, String familyName) {
		indexStores.add(new IndexStore(SPOTableName, familyName));
		indexStores.add(new IndexStore(POSTableName, familyName));
		indexStores.add(new IndexStore(OSPTableName, familyName));
	}

	/*---------*
	 * Methods *
	 *---------*/
	public void close() {

	}

	public ResultIterator getTriples(byte[] subj, byte[] pred, byte[] obj) throws IOException {
		// now the fourth parameter don't use
		return getTriples(subj, pred, obj, 0);
	}

	private ResultIterator getTriples(byte[] subj, byte[] pred, byte[] obj,
			int flag) throws IOException {
		// flag now don't use
		int index = 0;
		try {
			index = getBestIndex(subj, pred, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean doRangeSearch = indexStores.get(index).getPatternScore(subj,
				pred, obj) > 0;
		return getTripleUsingIndex(subj, pred, obj, index, flag, doRangeSearch);
	}

	private int getBestIndex(byte[] subj, byte[] pred, byte[] obj)
			throws Exception {
		// TODO Auto-generated method stub
		String type = "";
		int len = KVvalue.UNKNOWN_LEN;
		if (subj.length != len) {
			type += "S";
		}
		if (pred.length != len) {
			type += "P";
		}
		if (obj.length != len) {
			type += "O";
		}
		if (type.equals("SPO") || type.equals("SP") || type.equals("S")
				|| type.equals("")) {
			return 0;
		} else if (type.equals("PO") || type.equals("P")) {
			return 1;
		} else if (type.equals("SO") || type.equals("O")) {
			return 2;
		} else {
			logger.error("error type");
			throw new Exception("error type");
		}

	}

	private ResultIterator getTripleUsingIndex(byte[] subj, byte[] pred,
			byte[] obj, int index, int flag, boolean doRangeSearch) throws IOException {
		ResultIterator iter = null;
		byte[] searchRow = getData(subj, pred, obj);
		if (doRangeSearch) {
			// do range search
			byte[] minValue = getMinValue(subj, pred, obj, index);
			byte[] maxValue = getMaxValue(subj, pred, obj, index);

			iter = indexStores.get(index).iterateRangedValues(searchRow,minValue,
					maxValue,index);
		} else {
			// just get the result
			
			iter = indexStores.get(index).iterateValues(searchRow,index);
		}
		return iter;
	}

	private byte[] getMaxValue(byte[] subj, byte[] pred, byte[] obj, int index) {
		byte[] maskf=new byte[MD5Length];
		for(int i=0;i<MD5Length;i++){
			maskf[i]=-1;
		}
		return shuffleDataOrder((subj.length == MD5Length ? subj
				: maskf), (pred.length == MD5Length ? pred
				: maskf), (obj.length == MD5Length ? obj
				: maskf), index);
	}

	private byte[] getMinValue(byte[] subj, byte[] pred, byte[] obj, int index) {
		return shuffleDataOrder((subj.length == MD5Length ? subj
				: new byte[MD5Length]), (pred.length == MD5Length ? pred
				: new byte[MD5Length]), (obj.length == MD5Length ? obj
				: new byte[MD5Length]), index);
	}

	public boolean storeTriple(byte[] subjarr, byte[] predarr, byte[] objarr)
			throws IOException {
		byte[] data = getData(subjarr, predarr, objarr);
		if (indexStores.get(0).existTriple(data)) {
			// just do nothing
			return true;
		} else {
			for (int i = 0; i < 3; i++) {// here hard coding
				switch (i) {
				case 0:
					indexStores.get(0).addTriple(data);
					break;
				case 1:
					byte[] dataPOS = getData(predarr, objarr, subjarr);
					indexStores.get(1).addTriple(dataPOS);
					break;
				case 2:
					byte[] dataOSP = getData(objarr, subjarr, predarr);
					indexStores.get(2).addTriple(dataOSP);
					break;
				default:
					logger.error("indexstores.length was not between 0-2");
				}
			}
		}
		return false;
	}

	private byte[] getData(byte[] subjarr, byte[] predarr, byte[] objarr) {
		// TODO Auto-generated method stub
		return ByteArrUtil.mergeByteArray(subjarr, predarr, objarr);
	}

	private byte[] shuffleDataOrder(byte[] subjarr, byte[] predarr,
			byte[] objarr, int index) {
		if (index == 0) {
			return getData(subjarr, predarr, objarr);
		} else if (index == 1) {
			return getData(predarr, objarr, subjarr);
		} else if (index == 2) {
			return getData(objarr, subjarr, predarr);
		} else {
			logger.error("error index ");
			return null;
		}

	}

	public boolean removeTriple(String subj, String pred, String obj) {

		return false;
	}

	public int removeTriples(ResultIterator iter) {
		int count = 0;

		return count;
	}

	public KVvalue getValue(byte[] encode) {
		//TODO
		System.err.println("HbaseStore.getValue() is not realize");
		return null;
	}

	public byte[] getByteEncode(String value) {

		return null;
	}

	public boolean delAll() {
		boolean flag = true;
		for (IndexStore indexStore : indexStores) {
			flag &= indexStore.delTable();
		}
		if (!flag) {
			return false;
		}
		return true;
	}
}
