/* $Id: AutomationControl.java 380 2010-08-24 16:34:29Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.analyzation.BaseAlgorithm;
import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.analyzation.ScoreData;
import com.detectorvision.massspectrometry.analyzation.ScoreException;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.datacontrol.DefaultRecordLoader;
import com.detectorvision.massspectrometry.datacontrol.MascotXMLRecordLoader;
import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;
import com.detectorvision.utility.DeltaMassBase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import org.eclipse.swt.widgets.Display;

public class AutomationControl implements Runnable, ProgressListener{

	private ProgressListener progressListener;
	private EventListener eventListener;
	private Display display;
	private AlgoParams algoParams;
	private ArrayList<String> FilesToProcess;
	private Thread thread;
	private boolean isAlgorithmStopped = false;
	// Logging with log4j
	static Logger logger = Logger.getLogger(AutomationControl.class.getName());

	/**
	 * Constructor, loads the analizationclass.
	 */
	public AutomationControl(ArrayList<String> FilestoProcess, EventListener eventListener, ProgressListener progressListener,AlgoParams algoParams){
		this.progressListener = progressListener;
		this.eventListener = eventListener;
		this.display = Display.getCurrent();
		this.algoParams = algoParams;
		this.FilesToProcess =FilesToProcess;
	}

	/**
	 * Method for runnung an own thread.
	 */
	public void run(){
		ScoreData data;
		try {
			AsyncEventListenerRunnable async;
			// start the algorithm
			updateProgress(0 , "starting PTM detection...");
			async = new AsyncEventListenerRunnable(Event.DETECTBEGIN, this.eventListener);
			display.asyncExec(async);			 
			Thread.sleep(750);
			logger.debug("running process");

			int countToDo=0;//how many do we have to do ? (we need this for the progress calculation below)
			for(int loop=0;loop<FilesToProcess.size();loop++){
				if(FilesToProcess.get(loop).endsWith(".mgf")  || FilesToProcess.get(loop).endsWith(".xml")){
					countToDo++;
				}
			}
			
			int numDone=0;
			for(int loop=0;loop<FilesToProcess.size();loop++){
				if(FilesToProcess.get(loop).endsWith(".mgf")  || FilesToProcess.get(loop).endsWith(".xml")){
					logger.debug("processing:"+FilesToProcess.get(loop));
					
					String bareFile = FilesToProcess.get(loop).replaceFirst(".*/", "");
					
					int progress = (int)(100.00*(double)numDone/(double)countToDo);
					updateProgress(progress, "processing "+bareFile);
					
					
					logger.info("Starting to process: " + bareFile);
					File testFile= new File (FilesToProcess.get(loop));
					if(testFile.exists()){
					mascotProcessXml(FilesToProcess.get(loop));
					}
					else{
						logger.warn("File does not exist: " + FilesToProcess.get(loop));
					}
					numDone++;
					
					progress = (int)(100.00*(double)numDone/(double)countToDo);
					updateProgress(progress, "finished "+bareFile);
					
					logger.info("Finished: " + bareFile);
				}
			}
			this.updateProgress(100, "automation run completed");
			async = new AsyncEventListenerRunnable(Event.DETECTEND, this.eventListener);
			display.asyncExec(async);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the algorithm an calculate the related pairs with their modification. The thread
	 * has to be a gui thread, else it has no access to the monitorguis.
	 * @param record the read record.
	 * @param maxPairs max pairs which ist allowed to return.
	 */
	public void startAutomation(ArrayList<String> FilesToProcess, AlgoParams algoParams){
		logger.debug("AutomationControl started");
		// save the parameters
		this.FilesToProcess = FilesToProcess;
		this.algoParams = algoParams;
		// start up the algorithm as displaythread
		//org.eclipse.swt.widgets.Display.getCurrent().asyncExec(this);
		if(this.thread == null || isAlgorithmStopped == true){
			this.thread = new Thread(this);
			this.thread.start();
		}
	}

	/**
	 * Sets the algorithm on a wait state.
	 * @throws InterruptedException
	 */
	public void pauseAlgorithm() throws InterruptedException{
		org.eclipse.swt.widgets.Display.getCurrent().getSyncThread().wait();
	}

	/**
	 * Continue the algrithm.
	 */
	public void continueAlgorithm(){
		org.eclipse.swt.widgets.Display.getCurrent().getSyncThread().notify();
	}

	/**
	 * Stop the algorithm.
	 */
	public void stopAlgorithm(){
		isAlgorithmStopped = true;
		// stop the displaythread
		if(this.thread != null){
			this.thread.stop();
			// update statemachine
			AsyncEventListenerRunnable async;
			async = new AsyncEventListenerRunnable(Event.DETECTSTOP, this.eventListener);
			display.asyncExec(async);
			this.updateProgress(0, "PTM detetction stopped by user");
		}
	}

	public void updateProgress(int progress, String text) {
		AsyncUpdateRunnable async = new AsyncUpdateRunnable(progress, text, this.progressListener);
		//Display display = Display.getCurrent();
		if(display == null){
			//System.out.println("Display is null");
			return;
		}
		logger.debug("updateprogressFrank:"+text);
		display.asyncExec(async);
	}

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
			logger.error("Malformed URL exception while loading algorithms.jar: " +
									 e1.getLocalizedMessage());
		}
		urlClassLoader = new URLClassLoader(externalJars);
		// load the algorithm class
		try {
			algorithm = (DeltaMassesScore)urlClassLoader.loadClass(preferences.getAlgorithmClass()).newInstance();
		} catch (InstantiationException e) {
			logger.error(e.getLocalizedMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			logger.error(e.getLocalizedMessage());
		}

		try {
			uniModDB = new UniModDatabase(preferences.getUniModFile());
		} 
		catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
		logger.info("loaded unimod.xml file");

		if (logger.isDebugEnabled()) {
			logger.debug("testing connection to deltaMassBase");
			if (DeltaMassBase.validConnection())
				logger.debug("Connection to deltaMassBase OK");
			else
				logger.debug("Connection to deltaMassBase not established");
		}

		try {
			logger.debug("loading mascot record:"+outFile);
			if(outFile.endsWith(".xml")){
				logger.info("Loading xml: " + outFile);
				ProgressListener notUsedListener = null;
				record=MascotXMLRecordLoader.loadMascotRecord(outFile,notUsedListener);
			}
			else if(outFile.endsWith(".mgf")){
				logger.info("Loading .mgf: " + outFile);
				DefaultRecordLoader drl = new DefaultRecordLoader();
				ProgressListener notUsedListener=null;
				record=drl.loadRecord(outFile,notUsedListener);
				drl=null;
			}
			else{
				logger.error("SYSTEM_ERROR:no proper ending of outfile: " + outFile);
				return false;		
			}
			logger.info("Back from loading");

		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
		logger.debug("Loaded: " + record.getFileName());

		//Analyze ---------------------------------------------------------
		ScoreData data = null;
		BaseAlgorithm baseAlgo = new BaseAlgorithm();
		try{
			AlgoParams algoParams = new AlgoParams();
			algoParams.getDefault();
			ArrayList<Modification> modList = new ArrayList<Modification>();
			modList=uniModDB.getModifications();
			logger.debug("Starting to analyze:"+record.getFileName());
			com.detectorvision.massspectrometry.analyzation.ProgressListener listener = null; 	
			record.setMsmsPrecision((float)algoParams.msmsAccuracy);//TODO check if this is still used, probably not.
			record.setMsPrecision((float)algoParams.msAccuracy);//TODO check if this is still used, probably not.
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

}
