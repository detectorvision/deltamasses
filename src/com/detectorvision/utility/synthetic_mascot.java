package com.detectorvision.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import com.detectorvision.massspectrometry.biodata.SpectraPair;

public class synthetic_mascot {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numspec=200000;
	    long numPairs=(long)numspec*((long)numspec-1)/2;
		
		String tmpString = String.format("%,d",numPairs);		
		System.out.println("numpairs:" + tmpString+ " max:"+Integer.MAX_VALUE/1000000);
		String outFile = "ttttt.mgf";
		Random rand = new Random();	
		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter(outFile));
			System.out.println("location:"+out.toString());
			for(int i=0;i<numspec;i++){
				if(i%1000==0){System.out.println("spectrum:"+i);}
				String line;
				line="BEGIN IONS\n";
				out.write(line);
				double pepmass=400+10*rand.nextDouble();
				line="PEPMASS="+pepmass+"\n";
				out.write(line);
				line="CHARGE=2+\n";
				out.write(line);
				line="TITLE=testSpec"+i+"\n";
				out.write(line);
				
				for(int j=0;j<75;j++){
					double ran_x=1200+100*rand.nextDouble()+rand.nextDouble()*1000;
					double ran_i=rand.nextDouble()*1000;
					line=ran_x+" "+ran_i+"\n";
					out.write(line);
				}
				
				line="END IONS\n";
				out.write(line);
			}
			out.close();
		} catch (IOException e) {
			System.out.println("Record:tsvExport:IOException: " + e.getLocalizedMessage());
		}
	}

}
