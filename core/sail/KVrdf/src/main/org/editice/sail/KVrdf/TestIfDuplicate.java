package org.editice.sail.KVrdf;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class TestIfDuplicate {
	private static Configuration conf = null;

	static {
		conf = HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}
	
	public static void FixDuplicateHash(String a,String b) throws IOException{
		HTable table=new HTable(conf,"gr_final_Hash_100");
		
		byte[] a1=ByteArrUtil.getHashValue(a);
		byte[] b1=ByteArrUtil.getHashValue(b);
		
		Put put=new Put(b1);
		put.add(Bytes.toBytes("f"), Bytes.toBytes("c"), Bytes.toBytes(b));
		table.put(put);
		
		Get get=new Get(b1);
		Result res=table.get(get);
		if(res.isEmpty()){
			System.err.println("error");
		}else{
			System.out.println(new String(res.getValue(Bytes.toBytes("f"), Bytes.toBytes("c"))));
		}
	}

	public static void main(String[] args) throws IOException {
//		 TODO Auto-generated method stub
		String b="UndergraduateStudent94@Department2.University33.edu";
		String a="http://www.Department10.University39.edu/FullProfessor3";
//		
//		String b="UndergraduateStudent130@Department12.University5.edu";
//		String a="http://www.Department2.University99.edu/GraduateStudent65";
		byte[] a1=ByteArrUtil.getHashValue(a);
		byte[] b1=ByteArrUtil.getHashValue(b);
		System.out.println(ByteArrUtil.getBase64Code(a1));
		System.out.println(ByteArrUtil.getBase64Code(b1));
		
		FixDuplicateHash(a,b);
	}

	
}
