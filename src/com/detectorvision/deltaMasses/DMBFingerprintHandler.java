/* $Id: DMBFingerprintHandler.java 382 2010-08-25 16:45:50Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.analyzationmanagement.DeltaUtils;
import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.ExternalFileHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The DMBFingerprintHandler 
 * exports a DMB Fingerprint to a pdf
 *  
 * @author Frank Potthast
 */
public class DMBFingerprintHandler extends SelectionAdapter {

	private Shell mainShell;
	private Map opendialogWidgets;
	private Shell dialogShell;
	private static Button closeButton;
	public static Button createButton;
	public static Button stopButton;
	private boolean fingerprintOk = true;
	private EventListener eventListener;
	public double lowM;
	public double highM;
 
	postSQL printObj = null;
	// Logging with log4j
	static Logger logger = Logger.getLogger(DMBFingerprintHandler.class.getName());
	public DMBFingerprintHandler(EventListener eventListener) {
		super();	
		this.eventListener = eventListener;
		this.lowM=0;
		this.highM=2000;
	}

	/** 
	 * 
	 * 
	 */
	public void widgetSelected(SelectionEvent e){
		
		
		// get the mainshell. Since the Event-source may be either a menuitem of a toolitem, we have to 
		// identify the source.
		if(e.getSource() instanceof MenuItem){
			this.mainShell = ((MenuItem)e.getSource()).getParent().getShell();
		}
		else {
			this.mainShell = ((ToolItem)e.getSource()).getParent().getShell();
		}

		this.dialogShell = new Shell(mainShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			opendialogWidgets =  XSWT.create(this.getClass().getResourceAsStream("gui/DMBFingerprintDialog.xswt")).parse(dialogShell);
			closeButton = (Button)opendialogWidgets.get("cancelButton");

			Label DMBExperimentsStoredLabel = (Label)opendialogWidgets.get("DMBExperimentsStored");
			Label DMBRecordsStoredLabel = (Label)opendialogWidgets.get("DMBRecordsStored");
			Label DMBRSpectraStoredLabel = (Label)opendialogWidgets.get("DMBSpectraStored");
			Label DMBDpdPairsStoredLabel = (Label)opendialogWidgets.get("DMBDpdPairsStored");			
			Label DMBPeptideNetsStoredLabel = (Label)opendialogWidgets.get("DMBPeptideNetsStored");
			Label DMBpeptidesStoredLabel = (Label)opendialogWidgets.get("DMBpeptidesStored");
			Label DMBDistinctPeptidesStoredLabel = (Label)opendialogWidgets.get("DMBDistinctPeptidesStored");
			Label DMBDistinctProteinsStoredLabel = (Label)opendialogWidgets.get("DMBDistinctProteinsStored");
			Label DMBSpecsHavingDpdLabel = (Label)opendialogWidgets.get("DMBSpecsHavingDpd");
			Label DMBLightPepsWithDpdLabel = (Label)opendialogWidgets.get("DMBLightPepsWithDpd");
			Label DMBHeavyPepsWithDpdLabel = (Label)opendialogWidgets.get("DMBHeavyPepsWithDpd");
			Combo DMBcomboDropDown = (Combo)opendialogWidgets.get("DMBFingerPrintCombo");
			final ProgressBar totalProgressBar = (ProgressBar)opendialogWidgets.get("totalProgressBar");

			final Label createStatusLabel=(Label)opendialogWidgets.get("createStatus");
			final Text lowMass=(Text)opendialogWidgets.get("lowMass");
			final Text highMass=(Text)opendialogWidgets.get("highMass");
			final Text barCodeTitle = (Text)opendialogWidgets.get("barcodeTitle");

			createButton= (Button)opendialogWidgets.get("createButton");
			stopButton = (Button)opendialogWidgets.get("stopButton");
			createStatusLabel.setText("-----------------------------------------");

			EventListener eListener=null;
			stopButton.setEnabled(false);
			ProgressListener pListener = new ProgressListener() {
				public void updateProgress(int progress, String statusText) {
					logger.debug("text: " + statusText + " int: " + progress);
					createStatusLabel.setText(statusText);
					if(statusText.equals("processing completed")){
						stopButton.setEnabled(false);
						closeButton.setEnabled(true);
						createStatusLabel.setText("fingerprint creation ok");
						if (ExternalFileHandler.open("tmp/deltaProtein_report.pdf")!=0)
							logger.error("Acrobat reader not installled or file association for PDF is not there");
						
					}
					if (progress >= 0 && progress <= 100)
						totalProgressBar.setSelection(progress);
					else
						logger.warn("totalProgressBar:pListener send wrong progress: " +
												progress);
				}
			};
			
			printObj = new postSQL(eListener, pListener) ;
			Connection conn = DeltaMassBase.getConnection();

			if(conn==null){
				logger.warn("connection:null:returning:");
				return;
			}

			boolean dbOk=true;
			Statement s;
			try{
				s = conn.createStatement();
				s.execute("SELECT COUNT(*) FROM multimod");
				ResultSet rs = s.getResultSet();
				rs.next ();
			}
			catch (SQLException e1) {
				logger.error("cannot see any modifications. Datbase not created." +
										 e1.getLocalizedMessage());
				createButton.setEnabled(false);
				createButton.setToolTipText("Please create the database.");
				createButton.setText("database not created");
				createStatusLabel.setText("Please create the database");
				createStatusLabel.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
				createStatusLabel.setForeground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				stopButton.setEnabled(false);
				dbOk=false;
			}
			if(dbOk){createStatusLabel.setText("deltaMass Base looking fine");}

			try {	
				s = conn.createStatement();
				s.execute("SELECT count(*) FROM record");
				ResultSet rs = s.getResultSet();
				rs.next ();
				DMBRecordsStoredLabel.setText((""+  (rs.getInt(1))));
				if(rs.getInt(1)==0){//disable button
					createButton.setEnabled(false);
					createButton.setToolTipText("No data stored, cannot make a fingerprint.");
					createButton.setText("no data in database");
					stopButton.setEnabled(false);
					createStatusLabel.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
					createStatusLabel.setForeground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					createStatusLabel.setText("no data in database: nothing to do");
				}
				
				s.execute("SELECT last_value FROM spectrumSeq;");
				rs = s.getResultSet();
				rs.next ();
				DMBRSpectraStoredLabel.setText((""+ (rs.getInt(1))));

				s.execute("SELECT last_value FROM deltaMassSeq");
				rs = s.getResultSet();
				rs.next ();
				DMBDpdPairsStoredLabel.setText((""+ (rs.getInt(1))));

				s.execute("SELECT count(*) FROM specNet");
				rs = s.getResultSet();
				rs.next ();
				DMBPeptideNetsStoredLabel.setText(("xxxxxx"));//to make space - kind of stupid.
				DMBPeptideNetsStoredLabel.setText((""+ (rs.getInt(1))));

				s.execute("SELECT count(*) FROM peptide");
				rs = s.getResultSet();
				rs.next ();
				DMBpeptidesStoredLabel.setText((""+(rs.getInt(1))));

				//s.execute("SELECT count(distinct experiment_id) FROM experiment,record WHERE experiment_id=fk_experiment_id");
				//rs = s.getResultSet();
				//rs.next ();
				//DMBExperimentsStoredLabel.setText((""+(rs.getInt(1))));


				/*//20071021
				s.execute("SELECT count(*) FROM experiment,record where experiment_id=fk_experiment_id");
				rs = s.getResultSet();	
				rs.next ();
				if(rs.getInt(1)>1){
					DMBcomboDropDown.add("use all experiments");
				}
				else if(rs.getInt(1)==0){
					DMBcomboDropDown.add("no data to process");
					createStatusLabel.setText("no data to process");
					createButton.setEnabled(false);
				}

				s.execute("SELECT distinct experiment_id,name FROM experiment,record where experiment_id=fk_experiment_id");
				rs = s.getResultSet();				

				DMBcomboDropDown.setCapture(false);
				while(rs.next()){
					DMBcomboDropDown.add("E" + (rs.getInt(1))+ "   "+(rs.getString(2)));
				}*/

				conn.close();
			} catch (SQLException e1) {
				logger.error("SQLException:" + e1.getLocalizedMessage());
			}

			if(!DeltaUtils.DMBspecnetIsUpToDate()){
			    createStatusLabel.setBackground(createStatusLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			    createStatusLabel.setForeground(createStatusLabel.getDisplay().getSystemColor(SWT.COLOR_WHITE));	
				createStatusLabel.setText("pepnet not up to date !!!");
				createStatusLabel.setToolTipText("use the deltaMassBase interface to update the peptide nets");
				DeltaMasses.statusLabel.setText("use the deltaMassBase interface to update the peptide nets");
				createButton.setEnabled(false);
			}
			else{
				createButton.setEnabled(true);
			}

			/*Display the shell */
			dialogShell.pack();
			dialogShell.open();

			/*Close button function */
			closeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// When the button is pressed, close the child shell
					((Button)e.getSource()).getParent().getShell().close();
				}
			});


			/*Create button function */
			stopButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					printObj.stopDeltaMassFingerPrint();
					createStatusLabel.setText("Processing stopped by the user");
					closeButton.setEnabled(true);
					createButton.setEnabled(true);
					stopButton.setEnabled(false);
				}
			});

			
			/*Create button function */
			createButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String barcodeString=barCodeTitle.getText();
					boolean situationOk=true;
					double high=2000;
					
					createButton.setEnabled(false);//trac ticket #46
					closeButton.setEnabled(false);//trac ticket #46
					
					
					String highMassString=highMass.getText();
					try{
						high=Double.parseDouble(highMassString);
					}
					catch(Exception exp){
						createStatusLabel.setText("please enter valid high mass");
						situationOk=false;
					}
					double low=0;
					String lowMassString=lowMass.getText();
					try{
						low=Double.parseDouble(lowMassString);
					}
					catch(Exception exp){
						createStatusLabel.setText("please enter valid low mass");
						situationOk=false;
					}
					if(high<=low){
						high=2000;
						low=0;
						lowMass.setText(""+low);
						highMass.setText(""+high);
					}
					if(low<0){
						low=0;
						lowMass.setText(""+low);
					}
					if(high<=0){
						high=2000;
						highMass.setText(""+high);
					}
					if(high>2000){
						high=2000;
						highMass.setText("2000.0");
						createStatusLabel.setText("maximum high mass: 2000 Dalton");
						situationOk=false;
					}
					if(!situationOk){
						lowMass.setText(""+0.0);
						highMass.setText(""+2000.0);
					}
					else{
						createStatusLabel.setText("starting report generation");
						stopButton.setEnabled(true);
						createStatusLabel.setText("creating fingerprint .. can take some minutes ....");
						//boolean fingerprintOk = printObj.DMBgetDeltaMassFingerPrint(low,high,barcodeString);
						
						try
						{
							printObj.startDeltaMassFingerPrint(low, high, barcodeString);
						}
						catch(Exception ex)
						{
							fingerprintOk = false;
						}
						/*if(fingerprintOk){
							createStatusLabel.setText("fingerprint creation ok");
							if (ExternalFileHandler.open("tmp/deltaProtein_report.pdf")!=0)
								logger.error("Acrobat reader not installled or file association for PDF is not there");
						}
						else {
							createStatusLabel.setText("fingerprint creation failed. Please check the log.");
							logger.error("Fingerprint creation failed");
						}*/
					}
					//dialogShell.close();//track ticket #46
				}
			});

		} catch (XSWTException error) {
			logger.error("XSWTException:" + error.toString());
		}
	}

}