package com.detectorvision.utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class testBitComparator {

	public static void main(String[] args) {
		int NUMBER_OF_SPECTRA=3799;
		//int NUMBER_OF_SPECTRA=10000;
		
		
		ArrayList<BitSpec> BSL = new ArrayList<BitSpec>();

		//create random bitSpectra
		for(int i=0;i<NUMBER_OF_SPECTRA;i++){ 
			BitSpec tmp = new BitSpec();
			tmp.setRandom();	
			BSL.add(tmp);
		}
		
		Date startDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
		String formattedDate = formatter.format(startDate);
		System.out.println("compare_start_at:"+formattedDate);
		
		//compare them
		int similar=0;
		for(int i=0;i<NUMBER_OF_SPECTRA;i++){
			for(int j=i+1;j<NUMBER_OF_SPECTRA;j++){
				if(BitSpec.compare(BSL.get(i), BSL.get(j),10)){similar++;}
			}	
		}
		
		Date endDate = new java.util.Date();
	    formattedDate = formatter.format(endDate);
		System.out.println("compare_end_at:"+formattedDate);
		System.out.println("Equals:"+similar);
	}
}
