package org.editice.KVmodel;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;

public class KVStatement extends ContextStatementImpl{
	
	public KVStatement(KVResource subject,KVURI predicate, KVvalue object){
		this(subject,predicate,object,null);
	}

	public KVStatement(KVResource subject, KVURI predicate, KVvalue object,
			Resource context) {
		super(subject, predicate, object,null);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public KVResource getSubject(){
		return (KVResource)super.getSubject();
	}
	
	@Override
	public KVURI getPredicate(){
		return (KVURI)super.getPredicate();
	}
	
	@Override
	public KVvalue getObject(){
		return (KVvalue)super.getObject();
	}
	
	@Override
	public KVResource getContext(){
		//now we set it to null
		return null;
	}

}
