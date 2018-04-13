/* $Id: Modification.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.unimod;

/**
 * Data structure for a modification of the unimod database. Contains all known information
 * about this specific modification.
 * 
 * @author lehmamic
 */
public class Modification {
	
	// Attributs
	public String shortName;
	public String fullName;
	public String composition;
	public double monoisotopic;
	public double average;
	public int unimodID;
	
	// Datainformation
	public String modifiedDate;
	public String postedDate;
}
