/* $Id: XmlDialogHandler.java 284 2010-05-08 09:15:29Z frank $ */

package com.detectorvision.deltaMasses;

import java.io.File;
import java.io.IOException;
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
public class XmlDialogHandler extends SelectionAdapter{

	// Logging with log4j
	static Logger logger = Logger.getLogger(XmlDialogHandler.class.getName());


	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public XmlDialogHandler(){
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
			String targetFile=DeltaMasses.records[0].getFileName();
			if(targetFile.endsWith(".xml")){
				//TODO take off the .xml  
			}
			targetFile = targetFile + ".deltaMasses.xml";
			File toFile = new File(targetFile);

			if(!toFile.exists()){
				logger.info("Exporting xml results to:"+toFile.toString());
				if(DeltaMasses.records[0].printXML(toFile.toString(), DeltaMasses.pairList)){
					logger.info("Export ok.");
				} 
				else{
					logger.error("XML export had troubles");
				}
			}
			else{
				logger.error("Did not export XML results because file already exists: " +
										 toFile.toString());
			}
		}catch(Exception exception){
			logger.error("Exception:"+e.toString());
			Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			try {
				openWidgets = 
					XSWT.create(this.getClass().getResourceAsStream("gui/HelpScreen.xswt"))
					.parse(dialogShell);
			}
			catch (XSWTException error) {
				logger.error("XSWTException:"+error.toString());
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