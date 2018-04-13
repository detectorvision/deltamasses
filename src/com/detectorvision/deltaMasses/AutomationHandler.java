/* $Id: AutomationHandler.java 379 2010-08-24 16:33:56Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.analyzationmanagement.AnalyzationControl;
import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.deltaMasses.statemachine.State;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.analyzation.AutomationContainer;
import com.detectorvision.massspectrometry.analyzation.BaseAlgorithm;
import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.analyzation.ScoreData;
import com.detectorvision.massspectrometry.analyzation.ScoreException;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.datacontrol.DefaultRecordLoader;
import com.detectorvision.massspectrometry.datacontrol.MascotXMLRecordLoader;
import com.detectorvision.massspectrometry.datacontrol.RecordLoader;
import com.detectorvision.massspectrometry.datacontrol.ValidateXML;
import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;

import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.ExternalFileHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Eventhandler for setting Mascot Server variables
 * @author frank@detectorvision.com
 */
public class AutomationHandler  extends SelectionAdapter implements ProgressListener, EventListener{
	private AutomationControl[] automationControl;
	private State state;

	private static UniModDatabase uniModDB;
	private static DeltaMassesScore algorithm;	
	private static AlgoParams algoParams;
	private static AutomationContainer autoContainer;
	private RobotHandler rh;
	// Logging with log4j
	static Logger logger = Logger.getLogger(AutomationHandler.class.getName());
	static Logger deltaMlogger = Logger.getLogger(DeltaMasses.class.getName());

	/** 
	 * 
	 * @param none
	 */
	public AutomationHandler(){
		this.automationControl = new AutomationControl[1];
		algoParams= new AlgoParams();
		algoParams.getDefault();
	}

	/**
	 * Event method, open MascotServer.xswt
	 * @param e eventdata.
	 */ 
	public void widgetSelected(SelectionEvent e){

		// get the mainshell
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();

		Map openWidgets = null;
		final Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/AutomationDialog.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			deltaMlogger.error("XSWTException: " + error.getLocalizedMessage());
		}
		dialogShell.pack();
		deltaMlogger.debug("AutomationHandler starting");
		logger.debug("AutomationHandler starting");


		this.state = State.NONE;
		automationControl = new AutomationControl[1];
		try {
			AutomationHandler.uniModDB = new UniModDatabase(DeltaMasses.preferences.getUniModFile());
		} 
		catch (IOException e3) {
			logger.fatal("IOException whilte loading unimod:" +
									 e3.getLocalizedMessage());
			System.exit(1);
		}
		deltaMlogger.debug("loaded unimod.xml file");
		Listener listener = new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
			    int style = SWT.OK | SWT.CANCEL | SWT.APPLICATION_MODAL;
	            MessageBox box = new MessageBox(dialogShell, style);
	            box.setMessage("Close from automation analysis ?");
	            event.doit = box.open() == SWT.OK;
			}
	    };
	    
		dialogShell.addListener(SWT.Close, listener);
		    dialogShell.addListener(SWT.Dispose, new Listener() {
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					rh = null;
				}
	    });


		// objectreferences
		Button testButton = (Button)openWidgets.get("testButton");
		final Button saveButton = (Button)openWidgets.get("saveButton");
		final Button startButton = (Button)openWidgets.get("startButton");
		final Button stopButton = (Button)openWidgets.get("stopButton");
		final Button showButton = (Button)openWidgets.get("showButton");
		final Button getButton = (Button)openWidgets.get("getButton");
		final Button showMgfButton = (Button)openWidgets.get("showMgfButton");
		final Button showMascotButton = (Button)openWidgets.get("showMascotButton");
		final Button cancelButton = (Button)openWidgets.get("cancelButton");
		saveButton.setEnabled(false);
		showButton.setEnabled(true);
		showMascotButton.setEnabled(true);
		cancelButton.setEnabled(false);
		final Label  mascotHttpLabel  = (Label)openWidgets.get("mascotHttpLabel");
		final Label comLabel  = (Label)openWidgets.get("comLabel");
		final Label totalLabelState  = (Label)openWidgets.get("totalLabelState");
		final Label importLabelState = (Label)openWidgets.get("importLabelState");
		final Label numMascotDatFilesLabel = (Label)openWidgets.get("numMascotDatFiles");
		final Label mascotXmlToProcessLabel = (Label)openWidgets.get("mascotXmlToProcess");
		final Label mgfToProcessLabel = (Label)openWidgets.get("mgfToProcess");
		final Label recordLabelState = (Label)openWidgets.get("recordLabelState");
		final Text httpText=(Text)openWidgets.get("httpText");
		final ProgressBar importProgressBar = (ProgressBar)openWidgets.get("importProgressBar");
		final ProgressBar recordProgressBar = (ProgressBar)openWidgets.get("recordProgressBar");
		final ProgressBar totalProgressBar = (ProgressBar)openWidgets.get("totalProgressBar");
		httpText.setText(checkMascotServerTxt());
		
		
		EventListener eListener=null;
		ProgressListener pListener = new ProgressListener() {
			public void updateProgress(int progress, String statusText) {
				logger.debug("text: " + statusText + " int: " + progress);
				totalLabelState.setText(statusText);
				if(statusText.equals("automation run completed")){
					cancelButton.setEnabled(true);
					recordLabelState.setText("");
					stopButton.setEnabled(false);
				}
				if (progress >= 0 && progress <= 100)
					totalProgressBar.setSelection(progress);
				else
					logger.warn("totalProgressBar:pListener send wrong progress: " +
											progress);
			}
		};
		
		autoContainer = new AutomationContainer();
		//automationControl[0] = new AutomationControl(autoContainer.FilesToProcess,eListener, pListener,algoParams);
		automationControl[0] = new AutomationControl(autoContainer.FilesToProcess,eListener,pListener,algoParams);
		
		try{
		if(autoContainer.getNumMgf() + autoContainer.getNumMascotXml() == 0){
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
			recordLabelState.setText("nothing to process");
		}
		else{
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
		}catch(Exception ex){
			logger.error("automationHandler:"+ex.toString());
			return;
		}

		numMascotDatFilesLabel.setText(""+ (int)(autoContainer.getNumDat()-autoContainer.getNumDatAtLocalComputer()));

		if(autoContainer.getNumDat() - autoContainer.getNumDatAtLocalComputer()==0){
			getButton.setEnabled(false);			
		}

		mascotXmlToProcessLabel.setText(""+autoContainer.getNumMascotXml());
		mgfToProcessLabel.setText(""+autoContainer.getNumMgf());


		//check how many of the dat files to be processed we allready have locally....
		int numImportedFiles=autoContainer.getNumDatAtLocalComputer();
		int numImportedPercent=0;
		importLabelState.setText(""+numImportedFiles +" of "+numImportedFiles+" imported");
		numImportedPercent=100*(int)((double)numImportedFiles/(double)autoContainer.getNumDat());
		importProgressBar.setSelection(numImportedPercent);
		if(numImportedPercent==100){getButton.setEnabled(false);}

		SelectionAdapter showDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if (ExternalFileHandler.open("automation/mascot/searches.log.txt")==1)
					logger.warn("No file searches.log.txt");
			}
		};
		showButton.addSelectionListener(showDialogEvent);


		SelectionAdapter showMgfDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				Process p;
				String LogFile="automation/mgf/data/";
				try {
					java.io.File f = new java.io.File(LogFile);
					logger.info("trying to open "+f.getAbsoluteFile().toString());
					p= Runtime.getRuntime().exec(new String[] { "explorer.exe", f.getAbsoluteFile().toString() });
					InputStream stdOut = p.getInputStream();
				} catch (IOException e1) {
					logger.error("Cannot open automation.mgf.data directory: " +
											 LogFile + " : " + e1.getLocalizedMessage());
				}
			}
		};
		showMgfButton.addSelectionListener(showMgfDialogEvent);


		SelectionAdapter showMascotDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				Process p;
				String LogFile="automation/mascot/data/";
				try {
					java.io.File f = new java.io.File(LogFile);
					logger.info("trying to open "+f.getAbsoluteFile().toString());
					p= Runtime.getRuntime().exec(new String[] { "explorer.exe", f.getAbsoluteFile().toString() });
					InputStream stdOut = p.getInputStream();
				} catch (IOException e1) {
					logger.error("Cannot open automation.mascot.data directory: " +
											 LogFile + " : " + e1.getLocalizedMessage());
				}
			}
		};
		showMascotButton.addSelectionListener(showMascotDialogEvent);

		SelectionAdapter closeDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				rh = null;
				dialogComposite.getShell().close();
				
			}
		};
		cancelButton.addSelectionListener(closeDialogEvent);

		SelectionAdapter getMascotXmlEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){	
				importLabelState.setText("processing");
				autoContainer.refresh(); 
				int numImportedFiles=autoContainer.getNumDatAtLocalComputer();
				importLabelState.setText(""+numImportedFiles +" of "+autoContainer.getNumDat()+" imported");
				int numImportedPercent=100*(int)((double)numImportedFiles/(double)autoContainer.FilesToProcess.size());
				importProgressBar.setSelection(numImportedPercent);

				for(int loop=0;loop<autoContainer.FilesToProcess.size();loop++){
					if(autoContainer.FilesToProcess.get(loop).endsWith(".dat")){
						logger.debug("Franke, now in dat container ...");
						String mascotExportParams=getMascotExportString();
						String URLString="";
						URL url=null;
						try {						
							String mascotFilePath="/cgi/export_dat.pl?file=";
							URLString = ""+DeltaMasses.mascotHttp+mascotFilePath+autoContainer.FilesToProcess.get(loop)+mascotExportParams;
							logger.info("importing mascot xml:URLString: " + URLString);
							url = new URL(URLString);
						} catch (MalformedURLException e1) {
							logger.error("URL: " + URLString);
							logger.error("AutomationHandler:MalformedURL Exception:" +
													 e1.getLocalizedMessage());
							return;
						}
						logger.debug("Fetching: " + url.toString());

						String inputLine;
						try {
							BufferedReader in = new BufferedReader(
									new InputStreamReader(
											url.openStream()));
							String outFile=autoContainer.FilesToProcess.get(loop)+ ".xml";
							outFile = outFile.replaceFirst(".*/", "");
							String bareFile=outFile;
							importLabelState.setText("fetching:"+outFile);
							outFile = "automation/mascot/data/" + outFile;
							logger.info("Writing to: " + outFile);
							BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
							while ((inputLine = in.readLine()) != null){
								out.write(inputLine);
								logger.debug(inputLine);
							}
							out.close();
							in.close(); 
							logger.info("Validating against mascot_search_results_2.xsd: " +
													outFile);
							
							//if the received XML is invalid: remove it ...
							boolean isValidXML=ValidateXML.validateMascot(outFile);
							if(!isValidXML){
								importLabelState.setText("invalid XML format for :"+bareFile);
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
								logger.warn("getMascotXMLEvent:invalid XML: " + outFile +
														"...removing it ...");
								File tmpFile = new File(outFile);
								tmpFile.delete();
							}
							else{
								logger.info("XML validation ok for: " + bareFile);
							}

							int prog=(int) (100.0* (double)(loop+1.0)/(double)autoContainer.getNumDat());
							importProgressBar.setSelection(prog);
							logger.debug("Finished getting .mascot.xml: " + outFile);
						} catch (IOException e2) {
							logger.error("IOException:urlling .mascot.xml" +
													 e2.getLocalizedMessage());
						}
					}
				}
				logger.info("Ready with fetching .mascot.xml records");
				importLabelState.setText("http import ready");
				numMascotDatFilesLabel.setText("0");
				getButton.setEnabled(false);
				autoContainer.refresh();
			}
		};
		getButton.addSelectionListener(getMascotXmlEvent);

		SelectionAdapter actionAfterStart = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				cancelButton.setEnabled(false);
				recordLabelState.setText("automation started");
			}
		};
		startButton.addSelectionListener(actionAfterStart);
		startButton.addSelectionListener(rh = new RobotHandler(automationControl, autoContainer.FilesToProcess,algoParams));
		SelectionAdapter actionAfterStop = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				autoContainer.FilesToProcess = null;
				stopButton.setEnabled(false);
				startButton.setEnabled(true);
				cancelButton.setEnabled(false);
				if(rh != null)
					rh.automationControl[0].stopAlgorithm();
				recordLabelState.setText("automation stopped");
			}
		};
		stopButton.addSelectionListener(actionAfterStop);
		

		SelectionAdapter saveEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				final Button button = (Button)e.getSource();
				boolean saveOK=true;
				//save to ../automation/mascot/mascot.server.txt

				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("../automation/mascot/mascot.server.txt"));
					out.write(httpText.getText());
					out.close();
				} catch (IOException ex) {
					logger.error("mascot.server.txt:problem: " + ex.getLocalizedMessage());
					saveOK=false;
				}

				if(saveOK){
					deltaMlogger.info("saveEvent:server set to: " + httpText.getText());
					logger.info("saveEvent:server set to: " + httpText.getText());
					comLabel.setText("saved");
				}
				else{
					comLabel.setText("could not store mascot.server.txt");
				}
			}
		};
		saveButton.addSelectionListener(saveEvent);

		
		
		SelectionAdapter testEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				comLabel.setText("Testing communication");
				String tmpURL = httpText.getText();
				boolean goahead=true;
				if(tmpURL.length()<10){
					goahead=false;
					saveButton.setEnabled(false);
					comLabel.setText("http address too short");
					httpText.setText("http://"); 
				}
				URL url=null;
				if(goahead){
					try {
						url = new URL(tmpURL);
					} catch (MalformedURLException e2) {
						logger.error("url problems\n");
						logger.error(e2.getLocalizedMessage());
						goahead=false;
						saveButton.setEnabled(false);
						httpText.setText("http://");
						comLabel.setText("Malformed http address");
					}
					if(goahead){
						try {
							URLConnection conn = url.openConnection();
							String type = conn.getContentType();
							logger.debug(conn.getContent().toString());
							if(type != null){
								saveButton.setEnabled(true);
								comLabel.setText("Looking good - save if OK for you");
								;}
							else{ 
								saveButton.setEnabled(false);
								httpText.setText("http://");
								comLabel.setText("unable to open connection");
								logger.warn("unable to connect to: " + DeltaMasses.mascotHttp);
							}
						} catch (IOException e1) {
							logger.warn("URL connection issues: " + e1.getLocalizedMessage());
							httpText.setText("http://");
							comLabel.setText("Input Output anomalities on http");
							saveButton.setEnabled(false);
						}
					}
				}
			}
		};
		testButton.addSelectionListener(testEvent);

		dialogShell.open();	
		URL url=null;

		try {
			url = new URL(DeltaMasses.mascotHttp);
		} catch (MalformedURLException e2) {
			logger.error("URL problems\n"+e2.getLocalizedMessage());
		}
		comLabel.setText("url:"+DeltaMasses.mascotHttp);
		logger.info("Trying " + url.toString());
		try {
			URLConnection conn = url.openConnection();
			String type = conn.getContentType();
			logger.debug(conn.getContent().toString());
			if(type != null)comLabel.setText("communication OK");
			else{ 
				comLabel.setText("cannot connect");
				logger.warn("Unable to connect to: " + DeltaMasses.mascotHttp);
			}
		} catch (IOException e1) {
			comLabel.setText("cannot connect to URL");
			logger.error("URL connection issues: " + url.toString() + " : " +
									 e1.getLocalizedMessage());
		}
	}

	static boolean mascotProcessXml(String outFile){
		DeltaMassesScore algorithm;
		Preferences preferences=null;
		UniModDatabase uniModDB=null;
		try {
			preferences = new Preferences("config/preferences.xml");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		Record record = null;
		ArrayList<SpectraPair> specPair = new ArrayList<SpectraPair>();
//		include external java archive
		URLClassLoader urlClassLoader;
		URL[] externalJars = new URL[1];
		try {
			externalJars[0] = new File("lib/algorithms.jar").toURI().toURL();
		} catch (MalformedURLException e1) {
			deltaMlogger.error("Malformed URL exception while loading algorithms.jar: " +
									 e1.getLocalizedMessage());
			logger.error("Malformed URL exception while loading algorithms.jar: " +
									 e1.getLocalizedMessage());
		}
		urlClassLoader = new URLClassLoader(externalJars);
		// load the algorithm class
		try {
			algorithm = (DeltaMassesScore)urlClassLoader.loadClass(preferences.getAlgorithmClass()).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			uniModDB = new UniModDatabase(preferences.getUniModFile());
		} 
		catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
		logger.debug("loaded unimod.xml file");

		if (logger.isDebugEnabled()) {
			logger.debug("testing connection to deltaMassBase");
			if (DeltaMassBase.validConnection())
				logger.debug("Connection to deltaMassBase OK");
			else
				logger.debug("Connection to deltaMassBase not established");
		}

		try {
			
			ProgressListener uselessListener=null;
			logger.debug("loading mascot record:"+outFile);
			if(outFile.endsWith(".xml")){
				deltaMlogger.info("AutomationHandler:loading xml: " + outFile);
				record=MascotXMLRecordLoader.loadMascotRecord(outFile,uselessListener);
			}
			else if(outFile.endsWith(".mgf")){
				deltaMlogger.info("AutomationHandler:loading .mgf: " + outFile);
				DefaultRecordLoader drl = new DefaultRecordLoader();
				record=drl.loadRecord(outFile,uselessListener);
				drl=null;
			}
			else{
				deltaMlogger.error("SYSTEM_ERROR:no proper ending of outfile: " +
													 outFile);
				return false;		
			}
			deltaMlogger.info("AutomationHandler:back from loading");

		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("Loaded:"+record.getFileName());

		//Analyze ---------------------------------------------------------
		ScoreData data = null;
		BaseAlgorithm baseAlgo = new BaseAlgorithm();
		try{
			ArrayList<Modification> modList = new ArrayList<Modification>();
			modList=uniModDB.getModifications();
			logger.debug("Starting to analyze:"+record.getFileName());
			com.detectorvision.massspectrometry.analyzation.ProgressListener listener = null; 	
			record.setMsmsPrecision((float) 0.4);
			record.setMsPrecision((float) 0.01);
			algoParams.isDiscoveryEdition=DeltaMasses.isDiscoveryEdition;
			algoParams.setProgress=false;//important - otherwise the BaseAlgorithm crashes. 
			//TODO fix above shit
			data= baseAlgo.analyze(record, modList, listener,algoParams);

			ArrayList<SpectraPair> specPairList = new ArrayList<SpectraPair>();
			logger.debug("pairs found:"+data.spectraPairList.size());
			for(int i=0;i<data.spectraPairList.size();i++){
				specPairList.add(data.spectraPairList.get(i));
			}

			logger.debug("baseAlgo returned");
			record.DMBstoreRecord(record, specPairList,true);
			data.spectraPairList=null;
			data=null;
		} catch (ScoreException e) {
			e.printStackTrace();
		}
		return(false);
	}

	public static String getMascotExportString(){
		String mascotExportParams="&REPTYPE=export&_sigthreshold=0.05&REPORT=AUTO&_mudpit=1";
		mascotExportParams +=     "&_sigthreshold=0.05";
		mascotExportParams +=     "&REPORT=AUTO";
		mascotExportParams +=     "&_mudpit=1";
		mascotExportParams +=     "&_ignoreionsscorebelow=0.1";
		mascotExportParams +=     "&show_same_sets=1";
		mascotExportParams +=     "&_showsubsets=1";
		mascotExportParams +=     "&_requireboldred=1";

		mascotExportParams +=     "&do_export=1";
		mascotExportParams +=     "&prot_hit_num=1";
		mascotExportParams +=     "&prot_acc=1";
		mascotExportParams +=     "&pep_query=1";
		mascotExportParams +=     "&pep_exp_mz=1";
		mascotExportParams +=     "&export_format=XML";

		mascotExportParams +=     "&show_header=1";
		mascotExportParams +=     "&show_params=1";
		mascotExportParams +=     "&show_format=1";
		mascotExportParams +=     "&show_masses=1";
		mascotExportParams +=     "&show_unassigned=1";
		mascotExportParams +=     "&show_queries=1";

		mascotExportParams +=     "&prot_score=1";
		mascotExportParams +=     "&prot_desc=1";
		mascotExportParams +=     "&prot_mass=1";
		mascotExportParams +=     "&prot_matches=1";

		mascotExportParams +=     "&pep_exp_mr=1";
		mascotExportParams +=     "&pep_exp_z=1";
		mascotExportParams +=     "&pep_calc_mr=1";
		mascotExportParams +=     "&pep_delta=1";
		mascotExportParams +=     "&pep_start=1";
		mascotExportParams +=     "&pep_end=1";
		mascotExportParams +=     "&pep_miss=1";
		mascotExportParams +=     "&pep_score=1";
		mascotExportParams +=     "&pep_homol=1";
		mascotExportParams +=     "&pep_ident=1";
		mascotExportParams +=     "&pep_expect=1";
		mascotExportParams +=     "&pep_rank=1";
		mascotExportParams +=     "&pep_seq=1";
		mascotExportParams +=     "&pep_frrame=1";
		mascotExportParams +=     "&pep_var_mod=1";
		return(mascotExportParams);
	}

	public String checkMascotServerTxt(){
		boolean gotMascot=false;
		String returnString="";
		try {
			BufferedReader in = new BufferedReader(new FileReader("../automation/mascot/mascot.server.txt"));
			String str;
			while ((str = in.readLine()) != null) {
				if(str.startsWith("http:")){
					gotMascot=true;
					String tmp =str.trim();
					returnString=tmp;
					DeltaMasses.mascotHttp=tmp;
				}
			}
			in.close();
		} catch (IOException ex) {
			logger.warn("Could not find mascot.server.txt. Will try to write it now.");
			try {
				File tmp = new File("../automation/mascot/mascot.server.txt");
				if(!tmp.exists()){
					BufferedWriter out = new BufferedWriter(new FileWriter("../automation/mascot/mascot.server.txt"));
					out.write("http://www.matrixscience.com");
					out.close();
				}
				else{
					logger.warn("Could neither read nor write automtion/mascot/mascot.server.txt");
					DeltaMasses.mascotHttp="http://www.matrixscience.com";
					returnString="http://www.matrixscience.com";				
				}

			} catch (IOException ex2) {
				logger.error("mascot.server.txt:could not create: " +
										 ex.getLocalizedMessage());
			}	
		}
		return returnString;
	}

	public void updateProgress(int progress, String text) {
		// get the instances of the widgets
		//ProgressBar progressBar = (ProgressBar)this.openwidgets.get("progressBar");

		logger.info("yeeeeaaaahhh:UpdateProgress:text="+text+"   progress="+progress);

		// print state
		//statusLabel.setText(text);
		//if(progress >= 0 && progress <= 100) progressBar.setSelection(progress);
		//else progressBar.setSelection(0);

	}

	public void updateEvent(Event e, Object data) {
		// TODO Auto-generated method stub
		logger.info("funny event:"+e.toString());
	}
}
