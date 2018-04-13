/* $Id: Spectrum.java 405 2010-11-06 15:58:57Z frank $ */

package com.detectorvision.massspectrometry.biodata;
 
import java.util.ArrayList;

import com.detectorvision.graphTheory.AbstractLink;
import com.detectorvision.graphTheory.AbstractNets;
import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.utility.BitSpec;

/**
 * Datastructure for peptidespectras of a mass spectrometry measurement. A spectrum contains
 * the charge, title and the msms pairs.
 * 
 * @author lehmamic
 * 
 */
public class Spectrum implements Comparable<Spectrum>{
	// Spectrum information
	public String 		title;
	public int 			charge;
	public float 		retention;
	public double 		precursorMass;
	public PeptideID	peptideID;
	public double		minMZ;
	public double		maxMZ;
	public double		rangeMZ;
	//public int			queryNumber;//new 20061111 mascot integration null if unknown
	public String	    queryID;//new 20070323 to be able to contain x!tandem integration as well
	public boolean      hasPair;//new 20070130 for SQL statements
	public int		    DMBid;//new 20070130 SQL primary key goes here
	public double       tic;//new 20070202 total ion current - needed for .mgf export

	//frankp 20061019
	public String proteinAsc;
	public String pepSequence;

	//frankp 20061217
	public double pepMass;
	public double pepMz;
	public double pepError;//TODO not really clear which unit this is

	//frankp 20070116
	public int pepStart;
	public int pepEnd;

	//frankp 20070321
	public String proteinDesc; 
	public double proteinMass;
	public double proteinScore;
	public int proteinMatches;
	public String pepMod;//String describing the modifications on this one
	public double pepScore;

	//frankp 20080202
	public int   spectrum_id;//spectrum id used  in deltaMassBase

	// MSMS-Value List
	public ArrayList<MSMS> 	valueList;

	//20090607 In cooperation with MPI Magdeburg
	public ArrayList<MoleculeSeries> moleculeSeries= new ArrayList<MoleculeSeries>();
	public ArrayList<MoleculeLink>   moleculeLinks = new ArrayList<MoleculeLink>();
	public ArrayList<Modification>   moleculeSearch = new ArrayList<Modification>();
	public AbstractNets abstractnets;
	
	//20100606\
	public BitSpec bit1over5 = new BitSpec();//1 Bit approx 0.2 Dalton
	public BitSpec bit = new BitSpec();//1Bit approx  1 Dalton
	public BitSpec bit4 = new BitSpec();//1Bit approx 4 Daltons
	public BitSpec bit10 = new BitSpec();//1Bit approx 10 Dalton
	
	//20100622
	public int fk_experiment_id; //references the experiment ID in the experiment table.

	public boolean calculateMoleculeLinks(double accuracy){//In cooperation with MPI Magdeburg
		moleculeLinks.clear();
		moleculeSearch.clear();

		//add HEX and NAC
		Modification mod1 = new Modification();
		mod1.monoisotopic=162.052824;
		mod1.fullName="Hexose";
		mod1.shortName="Hex";
		mod1.unimodID=41;
		moleculeSearch.add(mod1);

		Modification mod2 = new Modification();
		mod2.monoisotopic=146.057909;
		mod2.fullName="Fucose";
		mod2.shortName="Fuc";
		mod2.unimodID=295;
		moleculeSearch.add(mod2);

		//loop through all pairs and store all which match a mod in mods.
		for(int i=0;i<valueList.size();i++){
			for(int j=i+1;j<valueList.size();j++){
				double dm=valueList.get(i).massToCharge-valueList.get(j).massToCharge;
				double dmAbs=Math.abs(dm);
				//check if this dm matches a mod
				for(Modification thisMod : moleculeSearch){
					if(Math.abs(dmAbs-thisMod.monoisotopic)<accuracy){//matches, store it.
						MoleculeLink m = new MoleculeLink();
						m.dm=dmAbs;
						if(valueList.get(i).massToCharge< valueList.get(j).massToCharge){
							m.fk_end=j;
							m.fk_start=i;
							m.m_end=valueList.get(j).massToCharge;
							m.m_start=valueList.get(i).massToCharge;
						}
						else{
							m.fk_end=i;
							m.fk_start=j;
							m.m_end=valueList.get(i).massToCharge;
							m.m_start=valueList.get(j).massToCharge;
						}
						m.fk_modification=thisMod.unimodID;
						moleculeLinks.add(m);
						m.print();
					}
				}
			}
		}
		
		ArrayList<AbstractLink> alList = new ArrayList<AbstractLink>();
		for(int i=0;i<this.moleculeLinks.size();i++){
			AbstractLink tmpLink = new AbstractLink();
			tmpLink.start=this.moleculeLinks.get(i).fk_start;
			tmpLink.end=this.moleculeLinks.get(i).fk_end;
			tmpLink.id=i;
			alList.add(tmpLink);
		}
		abstractnets = new AbstractNets(alList);
		abstractnets.printNets();
		
		
		
		
		return true;
	}

	public boolean setMoleculeSearch(ArrayList<Modification> inList){
		this.moleculeSearch.clear();
		for(int i=0;i<inList.size();i++){
			this.moleculeSearch.add(inList.get(i));
		}
		return true;
	}
	
	public void setBit(){
		bit1over5.bits.clear();
		bit1over5.bits.set(0,false);
		for(int i=0;i<valueList.size();i++){
			int pos=Math.round((float)(5 * 1.000458*valueList.get(i).massToCharge));
			//1.000458 Dalton is distance of peptide mass distribution, see 
			//[25] M. Mann, Proceedings of the 43rd ASMS Conference on Mass Spectrom-
			//etry and Allied Topics, Atlanta, GA, 1995, p. 693.
			//[26] M. Wehofsky, R. Hoffmann, M. Hubert, B. Spengler, Eur. J. Mass Spectrom.
			//7 (2001) 39.
			bit1over5.bits.set(pos);
		}
				
		bit.bits.clear();
		bit.bits.set(0,false);
		for(int i=0;i<valueList.size();i++){
			int pos=Math.round((float)(1.000458*valueList.get(i).massToCharge));
			//1.000458 Dalton is distance of peptide mass distribution, see 
			//[25] M. Mann, Proceedings of the 43rd ASMS Conference on Mass Spectrom-
			//etry and Allied Topics, Atlanta, GA, 1995, p. 693.
			//[26] M. Wehofsky, R. Hoffmann, M. Hubert, B. Spengler, Eur. J. Mass Spectrom.
			//7 (2001) 39.
			bit.bits.set(pos);
		}
		
		bit4.bits.clear();
		bit4.bits.set(0,false);
		for(int i=0;i<valueList.size();i++){
			int pos=Math.round((float)(0.25 * 1.000458*valueList.get(i).massToCharge));
			//1.000458 Dalton is distance of peptide mass distribution, see 
			//[25] M. Mann, Proceedings of the 43rd ASMS Conference on Mass Spectrom-
			//etry and Allied Topics, Atlanta, GA, 1995, p. 693.
			//[26] M. Wehofsky, R. Hoffmann, M. Hubert, B. Spengler, Eur. J. Mass Spectrom.
			//7 (2001) 39.
			bit4.bits.set(pos);
		}
			
		bit10.bits.clear();
		bit10.bits.set(0,false);
		for(int i=0;i<valueList.size();i++){
			int pos=Math.round((float)(0.05 * 1.000458*valueList.get(i).massToCharge));
			//1.000458 Dalton is distance of peptide mass distribution, see 
			//[25] M. Mann, Proceedings of the 43rd ASMS Conference on Mass Spectrom-
			//etry and Allied Topics, Atlanta, GA, 1995, p. 693.
			//[26] M. Wehofsky, R. Hoffmann, M. Hubert, B. Spengler, Eur. J. Mass Spectrom.
			//7 (2001) 39.
			bit10.bits.set(pos);
		}
		
		
	}
	
	public void print(){
		for(int i=0;i<valueList.size();i++){System.out.println(""+valueList.get(i).massToCharge);}
		for(int i=0;i<bit.bits.size();i++){
			if(bit.bits.get(i)){System.out.print("1");}
			else{System.out.print("0");}
		}
		System.out.println("\n---------------------------------------------------");
	}
	
	
	public int compareTo(Spectrum other) {
		if (other.charge > this.charge)
			return -1;
		else if (other.charge < this.charge)
			return 1;
		return 0;
	}
}
