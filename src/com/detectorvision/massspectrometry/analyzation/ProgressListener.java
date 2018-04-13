/* $Id: ProgressListener.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.analyzation;

/**
 * CallBack Interface for the analization algorithm.
 * 
 * @author lehmamic
 */
public interface ProgressListener {
	
	/**
	 * Tells the controlclass whitch percent of the analyzation is done.
	 * @param progress Progress in percent.
	 * @param text current action
	 */
	public void updateProgress(int progress, String text);
}
