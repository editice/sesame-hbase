package org.editice.sail.KVrdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.hbase.util.Bytes;
import org.editice.KVmodel.KVBNode;
import org.editice.KVmodel.KVLiteral;
import org.editice.KVmodel.KVResource;
import org.editice.KVmodel.KVURI;
import org.editice.KVmodel.KVvalue;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryBase;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;


public class RedisStore extends ValueFactoryBase implements
		RealValueStoreFactory {

	public final int hashLen=4;
	public final String prefixSP_O="0";
	public final String prefixPO_S="1";
	public final String prefixHash="2";
	
	public ShardedJedis shardJedis;
	public ShardedJedisPipeline shardPipeline;
	
	public RedisStore() {
		super();
		
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		List<String> redisServers=getRedisServer();
		
		Iterator<String> iter=redisServers.iterator();
		while(iter.hasNext())
		{
			String[] hostInfo=iter.next().split(":");
			System.out.print(hostInfo[0]+" ");
			JedisShardInfo si;
			if(hostInfo.length==1)
				si = new JedisShardInfo(hostInfo[0],6380);
			else
				si = new JedisShardInfo(hostInfo[0],6380,2000,Integer.valueOf(hostInfo[1]));
			shards.add(si);
		}
		this.shardJedis = new ShardedJedis(shards);
		this.shardPipeline = shardJedis.pipelined();
		System.out.println("Redis Cluster Successfully connected");
	}
	
	public List<String> getRedisServer()
	{
		List<String> redisServers=new ArrayList<String>();
		redisServers.add("slave002");
		redisServers.add("slave003");
		redisServers.add("slave004");
		redisServers.add("slave005");
		redisServers.add("slave006");
		redisServers.add("slave007");
		redisServers.add("slave008");
		redisServers.add("slave009");
		redisServers.add("slave010");
		redisServers.add("slave011");
		redisServers.add("slave012");
		redisServers.add("slave013");
		redisServers.add("slave014");
		redisServers.add("slave015");
		redisServers.add("slave016");
		redisServers.add("slave017");
		redisServers.add("slave018");
		return redisServers;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean delAll() {
		return false;
	}

	@Override
	public ResultIterator getTriples(byte[] subj, byte[] pred, byte[] obj) {
		// TODO Auto-generated method stub
		int index=0;
		try {
			index = getBestIndex(subj,pred,obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean doRangeSearch=false;
		return getTripleUsingIndex(subj, pred, obj, index,  doRangeSearch);
	}

	private ResultIterator getTripleUsingIndex(byte[] subj, byte[] pred,
			byte[] obj, int index, boolean doRangeSearch) {
		// TODO Auto-generated method stub
		// "SPO" 0;
		// "PO" 1;
		// "SP" 2;
		String key="";
		HashSet<String> set=null;
		RedisRangeIterator resultIter=null;;
		switch(index)
		{
		case 0:
			key=prefixPO_S+String.valueOf(Bytes.toLong(connectBytesArray(pred,obj)));
			String val=String.valueOf(Bytes.toInt(subj));
			boolean isContains=shardJedis.sismember(key,val);
			set= new HashSet<String>();
			if(isContains==true)
				set.add(val);
			resultIter = new RedisRangeIterator(set,index,subj,pred,obj);
			break;
			
		case 1:
			key=prefixPO_S+String.valueOf(Bytes.toLong(connectBytesArray(pred,obj)));
			set=(HashSet<String>) shardJedis.smembers(key);
			System.out.println("emlmennextValuet size : " +set.size());
			resultIter = new RedisRangeIterator(set,index,subj,pred,obj);
			break;
			
		case 2:
			key=prefixSP_O+String.valueOf(Bytes.toLong(connectBytesArray(subj,pred)));
			set=(HashSet<String>) shardJedis.smembers(key);
			resultIter = new RedisRangeIterator(set,index,subj,pred,obj);
			break;
			
		default:
			break;
		}
		
//		String val=String.valueOf(Bytes.toInt(objectHash));
		return resultIter;
	}

	private int getBestIndex(byte[] subj, byte[] pred, byte[] obj)
			throws Exception {
		// TODO Auto-generated method stub
		String type = "";
		int len = KVvalue.UNKNOWN_LEN;
		if (subj.length != len) {
			type += "S";
		}
		if (pred.length != len) {
			type += "P";
		}
		if (obj.length != len) {
			type += "O";
		}
		if (type.equals("SPO")) {
			return 0;
		} else if (type.equals("PO")) {
			return 1;
		} else if (type.equals("SP")) {
			return 2;
		} else {
			throw new Exception("error type:"+type+" this type does not supported in redis index yet");
		}

	}

	@Override
	public boolean removeTriple(String subj, String pred, String obj)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int removeTriples(ResultIterator iter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean storeTriple(byte[] subjarr, byte[] predarr, byte[] objarr)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
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
		return null;
	}

	@Override
	public URI createURI(String namespace, String localName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public byte[] connectBytesArray(byte[] arr1,byte arr2[])
	{
		byte[] result=new byte[arr1.length+arr2.length];
		
		System.arraycopy(arr1, 0, result, 0, arr1.length);
		System.arraycopy(arr2, 0, result,arr1.length,arr2.length);
		return result;
	}
	
	private class RedisRangeIterator implements ResultIterator {
		public HashSet<String> set;
		public Iterator<String> iter;
		public int c=0;
		public int index;
		public byte[] subj;
		public byte[]  pred;
		public byte[] obj;
		public RedisRangeIterator (HashSet<String> set,int index,byte[] subj,byte[]  pred,byte[] obj) {
			this.set=set;
			this.iter=set.iterator();
			this.index=index;
			this.subj=subj;
			this.pred=pred;
			this.obj=obj;
		}

		@Override
		public void close() throws IOException 
		{
//			this.close();
		}

		@Override
		public byte[] next() throws IOException 
		{
//			System.out.println(c++);
//			System.out.println(iter.next());
			if(!iter.hasNext())
				return null;
			
			byte[] bs=Bytes.toBytes(Integer.valueOf(iter.next()));
//			System.out.println(" hello");
//			System.out.println(bs.length);
			byte [] result=null;
			switch(index)
			{
			case 0:
				result=ByteArrUtil.mergeByteArray(subj, pred, obj);
				break;
			case 1:
				result=ByteArrUtil.mergeByteArray(bs, pred, obj);
				break;
			case 2:
				result=ByteArrUtil.mergeByteArray(subj, pred, bs);
				break;
			}
			return result;
		}

		@Override
		public void set(byte[] result) throws IOException {
			// TODO Auto-generated method stub
		}

	}

	public KVvalue getValue(byte[] arr) {
		if(arr!=null){
			KVvalue value=data2Value(arr);
			return value;
		}else{
			System.err.println("error! the arr is null");
			return null;
		}
		
	}

	/**
	 * now should be attention: here we just change everything to URI
	 * @param arr
	 * @return
	 */
	private KVvalue data2Value(byte[] arr) {
		// TODO Auto-generated method stub
//		switch ((data[0] & VALUE_TYPE_MASK)) {
//		case URI_VALUE:
//			return data2uri(id, data);
//		case BNODE_VALUE:
//			return data2bnode(id, data);
//		case LITERAL_VALUE:
//			return data2literal(id, data);
//		default:
//			throw new IllegalArgumentException(
//					"data does not specify a known value type");
//		}
		
		//now should be attention: here we just change everything to URI
		try {
//			return data2URI(arr);
			String value=getMappingString(arr);
			if(value==null){
				return new KVURI("http://fuck");
			}
//			System.out.println(value);
			if (value.indexOf(':') < 0){
				//use literal
				return new KVLiteral(value);
			}else{
				return new KVURI(value);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("data2Value error");
		return null;
	}

	private KVURI data2URI(byte[] data) throws IOException {
		return new KVURI(getMappingString(data));
	}
	
	private String getMappingString(byte[] data) throws IOException{
		
		String key=prefixHash+String.valueOf(Bytes.toInt(data));
		String val=shardJedis.get(key);
		return val;
		
	}

	public byte[] getByteArr(Value var) {
		//TODO need do something so that it won't throw Exception when no result
		return value2Data(var, true);
	}


	private byte[] value2Data(Value value, boolean create) {
		if (value instanceof URI) {
			return uri2data((URI) value, create);
		} else if (value instanceof BNode) {
			return bnode2data((BNode) value, create);
		} else if (value instanceof Literal) {
			return literal2data((Literal) value, create);
		} else {
			throw new IllegalArgumentException(
					"value parameter should be a URI, BNode or Literal");
		}
	}
	private byte[] literal2data(Literal value, boolean create) {
		// TODO Auto-generated method stub
		return ByteArrUtil.encodeString(value.getLabel());
	}

	private byte[] bnode2data(BNode value, boolean create) {
		// TODO Auto-generated method stub
		System.err.println("MappingStore.bnode2data() hasn't realize");
		return null;
	}

	private byte[] uri2data(URI value, boolean create) {
		// TODO Auto-generated method stub
		return ByteArrUtil.encodeString(value.toString());
	}

	public KVvalue getNativeValue(Value value) {
		if (value instanceof Resource) {
			return getKVResource((Resource) value);
		} else if (value instanceof Literal) {
			return getKVLiteral((Literal) value);
		} else {
			throw new IllegalArgumentException("Unknown value type: "
					+ value.getClass());
		}
	}

	private KVResource getKVResource(Resource resource) {
		if (resource instanceof URI) {
			return getKVURI((URI) resource);
		} else if (resource instanceof BNode) {
			return getKVBNode((BNode) resource);
		} else {
			throw new IllegalArgumentException("Unknown resource type: "
					+ resource.getClass());
		}
	}

	private KVBNode getKVBNode(BNode bnode) {
		if(isOwnValue(bnode)){
			return (KVBNode) bnode;
		}
		return new KVBNode(bnode);
	}

	private KVURI getKVURI(URI uri) {
		if(isOwnValue(uri)){
			return (KVURI) uri;
		}
		return new KVURI(uri.toString());
	}

	private KVLiteral getKVLiteral(Literal l) {
		if(isOwnValue(l)){
			return (KVLiteral) l;
		}
		if (l.getLanguage() != null) {
			return new KVLiteral( l.getLabel(), l.getLanguage());
		} else if (l.getDatatype() != null) {
			KVURI datatype = getKVURI(l.getDatatype());
			return new KVLiteral( l.getLabel(), datatype);
		} else {
			return new KVLiteral(l.getLabel());
		}
	}

	private boolean isOwnValue(Value value) {
		//TODO here we need another judge  to determine if the object is created by this mappingStore
		return value instanceof KVvalue;
	}

}
