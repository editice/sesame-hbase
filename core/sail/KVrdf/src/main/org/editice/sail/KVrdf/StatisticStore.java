package org.editice.sail.KVrdf;

import java.util.ArrayList;
import java.util.List;

import org.editice.KVmodel.KVvalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;


public class StatisticStore {
	
	private String prefixStatistic=KVStoreConfig.prefixStatistic;
	
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	
	private ShardedJedis jedis;
	
	public StatisticStore(int port,String... infos){
		List<JedisShardInfo> shards=new ArrayList<JedisShardInfo>();
		for(String info:infos){
			shards.add(new JedisShardInfo(info,port));
		}
		jedis=new ShardedJedis(shards);
		logger.info("RedisStatisStoreInfo:",shards.get(0).getHost());
	}

	public void close(){
		jedis.disconnect();
	}
	
	private void clear(){
		jedis.del("2");
		jedis.del("1");
		System.out.println(jedis.get("1"));
	}
	
	private void test(){
//		jedis.set("1", "aaa");
//		jedis.set("2", "bbb");
		String ss=jedis.get("1");
		System.out.println(ss==null);
	}
	
	public void storeTriple(byte[] subjarr, byte[] predarr, byte[] objarr){
		//need to store count for S, P, O, SP, PO
		String key=prefixStatistic;
		
		String subj=ByteArrUtil.getBase64Code(subjarr);
		String pred=ByteArrUtil.getBase64Code(predarr);
		String obj=ByteArrUtil.getBase64Code(objarr);
		
		String KS=key+subj;
		String KP=key+pred;
		String KO=key+obj;
		String KSP=key+subj+pred;
		String KPO=key+pred+obj;
		
		//s
		String subjCount=jedis.get(KS);
		if(subjCount==null){
			jedis.set(KS, "1");
		}else{
			long subjLong=Long.parseLong(subjCount)+1;
			jedis.set(KS, String.valueOf(subjLong));
		}
		
		//p
		String predCount=jedis.get(KP);
		if(predCount==null){
			jedis.set(KP, "1");
		}else{
			long predLong=Long.parseLong(predCount)+1;
			jedis.set(KP, String.valueOf(predLong));
		}
		
		//o
		String objCount=jedis.get(KO);
		if(objCount==null){
			jedis.set(KO, "1");
		}else{
			long objLong=Long.parseLong(objCount)+1;
			jedis.set(KO, String.valueOf(objLong));
		}
		
		//SP
		String spCount=jedis.get(KSP);
		if(spCount==null){
			jedis.set(KSP, "1");
		}else{
			long spLong=Long.parseLong(spCount)+1;
			jedis.set(KSP, String.valueOf(spLong));
		}
		
		//PO
		String poCount=jedis.get(KPO);
		if(poCount==null){
			jedis.set(KPO, "1");
		}else{
			long poLong=Long.parseLong(poCount)+1;
			jedis.set(KPO, String.valueOf(poLong));
		}
	}
	
	public double cardinality(byte[] subjarr, byte[] predarr, byte[] objarr) {
		
//		System.out.println("do cardinality");
		
		int len=KVvalue.UNKNOWN_LEN;
		
		//avoid to find p according to S,O
		if(predarr.length==len && subjarr.length!=len && objarr.length!=len){
			return 0;
		}
		
		String key=prefixStatistic;
		key+=(subjarr.length==len?"":ByteArrUtil.getBase64Code(subjarr));
		key+=(predarr.length==len?"":ByteArrUtil.getBase64Code(predarr));
		key+=(objarr.length==len?"":ByteArrUtil.getBase64Code(objarr));
		
		//if all data is known
		if(key.equals(prefixStatistic)){
			logger.error("this cardinality shouldn't be excute");
			return 0;
		}else{
			//if all data is don't known,don't consider about it
			return getValueCountEstimate(key);
		}
		
	}

	private double getValueCountEstimate(String key) {
		// TODO Auto-generated method stub
		return Double.parseDouble(jedis.get(key));
	}

}
