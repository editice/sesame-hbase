package org.editice.KVmodel;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

public class KVLiteral extends LiteralImpl implements KVvalue {
	
	
	public KVLiteral(String lit){
		super(lit);
	}
	
	
	public KVLiteral(String label, String language) {
		super(label,language);
	}
	


	public KVLiteral(String label, URI datatype) {
		super(label,datatype);
	}
	


	@Override
	public boolean equals(Object o) {
		if(this==o){
			return true;
		}
		return super.equals(o);
	}

}
