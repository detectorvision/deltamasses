/* $Id: RobotHandler.java 400 2010-11-06 15:37:07Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.deltaMasses.analyzationmanagement.AnalyzationControl;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Eventhandler for starting the algorithm.
 * @author lehmamic
 */
public class RobotHandler extends SelectionAdapter{
	
	// Attributs
	AutomationControl[] automationControl;
	private ArrayList<String> FilesToProcess;
	private AlgoParams algoParams;
	
	/**
	 * Constructor to submit some parameters.
	 * @param analyzationControl
	 * @param record
	 * @param maxPairs
	 */
	public RobotHandler(AutomationControl[] automationControl, ArrayList<String> FilesToProcess, AlgoParams algoParams){
		// save the parameter
		this.automationControl = automationControl;
		this.FilesToProcess = FilesToProcess;
		this.algoParams = algoParams;
	}
	
	/**
	 * Event for the analyzation start.
	 * @param e Eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		 
		if(this.FilesToProcess != null)
			this.automationControl[0].startAutomation(this.FilesToProcess, algoParams);
		else{
			Shell mainShell;
			if(e.getSource() instanceof ToolItem){
				mainShell = ((ToolItem)e.getSource()).getParent().getShell();
			}
			else{
				mainShell = ((MenuItem)e.getSource()).getParent().getShell();
			}
			
			MessageBox box = new MessageBox(mainShell,
					SWT.OK
					| SWT.ICON_ERROR
					| SWT.APPLICATION_MODAL);
			
			box.setText("Analyzation error");
			box.setMessage("Analization error: No peakfile was read!");
			box.open();
		}
	}
}
