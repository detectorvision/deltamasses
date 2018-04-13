/* $Id: UpdateDialogHandler.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.analyzationmanagement.DeltaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
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
public class UpdateDialogHandler extends SelectionAdapter{

	// Logging with log4j
	static Logger logger = Logger.getLogger(UpdateDialogHandler.class.getName());

	
	/** 
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public UpdateDialogHandler(){
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
			      XSWT.create(this.getClass().getResourceAsStream("gui/updateScreen.xswt"))
			          .parse(dialogShell);
			}
		catch (XSWTException error) {
			logger.error("XSWTException:" + error.getLocalizedMessage());
		}
		dialogShell.pack();
		logger.info("Checking for updates");
		
		// objectreferences
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		Label  updateLabel  = (Label)openWidgets.get("updateLabel");
		Label  updateLabel2 = (Label)openWidgets.get("updateLabel2");
		Label  updateLabel3 = (Label)openWidgets.get("updateLabel3");
	
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
		
		
		URL url=null;
		updateLabel.setText("Checking for updates at www.detectorvision.com/deltaMasses/updates.html");
		updateLabel3.setText("");
		/*for(int i=10;i>0;i--)
		try {
			updateLabel2.setText("Starting in " + i + " seconds");
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		updateLabel2.setText("");
		updateLabel3.setText("");*/
		
		try {
			url = new URL("http://www.detectorvision.com/deltaMasses/updates.html");
		} catch (MalformedURLException e2) {
			logger.error("URL connection issue:"+e2.getLocalizedMessage());
		}
		updateLabel.setText("Trying " + url.toString());
		try {
			URLConnection conn = url.openConnection();
			String type = conn.getContentType();
			logger.debug(conn.getContent().toString());
			if(type != null)updateLabel.setText(type.toString());
			else{ 
				updateLabel.setText("Sorry, could not connect to www.detectorvision.com/deltaMasses");
				updateLabel2.setText("Maybe you want to try again later");
				updateLabel3.setText("deltaMasses needs to be connected to the internet for checking.");
				logger.warn("Could not connect to deltaMasses URL.");
			}
			
			if(type != null){
				String response="";
				InputStream in =conn.getInputStream();
				logger.info("read:"+in.toString());
				int c;
				while((c = in.read()) != -1){
					response = response + (char)c;
					}
				//System.out.println(response);
				int start = response.indexOf("deltaMassesVersion=");
				String vers =response.substring(start+19, start+23);
				
				start = response.indexOf("deltaMassesBuild=");
				String build =response.substring(start+17, start+37);
				
				logger.info("version is:"+vers);
				logger.info("build is:"+build);
				
				double version=0;
				try{
				version = Double.parseDouble(vers);
				}catch(Exception e2){
					version=0;
					logger.error("Parse Exception:"+e2.getMessage());
				}
				if(version> Double.parseDouble(DeltaMasses.version)){	
					updateLabel.setText("New version available: You have V " + DeltaMasses.version + " " + DeltaMasses.build );
					updateLabel2.setText("Now Available: V "+version + " " + build);
					updateLabel3.setText("More at www.detectorvision.com/deltaMasses/updates.html");
					logger.info("New version available:"+version+" "+build);
				}
				else {updateLabel.setText("Your version is up to date, version V " + DeltaMasses.version + " " + DeltaMasses.build);
				updateLabel2.setText("Thanks for checking.");}
				logger.info("Version up to date");
				}
		} catch (IOException e1) {
			updateLabel.setText("Sorry, unable to connect to www.detectorvision.com/deltaMasses/updates.html");
			updateLabel2.setText("Maybe you want to check for updates lateron");
			updateLabel3.setText("deltaMasses needs to be connected to the internet for checking.");
			logger.error("URL connection issues:"+e1.getLocalizedMessage());
		}
	}
}
