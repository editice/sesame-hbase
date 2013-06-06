package org.editice.KVmodel;

import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeImpl;

public class KVBNode extends BNodeImpl implements KVResource {
	
	private BNode bnode;
	
	public KVBNode(BNode bnode){
		this.bnode=bnode;
	}
	
	public KVBNode(String nodeID) {
		// TODO Auto-generated constructor stub
	}

	public String getID(){
		return bnode.getID();
	}
	
	public String stringValue(){
		return bnode.stringValue();
	}
	
	@Override
	public String toString(){
		return bnode.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if(this==o){
			return true;
		}
		return bnode.equals(o);
	}
	
	@Override
	public int hashCode(){
		return bnode.hashCode();
	}

}
