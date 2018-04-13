/* $Id: RecordLoader.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.datacontrol;

import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.biodata.Record;

import java.io.IOException;

/**
 * Interface for access to the specific data structure loader of peakfiles.
 * 
 * @author lehmamic
 */
public interface RecordLoader {
	
	/**
	 * Loads the mass spectrometry measurement into the virtual data structure.
	 * 
	 * @param fileName Filename of the peakfile.
	 * @return Returns a List of spectras.
	 * @throws IOException Throws an exception if the method is unable to load the data.
	 */
	public Record loadRecord(String fileName, ProgressListener listener) throws IOException;
	
	public void setProgressListener(ProgressListener progress);
	
}
