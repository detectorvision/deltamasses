/* $Id: IcplDialogHandler.java 149 2008-03-30 22:29:09Z jari $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.MassUnit;
import com.detectorvision.massspectrometry.datacontrol.RecordLoader;
import com.detectorvision.utility.ExternalFileHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The IcplDialogHandler implements a simple open dialog to specify directory
 * peakfile and set some options to launch an ICPL quantatiation analysis
 * 
 * @author Frank Potthast
 */
public class IcplDialogHandler extends SelectionAdapter {

	//private Record records[];

	private Shell mainShell;
	private Map opendialogWidgets;
	private Shell dialogShell;
	private Composite optionsComposite;
	private Text peakfilenameText;
	private Spinner msValue;
	private Button closeButton, okButton, expanderButton, selectPeakfileButton, selectTandemfileButton;

	public double msAcc;
	public boolean tripleIcpl;
	public boolean useTxt;
	public boolean processAll;
	public boolean blockUsed;
	public String peakFile;

	private ProgressListener progressListener;
	private EventListener eventListener;	
	//private ArrayList<PeakFileLoaderItem> fileLoaders;

	// Logging with log4j
	static Logger logger = Logger.getLogger(IcplDialogHandler.class.getName());
	
	public IcplDialogHandler() {
		super();
		this.eventListener = eventListener;	
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
		//this.fileLoaders = new ArrayList<PeakFileLoaderItem>();

		/* Build the shell */
		try {
			/* Get the objects from the xswt-file and create references*/
			opendialogWidgets =  XSWT.create(this.getClass().getResourceAsStream("gui/ICPLDialog.xswt")).parse(dialogShell);
			closeButton = (Button)opendialogWidgets.get("closeButton");
			okButton =(Button)opendialogWidgets.get("okButton");
			okButton.setEnabled(false);
			selectPeakfileButton = (Button)opendialogWidgets.get("selectPeakfileButton");			
			selectTandemfileButton = (Button)opendialogWidgets.get("selectTandemfileButton");
			peakfilenameText = (Text)opendialogWidgets.get("peakfilenameText");
			optionsComposite = (Composite)opendialogWidgets.get("optionsComposite");
			msValue = (Spinner)opendialogWidgets.get("msValueSpinner");
			Button blockUsedCheckButton = (Button)opendialogWidgets.get("blockUsedCheckButton");
			blockUsedCheckButton.setSelection(true);

			logger.info("IcplDialogHandler:references created. All OK.");

			/*Display the shell */
			dialogShell.pack();
			dialogShell.open();

			okButton.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
					// When the button is pressed, close the child shell
					peakFile=peakfilenameText.getText();
					msValue = (Spinner)opendialogWidgets.get("msValueSpinner");
					double MAXDELTA=msValue.getSelection()/Math.pow(10, msValue.getDigits());
					if(MAXDELTA<=0){debuggDialog("mass accuracy = 0.00 [Dalton]?\nAre you kidding?\nWill use 0.01 [Dalton].\nHope that is fine.");msAcc=0.01;}
					Button icpl =(Button)opendialogWidgets.get("TripleIcplCheckButton");
					tripleIcpl=icpl.getSelection();
					Button txtCheckButton =(Button)opendialogWidgets.get("txtCheckButton");
					useTxt=txtCheckButton.getSelection();	
					Button allFileCheckButton =(Button)opendialogWidgets.get("allFileCheckButton");
					processAll=allFileCheckButton.getSelection();
					Button blockUsedCheckButton = (Button)opendialogWidgets.get("blockUsedCheckButton");
					blockUsed = blockUsedCheckButton.getSelection();

					if(tripleIcpl){informUserDialog("You have chosen the triple-ICPL option\nPlease note that this functionality\nis rather new and has not been massively tested.\nThanks for being careful.\nProgram continues after OK.");}

					spotret tmpSpotRet= new spotret("aaaaaaaaa",333);//TODO clean this up ... terrible !!!!
					spot2retention s2r= new spot2retention(tmpSpotRet);	

					IcplPairList IPL = new IcplPairList(peakFile,MAXDELTA,processAll,tripleIcpl,useTxt,blockUsed,s2r);
					File target = new File("../tmp/ICPL_tmp.csv");
					if(true){
						IPL.tsv2File(target, true);
					}

					((Button)e.getSource()).getParent().getShell().close();
				}
			});

			closeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// When the button is pressed, close the child shell
					//debuggDialog("close button pressed");
					((Button)e.getSource()).getParent().getShell().close();
				}
			});

			peakfilenameText.addModifyListener( new OkButtonGrayerAdapter(okButton));

			/*OK button function */
			//okButton.addSelectionListener( new OkButtonAdapter());

			//20061112 expanderButton.addSelectionListener( new ExpanderButtonAdapter(optionsComposite) );

			/* SelectFile function */
			selectPeakfileButton.addSelectionListener( new SelectPeakfileDialogAdapter(dialogShell, peakfilenameText));
			//selectTandemfileButton.addSelectionListener( new SelectTandemfileDialogAdapter(dialogShell, tandemfilenameText));

		} catch (XSWTException error) {
			logger.error("openDialogHandler: XSWTException:" + error.toString());
		}
	}


	public void setProgressListener(ProgressListener listener){
		this.progressListener = listener;
	}

	/**
	 * The SelectPeakfileDialog callback class opens a operating system specific
	 * fileselector dialog and returns the selected filename/filelocation.
	 * 
	 * @author Raphael Bosshard
	 */
	class SelectPeakfileDialogAdapter implements SelectionListener {

		Text filename;
		Shell parentShell;

		SelectPeakfileDialogAdapter(Shell parentShell, Text filename) {
			super();
			this.filename = filename;
			this.parentShell = parentShell;
		}

		/*
		 * Create the file selector dialog on button-click.
		 */
		public void widgetSelected(SelectionEvent event) {
			FileDialog fd = new FileDialog(parentShell, SWT.OPEN);
			String filename;
			fd.setText("Open Peakfile");
			fd.setFilterPath("~");
			filename = fd.open();
			if(filename != null){
				this.filename.setText(filename);
				logger.debug("IcplDialogHandler:widgetSelected:filename:"+filename);
			}
			//Read that frigging file ...
			//debuggDialog("opening file:"+filename);

		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	/*
	 * The SelectTandemfileDialog callback class opens a operating system specific
	 * fileselector dialog and returns the selected filename/filelocation.
	 * 
	 */
	class SelectTandemfileDialogAdapter implements SelectionListener {
		Text filename;
		Shell parentShell;

		SelectTandemfileDialogAdapter(Shell parentShell, Text filename) {
			super();
			this.filename = filename;
			this.parentShell = parentShell;
		}

		/* TODO Find out what kind of extensions tandem files have.
		 * Create the file selector dialog on button-click.
		 */
		public void widgetSelected(SelectionEvent event) {
			FileDialog fd = new FileDialog(dialogShell, SWT.OPEN);
			String filename;

			fd.setText("Open Tandemfile");
			fd.setFilterPath("~");
			String[] filterExt = { "*.xml" };
			fd.setFilterExtensions(filterExt);
			filename = fd.open();
			if(filename != null){
				peakfilenameText.setText(filename);
			}
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}


	/**
	 * The OkButtonGrayer callback class makes sure that the OK button is not available 
	 * for actions as long there is no file selected.
	 * 
	 * @author Raphael Bosshard
	 */
	class OkButtonGrayerAdapter implements ModifyListener{
		Button b;
		OkButtonGrayerAdapter(Button b){
			this.b = b;
		}

		public void modifyText(ModifyEvent event) {
			// Get the text in the textfield
			String text = ((Text)event.getSource()).getText();

			// disable if textfield is empty or given file does not exist
			if(text.equals("")){
				b.setEnabled(false);
			} else if ( !new File(text).exists() || new File(text).isDirectory() ){
				// disable if file does not exist or file is a directory entry
				b.setEnabled(false);
			} else {
				b.setEnabled(true);
				//STUPID fixme b.setEnabled(false);
			}

		}
	}
	/**
	 * The OkButtonAdapter waits for the OK button to be hit. After that, de input data is processed and the
	 * relevant files are read.
	 * This helper class also implements all necessary methods for the asynchron operations, preventing the gui from freezing.
	 * 
	 * @author Raphael Bosshard
	 */
	class OkButtonAdapter implements SelectionListener {

		class LoaderRunnable implements Runnable, ProgressListener  {
			String fileloaderClassname;
			String filename;
			LoaderRunnable(String filename, String fileloaderClassname){
				this.filename = filename;
				this.fileloaderClassname = fileloaderClassname;
			}

			public void run() {
				RecordLoader loader = null;
				debuggDialog("LoaderRunnabel::run");
				//AsyncEventListenerRunnable async = new AsyncEventListenerRunnable(Event.LOADBEGIN, eventListener);
				//mainShell.getDisplay().asyncExec(async);

				// load the algorithm class


				//async = new AsyncEventListenerRunnable(Event.LOADEND, eventListener);
				//mainShell.getDisplay().asyncExec(async);

				this.updateProgress(-1, "peaklist loaded");

			}

			public void updateProgress(int progress, String text) {
				AsyncUpdateRunnable async = new AsyncUpdateRunnable(progress, text, progressListener);
				//Display display = Display.getCurrent();
				if(mainShell.getDisplay() == null){
					//System.out.println("Display is null");
					return;
				}
				mainShell.getDisplay().asyncExec(async);
			}


			/*
			 *  Asynchron classes for gui-updates
			 */
			class AsyncUpdateRunnable implements Runnable{
				int progress;
				String text;
				ProgressListener listener;

				public AsyncUpdateRunnable(int progress, String text, ProgressListener listener){
					this.progress = progress;
					this.text = text;
					this.listener = listener;
				}

				public void run() {
					if(this.listener == null){
						return;
					}
					this.listener.updateProgress(this.progress, this.text);
				}	
			}

			class AsyncEventListenerRunnable implements Runnable{
				Event e; 
				EventListener listener;

				public AsyncEventListenerRunnable(Event e, EventListener listener){
					this.e = e;
					this.listener = listener;
				}

				public void run() {

					if(this.listener == null){
						return;
					}
					this.listener.updateEvent(e, null);
				}	
			}

		}


		public void widgetSelected(SelectionEvent arg0) {
			String fileloaderClassname = "DefaultRecordLoader";
			debuggDialog("widgetSelected:"+arg0.toString());
			//which of the available file loaders do we have to use?
			/*for(PeakFileLoaderItem item: fileLoaders){
				if(peakfilenameText.getText().endsWith(item.fileloaderExtension)){
					fileloaderClassname = item.fileloaderClassName;
					break;
				}
			}*/
			// When the button is pressed, close the child shell

			eventListener.updateEvent(Event.LOADBEGIN, null);
			dialogShell.setVisible(false);

			BusyIndicator.showWhile(mainShell.getDisplay(), new LoaderRunnable(peakfilenameText.getText(), fileloaderClassname));
			//new Thread(new LoaderRunnable(peakfilenameText.getText(), fileloaderClassname)).start();

			//eventListener.updateEvent(Event.LOADEND, null);
			//progressListener.updateProgress(-1, "peaklist loaded");
			dialogShell.dispose();
		}

		public void widgetDefaultSelected(SelectionEvent arg0) {


		}

	}


	/**
	 * The ExpanderButtonAdapter callback class implements a disclosure button/expander button
	 * which is not natively available on Microsoft Windows systems and therefore not included in SWT. 
	 * 
	 * @author Raphael Bosshard
	 * 
	 */
	class ExpanderButtonAdapter implements SelectionListener {
		Shell s;
		Control c;
		Boolean isVisible = false;

		GridData gridData;
		int expanderControlDesiredHeight;

		ExpanderButtonAdapter(Control c){

			this.c = c;
			this.s = c.getShell();

			this.gridData = (GridData)c.getLayoutData();

			this.expanderControlDesiredHeight = c.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
		}


		public void widgetSelected(SelectionEvent event) {
			Button expanderButton = (Button)event.getSource();

			if(isVisible){
				isVisible = false;
				expanderButton.setAlignment(SWT.RIGHT);
				gridData.heightHint = 0;
				//gridData.heightHint = this.expanderControlDesiredHeight;
				s.setSize(s.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			}
			else {
				isVisible = true;
				gridData.heightHint = this.expanderControlDesiredHeight;
				expanderButton.setAlignment(SWT.DOWN);
				s.setSize(s.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			}	
		}

		public void widgetDefaultSelected(SelectionEvent arg0) {

		}
	}

	class PeakFileLoaderItem {
		public String fileloaderClassName;
		public String fileloaderExtension;
	}

	private void errorDialog(String message){
		MessageBox box = new MessageBox(mainShell,
				SWT.OK
				| SWT.ICON_ERROR
				| SWT.APPLICATION_MODAL);

		box.setText("Error");
		box.setMessage(message);
		box.open();	
	}

	private void debuggDialog(String message){
		MessageBox box = new MessageBox(mainShell,
				SWT.OK
				| SWT.ICON_ERROR
				| SWT.APPLICATION_MODAL);

		box.setText("Debugg Info");
		box.setMessage(message);
		box.open();	
	}

	private void informUserDialog(String message){
		MessageBox box = new MessageBox(mainShell,
				SWT.OK
				| SWT.ICON_INFORMATION
				| SWT.APPLICATION_MODAL);

		box.setText("For your Information");
		box.setMessage(message);
		box.open();	
	}





	class spot2retention{
		public ArrayList<spotret> s2r =  new ArrayList<spotret>();

		public spot2retention(spotret input) {
			s2r.add(input);
			try {//TODO put this whole block into the constructor of s2R and call like 
				//spot2retention s2R= new spot2retention("../config/ICPL_spot2retention.csv");
				//read the spot to retention time file in config/ICPL_spot2retention.tsv
				FileReader spotFile;
				spotFile = new FileReader("../config/ICPL_spot2retention.tsv");
				BufferedReader buff = new BufferedReader(spotFile);
				String line;
				String dataRegex = "([A-Za-z0-9]+)\\s+([0-9.]+)";//frankp 20050922 new: \\s+ = one or several whitespace characters	
				Pattern pattern = Pattern.compile(dataRegex);
				while(true){
					line = buff.readLine();
					if(line == null){ // EOF reached. Boil out of the while-loop.
						break;
					}
					//System.out.println("debugg:" + line);
					Matcher matcher = pattern.matcher(line);
					if(matcher.matches()){
						spotret tmp = new spotret(matcher.group(1),Double.parseDouble(matcher.group(2)));
						//System.out.println("ret and spot: "+tmp.retention + " " + tmp.spot);
						s2r.add(tmp);
					}
				}
			} catch (FileNotFoundException e1) {
				logger.error("IcplDialogHandler:FileNotFoundException:"+e1.toString());
			} catch (IOException e2) {
				logger.info("IcplDialogHandler:IOException e2:"+e2.toString());
			}
		}
		public void addspot(spotret input){
			//System.out.print("addingspot:" + input.spot + " " + input.retention);
			s2r.add(input);
		}
		int length(){
			return(s2r.size());	
		}
		Double getRetention(String spotInO){
			String spotIn = spotInO.trim();
			spotIn.replaceAll("^[^A-Z]", "");
			//System.out.println("spotIn:"+spotIn);
			for(int i=0;i<s2r.size();i++){
				if(spotIn.equalsIgnoreCase( ((spotret)s2r.get(i)).spot  )){
					return( ((spotret)s2r.get(i)).retention  );
				}
			}
			return(-111.0);//error code
		}
		void printOut(){
			for(int i=0;i<s2r.size();i++){
				System.out.println(((spotret)s2r.get(i)).spot + " " + ((spotret)s2r.get(i)).retention);
			}
		}
	}
	class spotret{
		public String spot;
		public Double retention;
		public spotret(String spotti, double retti){
			this.spot=spotti;
			this.retention=retti;
		}
	}
	class spot{
		private String spotLabel;
		private double retention;
		private String spotFileName;

		spot(String label, double ret){
			this.retention=ret;
			this.spotLabel=label;
			this.spotFileName="";
		}
		spot(String label, double ret, String spotFile){
			this.retention=ret;
			this.spotLabel=label;
			this.spotFileName=spotFile;
		}
	}

	class IcplPairList{
		private ArrayList<IcplPair> IcplP = new ArrayList<IcplPair>();

		IcplPairList(){
			//stupid constructor ....
		}


		IcplPairList(String peakFile, double MAXDELTA,boolean processAll, boolean tripleIcpl, boolean useTxt, boolean blockUsed, spot2retention spot2R){
			String fileEnding=".mgf";
			if(useTxt){fileEnding=".txt";}
			ArrayList<String> fileList = new ArrayList<String>();

			if(processAll){//get all .mgf/.txt files in this directory
				//debuggDialog("processAll");
				//File inFile= new File(peakFile);

				try {
					//debuggDialog(peakFile);
					int cut=peakFile.lastIndexOf('\\');
					String dirString = peakFile.substring(0,cut+1);
					//debuggDialog("processing dirString:"+dirString);
					File dirFile= new File(dirString);
					if(dirFile.isDirectory()){
						String[] children = dirFile.list();
						for (int i=0; i<children.length; i++) {
							if(children[i].endsWith(fileEnding)){
								//check number of lines in file - if more than 500: exit
								FileReader tmpFileReader = new FileReader(dirString + File.separator + children[i]);		
								BufferedReader buff = new BufferedReader(tmpFileReader);
								String line;int countLines=0;
								while(true){
									line = buff.readLine();
									countLines++;
									if(line == null){ // EOF reached. Boil out of the while-loop.
										break;
									}
								}
								if(countLines>400){informUserDialog("The file "+children[i] +" has more than 400 lines. Not processing it because its strange.");}
								else{
									fileList.add(dirString + File.separator + children[i]);
								}
								//debuggDialog("adding to fileList:"+ dirString + File.separator + children[i]);

							}	
						}
					}
				} catch (FileNotFoundException e) {
					logger.error("IcplDialogHandler:FileNotFoundException:"+e.toString());
				} catch (IOException e) {
					logger.error("IcplDialogHandler:IoException:"+e.toString());
				} 
			}
			else{

				FileReader tmpFileReader;
				try {
					tmpFileReader = new FileReader(peakFile);

					BufferedReader buff = new BufferedReader(tmpFileReader);
					String line;int countLines=0;
					while(true){
						line = buff.readLine();
						countLines++;
						if(line == null){ // EOF reached. Boil out of the while-loop.
							break;
						}
					}
					if(countLines>400){
						logger.warn("IcplDialogHandler:The file " + peakFile + " has more than 400 lines. Not processing this because its strange ...");
						informUserDialog("The file "+peakFile +" has more than 400 lines. Not processing this because its strange ...");
					}
					else{
						fileList.add(peakFile);
					}
				} catch (FileNotFoundException e) {
					logger.error("IcplDialogHandler:FileNotFoundException:"+e.toString());
				} catch (IOException e) {
					logger.error("IcplDialogHandler:IOException while adding peakfile:" +
											 e.toString());
				}	
			}
			for(int loop=0;loop<fileList.size();loop++){
				//debuggDialog("icpl loop is processing file:"+fileList.get(loop));
				FileReader file;
				try {	
					ArrayList<MSMS> signals =  new ArrayList<MSMS>();
					file = new FileReader(fileList.get(loop));

					//BufferedWriter out = new BufferedWriter(new FileWriter(outfilename));

					BufferedReader buff = new BufferedReader(file);
					String line;
					String dataRegex = "([0-9.]+)\\s+([0-9.]+)";//frankp 20050922 new: \\s+ = one or several whitespace characters	
					String comRegex="COM=(.*)";
					Pattern comPattern = Pattern.compile(comRegex);
					Pattern dataPattern = Pattern.compile(dataRegex);
					String spotLabel="";//Label of the spot on the plate
					Double spotRetention=0.0;//Retention of spot on plate
					while(true){
						line = buff.readLine();
						if(line == null){ // EOF reached. Boil out of the while-loop.
							break;
						}
						Matcher dataMatcher = dataPattern.matcher(line);
						Matcher comMatcher = comPattern.matcher(line);
						if(dataMatcher.matches()){
							MSMS newMSMS = new MSMS();
							newMSMS.intensity = Double.parseDouble( dataMatcher.group(2) );
							newMSMS.massToCharge= Double.parseDouble( dataMatcher.group(1) );
							signals.add(newMSMS);
						}
						else if(comMatcher.matches()){
							String REGEX = ",";
							String INPUT = comMatcher.group(1);
							Pattern p = Pattern.compile(REGEX);
							String[] items = p.split(INPUT);
							for(int i=0;i<items.length;i++){
								String crap = items[i].trim();
								items[i]=crap;//TODO what a stupid construction
								String REGEX2 = ":";
								String INPUT2 = items[i];
								Pattern p2 = Pattern.compile(REGEX2);
								String[] items2 = p2.split(INPUT2);
								for(int j=0;j<items2.length;j++){
									String tmp = items2[j].trim();
									items2[j]=tmp;
									System.out.println("it:"+j+":"+items2[j]);
									if(items2[0].equals("Label")){
										spotLabel=items2[1].trim();  		
										spotRetention=spot2R.getRetention(spotLabel);
									}
								}
							}
						}
					}
					double dm[] = new double[3];
					dm[0]=1.9704;
					dm[1]=4.05;
					dm[2]=6.0204;
					double delta_ij;double rel_ij;double dist_ij;double dev_ij;
					double delta_ik;double rel_ik;double dist_ik;double dev_ik;
					double delta_jk;double rel_jk;double dist_jk;double dev_jk;
					boolean used[] = new boolean[signals.size()];//defaults to false
					if(tripleIcpl){
						for(int multiple=1;multiple<5;multiple++){

							for(int i=0;i<signals.size();i++){
								for(int j=i+1;j<signals.size();j++){
									delta_ij=Math.abs(signals.get(i).massToCharge-signals.get(j).massToCharge);
									if(delta_ij<multiple*dm[1] + MAXDELTA && delta_ij > multiple*dm[1]-MAXDELTA && !used[i] && ! used[j]){
										for(int k=j;k<signals.size();k++){
											delta_ik=Math.abs(signals.get(i).massToCharge-signals.get(k).massToCharge);
											if(delta_ik<multiple*dm[2]+MAXDELTA && delta_ik>multiple*dm[2]-MAXDELTA && !used[k]){
												rel_ik=signals.get(i).intensity/signals.get(k).intensity;  
												dist_ik=signals.get(i).massToCharge-signals.get(k).massToCharge;
												dev_ik=Math.abs(dist_ik)-multiple*dm[2];
												rel_ij=signals.get(i).intensity/signals.get(j).intensity;  
												dist_ij=signals.get(i).massToCharge-signals.get(j).massToCharge;
												dev_ij=Math.abs(dist_ij)-multiple*dm[1];  
												rel_jk=signals.get(j).intensity/signals.get(k).intensity;  
												dist_jk=signals.get(j).massToCharge-signals.get(k).massToCharge;
												dev_jk=Math.abs(dist_jk)-multiple*dm[0];
												if(blockUsed){used[i]=true;used[j]=true;used[k]=true;}
												spot tmpSpot = new spot(spotLabel,spotRetention);														
												IcplPair tmpIcpl = new IcplPair(tmpSpot, 3, rel_ij,rel_ik,rel_jk, dist_ij,dist_ik,dist_jk, dev_ij,dev_ik,dev_jk,signals.get(i).massToCharge,signals.get(j).massToCharge ,signals.get(k).massToCharge  , signals.get(i).intensity, signals.get(j).intensity, signals.get(k).intensity);
												IcplP.add(tmpIcpl);
												System.out.println("pair at llopp"+multiple);
											}
										}
									}
								}
							}
						}
					}
					for(int multiple=1;multiple<=5;multiple++){//distance 6 -for duplex AND triplex
						for(int i=0;i<signals.size();i++){
							for(int j=i;j<signals.size();j++){		
								delta_ij=Math.abs(signals.get(i).massToCharge-signals.get(j).massToCharge);	
								if(!used[i] && !used[j] && delta_ij<multiple*dm[2]+MAXDELTA && delta_ij > multiple*dm[2]-MAXDELTA){	
									rel_ij=signals.get(i).intensity/signals.get(j).intensity;
									dist_ij=signals.get(i).massToCharge-signals.get(j).massToCharge;
									dev_ij=Math.abs(dist_ij)-multiple*dm[2];
									if(blockUsed){used[i]=true;used[j]=true;}
									spot tmpSpot = new spot(spotLabel,spotRetention);
									IcplPair tmpIcpl = new IcplPair(tmpSpot, 1, rel_ij, dist_ij, dev_ij,signals.get(i).massToCharge , signals.get(j).massToCharge, signals.get(i).intensity, signals.get(i).intensity);
									IcplP.add(tmpIcpl);
								}
							}
						}
					}	
					if(tripleIcpl){
						for(int multiple=1;multiple<=5;multiple++){
							for(int i=0;i<signals.size();i++){
								for(int j=i;j<signals.size();j++){
									delta_ij=Math.abs(signals.get(i).massToCharge-signals.get(j).massToCharge);
									if(!used[i] && !used[j] && delta_ij<multiple*dm[1]+MAXDELTA && delta_ij > multiple*dm[1]-MAXDELTA){
										rel_ij=signals.get(i).intensity/signals.get(j).intensity;
										dist_ij=signals.get(i).massToCharge-signals.get(j).massToCharge;
										dev_ij=Math.abs(dist_ij)-multiple*dm[1];
										if(blockUsed){used[i]=true;used[j]=true;}											
										spot tmpSpot = new spot(spotLabel,spotRetention);
										IcplPair tmpIcpl = new IcplPair(tmpSpot, 0, rel_ij, dist_ij, dev_ij,signals.get(i).massToCharge , signals.get(j).massToCharge, signals.get(i).intensity, signals.get(j).intensity);
										IcplP.add(tmpIcpl);
									}
								}
							}
						}
					}
					if(tripleIcpl){
						for(int multiple=1;multiple<=5;multiple++){
							for(int i=0;i<signals.size();i++){
								for(int j=i;j<signals.size();j++){
									delta_ij=Math.abs(signals.get(i).massToCharge-signals.get(j).massToCharge);
									if(!used[i] && !used[j] && delta_ij<multiple*dm[0]+MAXDELTA && delta_ij > multiple*dm[0]-MAXDELTA){
										rel_ij=signals.get(i).intensity/signals.get(j).intensity;
										dist_ij=signals.get(i).massToCharge-signals.get(j).massToCharge;
										dev_ij=Math.abs(dist_ij)-multiple*dm[0];
										if(blockUsed){used[i]=true;used[j]=true;}
										spot tmpSpot = new spot(spotLabel,spotRetention);
										IcplPair tmpIcpl = new IcplPair(tmpSpot, 2, rel_ij, dist_ij, dev_ij,signals.get(i).massToCharge , signals.get(j).massToCharge, signals.get(i).intensity, signals.get(i).intensity);
										IcplP.add(tmpIcpl);
									}
								}
							}
						}
					}
				} catch (FileNotFoundException e1) {
					logger.error("Problem while trying to read\n" + peakFile +
											 "\nin IcplDialogHandler.java\n" + e1.toString());
					debuggDialog("Problem while trying to read\n"+peakFile+"\nin IcplDialogHandler.java\n"+e1.toString());
				} catch (IOException e2) {
					logger.error("IO problems while reading\n" + peakFile +
											 "\nin IcplDialogHandler.java");
					debuggDialog("IO problems while reading\n"+peakFile+"\nin IcplDialogHandler.java");
				}	
			}//end of file loop
		}

		IcplPairList(IcplPair inPair){
			this.IcplP.add(inPair);
		}
		void put(IcplPair inPair){
			this.IcplP.add(inPair);
		}
		int length(){
			return this.IcplP.size();
		}
		String printHeader(){
			return("spot\tretention\tmass1\tmass2\tmass3\t r1-2\tr1-3\tr2-3\td1-2\tdev1-2\td1-3\tdev1-3\td2-3\t dev2-3\t s1\ts2\ts3");

		}

		void printTsv(){
			System.out.println(this.printHeader());
			for(int i=0;i<IcplP.size();i++){
				System.out.println(IcplP.get(i).makeString());
			}
		}
		boolean tsv2File(File target, boolean launchEditor){
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(target));
				out.write(this.printHeader()+"\n");
				for(int i=0;i<IcplP.size();i++){
					out.write(IcplP.get(i).makeString()+"\n");
				}
				out.close();
			} catch (IOException e) {
				logger.error("tsv2File was unable to write to:"+target);
				debuggDialog("Unable to write to file:"+target+"\nIs it possible that you have this file open in some application?\nIf yes, please close it, thanks.");
				return(false);
			}

			if (launchEditor) {
				int retval=ExternalFileHandler.open(target);
				if (retval==3)
					System.err.println(".csv is not associated with any program on your computer");
				else if (retval!=0)
					logger.error("launchEditor: Exception occured");
			}

			return(true);
		}
	}

	class IcplPair {
		private spot spot;
		private double rel[]= new double[3];
		private double dist[]=new double[3];
		private double dev[]=new double[3];
		private double mass[]=new double[3];
		private double signal[]=new double[3];
		private int pairType=-1;//3=3-pair 0=1-2 pair 1=1-3 pair 2=2-3 pair;
		/**
		 * IcplPair by frank@detectorvision.com 20070124 
		 * Constructor for the dual three dual hit cases. The three-hit case has a separate constructor.
		 */
		IcplPair(spot spot, int pairType, double rel, double dist, double dev, double mass1, double mass2, double signal1, double signal2){
			this.spot=spot;
			this.pairType=pairType;
			this.rel[pairType]=rel;
			this.dist[pairType]=dist;
			this.dev[pairType]=dev;
			if(pairType==0){
				this.mass[0]=mass1;
				this.signal[0]=signal1;
				this.mass[1]=mass2;
				this.signal[1]=signal2;

			}
			else if(pairType==1){
				this.mass[0]=mass1;
				this.signal[0]=signal1;
				this.mass[2]=mass2;
				this.signal[2]=signal2;
			}
			else if(pairType==2){
				this.mass[1]=mass1;
				this.signal[1]=signal1;
				this.mass[2]=mass2;
				this.signal[2]=signal2;
			}
		}
		IcplPair(spot spot, int pairType, double rel0,double rel1,double rel2, double dist0 ,double dist1, double dist2,double dev0,double dev1,double dev2, double mass0,double mass1,double mass2, double signal0, double signal1,double signal2){
			this.spot=spot;
			this.pairType=pairType;
			if(pairType!=3){System.out.println("pairType Error:"+pairType);System.exit(1);}
			this.rel[0]=rel0;
			this.dist[0]=dist0;
			this.dev[0]=dev0;
			this.mass[0]=mass0;
			this.signal[0]=signal0;

			this.rel[1]=rel1;
			this.dist[1]=dist1;
			this.dev[1]=dev1;
			this.mass[1]=mass1;
			this.signal[1]=signal1;

			this.rel[2]=rel2;
			this.dist[2]=dist2;
			this.dev[2]=dev2;
			this.mass[2]=mass2;
			this.signal[2]=signal2;
		}
		String makeString(){
			if(pairType==0){//its a dual pair type 1-2

				String tmp= spot.spotLabel+"\t"+spot.retention+"\t"+mass[0]+"\t"+ mass[1]+
				"\t\t"+ rel[0]+"\t\t\t"+dist[0]+"\t"+dev[0]+
				"\t\t\t\t\t"+signal[0]+"\t"+signal[1]+"\t";
				return(tmp);
			}
			else if(pairType==1){//its a dual pair type 1-3
				String tmp= spot.spotLabel+"\t"+spot.retention+"\t"+mass[0]+"\t\t"+ mass[2]+
				"\t\t"+ rel[1]+"\t\t\t\t"+dist[1]+"\t"+dev[1]+
				"\t\t\t"+signal[0]+"\t\t"+signal[2];
				return(tmp);
			}
			else if(pairType==2){//its a dual pair type 2-3
				String tmp= spot.spotLabel+"\t"+spot.retention+"\t\t"+mass[1]+"\t"+ mass[2]+
				"\t\t\t"+ rel[2]+"\t\t\t\t\t"+dist[2]+"\t"+dev[2]+
				"\t\t"+signal[1]+"\t"+signal[2];
				return(tmp);
			}
			else if(pairType==3){
				String tmp=spot.spotLabel+"\t"+spot.retention+"\t"+mass[0]+"\t"+mass[1]+"\t"+ mass[2]+"\t"+
				rel[0]+"\t"+rel[1]+"\t"+rel[2]+"\t"+
				dist[0]+"\t"+dev[0]+"\t"+
				dist[1]+"\t"+dev[1]+"\t"+
				dist[2]+"\t"+dev[2]+"\t"+
				signal[0]+"\t"+signal[1]+"\t"+signal[2];
				return(tmp);
			}
			else{//serious error ...should never happen...
				System.out.println("abnormal value of pairType in method makeString:"+pairType);
				System.exit(1);
				return("");//stupid statement
			}
		}
	}
}