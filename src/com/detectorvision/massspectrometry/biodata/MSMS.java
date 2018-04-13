/* $Id: MSMS.java 104 2008-02-24 20:04:38Z jari $ */

package com.detectorvision.massspectrometry.biodata;

/**
 * Datastructur for the intensity-mass data pairs of a spectrum, containing two
 * double values the data.
 * 
 * @author lehmamic
 */
public class MSMS implements Comparable<MSMS> {
	
	// Attributes
	public double intensity, massToCharge;

	public int compareTo(MSMS other) {
		if (other.intensity > this.intensity)
			return -1;
		else if (other.intensity < this.intensity)
			return 1;
		return 0;
	}
}
