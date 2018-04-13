/* $Id: testEncodeDecode.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.biodata;

import java.util.ArrayList;

public class testEncodeDecode {
	public static void main(String[] args) {	
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i=0;i<10;i++){//produce some nonsense data
			list.add(((double)i) * 1.0/3.0);
		}
		//serialize Arraylist<Double >list to String encodedString
		String encodedString   = Base64.encodeObject(list,Base64.DONT_BREAK_LINES);
		System.out.println("encodedString:"+encodedString.length()+" "+encodedString);
		
		ArrayList<Double> decodedList = new ArrayList<Double>();
		//decode encodedString to object decodedList
		decodedList = (ArrayList<Double>)Base64.decodeToObject(encodedString);
		for(int i=0;i<decodedList.size();i++){
			System.out.println("i:"+i+"  value:"+decodedList.get(i));
		}
	}
}
