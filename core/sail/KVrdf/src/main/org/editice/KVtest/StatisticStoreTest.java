package org.editice.KVtest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.editice.sail.KVrdf.ByteArrUtil;
import org.editice.sail.KVrdf.KVStoreConfig;
import org.editice.sail.KVrdf.StatisticStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class StatisticStoreTest {
	
	//insert the data to statisticStore
	public void addStatementsFromLubmWithJena(StatisticStore ss,String FileDir) throws NoSuchAlgorithmException, IOException, InterruptedException{
		TraverseDir2RDF(ss,FileDir);
	}
	
	public  void TraverseDir2RDF(StatisticStore ss,String dirPath) throws NoSuchAlgorithmException, IOException, InterruptedException
	{
		File file=new File(dirPath);
	    File[] tempList = file.listFiles();
	    int count=0;
	    for(int i=0;i<tempList.length;i++)
	    {
	    	System.out.println((count++)+"/"+tempList.length+" "+tempList[i].getAbsolutePath());
	    	convertXMLFile2RDF(ss,tempList[i].getAbsolutePath());
	    }
	}
		
	public void convertXMLFile2RDF(StatisticStore ss,String owlfile) throws NoSuchAlgorithmException, IOException, InterruptedException
	{
		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(owlfile);
		if (in == null) 
		{
		    throw new IllegalArgumentException("File: " + owlfile+ " not found");
		}
		
		// read the RDF/XML file
		model.read(in, null);
		StmtIterator iterator= model.listStatements();
		int count=0;
		while(iterator.hasNext())
		{
			Statement statement=iterator.next();

			//make up the bytes for S,P,O , 0:SPO; 1:POS; 2:OSP; 3:HS; 4:HP; 5:HO
			byte[] subjectHash= ByteArrUtil.getHashValue(statement.getSubject().toString());
			byte[] predictHash= ByteArrUtil.getHashValue(statement.getPredicate().toString());
			byte[] objectHash= ByteArrUtil.getHashValue(statement.getObject().toString());
			
			ss.storeTriple(subjectHash, predictHash, objectHash);
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		StatisticStore ss=new StatisticStore(KVStoreConfig.RedisPort,KVStoreConfig.host);
		String FileDir = "/home/hadoop/zj/dataDir/semanticWeb";
//		ss.test();
//		ss.clear();
//		ss.close();
		StatisticStoreTest st=new StatisticStoreTest();
		
		long timea=System.currentTimeMillis();
		st.addStatementsFromLubmWithJena(ss, FileDir);
		long timeb=System.currentTimeMillis();
		System.out.println("cost time in generate statisticStore data: "+(timeb-timea));
		ss.close();
	}

}
