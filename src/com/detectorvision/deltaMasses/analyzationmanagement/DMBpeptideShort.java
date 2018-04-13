/* $Id: DMBpeptideShort.java 231 2008-12-02 21:57:36Z frank $ */

package com.detectorvision.deltaMasses.analyzationmanagement;

public class DMBpeptideShort {
public int peptide_id;
public String proteinasc;
public String pepsequence;
public int pepstart;
public int pepend;
public double pepmass;


public static boolean pepsAreEqual(DMBpeptideShort in1, DMBpeptideShort in2){
	boolean RC=true;
	if(! (in1.pepstart==in2.pepstart)){RC=false;}
	if(! (in1.pepend==in2.pepend)){RC=false;}
	if(! in1.proteinasc.equals(in2.proteinasc)){RC=false;}
	if(! in1.pepsequence.equals(in2.pepsequence)){RC=false;}
	if(! (Math.abs(in1.pepmass-in2.pepmass)<0.4)){RC=false;}
	return RC;
}
}
