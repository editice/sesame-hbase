package org.editice.KVtest;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class TestPureHbaseHandle {
	
	private static Configuration conf = null;

	static {
		conf = HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}
	
	public static void addOneStatementOnlyKey(String tableName) throws IOException{
		HTable table=new HTable(conf,tableName);
		byte[] data=new byte[24];
		Put put=new Put(data);
		put.add(Bytes.toBytes("family"),null,null);
		table.put(put);
	}

	public static void TestCostTimeGetVsScan() throws IOException{
		HTable table=new HTable(conf,"zjSPO");
		byte[] search=new byte[12];
		int loop=100;
		
		long timea=System.currentTimeMillis();
		for(int i=0;i<loop;i++){
			Get get=new Get(search);
			Result res=table.get(get);
		}
		long timeb=System.currentTimeMillis();
		System.out.println("get initialize cost: "+(timeb-timea)+"ms");
		
		long timec=System.currentTimeMillis();
		for(int i=0;i<loop;i++){
			Scan scan=new Scan(search);
			scan.setStartRow(search);
			scan.setStopRow(search);
			ResultScanner res=table.getScanner(scan);
			res.next();
		}
		long timed=System.currentTimeMillis();
		System.out.println("scan initialize cost: "+(timed-timec)+"ms");
		
	}
	public static void main(String[] args) throws IOException {
//		String tableName="zjhash";
//		Scan scan=new Scan();
//		scan.setCaching(10);
//		HTable tables=new HTable(conf,"zjSPO");
//		ResultScanner res=tables.getScanner(scan);
//		
//		int count=0;
//		long timea=System.currentTimeMillis();
//		Iterator<Result> iter=res.iterator();
//		while(iter.hasNext()){
//			iter.next();
//			count++;
//		}
//		long timeb=System.currentTimeMillis();
//		System.out.println("count: "+count);
//		System.out.println("time cost: "+(timeb-timea)+"ms");
		
		TestCostTimeGetVsScan();
	}

}
