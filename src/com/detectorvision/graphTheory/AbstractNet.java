package com.detectorvision.graphTheory;

import java.util.ArrayList;

public class AbstractNet {
	public int number;
	public ArrayList<AbstractLink> abstractNet = new ArrayList<AbstractLink>();
	
	
	public void print(){
		System.out.println("\nAbstractNet:------------------\n");
		for(int i=0;i<abstractNet.size();i++){
			System.out.println("Link:\t"+abstractNet.get(i).start+"\t"+abstractNet.get(i).end);
			
		}
		
	}
	
}
