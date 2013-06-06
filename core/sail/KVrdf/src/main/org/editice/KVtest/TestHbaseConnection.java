package org.editice.KVtest;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

public class TestHbaseConnection {
	private static Configuration conf = null;

	static {
		conf = HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		HTable table = new HTable(conf, "t1");
		System.out.println(table.getEndKeys());
		table.close();
	}

}
