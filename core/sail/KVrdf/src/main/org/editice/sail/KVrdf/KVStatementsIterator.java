package org.editice.sail.KVrdf;

import info.aduna.iteration.LookAheadIteration;

import java.io.IOException;

import org.editice.KVmodel.KVURI;
import org.editice.KVmodel.KVvalue;
import org.editice.KVtest.KVStoreTest;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A statement iterator that wraps a RecordIterator containing statement records
 * and translates these records to {@link Statement} objects.
 * 
 */
public class KVStatementsIterator extends
		LookAheadIteration<Statement, IOException> {

	private final ResultIterator Iter;

	private final MappingStore mappingStore;
	
	public KVStatementsIterator(ResultIterator iter,MappingStore store){
		this.Iter=iter;
		this.mappingStore=store;
	}
	
	@Override
	protected Statement getNextElement() throws IOException {
//		System.err.println("-------------use as trace for the lowest level");
//		if(Iter==null){
//			System.out.println("iter is null"+Iter==null+" iter's class: "+Iter.getClass().getName());
//		}
		byte[] nextValue=Iter.next();
		
		if(nextValue==null){
			return null;
		}
		
		byte[] subjarr = ByteArrUtil.getArray(nextValue,EncodeTripleStore.SUBJ_IDX);
		byte[] predarr = ByteArrUtil.getArray(nextValue,EncodeTripleStore.PRED_IDX);
		byte[] objarr = ByteArrUtil.getArray(nextValue,EncodeTripleStore.OBJ_IDX);
		
//		if(!(mappingStore.getValue(subjarr).toString().startsWith("http"))){
//			System.out.println(mappingStore.getValue(subjarr)+" "+mappingStore.getValue(predarr)+" "+mappingStore.getValue(objarr));
//		}
//		System.out.println(mappingStore.getValue(subjarr)+" "+mappingStore.getValue(predarr)+" "+mappingStore.getValue(objarr));
//		KVStoreTest.printValue(subjarr);
//		Resource subj;
//		try{
//			subj=(Resource) mappingStore.getValue(subjarr);
//		}catch(Exception e){
//			subj=new KVURI("http://"+mappingStore.getValue(subjarr).toString());
//		}
		
		Resource subj=(Resource) mappingStore.getValue(subjarr);
//		KVStoreTest.printValue(predarr);
		URI pred=(URI) mappingStore.getValue(predarr);
		
		
//		KVStoreTest.printValue(objarr);
		Value obj=mappingStore.getValue(objarr);
		
		return mappingStore.createStatement(subj, pred, obj);
			
		
	}
	
	@Override
	protected void handleClose() throws IOException{
		super.handleClose();
		Iter.close();
	}

}
