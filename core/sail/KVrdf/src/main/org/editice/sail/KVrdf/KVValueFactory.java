package org.editice.sail.KVrdf;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryBase;

public class KVValueFactory extends ValueFactoryBase{

	public KVValueFactory(String hashTable) {
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public BNode createBNode(String nodeID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Literal createLiteral(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Literal createLiteral(String label, String language) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Literal createLiteral(String label, URI datatype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate,
			Value object) {
		
		return new StatementImpl(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate,
			Value object, Resource context) {
		return new StatementImpl(subject, predicate, object);
	}



	@Override
	public URI createURI(String uri) {
		// TODO Auto-generated method stub
		System.err.println("createURI");
		return null;
	}

	@Override
	public URI createURI(String namespace, String localName) {
		// TODO Auto-generated method stub
		System.err.println("createURI with name");
		return null;
	}
	


	public void close() {
		// TODO Auto-generated method stub
		
	}


	
	public String getByteArr(Value subj) {
		// TODO Auto-generated method stub
		return null;
	}

}
