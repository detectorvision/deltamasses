/* $Id: ScoreException.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.massspectrometry.analyzation;

/**
 * Score Exception
 * @author lehmamic
 */
public class ScoreException extends Exception {
	
	/**
	 * Constructor of ScoreException, adds an exception message
	 * @param msg Message
	 */
	public ScoreException(String msg){
		super(msg);
	}

}
