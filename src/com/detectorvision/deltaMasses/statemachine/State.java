/* $Id: State.java 279 2010-03-17 20:03:12Z frank $ */

package com.detectorvision.deltaMasses.statemachine;

/**
 * Programstates for programinteractions
 * @author lehmamic
 */
public enum State {
	NONE, START, LOADFILE, READY, ANALYZE, END, CLUSTER_LOAD, CLUSTER_RUN, CLUSTER_SAVE
}
