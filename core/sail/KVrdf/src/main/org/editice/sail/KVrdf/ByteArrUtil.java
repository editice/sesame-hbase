package org.editice.sail.KVrdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * util class for MD5 code, decode; handling byte arrays
 * 
 * @author zj
 * 
 */
public class ByteArrUtil {
	
	private static final int MD5Len=KVStoreConfig.hashLen;;
	
	public static byte[] encodeString(String in){
		return getHashValue(in);
	}
	
	//the interface version: GuRong 
	public static byte[] getHashValue_InterfaceStandard(String key) throws NoSuchAlgorithmException
	{
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.reset();
		messageDigest.update(key.getBytes());
		byte[] resultBytes = messageDigest.digest();
		
		byte[] selectResultBytes =new byte[MD5Len];
		System.arraycopy(resultBytes,resultBytes.length-selectResultBytes.length, selectResultBytes,0,selectResultBytes.length);
		
		return selectResultBytes;
	}
	
	public static  byte[] getHashValue(String key) 
	{
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		messageDigest.reset();
		messageDigest.update(key.getBytes());
		byte[] resultBytes = messageDigest.digest();
		
		byte[] selectResultBytes =new byte[MD5Len];
		System.arraycopy(resultBytes,resultBytes.length-selectResultBytes.length, selectResultBytes,0,selectResultBytes.length);
		return selectResultBytes;
	}
	
	public static byte[] mergeByteArray(byte[] subjarr, byte[] predarr,
			byte[] objarr) {
		byte[] mergeArr=new byte[subjarr.length+predarr.length+objarr.length];
		System.arraycopy(subjarr, 0, mergeArr, 0, subjarr.length);
		System.arraycopy(predarr, 0, mergeArr, subjarr.length, predarr.length);
		System.arraycopy(objarr, 0, mergeArr, subjarr.length+predarr.length, objarr.length);
		return mergeArr;
	}

	public static byte[] POSArrToSPOArr(byte[] arr){
		if(arr.length!=3*MD5Len){
			System.err.println("error format in hbase store");
		}
		byte[] now=new byte[3*MD5Len];
		System.arraycopy(arr, 2*MD5Len, now, 0, MD5Len);
		System.arraycopy(arr, 0, now, MD5Len, 2*MD5Len);
		return now;
		
	}
	
	public static byte[] OSPArrToSPOArr(byte[] arr){
		if(arr.length!=3*MD5Len){
			System.err.println("error format in hbase store");
		}
		byte[] now=new byte[3*MD5Len];
		System.arraycopy(arr, MD5Len, now, 0, 2*MD5Len);
		System.arraycopy(arr, 0, now, 2*MD5Len, MD5Len);
		return now;
	}

	public static byte[] getArray(byte[] nextValue, int offset) {
		byte[] now=new byte[MD5Len];
		System.arraycopy(nextValue, offset, now, 0, MD5Len);
		return now;
	}
	
	public static String getBase64Code(byte[] bytes)
	{
		BASE64Encoder encode =new BASE64Encoder();
		String result = encode.encode(bytes);
		return result;
	}
}
