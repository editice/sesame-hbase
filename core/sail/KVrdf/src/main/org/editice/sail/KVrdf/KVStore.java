package org.editice.sail.KVrdf;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.editice.KVmodel.KVvalue;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

public class KVStore extends SailBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private RealValueStoreFactory realValueStore;//store the byte[] data by MD5 encode

	private MappingStore mappingStore;//store the mapping from string to byte[] encode
	
	private StatisticStore statisticStore;//store the statistic of the dataSet to do optimize

	private String realValueTable;

	private String mappingTable;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public KVStore() {
		super();
	}

	public KVStore(String realValueTableName, String hashTableName) {
		this();
		this.realValueTable = realValueTableName;
		this.mappingTable = hashTableName;
	}

	/*---------*
	 * Methods *
	 *---------*/
	protected void initializeInternal() throws SailException {
		logger.info("Initializing KVStore...");
		String[] valueTable = new String[3];
		valueTable=realValueTable.split(",");
		if (valueTable.length != 3) {
			System.err.println("Usage:[tableSPO],[tablePOS],[tableOSP]");
			throw new SailException("error format for real value Table Name");
		}

		try {
			realValueStore = new EncodeTripleStore(valueTable[0],valueTable[1],valueTable[2]);
			mappingStore = new MappingStore(mappingTable);
			statisticStore=new StatisticStore(KVStoreConfig.RedisPort,KVStoreConfig.host);
		} catch (Exception e) {
			if(realValueStore!=null){
				realValueStore.close();
			}
			if(mappingStore!=null){
				mappingStore.close();
				mappingStore=null;
			}
			throw new SailException(e);
		}
		logger.info("KVStore initialized");
	}
	
	@Override
	protected SailConnection getConnectionInternal() throws SailException {
		try{
			return new KVStoreConnection(this);
		}catch(Exception e){
			throw new SailException(e);
		}
	}

	@Override
	protected void shutDownInternal() throws SailException {
		// TODO Auto-generated method stub
		logger.debug("shutdown the KVStore...");
		try{
			realValueStore.close();
			mappingStore.close();
			logger.debug("KVStore shutdown");
		}catch(Exception e){
			throw new SailException(e);
		}
	}

	@Override
	public ValueFactory getValueFactory() {
		// TODO Auto-generated method stub
		return mappingStore;
	}

	@Override
	public boolean isWritable() throws SailException {
		// TODO Auto-generated method stub
		//here later should add 
		return true;
	}

	public RealValueStoreFactory getHbaseStore(){
		return realValueStore;
	}
	
	public MappingStore getMappingStore(){
		return mappingStore;
	}
	
	protected CloseableIteration<? extends Statement, IOException> createStatementIterator(
			Resource subj, URI pred, Value obj, boolean includeInferred,
			boolean readTransaction, Resource... contexts) throws IOException {
//		System.err.println(subj+"--"+pred+"--"+obj);
		
		
//		System.err.println(subj+"--"+pred+"--"+obj);
//		System.exit(0);
		int len=KVvalue.UNKNOWN_LEN;
		byte[] subjarr=new byte[len];
		if(subj!=null){
			subjarr=mappingStore.getByteArr(subj);
			if(subjarr.length==len){
				return new EmptyIteration<Statement, IOException>();
			}
		}
		
		byte[]  predarr=new byte[len];
		if(pred!=null){
			predarr=mappingStore.getByteArr(pred);
			if(predarr.length==len){
//				System.err.println(subjarr.length);
				return new EmptyIteration<Statement, IOException>();
			}
		}
		
		byte[] objarr=new byte[len];
		if(obj!=null){
			objarr=mappingStore.getByteArr(obj);
			if(objarr.length==len){
				return new EmptyIteration<Statement, IOException>();
			}
		}
		
		//contexts we now don't handle,thus later we 'll replace "1" with contextIDList.size()
		ArrayList<KVStatementsIterator> perContextIterList = new ArrayList<KVStatementsIterator>(
				1);
		
		ResultIterator iter ;
		if(includeInferred){
			// Get both explicit and inferred statements,and actually it won't work now
			iter=realValueStore.getTriples(subjarr, predarr, objarr);
		}else{
			iter = realValueStore.getTriples(subjarr, predarr, objarr);
		}
		
		perContextIterList.add(new KVStatementsIterator(iter,
				mappingStore));
		
		if(perContextIterList.size()==1){
			//actually now this is the only situation
//			logger.info("actually this is the only situation:");
			return perContextIterList.get(0);
		}else{
			//here will not execute
			logger.error("here will not execute,there must be any error");
			return new UnionIteration<Statement, IOException>(
					perContextIterList);
		}
	}

	/**
	 * this method just for some use, it should be private though now i set it to public
	 * @return
	 */
	public boolean totalDelete() {
		// TODO Auto-generated method stub
		if(realValueStore.delAll() && mappingStore.delAll()){
			return true;
		}
		return false;
	}
	

	public double cardinality(Resource subj, URI pred, Value obj,
			Resource context) {
		
//		System.err.println(subj+" "+obj+" "+pred);
		int len=KVvalue.UNKNOWN_LEN;
		byte[] subjarr=new byte[len];
		if(subj!=null){
			subjarr=mappingStore.getByteArr(subj);
			if(subjarr.length==len){
				return 0;
			}
		}
		
		byte[]  predarr=new byte[len];
		if(pred!=null){
			predarr=mappingStore.getByteArr(pred);
			if(predarr.length==len){
//				System.err.println(subjarr.length);
				return 0;
			}
		}
		
		byte[] objarr=new byte[len];
		if(obj!=null){
			objarr=mappingStore.getByteArr(obj);
			if(objarr.length==len){
				return 0;
			}
		}
		
		//TODO we didn't compute context here
//		System.out.println("statisticStore.cardinality()");
		return statisticStore.cardinality(subjarr, predarr, objarr);
	}


	public Iteration<? extends Statement, ? extends Exception> createStatementLitIterator(
			List<Statement> checkList, boolean includeInferred,
			boolean readTransaction) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
