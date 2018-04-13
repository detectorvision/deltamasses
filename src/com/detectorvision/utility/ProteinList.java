/* $Id: PeptideID.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.utility;

import java.util.ArrayList;

import com.detectorvision.utility.DeltaMassBase;

/**
 * Protein Information
 * 
 * @author frankp
 * v0.1 20081130 frankp  added attributes
 */
public class ProteinList {
	public ArrayList<Protein> list = new ArrayList<Protein>();

	public boolean setProteinList(){
		boolean returncode=false;
		this.list=DeltaMassBase.getProteinList();
		return returncode;
	}

	
	public void addProtein(Protein pro){
		//you can add only if the acc is not known yet.
		boolean isinlist=false;
		for(int i=0;i<this.list.size();i++){
			if(this.list.get(i).acc.equals(pro.acc)){
				isinlist=true;
			}
		}
		if(!isinlist){this.list.add(pro);}
		return;
	}

	
	public boolean containsAcc(String acc){
		for(int i=0;i<this.list.size();i++){
			if(list.get(i).acc.equals(acc)){
				return true;
			}
		}
		return false;
	}

	public boolean storeProteinList2DB(){
		boolean returncode=false;
		returncode=DeltaMassBase.storeProteinList(this.list);
		return returncode;
	}
	
}
