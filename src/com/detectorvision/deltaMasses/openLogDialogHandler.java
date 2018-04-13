/* $Id: openLogDialogHandler.java 322 2010-05-23 11:20:50Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.utility.ExternalFileHandler;
import com.detectorvision.utility.log4jSupport;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.util.Map;

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
 * Eventhandler for the openLogDialog Menu.
 * @author frank@detectorvision.com
 */
public class openLogDialogHandler extends SelectionAdapter{

	private String LogFile=null;

	// Logging with log4j
	static Logger logger = Logger.getLogger(openLogDialogHandler.class.getName());

	/**
	 * 
	 * @param
	 */
	public openLogDialogHandler() {
		LogFile=log4jSupport.getLogFile("deltamasses"); // Default log file
	}

	/**
	 * 
	 * @param
	 */
	public openLogDialogHandler(String logTarget){
		if(logTarget.equals("log")){
			java.io.File f = new java.io.File("log/deltaMasses.log");
			this.LogFile=f.getAbsoluteFile().toString();	
		}
		else if(logTarget.equals("automation")){
			//LogFile=log4jSupport.getLogFile("automation");
			java.io.File f = new java.io.File("log/automation.log");
			this.LogFile=f.getAbsoluteFile().toString();	
		}
		else if(logTarget.equals("mgf_automation_dir")){
			java.io.File f = new java.io.File("automation/mgf/data/");
			this.LogFile=f.getAbsoluteFile().toString();	
		}
		else if(logTarget.equals("mascot_automation_dir")){
			java.io.File f = new java.io.File("automation/mascot/data/");
			this.LogFile=f.getAbsoluteFile().toString();
		}
		else{
			java.io.File f = new java.io.File("log/deltaMasses.log");
			this.LogFile=f.getAbsoluteFile().toString();	
		}
		return;
	}

	/**
	 * Event method
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();

		Map openWidgets = null;

		try{
			//open the logfile, see http://forum.java.sun.com/thread.jspa?threadID=682870&messageID=3979401
			//needs to have the ending .txt associated with an application on users machine
			logger.info("openLogDialogHandler:widgetSelected:"+LogFile);
			Process p = null;
			if(LogFile.contains("mgf")){
				java.io.File f = new java.io.File(LogFile);
				logger.info("trying to open "+f.getAbsoluteFile().toString());

				p= Runtime.getRuntime().exec(new String[] { "explorer.exe", f.getAbsoluteFile().toString() });
				InputStream stdOut = p.getInputStream();
				if (stdOut.read() != -1) {
					logger.warn("Trouble with logFile: " + LogFile);
				}
			}
			else if(LogFile.contains("mascot")){
				java.io.File f = new java.io.File(LogFile);
				logger.info("trying to open "+f.getAbsoluteFile().toString());
				p= Runtime.getRuntime().exec(new String[] { "explorer.exe", f.getAbsoluteFile().toString()  });
				InputStream stdOut = p.getInputStream();
				if (stdOut.read() != -1) {
					logger.warn("Trouble with logFile: " + LogFile);
				}
			}
			else{
				if (ExternalFileHandler.open(LogFile)!=0) {
					logger.error("Trouble with logFile:" + LogFile);
					throw new Exception("openLogDialogHandler:trouble with logFile:"+
															LogFile);
				}
			}
		}catch(Exception exception){
			logger.info("privateOpen file has an exception. OK if not a Windows OS:"+exception.toString());
			 try {
				    //code below should only run on non-windows machines.
				    //bad news - this does not work on some windows machines....
					//see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6457572
					//see http://forums.sun.com/thread.jspa?threadID=682870&messageID=3979401
				    java.io.File f = new java.io.File(LogFile);
					logger.info("trying to open "+f.getAbsoluteFile().toString());
	
					Process p=null;
					p= Runtime.getRuntime().exec(new String[] { "soffice", f.getAbsoluteFile().toString()  });
					InputStream stdOut = p.getInputStream();
					if (stdOut.read() != -1) {
						logger.warn("Trouble with logFile: " + LogFile);
					}
					else {return;}
				 
					//Desktop.getDesktop().open(f);
				} catch (IllegalArgumentException ex) {
					logger.error("openLogDialogHandler 1: " + ex.getLocalizedMessage());
					return ;
				} catch (NullPointerException ex) {
					logger.error("openLogDialogHandler 2: " + ex.getLocalizedMessage());
					return ;
				} catch (UnsupportedOperationException ex) {
					logger.error("openLogDialogHandler 3: " + ex.getLocalizedMessage());
					return ;
				} catch (IOException ex) {
					logger.error("openLogDialogHandler 4: " + ex.getLocalizedMessage());
					return ;
				} catch (Exception ex) {
					logger.error("openLogDialogHandler 5: " + ex.getLocalizedMessage());
					return ;
				}
			
			
			
			
			
			
			
			Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			try {
				java.io.File f = new java.io.File("gui/HelpScreen.xswt");
				openWidgets = 
					XSWT.create(this.getClass().getResourceAsStream("gui/HelpScreen.xswt"))
					.parse(dialogShell);
	
			}
			catch (XSWTException error) {
				logger.error("XSWTException: " + error.toString());
			}
			dialogShell.pack();
			Button cancelButton = (Button)openWidgets.get("cancelButton");
			// eventhandler
			SelectionAdapter closeDialogEvent = new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e){

					// get the shell object
					final Button button = (Button)e.getSource();
					final Composite dialogComposite = (Composite)button.getParent();
					dialogComposite.getShell().close();
				}
			};
			cancelButton.addSelectionListener(closeDialogEvent);
			// show the screen
			dialogShell.open();	
		} 
	}
}
