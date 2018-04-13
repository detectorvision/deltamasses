/* $Id: SettingsDialogHandler.java 400 2010-11-06 15:37:07Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.biodata.Record;

import java.awt.Color;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;


/**
 * Eventhandler for the HelpDialog Menu.
 * @author lehmamic
 */
public class SettingsDialogHandler extends SelectionAdapter{
	private AlgoParams algoparams;
	//Label MsLabel=null;
	//Label MsmsLabel = null;
	Label VersionInfoLabel = null;
	Label statusLabel = null;
	Button fastDetectButton = null;
	Button fastDetectLazyButton = null;
	Button polymerFilterButton = null;
	Button neutralLossButton = null;
	Button storageButton = null;
	Button identifiedPairsOnlyButton = null;
	Text   fastDeltaMassText = null;
	Text   msAcc = null;
	Text   msmsAcc = null;
	//Spinner msValue = null;
	//Spinner msmsValue=null;
	Button defaultButton = null;
	Button setButton = null;
	Button cancelButton=null;
	Shell dialogShell=null;
	String status="";
	Button noLowMassBoundaryButton=null;

	static Logger logger = Logger.getLogger(SettingsDialogHandler.class.getName());

	public SettingsDialogHandler(AlgoParams anAlgoParam){
		this.algoparams=anAlgoParam;
	}

	public boolean checkParams(){
		boolean returnvalue=true;
		return returnvalue;
	}
	public void initScreen(){
		
		algoparams.getDefault();
		//msValue.setMinimum(1);
		//msmsValue.setMinimum(1);
		setScreen();

		if(!algoparams.isDiscoveryEdition){
			cancelButton.setEnabled(true);
			setButton.setEnabled(true);
			defaultButton.setEnabled(true);
			VersionInfoLabel.setText("Full functionality available in Discovery Edition");
			setStatus("weak delta, polymerfilter, fast detection only in Discovery Edition.");
			neutralLossButton.setEnabled(false);
			polymerFilterButton.setEnabled(false);
			fastDetectButton.setEnabled(false);
			fastDetectLazyButton.setEnabled(false);
			noLowMassBoundaryButton.setEnabled(false);
		}
		else{VersionInfoLabel.setText("");
			setStatus("dialog to change and set detection parameters.");
		}
	}


	public void setScreen(){
		setStatus(status);
		if(algoparams.DMBautoStore){
			storageButton.setText("on");
			storageButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		}
		else{
			storageButton.setText("off");
			storageButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
		}

		if(algoparams.neutralLossDetection){
			neutralLossButton.setText("on");
			neutralLossButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));		
		}
		else{
			neutralLossButton.setText("off");
			neutralLossButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));	
		}
		
		if(algoparams.noLowMassBoundary){
			noLowMassBoundaryButton.setText("on");
			noLowMassBoundaryButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));		
		}
		else{
			noLowMassBoundaryButton.setText("off");
			noLowMassBoundaryButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));	
		}
		
		if(algoparams.identifiedPairsOnly){
			identifiedPairsOnlyButton.setText("on");
			identifiedPairsOnlyButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));		
		}
		else{
			identifiedPairsOnlyButton.setText("off");
		    identifiedPairsOnlyButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));	
		}
		
		if(algoparams.polymerFiltering){
			polymerFilterButton.setText("on");
			polymerFilterButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));	
		
		}
		else{
			polymerFilterButton.setText("off");
			polymerFilterButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
		}


		if(algoparams.superFastDetection){
			fastDetectButton.setText("on");
			fastDetectLazyButton.setEnabled(true);
			fastDeltaMassText.setEnabled(true);
			fastDetectButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));	
			}
		else{
			fastDetectButton.setText("off");
			fastDetectLazyButton.setEnabled(false);
			fastDeltaMassText.setEnabled(false);
			fastDetectButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));	
		}
		fastDeltaMassText.setText(""+algoparams.superFastDeltaMass);
		logger.info("setting ms accuracies:"+algoparams.msAccuracy+" "+algoparams.msmsAccuracy);
		
		if(algoparams.superFastDetectionLazy){
			fastDetectLazyButton.setText("on");
			fastDetectLazyButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));	
			}
		else{
			fastDetectLazyButton.setText("off");
			fastDetectLazyButton.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));	
		}
		msAcc.setText(""+algoparams.msAccuracy);
		msmsAcc.setText(""+algoparams.msmsAccuracy);
		
		//int tmp=(int)(algoparams.msAccuracy*Math.pow(10, msmsValue.getDigits()));
		//msValue.setSelection(tmp);
	
		//tmp=(int)(algoparams.msmsAccuracy*Math.pow(10, msmsValue.getDigits()));
		//msmsValue.setSelection(tmp);
	}
	
	public void refreshScreen(){
		setScreen();
		parseData();
	}

	public void setStatus(String statusIn){
		status=statusIn;
		statusLabel.setText(status);
	}
	
	
    public boolean parseData(){
		try{
			algoparams.superFastDeltaMass=Double.parseDouble(fastDeltaMassText.getText());
			fastDeltaMassText.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			if(algoparams.superFastDeltaMass<13.5 || algoparams.superFastDeltaMass>2000){
				if(algoparams.superFastDeltaMass<13.5 ){setStatus("single channel deltaMass too small (<13.5 Da), please correct");}
				else{setStatus("single channel deltaMass too big (>2000 Da), please correct");}
				fastDeltaMassText.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
				return false;
			}	
		}
		catch(Exception ex){
			logger.info("SettingsDialogHandler:parseData:Exception:"+ex.toString());
			fastDeltaMassText.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
			setStatus("Please correct: "+ex.toString());
			return false;
		}
		
		
		
		try{
			algoparams.msAccuracy=Double.parseDouble(msAcc.getText());
			msAcc.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			if(algoparams.msAccuracy<=0 || algoparams.msAccuracy>1.0){
				msAcc.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
				return false;
			}	
		}
		catch(Exception ex){
			logger.info("SettingsDialogHandler:parseData:Exception:"+ex.toString());
			msAcc.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
			setStatus("Please correct: "+ex.toString());
			return false;
		}
		
		
		try{
			algoparams.msmsAccuracy=Double.parseDouble(msmsAcc.getText());
			msmsAcc.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			if(algoparams.msmsAccuracy<=0 || algoparams.msmsAccuracy>1.0){
				msmsAcc.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
				return false;
			}	
		}
		catch(Exception ex){
			logger.info("SettingsDialogHandler:parseData:Exception:"+ex.toString());
			msmsAcc.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
			setStatus("Please correct: "+ex.toString());
			return false;
		}
		
		
		
		//try{
		//	algoparams.msAccuracy=msValue.getSelection()/Math.pow(10, msValue.getDigits());
		//	logger.info("parseData set ms accuracy to "+algoparams.msAccuracy);
		//	msValue.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		//}
		//catch(Exception ex){
		//	msValue.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
		//	logger.info("SettingsDialogHandler:parseData:exception2:"+ex.toString());
		//	setStatus("Please correct: "+ex.toString());
		//	return false;
		//}

//		try{
//			algoparams.msmsAccuracy=msmsValue.getSelection()/Math.pow(10, msmsValue.getDigits());
//			logger.info("parseData set msms accuracy to "+algoparams.msmsAccuracy);
//			msmsValue.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
//		}
//		catch(Exception ex){
//			msmsValue.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
//			setStatus("Please correct: "+status.toString());
//			logger.info("SettingsDialogHandler:parseData:exception3:"+ex.toString());
//			return false;
//		}
		return true;
    }
	
	public void widgetSelected(SelectionEvent e){
		Shell mainShell = null;
		logger.info("SettingsDialogHandler.widgetSelected:"+e.toString());
		if(e.getSource() instanceof MenuItem){
			mainShell = ((MenuItem)e.getSource()).getParent().getShell();
		}
		else {
			mainShell = ((ToolItem)e.getSource()).getParent().getShell();
		}

		Map openWidgets = null;
		dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/SettingsDialog.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			logger.error("SettingsDialogHandler:XSWTException"+error.toString());
		}
		dialogShell.pack();

		cancelButton = (Button)openWidgets.get("cancelButton");
		setButton=(Button)openWidgets.get("setButton");
		defaultButton=(Button)openWidgets.get("defaultButton");
		fastDetectButton=(Button)openWidgets.get("fastDetectButton");
		fastDetectLazyButton=(Button)openWidgets.get("fastDetectLazyButton");
		polymerFilterButton=(Button)openWidgets.get("polymerFilterButton");
		neutralLossButton=(Button)openWidgets.get("neutralLossButton");
		storageButton=(Button)openWidgets.get("storageButton");
		identifiedPairsOnlyButton=(Button)openWidgets.get("identifiedPairsOnlyButton");
		fastDeltaMassText=(Text)openWidgets.get("fastDeltaMassText");
		fastDeltaMassText.setToolTipText("enter a deltaMass in Dalton for a single detection channel");
		
		msAcc=(Text)openWidgets.get("msAccText");
		msAcc.setToolTipText("MS accuracy. Only affects assignment of detected signals to known modifications from UNIMOD.");
		msmsAcc=(Text)openWidgets.get("msmsAccText");
		msmsAcc.setToolTipText("MSMS accuracy in Dalton.");
		
		//msValue = (Spinner)openWidgets.get("msValueSpinner");
		//msmsValue = (Spinner)openWidgets.get("msmsValueSpinner");
		VersionInfoLabel = (Label)openWidgets.get("versionInfo");
	    statusLabel = (Label)openWidgets.get("status");
	    noLowMassBoundaryButton = (Button)openWidgets.get("noLowMassBoundaryButton");
		this.initScreen();
		
		SelectionAdapter closeDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				dialogComposite.getShell().close();
			}
		};
		cancelButton.addSelectionListener(closeDialogEvent);

		SelectionAdapter setDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
	
				if(!parseData()){return;}//there was a parse error
				
				algoparams.printParams();
				algoparams.storeDefault();
				refreshScreen();

				//only place where parameters go back to deltaMasses
				DeltaMasses.algoParams=algoparams;
				setStatus("parameters set and stored");
				
				//close down the shell
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				dialogComposite.getShell().close();
			}
		};
		setButton.addSelectionListener(setDialogEvent);	

		SelectionAdapter fastDetectEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				//click changes the button text.
				if(fastDetectButton.getText().equals("on")){
					fastDetectButton.setText("off");
					algoparams.superFastDetection=false;
					fastDetectLazyButton.setText("off");
					algoparams.superFastDetectionLazy=false;
				}
				else{
					fastDetectButton.setText("on");
					algoparams.superFastDetection=true;
				}	
				refreshScreen();
			}
		};
		fastDetectButton.addSelectionListener(fastDetectEvent);	
		
		SelectionAdapter fastDetectLazyEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				//click changes the button text.
				if(fastDetectLazyButton.getText().equals("on")){
					fastDetectLazyButton.setText("off");
					algoparams.superFastDetectionLazy=false;
				}
				else{
					fastDetectLazyButton.setText("on");
					algoparams.superFastDetectionLazy=true;
				}	
				refreshScreen();
			}
		};
		fastDetectLazyButton.addSelectionListener(fastDetectLazyEvent);	
		
		SelectionAdapter noLowMassBoundaryAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				//click changes the button text.
				if(noLowMassBoundaryButton.getText().equals("on")){
					noLowMassBoundaryButton.setText("off");
					algoparams.noLowMassBoundary=false;
				}
				else{
					noLowMassBoundaryButton.setText("on");
					algoparams.noLowMassBoundary=true;
				}	
				refreshScreen();
			}
		};
		noLowMassBoundaryButton.addSelectionListener(noLowMassBoundaryAdapter);	
		
		SelectionAdapter polymerFilterEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(polymerFilterButton.getText().equals("off")){
					polymerFilterButton.setText("on");
					algoparams.polymerFiltering=true;
				}
				else{
					polymerFilterButton.setText("off");
					algoparams.polymerFiltering=false;
				}	
				refreshScreen();
			}
		};
		polymerFilterButton.addSelectionListener(polymerFilterEvent);	


		SelectionAdapter neutralLossEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(neutralLossButton.getText().equals("off")){
					neutralLossButton.setText("on");
					algoparams.neutralLossDetection=true;
				}
				else{
					neutralLossButton.setText("off");
					algoparams.neutralLossDetection=false;
				}	
				refreshScreen();
			}
		};
		neutralLossButton.addSelectionListener(neutralLossEvent);	




		SelectionAdapter storageEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(storageButton.getText().equals("off")){
					storageButton.setText("on");
					algoparams.DMBautoStore=true;
				}
				else{
					storageButton.setText("off");
					algoparams.DMBautoStore=false;
				}	
				refreshScreen();
			}
		};
		storageButton.addSelectionListener(storageEvent);	
		
		
		//New 20100508
		SelectionAdapter identifiedPairsOnlyButtonEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(identifiedPairsOnlyButton.getText().equals("off")){
					identifiedPairsOnlyButton.setText("on");
					algoparams.identifiedPairsOnly=true;
				}
				else{
					identifiedPairsOnlyButton.setText("off");
					algoparams.identifiedPairsOnly=false;
				}	
				refreshScreen(); 
			}
		};
		identifiedPairsOnlyButton.addSelectionListener(identifiedPairsOnlyButtonEvent);	


		SelectionAdapter defaultDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
			    logger.info("settings algoparams to default");
				algoparams.setDefault();
				algoparams.printParams();
				refreshScreen();
				setStatus("Default settings have been loaded");
			}
		};
		defaultButton.addSelectionListener(defaultDialogEvent);	

		// show the screen
		dialogShell.open();
		
	}
}
