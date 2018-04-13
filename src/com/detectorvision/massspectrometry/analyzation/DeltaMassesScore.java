/* $Id: DeltaMassesScore.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.analyzation;

import com.detectorvision.massspectrometry.biodata.Record;

import java.util.ArrayList;

/**
 * Interface for peakfile analyzation.
 * @author lehmamic
 */
public interface DeltaMassesScore {
	/**
	 * 
	 * @param record Data from a Peakfile.
	 * @param modList A list of known modification.
	 * @param listener A callback class, to submit the progress.
	 * @param maxPairs Maximum of allowed pairs.
	 * @return Spectrapairs and histogramm.
	 */
	public ScoreData analyze(Record record, ArrayList modList, ProgressListener listener, AlgoParams algoParams) throws ScoreException;
}
