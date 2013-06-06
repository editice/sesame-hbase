package org.editice.sail.KVrdf;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
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
import org.openrdf.sail.nativerdf.model.NativeLiteral;
import org.openrdf.sail.nativerdf.model.NativeURI;

/**
 * 
 * @author zj
 * 
 */
public class MappingStore extends ValueFactoryBase{

	private static Configuration conf = null;

	static {
		conf = HBaseConfiguration.create();
		conf.addResource("hbase-site.xml");
	}

	private HTable Byte2StringTable;

	private final String tableName;

	private String FamilyName = KVStoreConfig.familyName;

//	private static final String QualifierName = "qualifier";

	public MappingStore(String hashTable) {
		super();
		this.tableName = hashTable;
		// remove the older if exist and create the new table
		try {
			checkTable(hashTable);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkTable(String hashTable) throws IOException {
		// check if exist
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);

			if (!admin.tableExists(hashTable)) {
				HTableDescriptor desc = new HTableDescriptor(Bytes
						.toBytes(hashTable));
				HColumnDescriptor coldef = new HColumnDescriptor(Bytes
						.toBytes(FamilyName));
				desc.addFamily(coldef);
				admin.createTable(desc);
			}
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Byte2StringTable = new HTable(conf, hashTable);
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public byte[] getByteArr(Value var) {
		//TODO need do something so that it won't throw Exception when no result
		return value2Data(var, true);
	}

	public byte[] storeValue(Value value) throws IOException {
		boolean create = true;
		if (value instanceof URI) {
			byte[] out = uri2data((URI) value, create);
			storeData(out, value.toString());
			return out;
		} else if (value instanceof BNode) {
			byte[] out = bnode2data((BNode) value, create);
			storeData(out, value.toString());
			return out;
		} else if (value instanceof Literal) {
			byte[] out = literal2data((Literal) value, create);
			storeData(out, ((Literal) value).getLabel());
			return out;
		} else {
			throw new IllegalArgumentException(
					"value parameter should be a URI, BNode or Literal");
		}
	}

	/**
	 * used to store the value in list way, so that we can use the hbase
	 * table.put(list<put)
	 * 
	 * @param valueList
	 * @return
	 */
	public byte[] storeValueList(List<Value> valueList) {

		return null;

	}

	private void storeData(byte[] valueData, String valueString)
			throws IOException {
		// check if exist
		Get get = new Get(valueData);
		Result res = Byte2StringTable.get(get);
		if (res.isEmpty()) {
			// store file
			Put put = new Put(valueData);
			put.add(Bytes.toBytes(FamilyName), null,
					Bytes.toBytes(valueString));
			Byte2StringTable.put(put);
		} else {
			// do nothing
		}
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

		// now we not consider the data type and the language tag
		// we only consider about the label
		return ByteArrUtil.encodeString(value.getLabel());
	}

	private byte[] bnode2data(BNode value, boolean create) {

		// since our input data owl (xml format) has no bnode,we don't consider
		// about it now
		System.err.println("MappingStore.bnode2data() hasn't realize");
		return null;
	}

	private byte[] uri2data(URI value, boolean create) {
		return ByteArrUtil.encodeString(value.toString());
	}

	public boolean delAll() {
		// TODO Auto-generated method stub
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public BNode createBNode(String nodeID) {
		//TODO constructor of KVBNode
		return new KVBNode(nodeID);
	}
	
	@Override
	public Literal createLiteral(String label) {
		return new KVLiteral(label);
	}

	@Override
	public Literal createLiteral(String label, String language) {
		return new KVLiteral(label,language);
	}
	
	@Override
	public Literal createLiteral(String label, URI datatype) {
		return new KVLiteral(label,datatype);
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
		return new KVURI(uri);
	}

	@Override
	public URI createURI(String namespace, String localName) {
		return new KVURI(namespace,localName);
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

	/**
	 * Checks if the supplied Value object is a NativeValue object that has been
	 * created by this ValueStore.
	 */
	private boolean isOwnValue(Value value) {
		//TODO here we need another judge  to determine if the object is created by this mappingStore
		return value instanceof KVvalue;
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
		Get get = new Get(data);
//		System.out.println("len: "+data.length);
		Result res = Byte2StringTable.get(get);
		if(res.isEmpty()){
			System.err.println("the data is not in mappingStore");
			return null;
		}
		return Bytes.toString(res.raw()[0].getValue());
	}

}
