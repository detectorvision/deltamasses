/* $Id: StopHandler.java 102 2008-02-24 14:12:45Z jari $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.analyzationmanagement.AnalyzationControl;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Handlerclass to process a stopevent from the toolbar
 * @author lehmamic
 */
public class StopHandler  extends SelectionAdapter{
	// Attributs
	private AnalyzationControl[] analyzationControl;
	
	/**
	 * Constructor to submit the analization control
	 * @param analyzationControl
	 */
	public StopHandler(AnalyzationControl[] analyzationControl){
		this.analyzationControl = analyzationControl;
	}
	
	/**
	 * Event for the analyzation start.
	 * @param e Eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		if(this.analyzationControl[0] != null)
			this.analyzationControl[0].stopAlgorithm();
		
	}
}
