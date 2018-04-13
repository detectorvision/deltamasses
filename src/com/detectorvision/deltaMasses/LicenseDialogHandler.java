/* $Id: LicenseDialogHandler.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.deltaMasses;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Map;
import java.util.StringTokenizer;

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
 * Eventhandler for the License Handler
 * @author Frank Potthast
 */
public class LicenseDialogHandler extends SelectionAdapter{
	static String MACAddress="";

	// Logging with log4j
	static Logger logger = Logger.getLogger(LicenseDialogHandler.class.getName());
	
	/** 
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public LicenseDialogHandler(){
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
			      XSWT.create(this.getClass().getResourceAsStream("gui/licenseScreen.xswt"))
			          .parse(dialogShell);
			}
		catch (XSWTException error) {
			logger.error("LicensedialogHandler:XSWTException:"+error.toString());
		}
		dialogShell.pack();
		
		// objectreferences
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		Button macToClipBoardButton = (Button)openWidgets.get("macToClipBoardButton");
		
		Label  licenseLabel1  = (Label)openWidgets.get("licenseLabel1");
		Label  licenseLabel2 = (Label)openWidgets.get("licenseLabel2");
		Label  licenseLabel3 = (Label)openWidgets.get("licenseLabel3");
			
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
		
		SelectionAdapter macToClipBoardEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				 String tmpString="Please mail the following info to \ninfo@detectorvision.com\n";
				 tmpString += "Subject: Request for a Discovery Edition License\n\n";
				 tmpString += "Mac Address: "+MACAddress.toUpperCase()+"\n";
				 
				 StringSelection ss = new StringSelection(tmpString);
			     Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			     Toolkit.getDefaultToolkit().beep();
			}
		};
		macToClipBoardButton.addSelectionListener(macToClipBoardEvent);
		
		// show the screen
		dialogShell.open();	
		
		if(DeltaMasses.isDiscoveryEdition){
			licenseLabel1.setText("You allready have the Discovery Edition.");
			licenseLabel2.setText("Thanks for checking.");
			logger.info("LicenseDialogHandler: discovery edition");
		}
		else{
			
			try {
				MACAddress=getMacAddress().toUpperCase();
			} catch(Throwable t) {
				t.printStackTrace();
				logger.warn("LicenseDialogHandler:MacAdress incident:"+t.toString());
			}	
		licenseLabel1.setText("MAC Address: " + MACAddress.toUpperCase());
		licenseLabel2.setText("Please mail the MAC Address to");
		licenseLabel3.setText("info@detectorvision.com");
		logger.info("LicensDialogHandler: personal edition at mac:" +
								MACAddress.toUpperCase());
		}
	}

	
	private final static String getMacAddress() throws IOException {
		String os = System.getProperty("os.name");
		try {
			if(os.startsWith("Windows")) {
				return windowsParseMacAddress(windowsRunIpConfigCommand());
			} 
		} catch(ParseException ex) {
			logger.error("LicenseDialogHandler:Parse exception in getMacAddress:" +
									 ex.toString());
			throw new IOException(ex.getMessage());
		}
		return os;
	}
	
	private final static String windowsParseMacAddress(String ipConfigResponse) throws ParseException {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch(java.net.UnknownHostException ex) {
			ex.printStackTrace();
			logger.error("LicenseDialogHandler:windowsParsemacAddress:UnknownHost:" +
									 ex.toString());
			throw new ParseException(ex.getMessage(), 0);
		}
 
		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;
 
		while(tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			// see if line contains IP address
			if(line.endsWith(localHost) && lastMacAddress != null) {
				return lastMacAddress;
			}
 
			// see if line contains MAC address
			int macAddressPosition = line.indexOf(":");
			if(macAddressPosition <= 0) continue;
 
			String macAddressCandidate = line.substring(macAddressPosition + 1).trim();
			if(windowsIsMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		if(lastMacAddress == null){
			ParseException ex = new ParseException("cannot read MAC address from [" + ipConfigResponse + "]", 0);
			ex.printStackTrace();
			throw ex;
		}
		return lastMacAddress;
	}
 
 
	private final static boolean windowsIsMacAddress(String macAddressCandidate) {
		// TODO: use a smart regular expression
		//System.out.println("Mac Adress:"+macAddressCandidate);
		if(macAddressCandidate.length() != 17) {
			logger.info("LicenseDialogHandler:macAdressCandidate wrong length:" +
									 macAddressCandidate);
			return false;
		}
		if(macAddressCandidate.equalsIgnoreCase("00-00-00-00-00-00")){
			logger.info("macAdressCandidate seems to be a video card:" +
									macAddressCandidate);
			return false;
		}
		logger.info("LicenseDialogHandler:good MAC:"+macAddressCandidate);
		return true;
	}
 
 
	private final static String windowsRunIpConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ipconfig /all");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
 
		StringBuffer buffer= new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1) break;
			buffer.append((char)c);
		}
		String outputText = buffer.toString();
 
		stdoutStream.close();
 
		return outputText;
	}
}
