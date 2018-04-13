/* $Id: PeptideID.java 315 2010-05-17 13:25:18Z frank $ */

package com.detectorvision.massspectrometry.biodata;

/**
 * Peptide information of the . Adds secondary information to the spectras.
 * 
 * @author lehmamic
 * v0.2 20061002 frankp  added attributes
 */
public class PeptideID {
	public String 	title;
	public String 	peptideSequence;
	public int 		peptideCharge;
	public int 		peptideStart, peptideEnd;
	public double 	peptideExpect;
	public double 	mass;
	public double   exp_mz;
	public double 	deltaMass;
	
	//new 20061002 frankp
	public String   protein_acc;  //protein accession number, e.g. SMC4_HUMAN
	public String   protein_desc; //protein description, e.g.  	|Structural maintenance of chromosomes 4-like 1 protein"
	public double y_series_low[];
	public String y_series_amino[];
	public double y_series_end;
	public double b_series_low[];
	public String b_series_amino[];
	public double b_series_end;
	
	public int mod_position[];    //position in this sequence. minimum peptideStart maximum peptideEnd
	public double mod_mass[];
	
	//frankp 20070116 
	public String queryID;//changed from public int queryNumber frankp 20070323

	//frankp 20070321
	public double protein_score;
	public double protein_mass;
	public int protein_matches;
	public String peptide_mod;
	
	
	public void print(){
		System.out.println("---------------------------------");
		System.out.println("title          \t:"+title);
		System.out.println("queryId        \t:"+queryID);
		System.out.println("peptideSequence\t:"+peptideSequence);
		System.out.println("peptideStart   \t:"+peptideStart);
		System.out.println("peptideEnd     \t:"+peptideEnd);
		System.out.println("peptideExpect  \t:"+peptideExpect);
		System.out.println("mass           \t:"+mass);
		System.out.println("exp_mz         \t:"+exp_mz);
		System.out.println("deltaMass      \t:"+deltaMass);
		System.out.println("protein_acc    \t:"+protein_acc);
		System.out.println("protein_desc   \t:"+protein_desc);
		System.out.println("peptide_mod    \t:"+peptide_mod);
		System.out.println("---------------------------------");
	}
}