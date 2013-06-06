/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import info.aduna.io.ByteArrayUtil;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * A statement iterator that wraps a RecordIterator containing statement records
 * and translates these records to {@link Statement} objects.
 */
class NativeStatementIterator extends
		LookAheadIteration<Statement, IOException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final RecordIterator btreeIter;

	private final ValueStore valueStore;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStatementIterator.
	 */
	public NativeStatementIterator(RecordIterator btreeIter,
			ValueStore valueStore) throws IOException {
		this.btreeIter = btreeIter;
		this.valueStore = valueStore;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Statement getNextElement() throws IOException {
//		System.out.println("is null?"+btreeIter==null);
//		System.out.println("iter class :"+btreeIter.getClass().getName());
		byte[] nextValue = btreeIter.next();

		if (nextValue == null) {
			return null;
		}

		int subjID = ByteArrayUtil.getInt(nextValue, TripleStore.SUBJ_IDX);
		int predID = ByteArrayUtil.getInt(nextValue, TripleStore.PRED_IDX);
		int objID = ByteArrayUtil.getInt(nextValue, TripleStore.OBJ_IDX);
		
		if((valueStore.getValue(predID).toString()).equals("http://www.nju.edu.cn/#headOf")){
			System.out.println(valueStore.getValue(subjID)+ " "+ valueStore.getValue(predID)+" "+valueStore.getValue(objID));
		}
		
		Resource subj = (Resource) valueStore.getValue(subjID);
//		System.out.println(subj.getClass().getName());

		
		URI pred = (URI) valueStore.getValue(predID);
//		System.out.println(pred.getClass().getName());

		
		Value obj = valueStore.getValue(objID);
//		System.out.println(obj.getClass().getName());

		Resource context = null;
		int contextID = ByteArrayUtil
				.getInt(nextValue, TripleStore.CONTEXT_IDX);
		if (contextID != 0) {
			context = (Resource) valueStore.getValue(contextID);
		}
		// System.out.println(subj+"\t"+pred+"\t"+"\t"+obj);
		return valueStore.createStatement(subj, pred, obj, context);
	}

	@Override
	protected void handleClose() throws IOException {
		super.handleClose();
		btreeIter.close();
	}
}
