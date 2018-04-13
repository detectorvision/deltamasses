package com.detectorvision.utility;

import java.util.BitSet;
import java.util.Random;

public class BitSpec {
	public BitSet bits = new BitSet();
//	public static BitSet tmpBitSet = new BitSet();
	public void setRandom(){ 
		bits.clear();
		Random randomGenerator = new Random();
		int bitLength = 1+randomGenerator.nextInt(3000);
		bits.set(bitLength-1);
		for(int i=0;i<75;i++){
			int pos = randomGenerator.nextInt(bitLength);
			bits.set(pos);
		}	
	} 
 
	public static boolean compare(BitSpec s1,BitSpec s2, int lowestCardinality){
		BitSet tmpBitSet = new BitSet();
		tmpBitSet.or(s1.bits);       // Logical OR with empty BitSet , hence tmpBitSet is set with s1.bits
	    tmpBitSet.and(s2.bits);      // Logical AND with tempBitSet (i.e s1 and s2.bits)    
		if(tmpBitSet.cardinality()>= lowestCardinality){
			return true;
		}
		return false;
	}
	
	public static int getEqualOverlap(BitSpec s1,BitSpec s2){
		BitSet tmpBitSet = new BitSet();
		tmpBitSet.or(s1.bits);       // Logical OR with empty BitSet , hence tmpBitSet is set with s1.bits
	    tmpBitSet.and(s2.bits);      // Logical AND with tempBitSet (i.e s1 and s2.bits)    
		return tmpBitSet.cardinality();
	}
	
	public static void printBitSet(BitSet s){
		for(int i=0;i<s.size();i++){
			if(s.get(i)){System.out.print("1");}
			else{System.out.print("0");}
		}
		System.out.println();
	}
};




