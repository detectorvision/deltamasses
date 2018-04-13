/* $Id: DMBDialogHandler.java 401 2010-11-06 15:39:11Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.analyzationmanagement.DeltaUtils;
import com.detectorvision.deltaMasses.analyzationmanagement.PeptideNet;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;

import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.DeltaMassBase_table_record;
import com.detectorvision.utility.ExternalFileHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The OpenDialogHandler implements a simple open dialog to specify a
 * peakfile and set some options.
 * 
 * @author Raphael Bosshard
 */
public class DMBDialogHandler extends SelectionAdapter {

	private Shell mainShell;
	private Map opendialogWidgets;
	private Shell dialogShell;
	private Button closeButton,createButton,dropButton,vacuumButton,calcNetButton,editConfigButton,infoToPdfButton,showDetailsButton,setDBButton;	
	
	private ProgressListener progressListener;
	private EventListener eventListener;
	private Label createStatusLabel;
	private Label DMBExperimentsStoredLabel;
	private Label DMBRecordsStoredLabel;
	private Label DMBRSpectraStoredLabel; 
	private Label DMBDpdPairsStoredLabel;
	private Label DMBpeptidesStoredLabel;
	private Label DMBPeptideNetsStoredLabel;
	private Label DMBDistinctPeptidesStoredLabel;
	private Label DMBDistinctProteinsStoredLabel;
	private Label DMBSpecsHavingDpdLabel;
	private Label DMBLightPepsWithDpdLabel;
	private Label DMBHeavyPepsWithDpdLabel;
	private Label DMBcreatedLabel;
	private Label DMBversionLabel;
	private Label DMBversionNeededLabel;
	private Label DMBlastModificationLabel;
	private Label DMBlastSpecnetLabel;
	private Label DMB_filenameLabel;
	private Label DMB_DELTAMASSBASE_NAMELabel;
	private Label DMB_connectionErrorLabel;
	private Label DMB_URLLabel;

	public static Table DBTable=null;

	// Logging with log4j
	static Logger logger = Logger.getLogger(DMBDialogHandler.class.getName());

	public DMBDialogHandler(EventListener eventListener) {
		super();	
		this.eventListener = eventListener;
	}

	public void widgetSelected(SelectionEvent e){
		if(e.getSource() instanceof MenuItem){
			this.mainShell = ((MenuItem)e.getSource()).getParent().getShell();
		}
		else {
			this.mainShell = ((ToolItem)e.getSource()).getParent().getShell();
		}

		this.dialogShell = new Shell(mainShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			opendialogWidgets =  XSWT.create(this.getClass().getResourceAsStream("gui/DMBDialog.xswt")).parse(dialogShell);
			closeButton = (Button)opendialogWidgets.get("cancelButton");
			editConfigButton = (Button)opendialogWidgets.get("editConfig");
			infoToPdfButton = (Button)opendialogWidgets.get("infoToPdf");
			infoToPdfButton.setEnabled(false);
			showDetailsButton = (Button)opendialogWidgets.get("showDetails");
			showDetailsButton.setEnabled(false);
			setDBButton = (Button)opendialogWidgets.get("setDB");

			this.DMBExperimentsStoredLabel = (Label)opendialogWidgets.get("DMBExperimentsStored");
			this.DMBRecordsStoredLabel = (Label)opendialogWidgets.get("DMBRecordsStored");
			this.DMBRSpectraStoredLabel = (Label)opendialogWidgets.get("DMBSpectraStored");
			this.DMBDpdPairsStoredLabel = (Label)opendialogWidgets.get("DMBDpdPairsStored");
			this.DMBpeptidesStoredLabel = (Label)opendialogWidgets.get("DMBpeptidesStored");		
			this.DMBPeptideNetsStoredLabel = (Label)opendialogWidgets.get("DMBPeptideNetsStored");
			this.DMBDistinctPeptidesStoredLabel = (Label)opendialogWidgets.get("DMBDistinctPeptidesStored");
			this.DMBDistinctProteinsStoredLabel = (Label)opendialogWidgets.get("DMBDistinctProteinsStored");
			this.DMBSpecsHavingDpdLabel = (Label)opendialogWidgets.get("DMBSpecsHavingDpd");
			this.DMBLightPepsWithDpdLabel = (Label)opendialogWidgets.get("DMBLightPepsWithDpd");
			this.DMBHeavyPepsWithDpdLabel = (Label)opendialogWidgets.get("DMBHeavyPepsWithDpd");
			this.createStatusLabel=(Label)opendialogWidgets.get("createStatus");
			this.DMBcreatedLabel=(Label)opendialogWidgets.get("DMBcreated");
			this.DMBcreatedLabel.setToolTipText("date when your database schema was created the last time.");
			this.DMBversionLabel=(Label)opendialogWidgets.get("DMBversion");
			this.DMBversionLabel.setToolTipText("The version of the database schema currently used in your database");
			this.DMBversionNeededLabel=(Label)opendialogWidgets.get("DMBversionNeeded");
			this.DMBversionNeededLabel.setToolTipText("The version of the database schema needed by your current deltaMasses installation");
			this.DMBversionNeededLabel.setText(""+DeltaMasses.db_schema_version);
			this.DMBlastModificationLabel=(Label)opendialogWidgets.get("DMBlastModification");
			this.DMBlastModificationLabel.setToolTipText("date of the last change to the database being relevant to deltaProtein");
			this.DMBlastSpecnetLabel=(Label)opendialogWidgets.get("DMBlastSpecnet");
			this.DMBlastSpecnetLabel.setToolTipText("date of the last update of the specnet table by deltaProtein.");
			this.DMB_filenameLabel=(Label)opendialogWidgets.get("DMB_filename");
			this.DMB_DELTAMASSBASE_NAMELabel=(Label)opendialogWidgets.get("DMB_DELTAMASSBASE_NAME");
			this.DMB_connectionErrorLabel=(Label)opendialogWidgets.get("DMB_connectionError");
			this.DMB_URLLabel=(Label)opendialogWidgets.get("DMB_URL");
			String fontName = "Helvetica";
			int fontBaseSize = 12; 
			this.DMB_DELTAMASSBASE_NAMELabel.setFont(new Font(e.display, fontName, fontBaseSize, SWT.BOLD ));
			
			calcNetButton=(Button)opendialogWidgets.get("calcNetButton");
			calcNetButton.setToolTipText("Calculate peptide nets. Can take some minutes.");
			createButton = (Button)opendialogWidgets.get("create");
			createButton.setToolTipText("Creates the database.");
			dropButton =  (Button)opendialogWidgets.get("dropButton");
			dropButton.setToolTipText("Formats the database to the current schema. All data in the database will be lost.");
			vacuumButton = (Button)opendialogWidgets.get("vacuumButton");
			vacuumButton.setToolTipText("Use this button for database optimization once in a while. Data will NOT be affected by this operation.");
			createStatusLabel.setText("-----------------------------------------");
			DBTable = (Table)opendialogWidgets.get("DBTable");

			updateValues();

			createStatusLabel.setText(DeltaUtils.DMBgetVersionInfoString(DeltaMasses.db_schema_version));

			/*Display the shell */
			createStatusLabel.setText(createStatusLabel.getText()+"                       ");//to make the label big enough.
			dialogShell.pack();
			dialogShell.open();

			closeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// When the button is pressed, close the child shell
					((Button)e.getSource()).getParent().getShell().close();
				}
			});

			dropButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createStatusLabel.setText("formatting deltaMassBase");
					boolean creationOk=false; 
					createStatusLabel.setText("formatting deltaMassBase");
					logger.info("DMBDialogHandler:now attempting to drop deltaMassBase");
					creationOk=DeltaMassBase.formatDatabase();//create and drop are the same ....
					if(creationOk){
						logger.info("formatting DB OK");
						createStatusLabel.setText("formatting DB OK");
					}
					else{
						logger.fatal("deltaMassBase formatting DB :Error.");
						createStatusLabel.setText("Formatting error. contact info@detectorvision.com");
					}
					updateValues();
				}
			});

			createButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean creationOk=false;
					createStatusLabel.setText("creating deltaMassBase");
					logger.info("Attempting to create deltaMassBase");
					TableItem[] selection = DBTable.getSelection();
					try{
						logger.info("trying to set db to configfile:"+selection[0].getText(1));
						if(selection[0].getText(6).startsWith("FATAL: database") && selection[0].getText(6).endsWith("does not exist")){
							creationOk=DeltaMassBase.createDatabase(selection[0].getText(2));
							createStatusLabel.setText("creationOK:"+creationOk+":");
						}
						else{
							logger.warn("refused to create database:"+selection[0].getText(6)+":selection[0].getText(1)");
						}
					}
					catch(Exception eee){
						DeltaMasses.logger.error("setDBButton error:"+eee.toString());
						DeltaMasses.logger.error("setDBButton error:"+eee);
					}
					
					
					//creationOk=DeltaMassBase.formatDatabase();
					if(creationOk){
						logger.info("deltaMassBase creation:OK");
						createStatusLabel.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
						createStatusLabel.setForeground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
						createStatusLabel.setText("DB creation: OK");
						DeltaMassBase.setDeltaMassBase(selection[0].getText(2));
						boolean formatDbOK=DeltaMassBase.formatDatabase();
					}
					else{
						logger.warn("DB not created");
						createStatusLabel.setText("DB not created");
					}
					updateValues();
				}
			});

			setDBButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean creationOk=false;
					createStatusLabel.setText("setting database");
					logger.info("setDBButton pressed");
					if(DBTable.getSelectionCount()<1){
						logger.info("no db selected, no db set");
						createStatusLabel.setText("No DB selected. DB not set.");
						return;
					}

					TableItem[] selection = DBTable.getSelection();
					for (int l_i = 0; l_i < selection.length; l_i++){
						try{
							logger.info("trying to set db to configfile:"+selection[0].getText(1));
							if(!selection[0].getText(6).equals("OK")){
								logger.info("cannot set database when connectionError not OK:"+selection[0].getText(6)+":");
							}
							DeltaMassBase.setDeltaMassBase(selection[0].getText(1));
							createStatusLabel.setText("database set to "+selection[0].getText(2));
						}
						catch(Exception eee){
							DeltaMasses.logger.error("setDBButton error:"+eee.toString());
							DeltaMasses.logger.error("setDBButton error:"+eee);
						}
						
						
					}
					updateValues();
				}
			});
			

		    editConfigButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean creationOk=false;
					createStatusLabel.setText("edit configuration file");
					logger.info("editConfigButton pressed");
					if(DBTable.getSelectionCount()<1){
						logger.info("no db selected, cannot configure");
						createStatusLabel.setText("No DB selected. Cannot configure.");
						return;
					}
					TableItem[] selection = DBTable.getSelection();
					for (int l_i = 0; l_i < selection.length; l_i++){
						try{
							logger.info("trying to configure configfile:"+selection[0].getText(1));
							File f = new File("config/"+selection[0].getText(1));
							ExternalFileHandler.open(f);
						}
						catch(Exception eee){
							DeltaMasses.logger.error("setDBButton error:"+eee.toString());
							DeltaMasses.logger.error("setDBButton error:"+eee);
						}
					}
					updateValues();
				}
			});
			
			
			
			calcNetButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean creationOk=false;
					PeptideNet pepnet = new PeptideNet();
					createStatusLabel.setText("calculating peptide nets");
					calcNetButton.setText("please wait");
					logger.info("calcNetButton:now attempting to create peptide nets");
					creationOk=pepnet.calcNet(DeltaMasses.isDiscoveryEdition);
					if(creationOk){
						logger.info("peptide net calculation:OK");
						createStatusLabel.setText("peptideNet ready");
					}
					else{
						logger.error("calcNet:FAILED");
						createStatusLabel.setText("Failed. Please check the log");
					}	
					calcNetButton.setText("ready");
					updateValues();
				}
			});


			vacuumButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createStatusLabel.setText("vacuuming deltaMassBase");
					boolean creationOk=false; 
					logger.info("starting to vacuum deltaMassBase");
					creationOk=DeltaMassBase.vacuumDataBase();
					if(creationOk){
						logger.info("vacuuming:OK");
						createStatusLabel.setText("vacuuming DB OK");
					}
					else{
						logger.error("deltaMassBase vacuuming:Error.");
						createStatusLabel.setText("Failure. contact info@detectorvision.com");
					}
					updateValues();
				}
			});


		} catch (XSWTException error) {
			logger.error("openDialogHandler: XSWTException:" + error.toString());
		}
	}
	void updateValues(){
		logger.info("updateValues:start");	
		////////////////////DBTable population start
		DeltaMasses.configuredDatabases=DeltaMassBase.getConfiguredDatabases();
		//DeltaMassBase.setDeltaMassBase("default");//FRANKE
		DBTable.clearAll();
		DBTable.removeAll();
		for(int i=0; i<DeltaMasses.configuredDatabases.size(); i++){
			final TableItem tableItem = new TableItem(DBTable, 0);
			boolean isActive=false;
			String isActiveText="";

			//this logic is not supposed to live here....
			String crap =DeltaMasses.myDatabaseInfo.filename.substring(7);
			System.out.println(DeltaMasses.configuredDatabases.get(i).filename);
			System.out.println(crap);
			if(crap.equals(DeltaMasses.configuredDatabases.get(i).filename)){
				isActive=true;
				isActiveText="X";
			}
			
			tableItem.setText(new String[] {
					isActiveText,
					""+DeltaMasses.configuredDatabases.get(i).filename,
					""+DeltaMasses.configuredDatabases.get(i).DELTAMASSBASE_NAME  ,
					""+DeltaMasses.configuredDatabases.get(i).HOST,
					""+DeltaMasses.configuredDatabases.get(i).PORT ,
					""+DeltaMasses.configuredDatabases.get(i).USER ,
					""+DeltaMasses.configuredDatabases.get(i).connectionError,
					""+DeltaMasses.configuredDatabases.get(i).db_schema_version
			});
			if(isActive){
				tableItem.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			}
			if(DeltaMasses.configuredDatabases.get(i).db_schema_version < 5 && DeltaMasses.configuredDatabases.get(i).connectionError.equals("OK") ){
				tableItem.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
			}
			else if(DeltaMasses.configuredDatabases.get(i).db_schema_version < 5 && !DeltaMasses.configuredDatabases.get(i).connectionError.equals("OK") ){
				tableItem.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			}
			if(DeltaMasses.configuredDatabases.get(i).couldBeCreated){
				tableItem.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_YELLOW));	
			}

			
		}		
		////////////////////DBTable population end

		if (!DeltaMassBase.isCreated()) {
			createStatusLabel.setBackground(this.dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
			createStatusLabel.setForeground(this.dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			createButton.setBackground(this.dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			return;
		}

		DMB_filenameLabel.setText(DeltaMasses.myDatabaseInfo.filename+"                   ");
		DMB_URLLabel.setText(DeltaMasses.myDatabaseInfo.URL+"                     ");
		DMB_DELTAMASSBASE_NAMELabel.setText(DeltaMasses.myDatabaseInfo.DELTAMASSBASE_NAME+"                           ");
		DMB_connectionErrorLabel.setText(DeltaMasses.myDatabaseInfo.connectionError+"                     ");

		if(!DeltaUtils.DMBspecnetIsUpToDate()){	
			DMBlastSpecnetLabel.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			DMBlastSpecnetLabel.setToolTipText("specnet is not up to date in database and needs to be calculated");
			calcNetButton.setEnabled(true);
			calcNetButton.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			calcNetButton.setText("calculate peptide nets");
		}
		else{
			DMBlastSpecnetLabel.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			DMBlastSpecnetLabel.setToolTipText("specnet is up to date in database.");
			//calcNetButton.setEnabled(false);
			calcNetButton.setBackground(DMBlastSpecnetLabel.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			calcNetButton.setText("nets are up to date");
			calcNetButton.setEnabled(true);
		}

		Connection conn = DeltaMassBase.getCleanConnection();

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
			logger.error("Cannot see any modifications. Datbase not created." +
					e1.getLocalizedMessage());
			createStatusLabel.setBackground(this.dialogShell.getDisplay().getSystemColor(SWT.COLOR_RED));
			createStatusLabel.setForeground(this.dialogShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			createStatusLabel.setText("please create the database");
			createButton.setText("create database");
			createButton.setFocus();
			createButton.setBackground(this.dialogShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			createButton.setToolTipText("click to create the deltaMassBase database structure");
			dbOk=false;
		}

		try {

			s = conn.createStatement();
			s.execute("SELECT count(*) FROM record");
			ResultSet rs = s.getResultSet();
			rs.next ();
			DMBRecordsStoredLabel.setText((""+  (rs.getInt(1))+"      "));//the trailing spaces are important.
			                                                              //if left out, the following Display.pack will only allow for 
			                                                              //initial size of this label

			s.execute("SELECT count(*) FROM spectrum;");
			rs = s.getResultSet();
			rs.next ();
			DMBRSpectraStoredLabel.setText(""+ (rs.getInt(1))+"         ");

			s.execute("SELECT count(*) FROM deltamass");
			rs = s.getResultSet();
			rs.next ();
			DMBDpdPairsStoredLabel.setText(""+ (rs.getInt(1))+"        ");

			s.execute("SELECT count(*) FROM specNet");
			rs = s.getResultSet();
			rs.next ();
			DMBPeptideNetsStoredLabel.setText((""+ (rs.getInt(1))+"          "));

			s.execute("SELECT count(*) FROM peptide");
			rs = s.getResultSet();
			rs.next ();
			DMBpeptidesStoredLabel.setText((""+(rs.getInt(1))+"           "));

			//s.execute("SELECT count(*) FROM experiment");
			//rs = s.getResultSet();
			//rs.next ();
			//DMBExperimentsStoredLabel.setText((""+(rs.getInt(1))));

			s.execute("SELECT db_schema_version,createdate,lastmodificationdate,lastspecnetdate FROM meta_db;");
			rs = s.getResultSet();
			rs.next ();
			DMBversionLabel.setText((""+ (rs.getInt(1))+"         "));
			DMBcreatedLabel.setText((""+ (rs.getTimestamp(2).toString())));
			DMBlastModificationLabel.setText((""+ (rs.getTimestamp(3).toString())));
			DMBlastSpecnetLabel.setText((""+ (rs.getTimestamp(4).toString())));

			conn.close();//frankp 20070320

		} catch (SQLException e1) {
			logger.error("updateValues:SQLException:" + e1.getLocalizedMessage());
		}
	}
}