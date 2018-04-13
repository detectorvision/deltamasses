/* $Id: DeleteLogDialogHandler.java 312 2010-05-17 13:21:10Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.utility.log4jSupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Eventhandler for the HelpDialog Menu.
 * @author lehmamic
 */
public class DeleteLogDialogHandler extends SelectionAdapter{

	private String LogFile=null;

	// Logging with log4j
	static Logger logger =Logger.getLogger(DeleteLogDialogHandler.class.getName());

	public DeleteLogDialogHandler(){
		LogFile=log4jSupport.getLogFile("deltamasses"); // Default log file
	}

	/**
	 * 
	 * @param
	 */
	public DeleteLogDialogHandler(String logTarget){
		if(logTarget.equals("log")){
			LogFile=log4jSupport.getLogFile("deltamasses");

		}
		else if(logTarget.equals("automation")){
			LogFile=log4jSupport.getLogFile("automation");
		}
		else{
			LogFile=log4jSupport.getLogFile("deltamasses");
		}
		if(LogFile != null){
			File f = new File(LogFile);
			LogFile=f.getAbsoluteFile().toString();
			logger.info("DeleteLogDialogHandler:"+LogFile);
		}
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();

		Map openWidgets = null;
		try{
			java.io.File f = new java.io.File("log/deltaMasses.log");
			this.LogFile=f.getAbsoluteFile().toString();
			logger.info("About to delete log file: " + LogFile);
			String deleteFile = LogFile;
			File logFile =new File(deleteFile);
			logger.info("preparing to delete: " + LogFile);	
			if(logFile.canWrite() ){
				logger.info("can write this file: " + logFile);	
				Logger.shutdown();//delete_fehler if you do not have the shutdown, you cannot delete below
				if(logFile.delete()){
					{
						logger =Logger.getLogger(DeleteLogDialogHandler.class.getName());//delete_error however, this is not starting up the loggin again.
						logger.info("deleted logfile");
						try {
							BufferedWriter out = new BufferedWriter(new FileWriter(this.LogFile));
							String toLog = DateFormat.getDateTimeInstance().format(new Date()) + "\t"  + "Automation controller generated this log file" +  "\r\n";
							out.write(toLog);
							out.close();
						} catch (IOException e3) {
							logger.error("IOException: " + e3.getLocalizedMessage());
						}	

						logger.info("-------------------------------------------------------------------------");
						logger.info("DeleteLogDialogHandler cleaned " + LogFile);
						logger.info("deltaMasses version: " + DeltaMasses.version +
								" Discovery Edition: " + DeltaMasses.isDiscoveryEdition);
						logger.info("-------------------------------------------------------------------------");
						Properties pr = System.getProperties();
						TreeSet propKeys = new TreeSet(pr.keySet());  // TreeSet sorts keys
						String tmpText="SYSTEM_PROPERTIES: ";
						for (Iterator it = propKeys.iterator(); it.hasNext(); ) {
							String key = (String)it.next();
							tmpText += "" + key + "=" + pr.get(key) + " ";
						}
						logger.info(tmpText);	     
						logger.info("-------------------------------------------------------------------------");
					}
				}
				else{
					logger =Logger.getLogger(DeleteLogDialogHandler.class.getName());//delete_error this is not helping either.
					logger.info("could not delete logfile:"+LogFile.toString());	
				}
				
				try {
					File file = new File(deleteFile);
					boolean success = file.createNewFile();
					if (success) {
						// File did not exist and was created
					} else {
						// File already exists
					}
				} catch (IOException e2) {
					logger.error("IOException: " + e2.toString());
				}
			}
			else{
				logger.warn("cannot delete::"+LogFile+": canWrite is:"+logFile.canWrite());
			}
		}catch(Exception exception){
			logger.error("Exception occured: " + LogFile + ": " + e.toString());
			Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			try {
				openWidgets = 
					XSWT.create(this.getClass().getResourceAsStream("gui/HelpScreen.xswt"))
					.parse(dialogShell);
			}
			catch (XSWTException error) {
				logger.error("Exception:XSWTException: " + error.toString());
			}
			dialogShell.pack();
			Button cancelButton = (Button)openWidgets.get("cancelButton");
			
			// eventhandler
			SelectionAdapter closeDialogEvent = new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e){
					final Button button = (Button)e.getSource();
					final Composite dialogComposite = (Composite)button.getParent();
					dialogComposite.getShell().close();
				}
			};
			cancelButton.addSelectionListener(closeDialogEvent);
			dialogShell.open();	
		} 
	}
}
