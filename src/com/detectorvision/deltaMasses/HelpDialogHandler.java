/* $Id: HelpDialogHandler.java 425 2011-10-12 22:13:07Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.utility.ExternalFileHandler;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Eventhandler for the HelpDialog Menu.
 * @author lehmamic
 */
public class HelpDialogHandler extends SelectionAdapter{
	private String docType="deltaMasses";

	// Logging with log4j
	static Logger logger = Logger.getLogger(HelpDialogHandler.class.getName());
	
	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public HelpDialogHandler(String docType){
		this.docType=docType;
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){

		String docTarget="documentation/deltaMasses_manual.pdf";
		if (docType.equals("deltaMasses"))
			docTarget="documentation/deltaMasses_manual.pdf";
		//else if (docType.equals("postgreSQL"))
		//	docTarget="documentation/postgresql-8.2-A4.pdf";
		//else if (docType.equals("pgAdmin"))
		//	docTarget="documentation/pgadmin3.chm";
		//else if(docType.equals("deltaProteinManual"))
		//	docTarget="documentation/deltaProtein_manual.pdf";
		else if(docType.equals("deltaMassBaseInstallManual"))
			docTarget="documentation/deltaMassBase_Installation.pdf";
		else{
			logger.error("unexpected docType:"+docType.toString());
		}
		int retval=ExternalFileHandler.open(docTarget);
		if (retval!=0 ) {
			Map openWidgets = null;
			final MenuItem menuItem = (MenuItem)e.getSource();
			Shell dialogShell = new Shell(menuItem.getParent().getShell(),
																		SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			try {
				openWidgets = 
					XSWT.create(this.getClass().getResourceAsStream("gui/HelpScreen.xswt"))
					.parse(dialogShell);
			}
			catch (XSWTException error) {
				logger.error("HelpDialogHandler:XSWTException:" + error.toString());
			}
			String msg="";
			switch (retval) {
			case 1:
				msg=new String("Cannot find '" + docTarget + "'");
				break;
			case 2:
				msg=new String("Open of external application not supported by this OS");
				break;
			case 3:
				msg=new String("Cannot open external application or filetype not associated with application (" + docTarget + ").");
				break;
			default:
				msg=new String("Unknown error occured, cannot show '" + docTarget + "'");
			}
			Label msgLabel=(Label)openWidgets.get("Message");
			msgLabel.setText(msg);
			dialogShell.setText("Error");
			dialogShell.pack();
			Button cancelButton = (Button)openWidgets.get("cancelButton");
			// eventhandler
			SelectionAdapter closeDialogEvent = new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {
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
