package org.editice.KVtest;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.editice.sail.KVrdf.ResultIterator;

public class TestIteratorWrapper {
	
	private static Configuration conf=null;
	static{
		conf=HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}
	
	private HTable table;
	
	
	public TestIteratorWrapper(String tableName){
		try {
			table=new HTable(conf,tableName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ResultIterator search(byte[] key,byte[] min,byte[] max){
		return new MyRangeIterator(key,min,max);
	}

	public void ScanAll(String tableName) throws IOException{
		byte[] minValue=new byte[24];
		byte[] maxValue=new byte[24];
		for(int i=0;i<24;i++){
			maxValue[i]=-1;
		}
		
		HTable table=new HTable(conf,tableName);
		Scan scan=new Scan();
		scan.setStartRow(minValue);
		scan.setStopRow(maxValue);
		ResultScanner res=table.getScanner(scan);
		int count=0;
		while(count++<10){
//			System.out.println(res.next().getRow());
			
			HTable table1=new HTable(conf,"zjhash");
			KVStoreTest.printRow(res.next(),table,table1);
		}
		res.close();
	}
	
	private class MyRangeIterator implements ResultIterator{
		
		private final byte[] searchRow;
		
		private final byte[] minValue;
		
		private final byte[] maxValue;
		
		private Scan scan;
		
		private ResultScanner rs;

		public MyRangeIterator(byte[] searchRow, byte[] min, byte[] max) {
			this.searchRow=searchRow;
			this.minValue=min;
			this.maxValue=max;
			if(minValue!=null){
				System.out.println(minValue.length);
				scan=new Scan();
				scan.setStartRow(minValue);
				scan.setStopRow(maxValue);
				try {
					rs=table.getScanner(scan);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			rs.close();
		}

		@Override
		public byte[] next() throws IOException {
			if(minValue==null){
				Get get=new Get(searchRow);
				if(table.get(get).isEmpty()){
					return null;
				}
				return searchRow;
			}else{
				return rs.next().getRow();
			}
		}

		@Override
		public void set(byte[] result) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		byte[] key=new byte[24];
		byte[] minValue=new byte[24];
		byte[] maxValue=new byte[24];
		for(int i=0;i<24;i++){
			maxValue[i]=-1;
		}
		
		
		TestIteratorWrapper tiw=new TestIteratorWrapper("zjSPO");
		tiw.ScanAll("zjSPO");
		
		System.out.println("wrapper");
		ResultIterator iter=tiw.search(key,minValue,maxValue);
		
		HTable table1=new HTable(conf,"zjhash");
		HTable table0=new HTable(conf,"zjSPO");
		int count=0;
		while(count++<10){
//			System.out.println(iter.next());
//			System.out.println("-");
			KVStoreTest.printRow(iter.next(),table0,table1);
		}
			
	}

	
}
