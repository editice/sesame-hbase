package org.editice.sail.KVrdf;

import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;

public class KVStoreFactory implements SailFactory{
	
	public final static String SAIL_TYPE="editice:KVStore";

	@Override
	public SailImplConfig getConfig() {
		return new KVStoreConfig();
	}

	@Override
	public Sail getSail(SailImplConfig config1) throws SailConfigException {
		if(!SAIL_TYPE.equals(config1.getType())){
			throw new SailConfigException("Invalid Sail Type: "+config1.getType());
		}
		KVStoreConfig config=(KVStoreConfig)config1;
		String realValueTableName=config.getRealValueTableName();
		String hashTableName=config.getHashTableName();
		
		return new KVStore(realValueTableName,hashTableName);
	}

	@Override
	public String getSailType() {
		// TODO Auto-generated method stub
		return SAIL_TYPE;
	}

}
