package org.editice.KVtest;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.editice.sail.KVrdf.KVStore;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.SailException;

public class KVStoreTest {

	private static Configuration conf = null;

	static {
		conf = HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}

	private static final int MD5Length = 8;

	/**
	 * @param args
	 * @throws RepositoryException
	 * @throws SailException
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws RDFParseException
	 */

	public static void addFile(Repository repo, String filePath, String baseURI)
			throws RepositoryException, RDFParseException, IOException {
		RepositoryConnection con = repo.getConnection();
		File file = new File(filePath);

		// to find if filepath is dir?
		if (file.isDirectory()) {
			File[] dirArr = file.listFiles();
			for (int i = 0; i < dirArr.length; i++) {
				con.add(dirArr[i], baseURI, RDFFormat.RDFXML);
			}
		} else {
			con.add(file, baseURI, RDFFormat.RDFXML);
		}

		con.close();
	}

	// use this method we can find the *.owl was successfully import to the
	// hbase , and the format is right, besides the syn is also correct, we also
	// can prove that all the data is import to hbase 
	//sesame--jena
	//100545--100543(but 100545 all in 100543, maybe timestamp problem)
	//26454--26454
	public static void showTextValueInHBaseTable(String IndextableName,
			String mappingTablename) throws IOException {
		int printLineNum = 0;
		HTable tableIndex = new HTable(conf, IndextableName);
		HTable tableMapping = new HTable(conf, mappingTablename);
		HTable tableJena=new HTable(conf,"hj_SPO_1");
		
		// scan every line
		Scan scan = new Scan();
		ResultScanner scanner = tableIndex.getScanner(scan);
		for (Result res : scanner) {
//			System.out.println();
			// if(printLineNum++>20){
			// break;
			// }
//			printRow(res,tableIndex,tableMapping);
			Get get=new Get(res.getRow());
			if(tableJena.get(get).isEmpty()){
				System.err.println("---");
				printRow(res,tableIndex,tableMapping);
			}
		}

	}

	public static void printValue(byte[] row) throws IOException{
//		HTable table0=new HTable(conf,"zjSPO");
		HTable table1=new HTable(conf,"zjhash");
		Get get=new Get(row);
		Result r=table1.get(get);
		if (r.isEmpty()) {
			System.err.println("error: can't find value");
			// break;
			return;
		} else {
			System.out.println("value is : "
					+ Bytes.toString(r.raw()[0].getValue()));
		}
	}
	
	public static void printRow(byte[] row,HTable tableIndex,HTable tableMapping) throws IOException{
		byte[] subj = new byte[MD5Length];
		byte[] pred = new byte[MD5Length];
		byte[] obj = new byte[MD5Length];

		System.arraycopy(row, 0, subj, 0, MD5Length);
		System.arraycopy(row, MD5Length, pred, 0, MD5Length);
		System.arraycopy(row, MD5Length << 1, obj, 0, MD5Length);

		if (row.length != 3 * MD5Length) {
			System.err.println("error len");
			return;
			// break;
		} else {
			Get get = new Get(subj);
			Result subj1 = tableMapping.get(get);

			if (subj1.isEmpty()) {
				System.err.println("error: can't find subject");
				// break;
				return;
			} else {
				System.out.println("subj: "
						+ Bytes.toString(subj1.raw()[0].getValue()));
			}

			get = new Get(pred);
			Result pred1 = tableMapping.get(get);

			if (pred1.isEmpty()) {
				System.err.println("error: can't find predicate");
				// break;
				return;
			} else {
				System.out.println("pred: "
						+ Bytes.toString(pred1.raw()[0].getValue()));
			}

			get = new Get(obj);
			Result obj1 = tableMapping.get(get);

			if (obj1.isEmpty()) {
				System.err.println("error: can't find object");
				// break;
				return;
			} else {
				System.out.println("obj: "
						+ Bytes.toString(obj1.raw()[0].getValue()));
			}
		}
	}
	public static void printRow(Result res,HTable tableIndex,HTable tableMapping) throws IOException{
		byte[] row = res.getRow();
		printRow(row,tableIndex,tableMapping);
		
	}
	
	public static void initial(String real, String hash)
			throws RepositoryException {
		KVStore kvStore = new KVStore(real, hash);
		Repository kvRepository = new SailRepository(kvStore);
		kvRepository.initialize();

	}

	public static void main(String[] args) throws SailException,
			RepositoryException, RDFParseException, IOException {

		// initial variable
		String real = "zjSPO,zjPOS,zjOSP";
		String hash = "zjhash";

		// add statements
		String baseURI = "http://www.test.com/";
		String fullFilePath = "/home/hadoop/zj/dataDir/semanticWeb";

		initial(real, hash);
		showTextValueInHBaseTable("zjSPO", "zjhash");
		// addFile(kvRepository,fullFilePath,baseURI);
		// kvStore.totalDelete();

	}

}
