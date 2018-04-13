/* $Id: EventListener.java 91 2008-02-22 14:50:33Z jari $ */

package com.detectorvision.deltaMasses.statemachine;

/**
 * Interface for processing state changes
 * @author lehmamic
 */
public interface EventListener {
	
	/**
	 * Calls an event
	 * @param e Event
	 * @param data Some usefull data
	 */
	public void updateEvent(Event e, Object data);
}
