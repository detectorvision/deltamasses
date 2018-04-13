/* $Id: OpenDialogHandler.java 309 2010-05-16 09:12:18Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.datacontrol.RecordLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.regexp.internal.recompile;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * The OpenDialogHandler implements a simple open dialog to specify a
 * peakfile and set some options.
 * 
 * @author Raphael Bosshard
 */
public class OpenDialogHandler extends SelectionAdapter {
	
	private Record records[];
	private Shell mainShell;
	private Map opendialogWidgets;
	private Shell dialogShell;
	private Composite optionsComposite;
	private Text peakfilenameText;
	//private Spinner msValue, msmsValue;
	//private Combo msValueUnit, msmsValueUnit;
	private Button closeButton, okButton, expanderButton, selectPeakfileButton, selectTandemfileButton;
	private ProgressListener progressListener;
	private EventListener eventListener;
	private URLClassLoader urlClassLoader;
	private ArrayList<PeakFileLoaderItem> fileLoaders;
	private SAXBuilder builder;
	private Document doc;

	// Logging with log4j
	static Logger logger = Logger.getLogger(OpenDialogHandler.class.getName());


	public OpenDialogHandler(Record records[], EventListener eventListener) {
		super();	
		this.records = records;
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
		this.fileLoaders = new ArrayList<PeakFileLoaderItem>();
		
		
		/* Build the shell */
		try {
			/* Get the objects from the xswt-file and create references*/
			opendialogWidgets =  XSWT.create(this.getClass().getResourceAsStream("gui/OpenDialog.xswt")).parse(dialogShell);
			//20061112expanderButton = (Button)opendialogWidgets.get("expanderButton");
			closeButton = (Button)opendialogWidgets.get("closeButton");
			okButton =(Button)opendialogWidgets.get("okButton");
			selectPeakfileButton = (Button)opendialogWidgets.get("selectPeakfileButton");			
			selectTandemfileButton = (Button)opendialogWidgets.get("selectTandemfileButton");
			peakfilenameText = (Text)opendialogWidgets.get("peakfilenameText");
			//20061111 tandemfilenameText = (Text)opendialogWidgets.get("tandemfilenameText");
			optionsComposite = (Composite)opendialogWidgets.get("optionsComposite");
			
			//msValue = (Spinner)opendialogWidgets.get("msValueSpinner");
			//msmsValue = (Spinner)opendialogWidgets.get("msmsValueSpinner");
			
			//msValueUnit = (Combo)opendialogWidgets.get("msValueUnitCombo");
			//msmsValueUnit = (Combo)opendialogWidgets.get("msmsValueUnitCombo");
			
			try {
				logger.info("preparing external JARs");
				prepareExternalJARs();
				logger.info("prepared external JARs");
				getFileloaderImplementations();
				
			} catch (JDOMException e1) {
				errorDialog("Error in lib/peakfileloaders.jar. Please validate your configuration and re-install if necessary.");
				logger.error("IOException:" + e1.getMessage());
			} catch (IOException e1) {
				errorDialog("Error in classes.xml. Please validate your configuration and re-install if necessary.");
				logger.error("IOException:" + e1.getMessage());
			}
			
			logger.info("OpenDialogHandler:got all fileloaders");
			okButton.setEnabled(false);
			
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
			
			peakfilenameText.addModifyListener( new OkButtonGrayerAdapter(okButton));
			
			/*OK button function */
			okButton.addSelectionListener( new OkButtonAdapter());
			
			//20061112 expanderButton.addSelectionListener( new ExpanderButtonAdapter(optionsComposite) );
			
			/* SelectFile function */
			selectPeakfileButton.addSelectionListener( new SelectPeakfileDialogAdapter(dialogShell, peakfilenameText));
			//selectTandemfileButton.addSelectionListener( new SelectTandemfileDialogAdapter(dialogShell, tandemfilenameText));
			
		} catch (XSWTException error) {
			logger.error("XSWTException:" + error.toString());
		}
	}
	
	public void setProgressListener(ProgressListener listener){
		this.progressListener = listener;
	}
	
	private void prepareExternalJARs() throws JDOMException, IOException{
		// include external java archive
		this.builder = new SAXBuilder();
		this.doc = this.builder.build(ClassLoader.getSystemResourceAsStream("com/detectorvision/massspectrometry/datacontrol/fileloader_classes.xml"));
	}
	
	private void getFileloaderImplementations() throws IOException{
		// get the file's root element
		Element deltamasses = this.doc.getRootElement();
		// get a list of settings elements
		List fileloadersList = deltamasses.getChildren("loader");
		
		if(fileloadersList == null || fileloadersList.size() == 0){
			logger.error("OpenDialogHandler: No preferences data available!");
			throw new IOException("No preferences data available!");
		}
		// iterate thru the list an save the preferences
		for(int i=0; i<fileloadersList.size(); i++){
			// get the setting element
			Element setting = (Element) fileloadersList.get(i);
			
			// get the preference
			Element name = setting.getChild("name");
			Element extension = setting.getChild("extension");
			if(name == null || extension == null)
				throw new IOException("Incorrect preference values.");
			
			PeakFileLoaderItem fileloaderitem = new PeakFileLoaderItem();
			fileloaderitem.fileloaderClassName = name.getText();
			fileloaderitem.fileloaderExtension = extension.getText();
			this.fileLoaders.add(fileloaderitem);
			logger.info("Got fileloader:" + fileloaderitem.fileloaderClassName);
		}
		
		// store the preference
		/*if(name.getTextTrim().equals(Preferences.PREF_FILE_UNIMOD)){
		 this.uniModFile = value.getTextTrim();
		 }*/
		
		// store algorythm archive
		/*else if(name.getTextTrim().equals(Preferences.PREF_CLASS_ALGORITHM)){
		 this.algorithmClass = value.getTextTrim();
		 }*/
		
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
			String[] filterExt = new String[fileLoaders.size()];
			int i = 0;
			for(PeakFileLoaderItem item: fileLoaders){
				filterExt[i] = new String("*." + item.fileloaderExtension);
				i++;
			}

			fd.setText("Open Peakfile");
			fd.setFilterPath("~");
			//String[] filterExt = { "*.mgf" };
			fd.setFilterExtensions(filterExt);
			filename = fd.open();
			if(filename != null){
				this.filename.setText(filename);
				logger.info("OpenDialogHandler:widgetSelected:filename:"+filename);
			}
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
				
				AsyncEventListenerRunnable async = new AsyncEventListenerRunnable(Event.LOADBEGIN, eventListener);
				mainShell.getDisplay().asyncExec(async);
				
				// load the algorithm class
				try {
					ClassLoader cl=ClassLoader.getSystemClassLoader();
					loader = (RecordLoader)cl.loadClass(fileloaderClassname).newInstance();
					loader.setProgressListener(this);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				try {
					records[0] = new Record(filename, loader,progressListener);
					
				} catch (IOException e) {
					logger.error("IOException:" + e.toString());
					errorDialog("The peakfile could not be read: " + e.getMessage());
				}
				//CHIASSO FATAL COMMENT NEXT TWO LINES
				async = new AsyncEventListenerRunnable(Event.LOADEND, eventListener);
				mainShell.getDisplay().asyncExec(async);
				
				//if(records[0].getFileName()!=null)this.updateProgress(-1, ""+ records[0].getSpectrumList().size()+ " MSMS spectra loaded");
				if(records[0].getFileName()!=null){;}//do nothing, above print is allrady present.
				else{this.updateProgress(-1,"unable to load - please check the log");}
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
			
			//which of the available file loaders do we have to use?
			for(PeakFileLoaderItem item: fileLoaders){
				if(peakfilenameText.getText().endsWith(item.fileloaderExtension)){
					fileloaderClassname = item.fileloaderClassName;
					break;
				}
			}
			
			// When the button is pressed, close the child shell
			
			eventListener.updateEvent(Event.LOADBEGIN, null);
			dialogShell.setVisible(false);
			
			BusyIndicator.showWhile(mainShell.getDisplay(), new LoaderRunnable(peakfilenameText.getText(), fileloaderClassname));
			//new Thread(new LoaderRunnable(peakfilenameText.getText(), fileloaderClassname)).start();
			
			eventListener.updateEvent(Event.LOADEND, null);
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
}