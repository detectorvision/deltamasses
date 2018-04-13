/* $Id: DeltaMasses.java 461 2013-10-19 07:49:41Z frank $ */

package com.detectorvision.deltaMasses;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

import com.detectorvision.deltaMasses.analyzationmanagement.AnalyzationControl;
import com.detectorvision.deltaMasses.gui.diagrams.DeltaMassesCanvas;
import com.detectorvision.deltaMasses.gui.diagrams.RetentionCanvas;
import com.detectorvision.deltaMasses.gui.diagrams.SpectrumCanvas;
import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.deltaMasses.statemachine.State;
import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.Record.load_type_enum;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.DesEncrypter;
import com.detectorvision.utility.ExternalFileHandler;
import com.detectorvision.utility.GaussianFit;
import com.detectorvision.utility.currentDirectory;
import com.detectorvision.utility.databaseInfo;
import com.detectorvision.utility.pdf.HeaderFooterLandscape;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

/** 
 * deltaMasses mainclass.    
 * @author  Frank Potthast Rutz 
 * @author  Michael Lehmann 
 * @author  Raphael Bosshard 
 * @author  Jari Hakkinen
 */        
public class DeltaMasses implements ProgressListener, EventListener{
	public static Preferences preferences;
	private static UniModDatabase uniModDB;
	private static DeltaMassesScore algorithm;  
	public static String  version ="5.3"; //INTEGRATION_CONTROL
	public static String  build = "build #800 2018-04-16";   //INTEGRATION_CONTROL
	
	public static long expiresTimeMilliSec=Long.valueOf("1546300861375");//20190101 INTEGRATION CONTROL
	public static String expiresString="Jan 1st 2019";//INTEGRATION_CONTROL
	public static int     db_schema_version = 22; //INTEGRATION_CONTROL
	public static boolean db_schema_is_up_to_date=true;
	public static boolean DMBcanBeReached =false;
	public static boolean isDiscoveryEdition = true;
	private static String REG_LICENSE_KEY = "";
	private static String REG_LICENSE_NUMBER = "";
	private static String REG_MAC = "";
	public static String mascotHttp="http://www.matrixscience.com";
	public static AlgoParams algoParams= new AlgoParams();
	public static boolean showMarkerIons=false;
	public static boolean showMasses=true;
	public static double userDelta=113.084060;//frankp20070426 weight of Leucine
	public static boolean showUserDelta=false;
	public static int currentpairId=0;
	public static Label statusLabel          = null;
	public static Label pair_id_Label        = null;
	public static Label specNet_id_Label     = null;
	public static Label pair_is_marked_Label = null;
	public static Text  pairCommentText      = null;
	public static Button pairUnMarkedButton  = null;
	public static Button pairMarkedButton    = null;
	public static Button peptideNetButton    = null;
	//public static ToolItem openButton        = null;
	public static ToolItem storeButton        = null;
	public static ToolItem pdfButton          = null;
	public static ToolItem deltaClusterButton = null;
	//public static SpectrumCanvas mySpectrumCanvas = null;
	public static databaseInfo myDatabaseInfo = new databaseInfo();
	public static ArrayList<databaseInfo> configuredDatabases = new ArrayList<databaseInfo>();
	
	static Display display;
	final Shell mainShell, splashShell;
	Map widgets,splashWidgets;
	public static Record[] records = new Record[2];
	public static  ArrayList<SpectraPair> pairList;
	private AnalyzationControl[] analyzationControl;
	private State state;//statemachine

	public static int loading_delay = 0;
	public static int preferences_delay = 0;
	public static int unimod_delay = 0;
	public static int algorithms_delay = 0;
	public static int handlers_delay = 0;
	public static int cleanup_delay = 0;
	public static int edition_Delay = 0;
	public static int DMB_delay=0;
	public static boolean debug = false;//INTEGRATION CONTROL
	static Logger logger = Logger.getLogger(DeltaMasses.class.getName());		

	/**
	 * Constructor of the main class. Initializes the main screen.
	 */
	public DeltaMasses(){

		org.apache.log4j.BasicConfigurator.configure();
		{currentDirectory 	currDir = new currentDirectory();
		logger.info("workingDirectory:"+currDir.getCurrentDirectory());
		}
		
		this.display = new Display();
		this.mainShell = new Shell(display);
		this.splashShell = new Shell(display, SWT.ON_TOP | SWT.TOOL);
		this.pairList = new ArrayList<SpectraPair>(); 
		this.state = State.NONE;
		analyzationControl = new AnalyzationControl[1];
	}


	/**
	 * Mainclass DeltaMasses
	 * @param args
	 */
	public static void main(String[] args) {
		setDelays(debug);
		setLogger();
		//Runtime.getRuntime().loadLibrary("swt");
		

		//--------------------------------------------------------------------------
		// INITIALISATION
		//--------------------------------------------------------------------------
		// instance of the main class
		final DeltaMasses deltaMasses = new DeltaMasses();
		//--------------------------------------------------------------------------
		// PROGRAM START
		//--------------------------------------------------------------------------
		// show splash screen
		try {
			deltaMasses.splashWidgets = 
				XSWT.create(deltaMasses.getClass().getResourceAsStream("gui/SplashScreen.xswt"))
				.parse(deltaMasses.splashShell);
		} catch (XSWTException e) {
			logger.error("splashWidgets XSWTException:"+e.toString());
		}

		// get splash preferences
		Label splashText = (Label)deltaMasses.splashWidgets.get("textLabel");
		splashText.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_WHITE));
		deltaMasses.splashShell.pack();
		deltaMasses.splashShell.setBounds(deltaMasses.display.getBounds().width/2 - deltaMasses.splashShell.getSize().x/2, deltaMasses.display.getBounds().height/2 - deltaMasses.splashShell.getSize().y/2, deltaMasses.splashShell.getSize().x, deltaMasses.splashShell.getSize().y);
		deltaMasses.splashShell.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_WHITE));
		deltaMasses.splashShell.open();
		deltaMasses.splashText(loading_delay,"deltaMasses is preparing to load the data",false);

		// load preferences 
		deltaMasses.splashText(preferences_delay,"deltaMasses is loading the preferences",false);
		try {
			logger.info("loading preferences from config/preferences.xml");
			DeltaMasses.preferences = new Preferences("config/preferences.xml");
		} 
		catch (IOException e) {
			logger.error("pces.xml IOException:"+e.toString());
		}
		logger.info("loaded config/pces.xml");
		
		try {
			BufferedReader in = new BufferedReader(new FileReader("../automation/mascot/mascot.server.txt"));
			String str;
			while ((str = in.readLine()) != null) {
				if(str.startsWith("http:")){
					String tmp =str.trim();
					mascotHttp=tmp;
					logger.info("mascotHttp set to:"+mascotHttp);
				}
			}
			in.close();
		} catch (IOException ex) {
			logger.error("DeltaMasses:Could not find mascot.server.txt:"+ex.getMessage());
			mascotHttp="http://www.matrixscience.com";
		}

		// load modifications
		deltaMasses.splashText(unimod_delay,"deltaMasses is loading the unimod database",false);
		try {
			DeltaMasses.uniModDB = new UniModDatabase(DeltaMasses.preferences.getUniModFile());
		} 
		catch (IOException e) {
			logger.error("uniModDB IOException:"+e.toString());
		}
		catch (Exception e){
			logger.error("UniModDB Exception:"+e.toString());
		}
		logger.info("loaded unimod.xml file");
		deltaMasses.splashText(DMB_delay,"testing connection to deltaMassBase",false);

		configuredDatabases=DeltaMassBase.getConfiguredDatabases();

		
		logger.info("testing connection to deltaMassBase");
		if (DeltaMassBase.validConnection()) {
			DMBcanBeReached=true;
			deltaMasses.splashText(2*DMB_delay,"CONNECTION to deltaMassBase OK",false);
			logger.info("Connection to deltaMassBase OK");
			int deltaMassBaseVersion=DeltaMassBase.get_db_schema_version();
			if(deltaMassBaseVersion<0){
				logger.error("get_db_schema_version failure");
			}
			else{
				if(deltaMassBaseVersion==db_schema_version){//OK
					logger.info("db_schema_version:"+deltaMassBaseVersion);
					deltaMasses.splashText(2*DMB_delay,"database version OK",false);
				}
				else{//NOT OK
					db_schema_is_up_to_date=false;
					logger.error("db_schema_version not ok. Required"+db_schema_version+" found:"+deltaMassBaseVersion);
					for(int i=10;i-2>=0;i=i-3){
						deltaMasses.splashText(2000,i +  " Please create/update the database ",true);
						deltaMasses.splashText(2000,i-1+ " Do this in the deltaMassBase dialog",true);
						deltaMasses.splashText(2000,i-2+ " Consult the manual for details",true);
					}
				}
			}

		}
		else{
			DMBcanBeReached=false;
			deltaMasses.splashText(2*DMB_delay,"not connected to deltaMassBase",false);
			logger.warn("Connection to deltaMassBase not established");
		}

		deltaMasses.splashText(algorithms_delay,"deltaMasses is loading the algorithms",false);

		// include external java archive
		URLClassLoader urlClassLoader;
		URL[] externalJars = new URL[1];

		try {
			externalJars[0] = new File("lib/algorithms.jar").toURI().toURL();
		} catch (MalformedURLException e1) {
			logger.error("DeltaMasses: malformed URL exception while loading algorithms.jar:"+e1.getLocalizedMessage());
			e1.printStackTrace();
		}

		urlClassLoader = new URLClassLoader(externalJars);

		// load the algorithm class
		try {
			DeltaMasses.algorithm = (DeltaMassesScore)urlClassLoader.loadClass(DeltaMasses.preferences.getAlgorithmClass()).newInstance();
		} catch (InstantiationException e) {
			logger.error("externalJars error:InstantiationException:"+e.toString());
		} catch (IllegalAccessException e) {
			logger.error("externalJars error:IllegalAccessException:"+e.toString());
		} catch (ClassNotFoundException e) {
			logger.error("externalJars error:ClassNotFoundException:"+e.toString());
		} 
		logger.info("loaded lib/algorithms.jar");
		deltaMasses.analyzationControl[0] = new AnalyzationControl(DeltaMasses.algorithm, DeltaMasses.uniModDB.getModifications(), deltaMasses, deltaMasses,algoParams);
		logger.info("analyzationControl ok");

		//--------------------------------------------------------------------------
		// MAINSCREEN
		//--------------------------------------------------------------------------
		try {
			deltaMasses.widgets =  
				XSWT.create(deltaMasses.getClass().getResourceAsStream("gui/MainScreen.xswt"))
				.parse(deltaMasses.mainShell);
		} catch (XSWTException e) { 
			logger.error("DeltaMasses:mainShell parse problems:" +
					e.getLocalizedMessage());
		}
		deltaMasses.mainShell.setSize(1024, 768);
		deltaMasses.mainShell.setMaximized(true);
		logger.info("mainscreen created");


		//--------------------------------------------------------------------------
		// GUI OBJECT CES
		//--------------------------------------------------------------------------
		deltaMasses.mainShell.setText("deltaMasses                                        Discovery Edition");	

		deltaMasses.splashText(100,"deltaMasses is loading the ces",false);


		final SpectrumCanvas mySpectrumCanvas = (SpectrumCanvas)deltaMasses.widgets.get("spectrumCanvas");
		pairMarkedButton = (Button)deltaMasses.widgets.get("pairMarkedButton");
		// toolbar

		ToolItem openButton = (ToolItem)deltaMasses.widgets.get("openButton");
		openButton.setToolTipText("load a .mgf / Mascot-xml file");
		ToolItem parameterButton =(ToolItem)deltaMasses.widgets.get("parameterButton");
		parameterButton.setToolTipText("check and set algorithmic parameters");
		ToolItem runButton = (ToolItem)deltaMasses.widgets.get("runButton");
		runButton.setToolTipText("start differential PTM detection on the loaded dataset");
		ToolItem stopButton = (ToolItem)deltaMasses.widgets.get("stopButton");
		stopButton.setToolTipText("stop analysis (you will not receive any results in this case");
		
		
		
		deltaClusterButton = (ToolItem)deltaMasses.widgets.get("deltaClusterButton");
		deltaClusterButton.setToolTipText("start deltaCluster for large-scale differential PTM detection");
		deltaClusterButton.setEnabled(true);
	
		
	
		storeButton = (ToolItem)deltaMasses.widgets.get("storeButton");
		storeButton.setToolTipText("store current record in deltaMassBase");
		storeButton.setEnabled(false);
		
		ToolItem infoButton = (ToolItem)deltaMasses.widgets.get("infoButton");
		ToolItem DMBButton = (ToolItem)deltaMasses.widgets.get("DMBButton");
		Label YourLogoLabel = (Label)deltaMasses.widgets.get("YourLogoLabel");
		DMBButton.setToolTipText("deltaMassBase maintenance and simple statistics");

		//ToolItem record2experimentButton = (ToolItem)deltaMasses.widgets.get("record2experimentButton");
		//record2experimentButton.setToolTipText("map records to experiments");

		//ToolItem renameExpButton = (ToolItem)deltaMasses.widgets.get("renameExpButton");
		//renameExpButton.setToolTipText("change the names of experiments");

		ToolItem fingerprintButton = (ToolItem)deltaMasses.widgets.get("fingerprintButton");
		fingerprintButton.setToolTipText("create a deltaProtein report including the deltaMass fingerprint (needs deltaMassBase configured)");
		
		pdfButton = (ToolItem)deltaMasses.widgets.get("pdfButton");
		pdfButton.setToolTipText("show concise pdf result report");
		pdfButton.setEnabled(false);
		
		//hotimages are being set - currently a bit dumb but at least better design than before.
		fingerprintButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		parameterButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		openButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		DMBButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		runButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		stopButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		deltaClusterButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		pdfButton.setHotImage(new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png")));
		
		


		Button graphicsToPdfButton = (Button)deltaMasses.widgets.get("graphicsToPdfButton");
		graphicsToPdfButton.setToolTipText("export of Differential PTM graphics and neutral loss graphics to a pdf file");
		SelectionAdapter graphicsToPdfAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){

				Document document =  new Document(PageSize.A4.rotate(), 45, 40, 110, 50);//left right top bottom
				document.setMarginMirroring(true);
				document.addTitle("deltaMasses: UNIMOD modifications");
				document.addSubject("Detection of protein modifications");
				document.addKeywords("PTM PTM-Detection PTM-Localization deltaMasses Detectorvion AG Differential PTM Detection");
				document.addAuthor("deltaMasses used by"+System.getProperty("user.name"));


				try {
					int pdfPictureWidth=750;
					int pdfPictureHeight=410;
					PdfWriter writer = PdfWriter.getInstance(document,
							new FileOutputStream("tmp/Differential_PTM_Detection.pdf"));
					writer.setPageEvent(new HeaderFooterLandscape());
					document.open();
					document.add(new Paragraph("."));
					PdfContentByte cb = writer.getDirectContent();
					PdfTemplate tp = cb.createTemplate(pdfPictureWidth,pdfPictureHeight);
					Graphics2D g3 = tp.createGraphicsShapes(pdfPictureWidth,pdfPictureHeight);
					SpectrumCanvas.imageToClipboard();

					//scale the iamge
					int scaley=pdfPictureHeight;
					int ychange=0;
					if(SpectrumCanvas.getHeight()>0 && SpectrumCanvas.getWidth()>0){
						scaley=pdfPictureWidth*SpectrumCanvas.getHeight()/SpectrumCanvas.getWidth();
						ychange=(pdfPictureHeight-scaley)/2;
					}
					if(scaley>pdfPictureHeight){
						scaley=pdfPictureHeight;
						ychange=0;
					}

					//put awt image to g3 while scaling
					g3.drawImage(SpectrumCanvas.getAwtImage(),0,0,pdfPictureWidth,scaley,null);

					cb.addTemplate(tp, document.leftMargin(), 0);
					g3.dispose();
				} catch (Exception e3) {
					logger.error("graphicsToPdfAdapter:Exception:" + e3.toString());
				}
				document.close();

				if (ExternalFileHandler.open("tmp/Differential_PTM_Detection.pdf")!=0)
					logger.error("graphicsToPdfAdapter:pdf error:Acrobat Reader not installled or file association for pdf is not there");
			}
		};
		graphicsToPdfButton.addSelectionListener(graphicsToPdfAdapter);
		Button graphicsToClipBoardButton = (Button)deltaMasses.widgets.get("graphicsToClipBoardButton");
		Button mdhButton = (Button)deltaMasses.widgets.get("mdhButton");

		graphicsToClipBoardButton.setToolTipText("export spectrum graphics to clipboard (Discovery Edition only)");
		SelectionAdapter graphicsToClipBoardAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				SpectrumCanvas.imageToClipboard();
			}
		};

		mdhButton.setToolTipText("msms mass distance histogram (Discovery Edition only)");
		mdhButton.addSelectionListener(new NeutralLossDetectorDialogHandler(DeltaMasses.records));

		// menubar
		//TODO order this in correct order
		MenuItem dbMenuItem = (MenuItem)deltaMasses.widgets.get("dbMenuItem");
		MenuItem pdfMenuItem = (MenuItem)deltaMasses.widgets.get("pdfMenuItem");
		MenuItem tsvMenuItem = (MenuItem)deltaMasses.widgets.get("tsvMenuItem");
		MenuItem proteinViewMenuItem = (MenuItem)deltaMasses.widgets.get("proteinViewMenuItem");
		MenuItem DMBFMenuItem = (MenuItem)deltaMasses.widgets.get("DMBFMenuItem");
		MenuItem openMenuItem = (MenuItem)deltaMasses.widgets.get("openMenuItem");
		MenuItem exitMenuItem = (MenuItem)deltaMasses.widgets.get("exitMenuItem");
		MenuItem runMenuItem = (MenuItem)deltaMasses.widgets.get("runMenuItem");
		MenuItem stopMenuItem = (MenuItem)deltaMasses.widgets.get("stopMenuItem");
		MenuItem helpMenuItem = (MenuItem)deltaMasses.widgets.get("helpMenuItem");
		MenuItem deltaMassBaseInstallMenuItem = (MenuItem)deltaMasses.widgets.get("deltaMassBaseInstall");
		MenuItem aboutMenuItem = (MenuItem)deltaMasses.widgets.get("aboutMenuItem");
		MenuItem unimodMenuItem = (MenuItem)deltaMasses.widgets.get("unimodMenuItem");
		MenuItem experimentTableMenuItem = (MenuItem)deltaMasses.widgets.get("experimentTableMenuItem");
		MenuItem recordTableMenuItem = (MenuItem)deltaMasses.widgets.get("recordTableMenuItem");		
		MenuItem chooseDatabaseMenuItem = (MenuItem)deltaMasses.widgets.get("chooseDatabaseMenuItem");
		MenuItem pmcMenuItem = (MenuItem)deltaMasses.widgets.get("pmcMenuItem");
		MenuItem accuracyItem = (MenuItem)deltaMasses.widgets.get("accuracyItem");
		MenuItem updateMenuItem = (MenuItem)deltaMasses.widgets.get("updateMenuItem");
		MenuItem licenseMenuItem = (MenuItem)deltaMasses.widgets.get("licenseMenuItem");
		MenuItem telephoneMenuItem = (MenuItem)deltaMasses.widgets.get("telephoneMenuItem");
		MenuItem skypeMenuItem = (MenuItem)deltaMasses.widgets.get("skypeMenuItem");
		MenuItem icplMenuItem = (MenuItem)deltaMasses.widgets.get("icplMenuItem");
		MenuItem carbonCounterMenuItem = (MenuItem)deltaMasses.widgets.get("carbonCounterMenuItem");
		MenuItem xmlMenuItem = (MenuItem)deltaMasses.widgets.get("xmlMenuItem");
		MenuItem openLogMenuItem = (MenuItem)deltaMasses.widgets.get("openLogMenuItem");
		MenuItem deleteLogMenuItem = (MenuItem)deltaMasses.widgets.get("deleteLogMenuItem");
		deleteLogMenuItem.setEnabled(false);
		MenuItem autoViewDirMgfMenuItem = (MenuItem)deltaMasses.widgets.get("autoViewDirMgfMenuItem");
		MenuItem autoViewDirMascotMenuItem = (MenuItem)deltaMasses.widgets.get("autoViewDirMascotMenuItem");
		MenuItem autoSetDirMenuItem = (MenuItem)deltaMasses.widgets.get("autoSetDirMenuItem");
		MenuItem autoRecursiveMenuItem = (MenuItem)deltaMasses.widgets.get("autoRecursiveMenuItem");
		MenuItem autoSetRaw2mgfMenuItem = (MenuItem)deltaMasses.widgets.get("autoSetRaw2mgfMenuItem");
		MenuItem autoConfRaw2mgfMenuItem = (MenuItem)deltaMasses.widgets.get("autoConfRaw2mgfMenuItem");
		MenuItem autoLogMenuItem = (MenuItem)deltaMasses.widgets.get("autoLogMenuItem");
		MenuItem autoCleanLogMenuItem = (MenuItem)deltaMasses.widgets.get("autoCleanLogMenuItem");
		MenuItem settingsMenuItem = (MenuItem)deltaMasses.widgets.get("settingsMenuItem");
		MenuItem autoAutomationMenuItem = (MenuItem)deltaMasses.widgets.get("autoAutomationMenuItem");


		Label barLabel1 = (Label)deltaMasses.widgets.get("barLabel1");	
		barLabel1.setText("");
		statusLabel= (Label)deltaMasses.widgets.get("labelState");

		// tables
		Table modTable        = (Table)deltaMasses.widgets.get("modificationsTable");
		final Table pairTable = (Table)deltaMasses.widgets.get("spectrumPairsTable");

		//trac_ticket_76-------------------------------------------
		records[0]= new Record();//if you take this away selectionAdapter below will crash

		pair_id_Label =(Label)deltaMasses.widgets.get("pair_id_Label");
		specNet_id_Label = (Label)deltaMasses.widgets.get("specNet_id_Label");
		pair_is_marked_Label = (Label)deltaMasses.widgets.get("pair_is_marked_Label");
		pairCommentText= (Text)deltaMasses.widgets.get("pairCommentText");

		peptideNetButton = (Button)deltaMasses.widgets.get("peptideNetButton");
		peptideNetButton.setEnabled(false);
		SelectionAdapter peptidenetButtonAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				String tmp = DeltaMasses.specNet_id_Label.getText();
				int l_specnet_id = Integer.parseInt(tmp.substring(12,tmp.length()));

				if(!isDiscoveryEdition){
					MessageBox messageBox = new MessageBox(deltaMasses.mainShell, SWT.OK );
					messageBox.setText("deltaProtein");
					String message="";
					message +=  "This functionality is only available in Discovery Edition. It displays a pdf with the deltaProtein information about specnet number "+l_specnet_id;
					messageBox.setMessage(message);
					messageBox.open();
					logger.info("peptideNet Button no action for Personal Edition displayed");
					return;
				}
				else{
					logger.info("start making pdf of specnet_id:"+l_specnet_id);
					try{
						String l_title="specNet_ID:"+l_specnet_id;
							postSQL.printSpecNet2Pdf(l_title, l_specnet_id,true);
					}catch(Exception err){
						logger.error("info in specnet pdf generation:"+err);
					}
					logger.info("end making pdf of specnet_id:"+l_specnet_id);
				}

				logger.info("Displaying peptide net for discovery edition only");
			}
		};
		peptideNetButton.addSelectionListener(peptidenetButtonAdapter);
		
		pairCommentText.setToolTipText("Enter a comment on the pair here.");
		
		pairUnMarkedButton = (Button)deltaMasses.widgets.get("pairUnMarkedButton");
		pairUnMarkedButton.setToolTipText("click to unmark this pair. Comment will be deleted");
		pairUnMarkedButton.setEnabled(false);
		SelectionAdapter pairUnMarkedAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				boolean actionOK=true;
				statusLabel.setText("un marking pair_id "+SpectrumCanvas.myPairId);
				String commentText=pairCommentText.getText();

				actionOK=DeltaMassBase.setPairMarked(SpectrumCanvas.myPairId,"",false);
				if(actionOK){
					statusLabel.setText("unmarked pair_id "+SpectrumCanvas.myPairId);
					logger.info("pairMarkedAdapter unmarked pair:"+SpectrumCanvas.myPairId);
					pairUnMarkedButton.setEnabled(false);
					pairCommentText.setText("");
					SpectraPair tmpPair= records[0].getCurrentSpectraPair();
					tmpPair.comment=pairCommentText.getText();
					tmpPair.marked=false;
					records[0].setCurrentSpectraPair(tmpPair);
					SpectrumCanvas.myComment="";
				}
				else{
					statusLabel.setText("could not unmark pair_id "+SpectrumCanvas.myPairId);
					logger.error("could not unmark pair:"+SpectrumCanvas.myPairId);					
				}
				mySpectrumCanvas.redraw();
				pair_is_marked_Label.setText("no");
			}			
		};
		pairUnMarkedButton.addSelectionListener(pairUnMarkedAdapter);

		Button pairMarkedButton = (Button)deltaMasses.widgets.get("pairMarkedButton");
		pairMarkedButton.setToolTipText("click to mark/unmark this pair as being of special interest to you");
		pairMarkedButton.setEnabled(DMBcanBeReached);
		SelectionAdapter pairMarkedAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				statusLabel.setText("marking pair_id "+SpectrumCanvas.myPairId);
				String commentText=pairCommentText.getText();
				if(DeltaMassBase.setPairMarked(SpectrumCanvas.myPairId,commentText,true)){
					statusLabel.setText("marked pair_id "+SpectrumCanvas.myPairId);
					logger.info("pairMarkedAdapter marked pair:"+SpectrumCanvas.myPairId);
					pairUnMarkedButton.setEnabled(true);
					SpectrumCanvas.myComment=commentText;
					SpectraPair tmpPair= records[0].getCurrentSpectraPair();
					tmpPair.comment=pairCommentText.getText();
					tmpPair.marked=true;
					records[0].setCurrentSpectraPair(tmpPair);
					mySpectrumCanvas.redraw();
					pair_is_marked_Label.setText("yes");
				}
				else{
					statusLabel.setText("could not mark pair_id "+SpectrumCanvas.myPairId);
					logger.error("could not mark pair:"+SpectrumCanvas.myPairId);					
				}		
			}			
		};
		pairMarkedButton.addSelectionListener(pairMarkedAdapter);

		final Combo loadCombo = (Combo)deltaMasses.widgets.get("loadCombo");
		loadCombo.setText("with peptide ID");
		loadCombo.add("record");
		loadCombo.add("peptideNet");
		loadCombo.add("deltaMass");
		loadCombo.add("pair ID");
		loadCombo.add("marked pairs");
		loadCombo.add("identified pairs");
		loadCombo.add("of protein");
		loadCombo.add("all pairs");
		loadCombo.add("load all for deltaCluster");

		Button loadRecordButton = (Button)deltaMasses.widgets.get("loadRecordButton");
		final Text loadRecordText=(Text)deltaMasses.widgets.get("loadRecordText");

		loadRecordText.setToolTipText("enter what you want to load here");
		if(DMBcanBeReached){
			experimentTableMenuItem.setEnabled(true);
			recordTableMenuItem.setEnabled(true);
			loadRecordButton.setEnabled(true);
			loadRecordButton.setToolTipText("click to load data as indicated by the two controls to the right");
		} 
		else{loadRecordButton.setEnabled(false);
		experimentTableMenuItem.setEnabled(false);
		recordTableMenuItem.setEnabled(false);
		loadRecordButton.setToolTipText("you can only load data if deltaMassBase is configured");
		}
		SelectionAdapter RecordFromDBAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				logger.info("loadCombo.getText:"+loadCombo.getText());
				int loadInteger=0;
				double loadDouble=0.0;
				String loadStringProt="";
				statusLabel.setText("trying to load "+loadCombo.getText()+" from deltaMassBase. Please wait a minute.");
				boolean parsingOK=false;
				try{
					if(loadCombo.getText().equals("record")      || 
							loadCombo.getText().equals("peptideNet")  ||
							loadCombo.getText().equals("pair ID"))
					{
						loadInteger=Integer.parseInt(loadRecordText.getText());
					}
					else if(loadCombo.getText().equals("deltaMass")){
						loadDouble=Double.parseDouble(loadRecordText.getText());
					}
					else if(loadCombo.getText().equals("all pairs")){
					}
					else if(loadCombo.getText().equals("load all for deltaCluster")){
					}
					else if(loadCombo.getText().equals("of protein")){
						loadStringProt=loadRecordText.getText().trim();
					}
					else{
						logger.error("loadCombo.getText unresolved:"+loadCombo.getText());
					}
					
					parsingOK=true;
					statusLabel.setText("trying to load "+loadCombo.getText()+" from deltaMassBase. Please wait a minute.");
				}
				catch (Exception e1) {
					statusLabel.setText("Please excuse, could not parse "+loadCombo.getText()+" from \""+loadRecordText.getText()+"\"");
					logger.error("loadRecordFromDBAdapter Exception:"+e1.toString());
				}
				if(parsingOK){ 
					String loadString="";
					//records[0]= new Record();Dont reactivate this code.
					deltaMasses.updateEvent(Event.LOADBEGIN, null); 
					deltaMasses.updateEvent(Event.LOADPROGRESS, null); 

					if(loadCombo.getText().equals("record")){loadString=records[0].loadRecordFromDeltaMassBase(loadInteger,DeltaMasses.uniModDB.getModifications());}
					else if(loadCombo.getText().equals("peptideNet")){loadString=records[0].loadPeptideNetFromDeltaMassBase(loadInteger,DeltaMasses.uniModDB.getModifications());}
					else if(loadCombo.getText().equals("deltaMass")){loadString=records[0].loaddeltaMassFromDeltaMassBase(1,DeltaMasses.uniModDB.getModifications(), loadDouble  ,0.01);}//TODO mass accuracy to be added
					else if(loadCombo.getText().equals("of protein")){loadString=records[0].loadIdentifiedPairsFromDeltaMassBase(DeltaMasses.uniModDB.getModifications(),loadStringProt);}
					else if(loadCombo.getText().equals("pair ID"))     {loadString=records[0].loadpairIDFromDeltaMassBase(loadInteger, DeltaMasses.uniModDB.getModifications());}
					else if(loadCombo.getText().equals("marked pairs")){loadString=records[0].loadMarkedPairsFromDeltaMassBase(DeltaMasses.uniModDB.getModifications());}
					else if(loadCombo.getText().equals("identified pairs")){loadString=records[0].loadIdentifiedPairsFromDeltaMassBase(DeltaMasses.uniModDB.getModifications(),"all");}
					else if(loadCombo.getText().equals("all pairs")){loadString=records[0].loadAllPairsFromDeltaMassBase(DeltaMasses.uniModDB.getModifications());}
					else if(loadCombo.getText().equals("load all for deltaCluster")){
						if(isDiscoveryEdition){
						loadString=records[0].loadAllFromDeltaMassBase(DeltaMasses.uniModDB.getModifications());
						}
						else{
							MessageBox messageBox = new MessageBox(deltaMasses.mainShell, SWT.OK );
							messageBox.setText("load all for deltaCluster");
							String message="";
							message +=  "deltaCluster is only available in Discovery Edition. It enables you to load all spectra in your database and to compare all possible pairs. In this way, deltaCluster performs differential PTM detection across records/runs/measurements. Combined with deltaProtein it gives you the possibility to do PTM based biomarker Discovery";
							messageBox.setMessage(message);
							messageBox.open();
						}
						}
					else if(loadCombo.getText().length()<3){loadString=" please choose a load method to the right of the load button";}
					else{
						loadString=" unsupported load type";
						logger.error("unsupported load type");
					}
					if(loadString.equals("OK")){
						statusLabel.setText("loaded "+loadCombo.getText()+" "+ loadRecordText.getText()+" from DB, OK");
						logger.info("loaded "+loadCombo.getText()+" from DB, OK");
						deltaMasses.updateEvent(Event.LOADEND, null);
						deltaMasses.useDataFromDbLoader();
					}
					else{
						logger.info("loading "+loadCombo.getText()+" from DB  not OK:"+loadString);
						statusLabel.setText("loading "+loadCombo.getText()+" not ok: "+loadString);
					}		
				}
			}
		};
		loadRecordButton.addSelectionListener(RecordFromDBAdapter);

		try{
			YourLogoLabel.setToolTipText("To see your own logo here, put the image YourLogo.png of size 200*40 pixels into the config directory");
			java.io.File fff = new java.io.File("config/YourLogo.png");
			File tmpFile=new File(fff.getAbsoluteFile().toString());

			if(tmpFile.exists()){
				Image   result = new Image(Display.getCurrent(), tmpFile.toString());
				YourLogoLabel.setImage(result);
				YourLogoLabel.setToolTipText("Protein Science Professionals");
			}
		}
		catch(Exception e){
			logger.info("problems while setting YourLogolabel:" + e.getMessage());
		}

		//if(DMBcanBeReached){
		if(true){
		DMBButton.setEnabled(true);
			fingerprintButton.setEnabled(true);
			//record2experimentButton.setEnabled(true);
			//renameExpButton.setEnabled(true);
			dbMenuItem.setEnabled(true);
			if (DeltaMassBase.isCreated()) {
				Image result = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on.png"));
				DMBButton.setImage(result);
			}
			else{
				Image result = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-off.png"));
				DMBButton.setImage(result);
				DMBButton.setToolTipText("Please create the database");
			}
		}
		else{
			Image result = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-off.png"));
			fingerprintButton.setEnabled(false);
			fingerprintButton.setImage(result);

			//DMBButton.setEnabled(false);
			DMBButton.setImage(result);
			DMBButton.setToolTipText("database not available. Click to configure");
			DMBButton.setText("configure database");

			fingerprintButton.setEnabled(false);
			//record2experimentButton.setEnabled(false);
			//renameExpButton.setEnabled(false);
			dbMenuItem.setEnabled(false);
		}



		graphicsToClipBoardButton.setEnabled(true);
		graphicsToClipBoardButton.setToolTipText("export graphics to clipboard\nbeeps when ready");
		pdfMenuItem.setEnabled(false);
		xmlMenuItem.setEnabled(false);
		tsvMenuItem.setEnabled(false);
		dbMenuItem.setEnabled(isDiscoveryEdition);
		proteinViewMenuItem.setEnabled(false);
		licenseMenuItem.setEnabled(!isDiscoveryEdition);
		DMBFMenuItem.setEnabled(isDiscoveryEdition && DMBcanBeReached);
		skypeMenuItem.setEnabled(false);
		telephoneMenuItem.setEnabled(false);
		icplMenuItem.setEnabled(true);
		carbonCounterMenuItem.setEnabled(false);
		pmcMenuItem.setEnabled(true);
		accuracyItem.setEnabled(isDiscoveryEdition);
		mdhButton.setEnabled(true);
		mdhButton.setToolTipText("MSMS mass distance histogram tool");
		autoViewDirMgfMenuItem.setEnabled(true);//ok if it can be seen in PE
		autoViewDirMascotMenuItem.setEnabled(true);//ok if it can be seen in PE
		autoSetRaw2mgfMenuItem.setEnabled(false);
		autoConfRaw2mgfMenuItem.setEnabled(false);
		autoLogMenuItem.setEnabled(true);//ok if it can be seen in PE
		autoCleanLogMenuItem.setEnabled(true);//ok if it can be seen in PE
		autoAutomationMenuItem.setEnabled(isDiscoveryEdition);



		//--------------------------------------------------------------------------
		// EVENTS
		//--------------------------------------------------------------------------
		deltaMasses.splashText(handlers_delay,"deltaMasses is creating handlers",false);
		OpenDialogHandler openDialogHandler = new OpenDialogHandler(DeltaMasses.records, deltaMasses);
		SettingsDialogHandler settingsDialogHandler = new SettingsDialogHandler(DeltaMasses.algoParams);
		openDialogHandler.setProgressListener(deltaMasses);

		IcplDialogHandler icplDialogHandler = new IcplDialogHandler();

		pdfMenuItem.addSelectionListener(pdfSelectionListener);
		xmlMenuItem.addSelectionListener(new XmlDialogHandler());

		algoParams.isDiscoveryEdition=isDiscoveryEdition;

		// toolbar
		openButton.addSelectionListener(openDialogHandler);
		parameterButton.addSelectionListener(settingsDialogHandler);
		runButton.addSelectionListener(new AnalyzeHandler(deltaMasses.analyzationControl, DeltaMasses.records,algoParams));
		stopButton.addSelectionListener(new StopHandler(deltaMasses.analyzationControl));

		DMBDialogHandler dmbDialogHandler = new DMBDialogHandler(deltaMasses);
		DMBButton.addSelectionListener(dmbDialogHandler);

		fingerprintButton.addSelectionListener(new DMBFingerprintHandler(deltaMasses));
		//record2experimentButton.addSelectionListener(new record2experimentHandler(DeltaMasses.records,deltaMasses));
		//renameExpButton.addSelectionListener(new renameExpHandler(DeltaMasses.records,deltaMasses));



		// menubar
		openMenuItem.addSelectionListener(openDialogHandler);
		unimodMenuItem.addSelectionListener(new UniModHandler(DeltaMasses.uniModDB));
		recordTableMenuItem.addSelectionListener(new RecordTableHandler());
		experimentTableMenuItem.addSelectionListener(new ExperimentTableHandler());
		//chooseDatabaseDialogHandler.addSelectionListener(new chooseDatabaseDialogHandler());
		
		pmcMenuItem.addSelectionListener(new pmcDialogHandler(DeltaMasses.records));
		//accuracyItem.addSelectionListener(new accuracyHandler(DeltaMasses.records));
		
		updateMenuItem.addSelectionListener(new UpdateDialogHandler());
		licenseMenuItem.addSelectionListener(new LicenseDialogHandler());
		runMenuItem.addSelectionListener(new AnalyzeHandler(deltaMasses.analyzationControl, DeltaMasses.records,algoParams));
		stopMenuItem.addSelectionListener(new StopHandler(deltaMasses.analyzationControl));
		helpMenuItem.addSelectionListener(new HelpDialogHandler("deltaMasses"));
		deltaMassBaseInstallMenuItem.addSelectionListener(new HelpDialogHandler("deltaMassBaseInstallManual"));
		pdfButton.addSelectionListener(pdfHandler);
		openLogMenuItem.addSelectionListener(new openLogDialogHandler("log"));
		deleteLogMenuItem.addSelectionListener(new DeleteLogDialogHandler("log"));
		deleteLogMenuItem.setEnabled(false);
		xmlMenuItem.addSelectionListener(new XmlDialogHandler());
		tsvMenuItem.addSelectionListener(new TsvDialogHandler());
		DeltaMasses.algoParams.isDiscoveryEdition=isDiscoveryEdition;
		settingsMenuItem.addSelectionListener(new SettingsDialogHandler(DeltaMasses.algoParams));
		icplMenuItem.addSelectionListener(icplDialogHandler);
		graphicsToClipBoardButton.addSelectionListener(graphicsToClipBoardAdapter);
		autoAutomationMenuItem.addSelectionListener(new AutomationHandler());
		dbMenuItem.addSelectionListener(dmbDialogHandler);
		autoLogMenuItem.addSelectionListener(new openLogDialogHandler("automation"));
		autoCleanLogMenuItem.addSelectionListener(new DeleteLogDialogHandler("automation"));
		autoViewDirMgfMenuItem.addSelectionListener(new openLogDialogHandler("mgf_automation_dir"));
		autoViewDirMascotMenuItem.addSelectionListener(new openLogDialogHandler("mascot_automation_dir"));
		DMBFMenuItem.addSelectionListener(new DMBFingerprintHandler(deltaMasses));

		aboutMenuItem.addSelectionListener(new AboutDialogHandler());
		exitMenuItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				logger.info("exitMenuItem:Selection Event:user controlled end of program");
				System.exit(0);
			}
		});

		accuracyItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				logger.info("accuracyItem selected");
				try{
					int numSpectra=records[0].getSpectrumNumber() ;
					if(numSpectra<=0){
						MessageBox messageBox = new MessageBox(deltaMasses.mainShell, SWT.OK );
						messageBox.setText("MS accuracy estimation");
						String message="";
						message +=  "No data loaded, cannot perform MS accuracy estimation.";
						messageBox.setMessage(message);
						messageBox.open();
						logger.info("no data loaded for MS accuracy estimation.");
						return;
					}
					if(numSpectra<1000){
						MessageBox messageBox = new MessageBox(deltaMasses.mainShell, SWT.OK );
						messageBox.setText("MS accuracy estimation");
						String message ="No enough data loaded, cannot perform MS accuracy estimation.\n";
						message+="Need to load at least 1000 spectra.\n";
						message+="Right now, you have loaded "+numSpectra+" spectra.";
						messageBox.setMessage(message);
						messageBox.open();
						logger.info("not enough data loaded for MS accuracy estimation.");
						return;
					}
					
					GaussianFit zeroFit=records[0].calcPrecision(0.0, 0.4, 20);
					MessageBox messageBox = new MessageBox(deltaMasses.mainShell, SWT.OK );
					messageBox.setText("ms accurracy estimation");

					String message="";
					if(zeroFit.getSigma()<=0){
						message="calculated sigma is <= zero, this cannot be true. skipping. Please report to Detectorvision.";
						logger.error("getSigma returned zero.");
					}
					else if(zeroFit.getSigma()>0.2){
						message="calculated MS-sigma too large. Cannot calculate sigma.\nYou need ppm-accuracy MS to use this feature.";
						logger.info("zeroFit.getSigma:"+zeroFit.getSigma());
					}
					else{
						String sigmaString = String.format("%.4f", zeroFit.getSigma()); 	
						String sigmaString3 = String.format("%.4f",3.0*zeroFit.getSigma());
						message +="estimated MS sigma:"+sigmaString;
						message +="\n\n3 sigma="+sigmaString3;
						logger.info("MS accuracy calculated.");
					}

					messageBox.setMessage(message);
					int buttonID = messageBox.open();
					if(buttonID==SWT.OK){
						logger.info("SWT.OK from user.");
						return;
					}
				}catch(Exception e){
					logger.error("GaussianFit failed:"+e.toString());
					return;
				}
			}
		});

		
		setStoreButton();
		storeButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event){
				if(!DMBcanBeReached){storeButton.setEnabled(false);return;}				
				if(records[0]==null){storeButton.setEnabled(false);}
				else if(!records[0].hasBeenStored){
					boolean isok=false;
					statusLabel.setText("storing record in database, this can take a minute. Please wait.");
					try{
					isok=records[0].DMBstoreRecord(records[0], pairList, isDiscoveryEdition);
					} catch(Exception e){
						logger.error("error while storing data:"+e.toString());
					}			
					storeButton.setEnabled(false);
					if(isok){
						statusLabel.setText("stored record "+records[0].getFileName()+" in deltaMassBase. record_id: "+records[0].getRecordId());
					}
					else{
						statusLabel.setText("Unable to store record, sorry. You may want to check the logfile.");
					}
				}
			}
		});
		
		
		
		deltaClusterButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event){
				if(!DMBcanBeReached){deltaClusterButton.setEnabled(false);return;}				
				logger.info("starting deltaCluster");
				 MessageBox messageBox = new MessageBox(deltaMasses.mainShell, SWT.ICON_INFORMATION );
				 messageBox.setText("deltaCluster info:");
			     if(isDiscoveryEdition){ messageBox.setMessage("deltaCluster is now operated via load->load all for deltaCluster. After that, click analyze. Please read the manual carefully before doing this. Available in Discovery Edition only.");}
			     else{ messageBox.setMessage("You do not have a deltaCluster license, sorry.");}
			     messageBox.open();
			     System.out.println("deltaCluster-click.");
				
			}
		});
		
		
		
		

		// canvases
		final RetentionCanvas retentionCanvas = (RetentionCanvas)deltaMasses.widgets.get("retentionCanvas");
		retentionCanvas.setRecords(DeltaMasses.records);

		final SpectrumCanvas spectrumCanvas = (SpectrumCanvas)deltaMasses.widgets.get("spectrumCanvas");
		spectrumCanvas.setRecords(DeltaMasses.records);
		spectrumCanvas.setDiscoveryEdition(isDiscoveryEdition);

		org.eclipse.swt.graphics.Image swtimage= new Image(display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/start_background_spectrum_canvas.png"));
		spectrumCanvas.setBackgroundImage(swtimage);

		final DeltaMassesCanvas deltaMassesCanvas = (DeltaMassesCanvas)deltaMasses.widgets.get("deltaMassesCanvas");
		deltaMassesCanvas.setRecords(DeltaMasses.records);

		// spectrum canvas zoom scaler
		Scale spectrumZoomScale = (Scale)deltaMasses.widgets.get("spectrumZoomScale");
		spectrumZoomScale.setMinimum(10); 
		spectrumZoomScale.setMaximum(100);
		spectrumZoomScale.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				Scale sc = (Scale)event.getSource();
				spectrumCanvas.setZoom((double)sc.getSelection() / 10.0);
			}
		});

		// retention histogram canvas zoom scaler
		Scale retentionZoomScale = (Scale)deltaMasses.widgets.get("retentionZoomScale");
		retentionZoomScale.setMinimum(10); 
		retentionZoomScale.setMaximum(100);
		retentionZoomScale.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				Scale sc = (Scale)event.getSource();
				retentionCanvas.setZoom((double)sc.getSelection() / 10.0);
			}
		});

		// deltaMasses histogram canvas zoom scaler
		Scale deltaMassesZoomScale = (Scale)deltaMasses.widgets.get("deltaMassesZoomScale");
		deltaMassesZoomScale.setMinimum(10); 
		deltaMassesZoomScale.setMaximum(12000);

		deltaMassesZoomScale.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				Scale sc = (Scale)event.getSource();
				deltaMassesCanvas.setZoom((double)sc.getSelection() / 10.0);
			}
		});

		// tables
		for(int i=0;i<modTable.getColumnCount(); i++)
			modTable.getColumn(i).addListener(SWT.Selection, new TableSortHandler(i));
		ModTableSelectionHandler modTableSelectionHandler = new ModTableSelectionHandler(pairTable,deltaMasses.pairList);
		modTable.addSelectionListener(modTableSelectionHandler);

		for(int i=0;i<pairTable.getColumnCount(); i++){
			pairTable.getColumn(i).addListener(SWT.Selection, new TableSortHandler(i));}
		PairTableSelectionHandler pairTableSelectionHandler = new PairTableSelectionHandler(deltaMasses.records, deltaMasses.pairList);

		// control widgets to update whenever a spectrum pair is selected
		pairTable.addSelectionListener(pairTableSelectionHandler);
		pairTableSelectionHandler.addRedrawListener(spectrumCanvas);
		pairTableSelectionHandler.addRedrawListener(retentionCanvas);
		pairTableSelectionHandler.addRedrawListener(deltaMassesCanvas);
		

		final Button markerButton = (Button)deltaMasses.widgets.get("markerButton");
		markerButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_RED));//assumes that showMarkerIons==false upon start
		markerButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_RED));
		markerButton.setToolTipText("click to mark/unmark fragments unique to the heavy spectrum excluding deltaChannel fragments");
		markerButton.update();
		SelectionAdapter markerAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(showMarkerIons){
					markerButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_RED));
					markerButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_RED));
					markerButton.setText("marker ions off");
					showMarkerIons=false;
					spectrumCanvas.setShowMarkerIons(false);
				}
				else{
					markerButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GREEN));
					markerButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_GREEN));
					markerButton.setText("marker ions on");
					spectrumCanvas.setShowMarkerIons(true);
					showMarkerIons=true;	
				}
			}
		};
		//if(!isDiscoveryEdition){markerButton.setEnabled(false);}
		markerButton.addSelectionListener(markerAdapter);

		//		new 20070405
		final Button massesButton = (Button)deltaMasses.widgets.get("massesButton");
		massesButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GREEN));//assumes that showMarkerIons==false upon start
		massesButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_GREEN));
		massesButton.setText("masses on");
		massesButton.setToolTipText("click to label/unlabel fragments with 2-digit precision m/z values");
		SelectionAdapter massesAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(showMasses){
					massesButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_RED));
					massesButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_RED));
					massesButton.setText("masses off");
					showMasses=false;
					spectrumCanvas.setShowMasses(false);
				}
				else{
					massesButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GREEN));
					massesButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_GREEN));
					massesButton.setText("masses on");
					showMasses=true;
					spectrumCanvas.setShowMasses(true);
				}
			}
		};
		massesButton.addSelectionListener(massesAdapter);

		//		new 20070426
		final Text userDeltaText=(Text)deltaMasses.widgets.get("userDelta");
		userDeltaText.setToolTipText("enter user defined deltaMass[Dalton] for above plot");
		userDeltaText.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));

		final Button userDeltaButton = (Button)deltaMasses.widgets.get("userDeltaButton");
		userDeltaButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));//assumes that tart
		userDeltaButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
		userDeltaButton.setToolTipText("click to switch on/off user defined deltaMass in plot");
		SelectionAdapter userDeltaAdapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				double userDeltaMass=0.0;
				boolean parsingOK=false;
				String tmp = userDeltaText.getText();
				try{
					userDeltaMass=Double.parseDouble(tmp);
					userDeltaText.setText(""+userDeltaMass);
					parsingOK=true;
				}
				catch (Exception e1) {
					userDeltaText.setText("0.0");
					userDeltaMass=0;
					showUserDelta=false;
					userDeltaText.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
					userDeltaText.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_BLACK));	
					userDeltaButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
					userDeltaButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
					parsingOK=false;
				};

				if(parsingOK){
					if(showUserDelta){
						userDeltaButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
						userDeltaButton.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
						showUserDelta=false;
						userDeltaButton.setText("off");
						userDeltaText.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_GRAY));
						userDeltaText.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_BLACK));						
						spectrumCanvas.setUserDeltaMass(userDeltaMass);
						spectrumCanvas.setShowUserDelta(false);
						spectrumCanvas.redraw();
						//spectrumCanvas.setShowMasses(false);
					}
					else{
						userDeltaButton.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_BLUE));
						showUserDelta=true;
						userDeltaButton.setText("on");
						userDeltaText.setBackground(deltaMasses.display.getSystemColor(SWT.COLOR_BLUE));
						userDeltaText.setForeground(deltaMasses.display.getSystemColor(SWT.COLOR_WHITE));
						spectrumCanvas.setUserDeltaMass(userDeltaMass);
						spectrumCanvas.setShowUserDelta(true);
						spectrumCanvas.redraw();
						//spectrumCanvas.setShowMasses(true);
					}
				}
			}
		};
		userDeltaButton.addSelectionListener(userDeltaAdapter);

		//--------------------------------------------------------------------------
		// END
		//--------------------------------------------------------------------------

		// Splashcreen close
		deltaMasses.splashText(cleanup_delay,"deltaMasses is cleaning up",false);
		deltaMasses.splashShell.close();
		logger.info("startup completed");

		deltaMasses.mainShell.open();
		deltaMasses.updateEvent(Event.START, null);

		int dumbloop=0;//some special effects if the db schema is not up to date
		if(db_schema_is_up_to_date){dumbloop=3;}
		for(int i=dumbloop;i<4;i++){
			if(isDiscoveryEdition){statusLabel.setText("welcome to deltaMasses Discovery Edition v" + version + " " + build);}
			else {statusLabel.setText("welcome to deltaMasses personal edition v" + version + " " + build);}
			try{
				if(!db_schema_is_up_to_date){
					Thread.currentThread().sleep(1000);
				}
			}
			catch(Exception ie){
				logger.error("db_schema_is_up_to_date.statuslabel sleeper interrupted");
			}	
			if(!db_schema_is_up_to_date){
				org.eclipse.swt.graphics.Color red = display.getSystemColor(SWT.COLOR_RED);
				statusLabel.setBackground(red);
				try{
					statusLabel.setText("use the green button above to create/update the database");
					Thread.currentThread().sleep(4000);
				}
				catch(Exception aa){
					logger.error("db_schema_is_up_to_date.statuslabel sleeper interrupted");
				}
				org.eclipse.swt.graphics.Color statusgray = display.getSystemColor(SWT.COLOR_GRAY);
				statusLabel.setBackground(statusgray);
			}
		}
		logger.info("listening for events");
		try{
		while (!deltaMasses.mainShell.isDisposed()) {
			if (!deltaMasses.display.readAndDispatch())
				deltaMasses.display.sleep();
		}
		}catch(Exception ex){
			logger.error("fatal: eventlistener crashes, uncatched error. Must be controlled.");
			logger.error("Exception:"+ex.getMessage());
			logger.error("Stacktrace:"+ex);
		}
		logger.info("display.dispose");
		deltaMasses.display.dispose();
	}

	/**
	 * Calculates the states of the statemachine an performe the actual actions.
	 * @param e Event
	 * @param data Eventdate
	 */
	public void updateEvent(Event e, Object data){
		ToolItem openButton = (ToolItem)this.widgets.get("openButton");
		ToolItem runButton = (ToolItem)this.widgets.get("runButton");
		ToolItem stopButton = (ToolItem)this.widgets.get("stopButton");
		ToolItem pdfButton = (ToolItem)this.widgets.get("pdfButton");
		MenuItem runMenuItem = (MenuItem)this.widgets.get("runMenuItem");
		MenuItem stopMenuItem = (MenuItem)this.widgets.get("stopMenuItem");
		MenuItem pmcMenuItem = (MenuItem)this.widgets.get("pmcMenuItem");
		MenuItem xmlMenuItem = (MenuItem)this.widgets.get("xmlMenuItem");
		MenuItem tsvMenuItem = (MenuItem)this.widgets.get("tsvMenuItem");
		MenuItem pdfMenuItem= (MenuItem)this.widgets.get("pdfMenuItem");
		Label barLabel1  = (Label)this.widgets.get("barLabel1");
		Label labelState  = (Label)this.widgets.get("labelState");

		// Perform event for the matching state
		if(this.state == State.NONE){
			if(e == Event.START){
				this.state = State.START;
				logger.info("Event.START");
				openButton.setEnabled(true);
				runButton.setEnabled(false);
				stopButton.setEnabled(false);
				pdfButton.setEnabled(false);
				runMenuItem.setEnabled(false);
				stopMenuItem.setEnabled(false);
				openButton.setWidth(40);
				runButton.setWidth(40);
				stopButton.setWidth(40);
			}
			else {
				logger.error("no matching event found:"+e.toString());
			}
		}

		// program is started up
		else if(this.state == State.START){
			if(e == Event.LOADBEGIN){
				logger.info("Event.LOADBEGIN");
				//org.eclipse.swt.graphics.Image swtimage= new Image(display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/start_background_spectrum_canvas.png"));
			    //mySpectrumCanvas.setBackgroundImage(swtimage);
		        openButton.setEnabled(false);
				storeButton.setEnabled(false);
				this.state = State.LOADFILE;
				pdfMenuItem.setEnabled(false);
				xmlMenuItem.setEnabled(false);
				pdfMenuItem.setEnabled(false);
				tsvMenuItem.setEnabled(false);
				logger.info("DeltaMasses:updateEvent:LOADFILE");
			}
			else {
				logger.error("no matching event found");
			}
		}

		// program is loading a peakfile
		else if(this.state == State.LOADFILE){
			if(e == Event.LOADEND){
				logger.info("Event.LOADEND");
				if(this.records[0].getFileName() != null){
					this.state = State.READY;
					logger.info("DeltaMasses:updateEvent:LOADED");

					{//put info into barLabel1
						//Get the bareFileName - get rid of the \'s
						//TODO more elegant, please
						String bareFileName=this.records[0].getFileName();
						int posi = bareFileName.indexOf("\\");
						while(posi>0){ 
							bareFileName = bareFileName.substring(posi + 1);
							posi = bareFileName.indexOf("\\");
						}
						logger.info("" + bareFileName);

						//TODO fix this functionality in 5.0
						/*	labelState.setText("loaded " + this.records[0].getSpectrumList().size() + " MSMS spectra from " + bareFileName + ". Now trying to calculate MS accuracy. Please wait.");	
						barLabel1.setText("" + bareFileName);
						GaussianFit GaussFit= this.records[0].calcPrecision(0,0.3,100);

						if(GaussFit.computationOK){
							String tmpSigma= String.format("%.6f",GaussFit.getSigma());  
							labelState.setText("loaded " + this.records[0].getSpectrumList().size() + " MSMS spectra from " + bareFileName + "  Calculated 1\u03C3 MS accuracy:"+tmpSigma+"[Da]");	
						}
						barLabel1.setText("" + bareFileName);*/



					}//end of barLabel1 section
					xmlMenuItem.setEnabled(false);
					tsvMenuItem.setEnabled(false);
					pdfMenuItem.setEnabled(false);
					openButton.setEnabled(true);
					runButton.setEnabled(true);
					stopButton.setEnabled(false);
					pdfButton.setEnabled(false);
					runMenuItem.setEnabled(true);
					stopMenuItem.setEnabled(false);
					//pmcMenuItem.setEnabled(true);
				}
				else {
						openButton.setEnabled(true);
						labelState.setText("Unable to load - please consult the log files");
				      }
			}
			else if(e == Event.LOADPROGRESS){
				logger.info("Event.LOADPROGRESS");

				// TODO Calculate fileloadingprogress
			}
			else if(e == Event.ERROR){
				this.state = State.START;
				logger.error("Event.error:"+e.toString());
			}
			else {
				logger.error("no matching event found");
			}
		}

		// program is ready to analyze
		else if(this.state == State.READY){
			if(e == Event.DETECTBEGIN){
				logger.info("Event.DETECTBEGIN");
				this.state = State.ANALYZE;
				for(int i=0; i<this.pairList.size(); i++)
				{this.pairList.remove(i);}
				pairList.clear();

				logger.info("updateEvent:ANALYZE");
				openButton.setEnabled(true);
				runButton.setEnabled(false);
				stopButton.setEnabled(true);
				pdfButton.setEnabled(false);
				runMenuItem.setEnabled(false);
				stopMenuItem.setEnabled(true);
				pdfMenuItem.setEnabled(false);
				xmlMenuItem.setEnabled(false);
				tsvMenuItem.setEnabled(false);
			}
			else if(e == Event.LOADBEGIN){
				this.state = State.LOADFILE;
				logger.info("Event.LOADBEGIN");
			}
			else {
				  logger.error("no event matched. Event:"+e.toString());
			}
		}

		// programm is analyzing the spectras
		else if(this.state == State.ANALYZE){
			openButton.setEnabled(false);
			logger.info("Event.ANALYZE");
			if(e == Event.DETECTEND){
				logger.info("Event.DETECTEND");
				this.state = State.END;
				if(!records[0].hasBeenStored){storeButton.setEnabled(true);}
				else{storeButton.setEnabled(false);}
				logger.info("DeltaMasses:updateEvent:DETECT_END");
				for(int i=0; i<this.pairList.size(); i++)
					this.pairList.remove(i);
				this.useData();
				if(DMBcanBeReached){
					String bufferText=labelState.getText();
					if(algoParams.DMBautoStore){
						labelState.setText("Storing data in deltaMassBase - this might take a minute. Please wait ...");
						if(records[0].DMBstoreRecord(records[0], pairList,isDiscoveryEdition)){ 
							logger.info("DeltaMasses:put record into DMB:" +
									records[0].getFileName());
							labelState.setText("deltaMassBase storage completed");
						}
						else{
							logger.info("DeltaMasses:not saved to deltaMassBase: " +
									records[0].getFileName());
						}
					}
					else{
						//enable storeButton of this is from a DELTA_CLUSTER_RUN
						if(records[0].load_type==load_type_enum.LOADED_FOR_DELTA_CLUSTER){
							storeButton.setEnabled(true);
							}
						logger.info("not stored in db because autostore parameter switched off.");
					}
					labelState.setText(bufferText);
				}
				else{
					if(algoParams.DMBautoStore){
						labelState.setText("Not stored because I cannot connect to the database.");
					}
					else
					{
						labelState.setText("Not stored because autostore switched off and I cannot connect to the database.");	
					}
				}
				this.analyzationControl[0] = new AnalyzationControl(DeltaMasses.algorithm, DeltaMasses.uniModDB.getModifications(), this, this,algoParams);
				openButton.setEnabled(true);
				runButton.setEnabled(false);
				stopButton.setEnabled(false);
				pdfButton.setEnabled(true);
				runMenuItem.setEnabled(false);
				stopMenuItem.setEnabled(false);
				pdfMenuItem.setEnabled(isDiscoveryEdition);
				xmlMenuItem.setEnabled(isDiscoveryEdition);
				tsvMenuItem.setEnabled(isDiscoveryEdition);
			}
			else if(e == Event.DETECTPROGRESS){
				logger.info("Event.DETECTPROGRESS");
				// TODO Calculate detectionSprogress
			}
			else if(e == Event.DETECTSTOP){
				logger.info("Event.DETECTSTOP");
				this.state = State.READY;
				this.analyzationControl[0] = new AnalyzationControl(DeltaMasses.algorithm, DeltaMasses.uniModDB.getModifications(), this, this,algoParams);
				openButton.setEnabled(true);
				runButton.setEnabled(true);
				stopButton.setEnabled(false);
				pdfButton.setEnabled(false);
				runMenuItem.setEnabled(true);
				stopMenuItem.setEnabled(false);
			}
			else if(e == Event.ERROR){
				this.state = State.READY;
				openButton.setEnabled(true);
				logger.error("Event.ERROR"+e.toString());
				xmlMenuItem.setEnabled(false);
				tsvMenuItem.setEnabled(false);
				pdfMenuItem.setEnabled(false);
				pdfButton.setEnabled(false);
			}
			// error if the event doesn't match
			else {
				logger.error("no correct event found");
			}
		}

		// spectras analyzed, data available
		else if(this.state == State.END){
			if(e == Event.LOADBEGIN){
				// set new state
				this.state = State.LOADFILE;
			}
			else if(e == Event.DETECTBEGIN){
				// set new state
				this.state = State.ANALYZE;
			}

			// error if the event doesn't match
			else {
				// TODO errormanagement
			}
		}
	}


	/**
	 * Method from the interface DeltaMassesScoreEvent Calculate the algorithm progress.
	 * 
	 */
	public void updateProgress(int progress, String text) {
		ProgressBar progressBar = (ProgressBar)this.widgets.get("progressBar");
		Label statusLabel= (Label)this.widgets.get("labelState");
		statusLabel.setText(text);
		if(progress >= 0 && progress <= 100) progressBar.setSelection(progress);
		else progressBar.setSelection(0);
	}

	/**
	 * check the license
	 * return an empty string if everything okay
	 * returns a string with the license problem if there are issues
	 */
	public static String checkLicense(boolean isDiscoveryVersion) {
		String tmpString=""; 
		long nowTimeMilliSec=System.currentTimeMillis();
		logger.info("SYSTEM_TIME="+nowTimeMilliSec);

		//if no ../license/discovery.txt file, return a personal license.
		java.io.File f = new java.io.File("license/discovery.txt");
		String discoveryString=f.getAbsoluteFile().toString();	
		logger.info("checking license file:"+discoveryString);

		File discoveryFile = new File(discoveryString);
		if(! discoveryFile.exists()){
			if(nowTimeMilliSec>expiresTimeMilliSec){
				logger.fatal("License guard says: version expired. please update");
				tmpString ="Expired. Please Update.";
				return(tmpString);
			}
			tmpString="";
			logger.info("License guard returns personal edition. Discovery key is missing");
			return(tmpString);
		}
		//../license/discovery.txt file must exist from here on
		try {
			FileReader licenseFileReader;
			licenseFileReader = new FileReader(discoveryString);
			logger.info("checking for file:"+licenseFileReader.toString());
			BufferedReader buff = new BufferedReader(licenseFileReader);
			String line;
			while(true){
				line = buff.readLine();
				if(line == null){ 
					break;
				}
				if(line.startsWith("REG_")){
					String[] words = line.split("\\s+"); 
					if(line.startsWith("REG_LICENSE_KEY=")){REG_LICENSE_KEY=words[1];}
					if(line.startsWith("REG_LICENSE_NUMBER=")){REG_LICENSE_NUMBER=words[1];}
					if(line.startsWith("REG_MAC=")){REG_MAC=words[1];}
					if(line.startsWith("REG_EDITION=")){}
				}	

			}
		} catch (FileNotFoundException e1) {
			logger.error("checkLicense:DE licensse file not found:" +
					e1.getLocalizedMessage());
			tmpString="error in license checking: registration.txt not found";
			if(nowTimeMilliSec>expiresTimeMilliSec){
				logger.fatal("License guard says: version expired. please update");
				tmpString ="Version Expired. Please update.";
				return(tmpString);
			}
			return tmpString;
		} catch (IOException e2) {
			logger.error("checkLicense:IOException:"+e2.getLocalizedMessage());
			if(nowTimeMilliSec>expiresTimeMilliSec){
				logger.fatal("License guard says: version expired. please update");
				tmpString ="Version Expired. Please update.";
				return(tmpString);
			}
			tmpString="problem while checking your license:IOExeption in registration.txt";
			return tmpString;
		}

		if(REG_LICENSE_KEY.length()==0 || REG_LICENSE_NUMBER.length()==0 || REG_MAC.length()==0){
			logger.fatal("unnatural registration.txt file. Check");
			if(nowTimeMilliSec>expiresTimeMilliSec){
				logger.fatal("License guard says: version expired. please update");
				tmpString ="Version Expired. Please update.";
				return(tmpString);
			}
			return("unnatural registration.txt file. Check");
		}
		else{//only place from which a DE can be activated
			if(checkLicenseKeyOk(REG_MAC,REG_LICENSE_KEY,REG_LICENSE_NUMBER)){
				ArrayList<String> jjooppwef = new ArrayList<String>();
				try {
					jjooppwef=getMacAddresses();
				} catch(Throwable t) {
					logger.error("checkLicenseError:"+t.toString());
				}	

				String jooooooop ="none_found";
				for(int l=0;l<jjooppwef.size();l++){
					if(jjooppwef.get(l).equals(REG_MAC)){
						jooooooop=jjooppwef.get(l);
					}
				}

				if(! jooooooop.equals(REG_MAC)){
					logger.info("MAC address registered:"+REG_MAC+":seen:"+jooooooop);
				}
				String fiewp3039="47474ha"+jooooooop+"4322.aha"+REG_LICENSE_NUMBER+"79x43..j";
				String fiewp3030="";

				try {
					DesEncrypter fffldkseowpas = new DesEncrypter("f89akjl4f2gs443kj.,ff"); 
					fiewp3030 = fffldkseowpas.encrypt(fiewp3039);
				} catch (Exception e) {
					logger.error("Encryption issue in registration.txt reader:" +
							e.toString());
				}

				if(fiewp3030.equals(REG_LICENSE_KEY)){
					tmpString="8494.4343.ji3k.2235.3242.5313";
					logger.info("Discovery Edition License found");				
					return(tmpString);
				}
				else{
					logger.info("license check: no valid Discovery License found:" +
							REG_MAC + "\t" + REG_LICENSE_KEY + "\t" +
							REG_LICENSE_NUMBER + "\t" + jooooooop);
					if(nowTimeMilliSec>expiresTimeMilliSec){
						logger.fatal("License guard says: version expired. please update");
						tmpString ="Version Expired. Please update.";
						return(tmpString);
					}
					tmpString="";
					return(tmpString);
				}
			}
			else{
				if(nowTimeMilliSec>expiresTimeMilliSec){
					logger.fatal("License guard says: version expired. please update");
					tmpString ="Version Expired. Please update.";
					return(tmpString);
				}
				tmpString="No valid Discovery Edition License found";
				logger.info("License guard returns personal edition credentials.");
				return(tmpString);
			}
		}
	}

	private static boolean checkLicenseKeyOk(String REG_MAC,String REG_LICENSE_KEY,String REG_LICENSE_NUMBER){
		return true;
	}

	/**
	 * Fill the data in the gui structures.
	 */
	private void useData(){
		HashMap modList = new HashMap();
		// get the SpectraPairs
		for(int i=0; i<this.pairList.size(); i++)
			this.pairList.remove(i);    
		this.pairList.clear();

		for(SpectraPair pair:this.analyzationControl[0].getAnalyzedPairs())
		{
			this.pairList.add(pair);
		}

		Table modTable = (Table)this.widgets.get("modificationsTable");
		//modTable.setSortDirection(SWT.DOWN);
		//modTable.setSortColumn(modTable.getColumn(0));

		// clear table
		modTable.removeAll();

		// add a non mod row
		TableItem tableItem = new TableItem(modTable, 0);

		int numUnknown=0;//frankp 20061019 count number of unknown mods
		for(SpectraPair pair:this.pairList){
			if (pair.knownModification == null){
				numUnknown++;
			}
		}
		tableItem.setText(new String[] {"None", "", "", "", "", ""+numUnknown});

		// add modificationdata to the table
		for(SpectraPair pair:this.pairList){
			// only new modifications
			if(pair.knownModification != null && !modList.containsKey(pair.knownModification.unimodID)){
				modList.put(pair.knownModification.unimodID, pair.knownModification);

				//frankp 20061019 count number of mods of this type
				int numModsFound=0;
				for(SpectraPair pairb:this.pairList){
					if (pairb.knownModification == pair.knownModification){
						numModsFound++;
					}
				}
				//deltaLog("DeltaMasses:useData:modifications:"+ numModsFound);

				// add data to the table
				// set table data for ths modification
				tableItem = new TableItem(modTable, 0);
				tableItem.setText(new String[] {pair.knownModification.shortName, ""+pair.knownModification.monoisotopic, 
						pair.knownModification.composition, pair.knownModification.fullName, ""+pair.knownModification.unimodID, ""+numModsFound});
			}
		}

		// select row "none" automaticaly 
		modTable.select(0);
	}

	/**
	 * Fill the data in the gui structures.
	 */
	private void useDataFromDbLoader(){
		//this method is a cheap ripoff of the useData method.
		HashMap modList = new HashMap();
		// get the SpectraPairs

		// objectces
		Table modTable = (Table)this.widgets.get("modificationsTable");
		//modTable.setSortDirection(SWT.DOWN);
		//modTable.setSortColumn(modTable.getColumn(0));
		// clear table
		modTable.removeAll();


		for(int i=0; i<this.pairList.size(); i++)
			this.pairList.remove(i);    
		this.pairList.clear();

		for(SpectraPair pair:this.records[0].getPairlist())
		{
			this.pairList.add(pair);
		}

		// add a non mod row
		TableItem tableItem = new TableItem(modTable, 0);

		int numUnknown=0;//frankp 20061019 count number of unknown mods
		logger.debug("usedatafromdbloader:pairlist.size:"+this.pairList.size());
		for(SpectraPair pair:this.pairList){
			if (pair.knownModification == null){
				numUnknown++;
			}
		}
		tableItem.setText(new String[] {"None", "", "", "", "", ""+numUnknown});

		// add modificationdata to the table
		for(SpectraPair pair:this.pairList){
			// only new modifications
			if(pair.knownModification != null && !modList.containsKey(pair.knownModification.unimodID)){
				modList.put(pair.knownModification.unimodID, pair.knownModification);

				//frankp 20061019 count number of mods of this type
				int numModsFound=0;
				for(SpectraPair pairb:this.pairList){
					if (pairb.knownModification == pair.knownModification){
						numModsFound++;
					}
				}
				//deltaLog("DeltaMasses:useData:modifications:"+ numModsFound);

				// add data to the table
				// set table data for ths modification
				tableItem = new TableItem(modTable, 0);
				tableItem.setText(new String[] {pair.knownModification.shortName, ""+pair.knownModification.monoisotopic, 
						pair.knownModification.composition, pair.knownModification.fullName, ""+pair.knownModification.unimodID, ""+numModsFound});
			}
		}

		// select row "none" automaticaly 
		modTable.select(0);
	}

	/**
	 * Prints a text to the splashscreen, and waits a bit ...
	 * @param time
	 * @param text
	 */
	private void splashText(int time, String text,boolean isWarning){	
		Label splashText = (Label)this.splashWidgets.get("textLabel");
		splashText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));		
		if(isWarning){
			splashText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			splashText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		}
		else{
			splashText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			splashText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		}
		splashText.setText(text);
		splashText.update();
		try {
			Thread.sleep(time);
		} catch (InterruptedException e1) {
			logger.error("splashText:interrupted exception:"+e1.getLocalizedMessage());
		}
	}

	private final static ArrayList<String> getMacAddresses() throws IOException {
		try {
			return windowsParseMacAddresses(windowsRunIpConfigCommand());
		} catch (ParseException e) {
			logger.error("getMacAddresses:ParseException:"+e.toString());
			ArrayList<String> justToReturnTrash = new ArrayList<String>();
			justToReturnTrash.add("parse-exception");
			return justToReturnTrash;
		}
	}

	private final static ArrayList<String> windowsParseMacAddresses(String ipConfigResponse) throws ParseException {
		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;
		ArrayList<String> macAddresses = new ArrayList<String>();

		while(tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			int macAddressPosition = line.indexOf(":");
			if(macAddressPosition <= 0) continue;

			String macAddressCandidate = line.substring(macAddressPosition + 1).trim();
			if(windowsIsMacAddress(macAddressCandidate)) {
				macAddresses.add(macAddressCandidate.toUpperCase());
				lastMacAddress = macAddressCandidate.toUpperCase();
				continue;
			}
		}
		if(lastMacAddress == null){
			String tmp="no MAC address found";
			macAddresses.add(tmp);
			logger.error("windowsParseMacAdresses:no MAC address found");
		}
		return macAddresses;
	}

	private final static boolean windowsIsMacAddress(String macAddressCandidate) {
		if(macAddressCandidate.length() != 17){return false;}
		if(macAddressCandidate.equalsIgnoreCase("00-00-00-00-00-00")){
			logger.warn("windowsParsemacAdresses:LicenseDialogHandler:macAdressCandidate seems to be a video card:"+macAddressCandidate);
			return false;
		}
		if(macAddressCandidate.toUpperCase().matches("[A-Z0-9][A-Z0-9]-[A-Z0-9][A-Z0-9]-[A-Z0-9][A-Z0-9]-[A-Z0-9][A-Z0-9]-[A-Z0-9][A-Z0-9]-[A-Z0-9][A-Z0-9]")){
			logger.info("windowsParseMacAdresses:mac adress ok:"+macAddressCandidate);
			return true;
		}
		else{
			return false;
		}
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

	static SelectionAdapter pdfSelectionListener = new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e){
			Toolkit.getDefaultToolkit().beep();
		}
	};

	static SelectionAdapter pdfHandler = new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e){
			String docTarget=records[0].getFileName()+".deltaMasses.pdf";
			if (ExternalFileHandler.open(docTarget)!=0) {
				logger.error("pdfHandler:Acrobat Reader not installed or file association for pdf not existing");
			}
		}
	};	

	public final static void setMarkedButton(Boolean isMarked){
		if(DeltaMasses.pairMarkedButton == null || DeltaMasses.pairUnMarkedButton==null || !DMBcanBeReached){
			return;//otherwise crash possible below
		}
		if(isMarked ){//you can save a new comment and delete the comment
			DeltaMasses.pairMarkedButton.setEnabled(true);
			DeltaMasses.pairUnMarkedButton.setEnabled(true);
			DeltaMasses.pair_is_marked_Label.setText("yes");
		}
		else{
			DeltaMasses.pairMarkedButton.setEnabled(true);//you can comment on the pair
			DeltaMasses.pairUnMarkedButton.setEnabled(false);//but you cannot delete the comment - there is none.
			DeltaMasses.pair_is_marked_Label.setText("no");
		}
	}	

	public static void recordLoader(String loadString, String loadValueString){
		int loadInteger=0;
		double loadDouble=0.0;
		statusLabel.setText("trying to load "+loadString+" from deltaMassBase. Please wait a minute.");
		boolean parsingOK=false;
		try{
			if(loadString.equals("record")      || 
					loadString.equals("peptideNet")  ||
					loadString.equals("pair ID"))
			{
				loadInteger=Integer.parseInt(loadValueString);
			}
			else if(loadString.equals("deltaMass")){
				loadDouble=Double.parseDouble(loadValueString);
			}
			parsingOK=true;
			statusLabel.setText("trying to load "+loadString+" from deltaMassBase. Please wait a minute.");
		}
		catch (Exception e1) {
			statusLabel.setText("Please excuse, could not parse "+loadString+" from \""+loadValueString+"\"");
			logger.error("loadRecordFromDBAdapter Exception:"+e1.toString());
		}
		if(parsingOK){ 
			if(loadString.equals("record")){loadString=records[0].loadRecordFromDeltaMassBase(loadInteger,DeltaMasses.uniModDB.getModifications());}
			else if(loadString.equals("peptideNet")){loadString=records[0].loadPeptideNetFromDeltaMassBase(loadInteger,DeltaMasses.uniModDB.getModifications());}
			else if(loadString.equals("deltaMass")){loadString=records[0].loaddeltaMassFromDeltaMassBase(1,DeltaMasses.uniModDB.getModifications(), loadDouble  ,0.01);}//TODO mass accuracy to be added
			else if(loadString.equals("protein")){loadString=records[0].loadProteinFromDeltaMassBase(DeltaMasses.uniModDB.getModifications(),loadValueString.trim());}
			else if(loadString.equals("pair ID"))     {loadString=records[0].loadpairIDFromDeltaMassBase(loadInteger, DeltaMasses.uniModDB.getModifications());}
			else if(loadString.equals("marked pairs")){loadString=records[0].loadMarkedPairsFromDeltaMassBase(DeltaMasses.uniModDB.getModifications());}	
			else if(loadString.length()<3){loadString=" please choose a load method to the right of the load button";}
			else{
				loadString=" unsupported load type";
				logger.error("unsupported load type");
			}
			if(loadString.equals("OK")){
				logger.info("loaded "+loadString+" from DB, OK");
			}
			else{
				logger.info("loading "+loadString+" from DB  not OK:"+loadString);
				statusLabel.setText("loading "+loadString+" not ok: "+loadString);
			}		
		}
	}

	public static void setDelays(boolean debug){
		loading_delay = 500;
		preferences_delay = 100;
		unimod_delay = 200;
		algorithms_delay = 200;
		handlers_delay = 100;
		cleanup_delay = 200;
		edition_Delay = 1250;
		DMB_delay=501;
		if(debug == true || isDiscoveryEdition){
			DMB_delay=0;
			loading_delay = 0;
			preferences_delay = 0;
			unimod_delay = 0;
			algorithms_delay = 0;
			handlers_delay = 0;
			cleanup_delay = 0;
			edition_Delay=0;
		}
	}
	
	public static void setLogger(){
		try{
		java.io.File ffff = new java.io.File("config/log4j.properties");
		PropertyConfigurator.configure(ffff.getAbsoluteFile().toString());
		logger.info("-------------------------------------------------------------------------");
		logger.info("starting deltaMasses version:" + DeltaMasses.version + " " + DeltaMasses.build + " Discovery Edition:" + isDiscoveryEdition);
		logger.info("expires:"+expiresString);
		logger.info("-------------------------------------------------------------------------");
		Properties pr = System.getProperties();
		TreeSet propKeys = new TreeSet(pr.keySet());  // TreeSet sorts keys
		
		for (Iterator it = propKeys.iterator(); it.hasNext(); ) {
			String tmpText="SYSTEM_PROPERTIES: ";
			String key = (String)it.next();
			tmpText += "" + key + "=" + pr.get(key) + " ";
			logger.info(tmpText);	     
		}
		
		logger.info("-------------------------------------------------------------------------");
		}catch(Exception e){
			logger.error("setLogger:"+e.toString());
		}
	}
	
	private static void setStoreButton(){
		Image result = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on.png"));
		storeButton.setImage(result);
		result = new Image(Display.getCurrent(), ClassLoader.getSystemResourceAsStream("com/detectorvision/images/24-on-yellow.png"));
		storeButton.setHotImage(result);
	}
	
	
	private static  void infoDialog(String message){
	
	}	
	
}
