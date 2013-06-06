package org.editice.sail.KVrdf;

import org.openrdf.sail.config.SailImplConfigBase;

public class KVStoreConfig extends SailImplConfigBase{
	
	private String realValueTableName;
	
	private String hashTableName;
	
	public static final String familyName="f";
	
	private String qualifierName;
	
	public static final int hashLen=4;
	
	public static int RedisPort=6381;
	
	public static final String host="slave035";
	
	public static final String prefixStatistic="9";
	
	public KVStoreConfig(){
		super(KVStoreFactory.SAIL_TYPE);
	}
	
	public String getRealValueTableName(){
		return realValueTableName;
	}
	
	public void setRealValueTableName(String name){
		this.realValueTableName=name;
	}
	
	public String getHashTableName(){
		return hashTableName;
	}
	
	public void setHashTableName(String name){
		this.hashTableName=name;
	}

}
