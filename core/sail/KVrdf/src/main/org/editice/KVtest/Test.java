package org.editice.KVtest;

import java.util.ArrayList;
import java.util.List;

import org.editice.KVmodel.KVvalue;



public class Test  {
	
	public static void testClearList(){
		List<String> list=new ArrayList<String>();
		list.add("aa");
		list.add("bb");
		System.out.println(list);
		list.clear();
		System.out.println(list);
	}

	public static void main(String[] args){
		byte[] a=new byte[2];
		System.out.println(a[1]);
		byte[] b={2,3,4};
		a=b;
		System.out.println(a[2]);
		
		for(int i=0;i<100;i++){
			System.err.println("err");
			System.out.println("out");
		}
		
		testClearList();
	}
}
