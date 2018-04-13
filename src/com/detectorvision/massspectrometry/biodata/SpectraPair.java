/* $Id: SpectraPair.java 202 2008-07-29 17:59:29Z frank $ */

package com.detectorvision.massspectrometry.biodata;

import com.detectorvision.massspectrometry.unimod.Modification;

/**
 * Datastructure for a spectra pair. It contains two pointers to different spectras, which
 * where scored from the analization algorithm.
 * 
 * @author lehmamic
 */
public class SpectraPair {
	
	// Attributs
	public Spectrum spectrumA;
	public Spectrum spectrumB;
	public double score;
	public double totalOverlap;
	public double equalOverlap;
	public double shiftOverlap;
	public double p;
	public Modification knownModification;
	public boolean hasWeakDeltaSignal; //20070317
	public double unsuspectedDelta;    //20070317 if weakDeltasignal is on: strongest deltaMass at least 2 Dalton below deltaMass.
	public int unsupsectedDeltaCount;  //20070317 counts on above channel
	public double deltaMass;//20070321 SpectrumA-Spectrum B. Needed if deltaMass calculated from MSMS
	public boolean marked;//20080729 indicates if user has marked this. false by default.
	public String comment;//max length 100 characters
	public int pair_id;
	public int specnet_id;
}
