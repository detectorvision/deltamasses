/* $Id: PeptideIDLoader.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.datacontrol;

import com.detectorvision.massspectrometry.biodata.PeptideID;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface for access to the specific data structure loader of tandemfiles.
 * 
 * @author lehmamic
 *
 */
public interface PeptideIDLoader {

	/**
	 * Loads the additional peptide information from a tandemfile into the virtual data structure.
	 * 
	 * @param fileName Filename of the tandemfile.
	 * @return Returns a List of peptide id's.
	 * @throws IOException Throws an exception if the method is unable to load the data.
	 */
	public ArrayList<PeptideID> loadPeptideID(String fileName) throws IOException;
}
