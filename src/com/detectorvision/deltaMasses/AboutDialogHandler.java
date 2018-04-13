/* $Id: AboutDialogHandler.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Eventhandler for the HelpDialog Menu.
 * @author lehmamic
 */
public class AboutDialogHandler extends SelectionAdapter{
	
	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public AboutDialogHandler(){
	}
	
	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		
		// get the mainshell
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();
		
		Map openWidgets = null;
		Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			   openWidgets = 
			      XSWT.create(this.getClass().getResourceAsStream("gui/AboutScreen.xswt"))
			          .parse(dialogShell);
			}
		catch (XSWTException error) {
			   error.printStackTrace();
			}
		dialogShell.pack();
		
		// objectreferences
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		Label  versionLabel  = (Label)openWidgets.get("versionLabel");
		
		if(DeltaMasses.isDiscoveryEdition)
			versionLabel.setText("version:" + DeltaMasses.version +" "+DeltaMasses.build + " Discovery Edition ");
		else
			versionLabel.setText("version:" + DeltaMasses.version +" "+DeltaMasses.build + " Personal Edition expires: "+DeltaMasses.expiresString);
			
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
