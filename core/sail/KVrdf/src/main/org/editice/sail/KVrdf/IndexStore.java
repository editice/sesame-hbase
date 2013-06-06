package org.editice.sail.KVrdf;

import java.io.IOException;

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
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.editice.KVmodel.KVvalue;
import org.editice.KVtest.KVStoreTest;

public class IndexStore {

	private static Configuration conf = null;

	static {
		conf = HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}

	private HTable table;

	private final String tableName;

	private final String familyName;

	private byte[] family;

	public IndexStore(String indexTableName, String familyName) {
		this.tableName = indexTableName;
		this.familyName = familyName;
		this.family = Bytes.toBytes(familyName);
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			try {
				if (!admin.tableExists(indexTableName)) {
					HTableDescriptor desc = new HTableDescriptor(indexTableName);
					HColumnDescriptor colum = new HColumnDescriptor(familyName);
					desc.addFamily(colum);
					admin.createTable(desc);
				}
				table = new HTable(conf, indexTableName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MasterNotRunningException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ZooKeeperConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public ResultIterator iterateValues(byte[] searchRow, int index)
			throws IOException {
		return new MyRangeIterator(searchRow, null, null, index);
	}

	public ResultIterator iterateRangedValues(byte[] searchRow,
			byte[] minValue, byte[] maxValue, int index) {
		return new MyRangeIterator(searchRow, minValue, maxValue, index);
	}

	public void addTriple(byte[] data) throws IOException {
		Put put = new Put(data);
		put.add(family, null, null);
		table.put(put);
	}

	public boolean existTriple(byte[] data) throws IOException {
		Get get = new Get(data);
		if (!table.get(get).isEmpty()) {
			return true;
		}
		return false;
	}

	public int getPatternScore(byte[] subj, byte[] pred, byte[] obj) {
		int len = KVvalue.UNKNOWN_LEN;
		if (subj.length == len || pred.length == len || obj.length == len) {
			return 1;
		}
		return 0;
	}

	public String getFamilyName() {
		return familyName;
	}

	public boolean delTable() {
		HBaseAdmin admin;
		try {
			admin = new HBaseAdmin(conf);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
			return true;
		} catch (MasterNotRunningException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ZooKeeperConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/*--------------*
	 * inner class  *
	 *--------------*/
	private class MyRangeIterator implements ResultIterator {

		private final byte[] searchRow;

		private byte[] minValue;

		private byte[] maxValue;

		private Scan scan;

		private ResultScanner rs = null;

		private int index;

		private boolean flag = true;

		public MyRangeIterator(byte[] searchRow, byte[] min, byte[] max,
				int index) {
			this.searchRow = searchRow;
			this.minValue = min;
			this.maxValue = max;
			this.index = index;
			if (minValue != null) {
				scan = new Scan();
				scan.setCaching(10000);
				scan.setStartRow(minValue);
				scan.setStopRow(maxValue);
				try {
					rs = table.getScanner(scan);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
//				 scan = new Scan();
//				 minValue = searchRow;
//				 maxValue = searchRow;
//				 scan.setStartRow(minValue);
//				 scan.setStopRow(maxValue);
//				 try {
//				 rs = table.getScanner(scan);
//				 } catch (IOException e) {
//				 // TODO Auto-generated catch block
//				 e.printStackTrace();
//				 }

			}
		}

		@Override
		public void close() throws IOException {
			if (rs!= null) {
				rs.close();
			}
		}

		public boolean hasNext() {
			if (minValue == null) {
				return false;
			} else
				return rs.iterator().hasNext();
		}

		@Override
		public byte[] next() throws IOException {
			//use get will be quick than scan in this situation
			if (minValue == null) {
				if (flag) {
					Get get = new Get(searchRow);
					Result res = table.get(get);
					if (!res.isEmpty()) {
						flag = false;
						return ByteArrUtil.POSArrToSPOArr(res.getRow());
					}else{
						return null;
					}
				} else {
					return null;
				}
			} else {
				Result result = rs.next();
				if (result != null) {
					byte[] next = result.getRow();
					switch (index) {
					case 0:
						break;
					case 1:
						// System.err.println("POS");
						next = ByteArrUtil.POSArrToSPOArr(next);

						// HTable table1=new HTable(conf,"zjhash");
						// KVStoreTest.printRow(next, table, table1);
						break;
					case 2:
						next = ByteArrUtil.OSPArrToSPOArr(next);
						break;
					default:
						return null;
					}
					return next;
				} else {
					return null;
				}
			}
		}

		@Override
		public void set(byte[] result) throws IOException {
			// TODO Auto-generated method stub

		}

	}

}
