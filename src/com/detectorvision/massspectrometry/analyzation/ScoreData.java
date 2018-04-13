/* $Id: ScoreData.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.analyzation;

import com.detectorvision.massspectrometry.biodata.SpectraPair;

import java.util.ArrayList;

/**
 * Score data structure.
 * 
 * @author lehmamic
 */
public class ScoreData {
	public ArrayList<SpectraPair> 		spectraPairList;
	public int[]						histogram;
}
