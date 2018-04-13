/* $Id: AnalyzationControl.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.deltaMasses.analyzationmanagement;

import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.analyzation.ScoreData;
import com.detectorvision.massspectrometry.analyzation.ScoreException;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.unimod.Modification;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.eclipse.swt.widgets.Display;

public class AnalyzationControl implements Runnable, ProgressListener{
	
	// Attributs
	private DeltaMassesScore algorithm;
	private ArrayList<Modification> modList;
	private ProgressListener progressListener;
	private EventListener eventListener;
	private Record record;
	private Display display;
	private AlgoParams algoParams;
	
	// Result
	private ArrayList<SpectraPair> pairList;
	private int[] histogram;
	
	// Thread
	private Thread thread;

	// Logging with log4j
	static Logger logger = Logger.getLogger(AnalyzationControl.class.getName());

	 
	/**
	 * Constructor, loads the analizationclass.
	 */
	public AnalyzationControl(DeltaMassesScore algorithm, ArrayList<Modification> modList, EventListener eventListener, ProgressListener progressListener,AlgoParams algoParams){
		// save the parameters
		this.algorithm = algorithm;
		this.modList = modList;
		this.progressListener = progressListener;
		this.eventListener = eventListener;
		this.display = Display.getCurrent();
		this.algoParams = algoParams;
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

			data = this.algorithm.analyze(record, modList, this, algoParams);
			if(data != null){
				this.pairList = data.spectraPairList;
				this.histogram = data.histogram;
			}
			
			int numDetPairs=0;//count number of pairs
			for(SpectraPair tmpPair:this.pairList){
				numDetPairs++;
			}
			int tmp = record.getSpectrumList().size()*(record.getSpectrumList().size()-1)/2;
			this.updateProgress(0, "PTM-comparing "
					               +  String.format("%,d",tmp) + " pairs. Done: "
					               //+  this.record.getFileName()
					               + ". Detected " 
					               + String.format("%,d",numDetPairs) + " potential PTM pairs.");
			async = new AsyncEventListenerRunnable(Event.DETECTEND, this.eventListener);
			display.asyncExec(async);
		} catch (ScoreException e) {
			logger.error("ScoreException:"+e.toString());
		}
		catch (InterruptedException e) {
			logger.error("InterruptecException:"+e.toString());
		}
	}
	
	/**
	 * Starts the algorithm and calculates the related pairs with their modification. The thread
	 * has to be a gui thread, else it has no access to the monitorguis.
	 * @param record the read record.
	 * @param maxPairs max pairs whitch ist allowed to return.
	 */
	public void startAlgorithm(Record record, AlgoParams algoParams){
	
		// save the parameters
		this.record = record;
		this.algoParams = algoParams;
		
		// start up the algorithm as displaythread
		//org.eclipse.swt.widgets.Display.getCurrent().asyncExec(this);
		if(this.thread == null){
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
		// stop the displaythread
		if(this.thread != null){
			logger.info("thread stopped");
			this.thread.stop();
			
			// update statemachine
			AsyncEventListenerRunnable async;
			async = new AsyncEventListenerRunnable(Event.DETECTSTOP, this.eventListener);
			display.asyncExec(async);
			this.updateProgress(0, "PTM detetction stopped by user");
		}
	}
	
	/**
	 * Returns a List of matching pairs.
	 * @return ArrayList of Pairs.
	 */
	public ArrayList<SpectraPair> getAnalyzedPairs(){
		return this.pairList;	
	}
	
	/**
	 * Returns a histogram of the analization
	 * @return
	 */
	public int[] getHistogram(){
		return histogram;
		
	}

	public void updateProgress(int progress, String text) {
		AsyncUpdateRunnable async = new AsyncUpdateRunnable(progress, text, this.progressListener);
		//Display display = Display.getCurrent();
		if(display == null){
			logger.warn("Display is null");
			return;
		}
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
}
