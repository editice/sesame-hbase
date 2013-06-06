package org.editice.KVmodel;

import org.openrdf.model.impl.URIImpl;

public class KVURI extends URIImpl implements  KVResource {
	
	public KVURI(String uri2) {
		super(uri2);
	}
	

	public KVURI(String namespace, String localName) {
		this(namespace+localName);
	}
	


	@Override
	public boolean equals(Object o){
		if(this==o){
			return true;
		}
		return super.equals(o);
	}
	

}
