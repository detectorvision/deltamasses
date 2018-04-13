package com.detectorvision.massspectrometry.biodata;

public class MoleculeLink {
	public int fk_start;
	public double m_start;

	public int fk_end;
	public double m_end;
	
	public int fk_modification; //references unimodID
	public double dm; //experimental deltaMass
	
	public int moleculeNet = 0;//bigger than 0 if linked to a net.
	
	
	public void print(){
		System.out.println(""+fk_start+"\t"+fk_end+"\t"+String.format("%.2f",dm)+"\t"+String.format("%.2f",m_start)+"\t"+String.format("%.2f",m_end)+"\t"+moleculeNet);
	}
	
}
