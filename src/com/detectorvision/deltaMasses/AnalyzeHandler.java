/* $Id: AnalyzeHandler.java 91 2008-02-22 14:50:33Z jari $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.analyzationmanagement.AnalyzationControl;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.biodata.Record;

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
public class AnalyzeHandler extends SelectionAdapter{
	
	// Attributs
	private AnalyzationControl[] analyzationControl;
	private Record[] record;
	private AlgoParams algoParams;
	
	/**
	 * Constructor to submit some parameters.
	 * @param analyzationControl
	 * @param record
	 * @param maxPairs
	 */
	public AnalyzeHandler(AnalyzationControl[] analyzationControl, Record[] record, AlgoParams algoParams){
		
		// save the parameter
		this.analyzationControl = analyzationControl;
		this.record = record;
		this.algoParams = algoParams;
	}
	
	/**
	 * Event for the analyzation start.
	 * @param e Eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		 
		if(this.record[0] != null)
			this.analyzationControl[0].startAlgorithm(this.record[0], algoParams);
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
