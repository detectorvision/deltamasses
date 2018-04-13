/* $Id: BaseAlgorithm.java 409 2010-12-26 10:35:48Z frank $ */

//(c) 2006 by Detectorvision AG
//Technoparkstrasse 1
//CH 8005 Zurich
//www.detectorvision.com

//version 1.0   20061002  14:29 CET frank@detectorvision.com
//version 1.0.1 20061006  11:26 CET frank added control preventing more than maxPairs being created.
////TODO add better logical handling of this

package com.detectorvision.massspectrometry.analyzation;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;
import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.analyzation.ScoreData;
import com.detectorvision.massspectrometry.analyzation.ScoreException;
import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.RecordChargeInfo;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.biodata.Spectrum;
import com.detectorvision.massspectrometry.unimod.Modification;

import com.detectorvision.utility.BitSpec;
import com.detectorvision.utility.pdf.HeaderFooter;

import java.awt.Color; 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import org.apache.log4j.Logger;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public final class BaseAlgorithm implements DeltaMassesScore{ 

	static public ArrayList<Double> modMasses=new ArrayList<Double>();
	static boolean testThis;
	static boolean modKnown=false;
	static boolean msmsBetterPrecisionThanMs=false;
	static double maxRandomProbability=1.0/100000000;
	static AlgoParams detectorParameters;
	public ArrayList<RecordChargeInfo> recordChargeInfo= new ArrayList<RecordChargeInfo>();//MARKER_5 
	//private double [][][] binom = new double[76][76][101];
	static Logger logger = Logger.getLogger(BaseAlgorithm.class.getName());
	private ArrayList <Modification> modList = null;
	private ProgressListener listener = null;
	private AlgoParams algoParams = null;

	private ArrayList<Spectrum> spectraList = new ArrayList<Spectrum>();
	private ArrayList<SpectraPair> pairList  = null;
    long numPairs = 0;
	long countdone = 0;
	int percentage = 0;
	int debugg_found_hits = 0;
	static int countX = 0;
	int chargeBlock=0;
	int noIter = 1;
	
	long comparisonFreq = 300; // change Comparison frequency to speed up the processing time 
	// 300 * 300 comparisons per thread 
	
	public ScoreData analyze(Record record, ArrayList modList,
			 ProgressListener listener,AlgoParams algoParams) throws ScoreException {
		pairList = new ArrayList<SpectraPair>();
		int counter = 0;
    	long start , end , diff, mod;
		this.modList = modList;
		this.listener = listener;
		this.algoParams = algoParams;
		countX = 0;
		
		Thread t1[] = null;
		logger.info("mark5");
		
		this.detectorParameters = algoParams;
		this.detectorParameters.setScoring();
		detectorParameters.printParams();
		ScoreData data = new ScoreData();
		if(detectorParameters.superFastDetectionLazy){
			maxRandomProbability=1.0/100000;
		}
		
		spectraList = record.getSpectrumList();		
		//MARKER_5-----------------start------------------------------------------------
		//sorting the spectra with respect to charge
		//set up a list where the charge changes in the list (recordChargeInfo)
		//set the bitspectra for all spectra, we need it further down below.
		if(algoParams.setProgress){; 
		listener.updateProgress(0, "Sorting spectrumList");
		}
		Collections.sort(spectraList); 
		if(algoParams.setProgress){; 
		listener.updateProgress(0, "Sorting done");
		}

		int lastCharge=spectraList.get(0).charge;
		int lastStart=0;
		recordChargeInfo.clear();
		boolean foundSeveralCharges=false;
		for(int i=0;i<spectraList.size();i++){
			if(spectraList.get(i).charge != lastCharge || i==spectraList.size()-1){
				//charge has changed or we are at the end of the last charge block.
				foundSeveralCharges=true;
				RecordChargeInfo tmpInfo = new RecordChargeInfo();
				tmpInfo.charge=lastCharge;
				tmpInfo.start_index=lastStart;
				tmpInfo.end_index=i-1;
                            if(i==spectraList.size()-1){tmpInfo.end_index=i;}
				recordChargeInfo.add(tmpInfo);
				lastCharge=spectraList.get(i).charge;
				lastStart=i;
				logger.info("chargeInfo charge:"+tmpInfo.charge+" start:"+tmpInfo.start_index+" end:"+tmpInfo.end_index);
				System.out.println("chargeInfo charge:"+tmpInfo.charge+" start:"+tmpInfo.start_index+" end:"+tmpInfo.end_index);
			}
		}
		//set the bitspectrum
		for(int i=0;i<spectraList.size();i++){
			Spectrum tmpSpec = new Spectrum();
			tmpSpec=spectraList.get(i);
			tmpSpec.setBit();
			spectraList.set(i, tmpSpec);
		}
		//MARKER_5-----------------end------------------------------------------------

		Date startDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
		String formattedDate = formatter.format(startDate);
		logger.info("analyze_start_at:"+formattedDate);
	
		data.spectraPairList = pairList;
		int []scoreHist = new int[20];

		record.setMsmsPrecision((float)detectorParameters.msmsAccuracy);
		record.setMsPrecision((float)detectorParameters.msAccuracy);
		if(detectorParameters.msAccuracy<= detectorParameters.msmsAccuracy){//this is for security reasons only - if someone forgot to set it correctly ..
			record.msmsBetterPrecisionThanMs=true;
			this.msmsBetterPrecisionThanMs=true;
		}
		else{
			record.msmsBetterPrecisionThanMs=false;
		}

		for(int k=1;k<modList.size();k++){
			boolean newMass=true;
			for(int l=0;l<modMasses.size();l++){
				if(modMasses.get(l)==Math.abs(((Modification)modList.get(k)).monoisotopic)){
					newMass=false;
					break;
				}
			}
			if(newMass){
				modMasses.add(Math.abs(((Modification)modList.get(k)).monoisotopic));
			}
		}

		Collections.sort(modMasses);
		if (logger.isDebugEnabled()) {
			for(int k=0;k<modMasses.size();k++){
				logger.debug("modMass:" + k + ":" + modMasses.get(k));
			}
		}
		
		numPairs=(long)spectraList.size()*(long)(spectraList.size()-1)/2;

		logger.debug("BaseAlgorithm:maxRandomProb: " + maxRandomProbability +
				"  DiscoveryEdition:" + algoParams.isDiscoveryEdition);

		if(algoParams.setProgress){
			String tmpString = String.format("%,d",numPairs);
			String tmpString2 = String.format("%,d",spectraList.size());
			listener.updateProgress(0, "Preparing to compare " + tmpString2 + " spectra, i.e. " + tmpString + " MSMS spectrum pairs");
		}
		try{
			Thread.sleep(1000);
		}
		catch(InterruptedException e){
			logger.info("Sleep Interrupted");
		}
		debugg_found_hits = 0;
		noIter = getNoofIterations();
     	t1 = new Thread[noIter];
     	// Runtime runtime = Runtime.getRuntime();
     	// int nrOfProcessors = runtime.availableProcessors();
     	// ExecutorService execSvc = Executors.newFixedThreadPool(nrOfProcessors);
     	ExecutorService execSvc = Executors.newFixedThreadPool(noIter); 
    	for(chargeBlock=0;chargeBlock<recordChargeInfo.size();chargeBlock++)
    	{
    		ArrayList<Integer> chBlockSt =  new ArrayList<Integer>(); 
         	ArrayList<Integer> chBlockEnd = new ArrayList<Integer>(); 
    		start = recordChargeInfo.get(chargeBlock).start_index;
			end = recordChargeInfo.get(chargeBlock).end_index;
			diff = end - start+ 1;
			long iterations = diff/comparisonFreq;
			mod = diff % comparisonFreq;
			if( mod != 0) iterations++;
			long st = start;
			for(int aa= 0 ; aa < iterations ; aa++)
			{
				final int compStart = (int)(st + aa * comparisonFreq);
				int compEnd = (int)( st + comparisonFreq * (aa+1) - 1);
				
				if( aa == iterations-1 && mod != 0){
					 compEnd = (int) (compStart + mod-1);
				}
				chBlockSt.add(compStart);
				chBlockEnd.add(compEnd);
				final int cEnd = compEnd;
				final int x = counter;
				if(x < noIter)
				{
					t1[counter]=new Thread(new Runnable(){
						public void run()
						{
							multiThread(x, compStart, cEnd ,-1, -1);
						}
					});
					execSvc.execute(t1[x]);
				}
     	    	counter++;
				
			}//end of aa
			if(iterations > 1)
			{
				for(int k= 0 ; k < chBlockSt.size() ; k++)
					{
					  final	int compStart1=chBlockSt.get(k);
					  final int compEnd1 = chBlockEnd.get(k); 	 
					  for(int l = k+1 ; l < chBlockSt.size(); l++)
					  {
						final int compStart2=chBlockSt.get(l);
					   	final int compEnd2 = chBlockEnd.get(l);
					   	final int x1 = counter;
						if(x1< noIter)
							t1[counter]=new Thread(new Runnable(){
	     	    		
								public void run()
								{
									multiThread(x1, compStart2,compEnd2,compStart1, compEnd1);
								}
							});
						execSvc.execute(t1[x1]);
						counter++;
					  }
					}
				
			}
    	 }//end of chargeBlock 
     	execSvc.shutdown();
     	//Wait for the sub threads to get completed
   		while (!execSvc.isTerminated()) {
   		}
    	countX = 0;
		Date endDate = new java.util.Date();
		formattedDate = formatter.format(endDate);
		logger.info("analyze_ready_at:"+formattedDate);
		try{
			if(algoParams.setProgress){
				listener.updateProgress(percentage, "Ready with analyzing !!!" + spectraList.size() + " spectra, i.e. " + numPairs + " spectrum pairs");
			}
			Thread.sleep(500);
		}
		catch(InterruptedException e){
			logger.info("Sleep Interrupted");
		}
		data.histogram= scoreHist;
		data.spectraPairList= pairList;

		if(true){
			if(algoParams.setProgress){
				listener.updateProgress(percentage, "Please wait - exporting pdf to:"+record.getFileName()+".deltaMasses.pdf");
			}
			logger.debug("Printing PDF for: " + record.getFileName());
			printPdf(record, data, modList, algoParams);
			logger.debug("PDF ready for: " + record.getFileName());
			if(algoParams.setProgress){
				listener.updateProgress(percentage, "Analyzed "+numPairs+" pairs; pdf exported to "+record.getFileName()+".deltaMasses.pdf");
			}
		}

		logger.debug("BaseAlgorithm:returning to application:detected pairs: " +
				data.spectraPairList.size());
		return data;
	}
	
	private int getNoofIterations()
	{
		long start , end , diff, mod;
		int noofIterations = 1;
		for(chargeBlock=0;chargeBlock<recordChargeInfo.size();chargeBlock++)
    	{ 
     		start = recordChargeInfo.get(chargeBlock).start_index;
     		end = recordChargeInfo.get(chargeBlock).end_index;
     		diff  =end - start+ 1;
     		noofIterations += diff/comparisonFreq;
     		mod = diff % comparisonFreq;
     		if( mod != 0) noofIterations++;
    	}
     	int diffIter = noofIterations - recordChargeInfo.size();
     	if(diffIter != 0)
     	{
     		noofIterations = noofIterations + ( diffIter * (diffIter -1)) /2;
     	}
     	return noofIterations;
	}
	
    private void multiThread(int x, int st1,int end1, int st2, int end2)
	{
    	 Spectrum spec1,spec2;
		 double r1;
		 double p1;
		 int n11;
		 int n21;
		 int equalHit1;
		 double  p1_equal;
		 ////////////////////////only used in Franks area below end
		 double dmSigned=0;
		 double range=0;
		 double tmpScore=0;
		 int count_found_pairs=0;
		 SpectraPair helpPair = new SpectraPair();
		 int i=0 , j=0;
		 final int totalCount = noIter;
		 
         if(x < totalCount)
    	 {	
        	 	long stTime = System.currentTimeMillis();
				 for(i=st1; i<= end1; i++){
					spec1=spectraList.get(i);
					if(st2 == -1 )
					{
						j = i+1;
						end2= end1;
					}
					else
					{
						j =st2;
					}
		     		for(;j<=end2; j++){
			     	   			//compare spectrum i and j
			     	   			range= Math.min(spec1.maxMZ,spectraList.get(j).maxMZ)-Math.max(spec1.minMZ,spectraList.get(j).minMZ);					
			     	   			if(range>=500){	
			     	   				spec2=spectraList.get(j); 
			     	   				
			     	   					if(detectorParameters.identifiedPairsOnly && (spec1.pepSequence == null && spec2.pepSequence == null)){
			     	   					continue;
			     	   					}
			     	   				dmSigned=spec2.precursorMass-spec1.precursorMass;
			     	   					helpPair=null;
			     	   					if(detectorParameters.superFastDetection){
			     	   					if(Math.abs(Math.abs(dmSigned)-detectorParameters.superFastDeltaMass)>detectorParameters.msAccuracy){
			     	   					continue;
			     	   						}	
			     	   					}
			     	   					if(dmSigned>=13.5 || dmSigned<=-13.5){
			     	   						if(BitSpec.compare(spec1.bit4, spec2.bit4,10)){
			     	   							if(BitSpec.compare(spec1.bit, spec2.bit,10)){
			     	   								{  
			     	   									//Franks area will speed up this code by a lookup method////////////////////////start
			     	   									r1= Math.min(spec1.maxMZ,spec2.maxMZ)-Math.max(spec1.minMZ,spec2.minMZ);				
			     	   									n11=spec1.valueList.size();
			     	   									n21=spec2.valueList.size();
			     	   									p1=Math.min( (2 * detectorParameters.msmsAccuracyScoring * n21)/r1, 1);
			     	   									equalHit1=BitSpec.getEqualOverlap(spec1.bit, spec2.bit);
			     	   									///////////////////////////////////////////////////////////////////////////////////
			     	   									p1_equal=binomialIntegrated(n11, p1 ,equalHit1);//time-consuming mathematics///////
			     	   									///////////////////////////////////////////////////////////////////////////////////
			     	   									if(p1_equal>maxRandomProbability){continue;}
			     	   									//Franks area will speed up this code by a lookup method/////////////////////////end
			     	   								}
			     	   								if(dmSigned>=13.5){
			     	   									helpPair=ptm_score(spec1,spec2,dmSigned);
			     	   								}
			     	   								else {//dmSigned is <=-13.5
			     	   									helpPair=ptm_score(spec2,spec1,-dmSigned);
			     	   								}
			     	   							}
			     	   						}
			     	   					}
			     	   					{//---------------------------------------AFTER SCORE ROUTINE
			     	   						if(helpPair != null ){
			     	   							tmpScore=helpPair.score;
			     	   							if(tmpScore>0){
			     	   								if(count_found_pairs < algoParams.maxPairs){//20061006 frankp dont add more than maxPairs pairs 
			     	   									//create pair
			     	   									debugg_found_hits++;
			     	   									SpectraPair tmpPair = new SpectraPair();
			     	   									if (spec1.precursorMass>=spec2.precursorMass){//ensure that spectrumA lighter than spectrumB
			     	   										tmpPair.spectrumA = spec2;
			     	   										tmpPair.spectrumB = spec1;
			     	   										helpPair.deltaMass=spec1.precursorMass-spec2.precursorMass;
			     	   									}
			     	   									else{
			     	   										tmpPair.spectrumA = spec1;
			     	   										tmpPair.spectrumB = spec2;
			     	   										helpPair.deltaMass=spec2.precursorMass-spec1.precursorMass;
			     	   									}
			     	   									tmpPair.score = tmpScore;
			     	   									tmpPair.marked=false;
			     	   									tmpPair.pair_id=-1;//unknown at this point
			     	   									tmpPair.specnet_id=-1;//unknown at this point
			     	   									tmpPair.comment="";
			     	   									tmpScore=0;
			     	   									helpPair.knownModification = null;//for code clarity
			     	   								
			     	   									//search if we have a reasonable modification
			     	   									double tmpDeltaMass=Math.abs(spec1.precursorMass-spec2.precursorMass);
			     	   									double tmpBestMatch=Double.MAX_VALUE;
			     	   									double tmpDist;
			     	   									for(int k=modList.size()-1;k>=0;k--){
			     	   										tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
			     	   										if( tmpDist<detectorParameters.msAccuracy){
			     	   											if(tmpDist<=tmpBestMatch){
			     	   												tmpBestMatch=tmpDist;
			     	   												helpPair.knownModification=(Modification)modList.get(k);
			     	   											}
			     	   										}
			     	   									}
			     	   									pairList.add(helpPair);
			     	   									count_found_pairs++;
			     	   								}
			     	   							}
			     	   						}
			     	   					
			     	   				}
			     	   			}
			     	   		}//end of j-loop
		     
			     			
			     	   	 }//end of i
				 if(st2 == -1 )
					{
						countX += end1 - st1 + 1;
						countdone=numPairs - ((long)((long)spectraList.size()-countX)*(long)((long)spectraList.size()-countX-1))/2;
						long tmp=numPairs-countdone;
								percentage=(int)(100*((float)countdone/(float)numPairs));
								if(percentage==0){percentage=1;}
								String tmpString = String.format("%,d",numPairs);
								String tmpString2 = String.format("%,d",spectraList.size());
								String tmpString3 = String.format("%,d",tmp);
								String tmpString4 = String.format("%,d",countdone);
								if(algoParams.setProgress){
									listener.updateProgress(percentage, 
											"Spectra: " + tmpString2 + 
											"  PTM-analyzed " +tmpString4 + 
											" of " + tmpString + 
											" pairs, " + tmpString3 + 
											" remaining. " + percentage + "% ready" + 
											" pairs detected:" + debugg_found_hits);
									
								}	
					}
				/*	if(i%1000==0){
						logger.info("Memory information free:"+Runtime.getRuntime().freeMemory()+" max:"+Runtime.getRuntime().maxMemory()+ " total:"+Runtime.getRuntime().totalMemory());
					}*/
    	 	}
	}

 
	//warning: this is not a pure function call - crosstalking using global parameters of this class
	 static SpectraPair ptm_score(Spectrum spec1, Spectrum spec2,double dm){
		//spectra come in as spec1=light spec2=heavy
		int equalHit=0;  //how many times do we hit the same?
		{
			for(MSMS tmpMSMSa:spec1.valueList){	
				for(MSMS tmpMSMSb:spec2.valueList){	
					if(Math.abs(tmpMSMSa.massToCharge - tmpMSMSb.massToCharge)<detectorParameters.msmsAccuracy){
						equalHit++;break;
					}
				}
			}
		}
		if(equalHit<10){return(null);}

		double range= Math.min(spec1.maxMZ,spec2.maxMZ)-Math.max(spec1.minMZ,spec2.minMZ);				
		double p=0;       //probability for a random hit
		int n1=spec1.valueList.size();
		int n2=spec2.valueList.size();
		p=Math.min( (2 * detectorParameters.msmsAccuracyScoring * n2)/range, 1);	

		double  p_equal = binomialIntegrated(n1, p ,equalHit);
		if(p_equal>maxRandomProbability){return null;}

		SpectraPair returnPair = new SpectraPair();
		returnPair.hasWeakDeltaSignal=false;
		if(msmsBetterPrecisionThanMs){ //frankp 20070323
			double newDm=dm;
			double worstPrecision=Math.max(detectorParameters.msAccuracy, detectorParameters.msmsAccuracy);
			int histo[] = new int[4000];
			{
				double averageMass=0;
				int countaverage=0;
				double delta=0;
				for(MSMS tmpMSMSa:spec1.valueList){	
					for(MSMS tmpMSMSb:spec2.valueList){
						delta= (tmpMSMSb.massToCharge - tmpMSMSa.massToCharge);
						if(delta>0 && Math.abs(dm-delta)<worstPrecision){
							averageMass+=delta;
							countaverage++;
						}
						if(delta>20 && delta<4000){
							int bin = (int)(delta);
							if (bin>3999 || bin <0) {
								logger.error("bin error: " + bin + " delta: " + delta);
								bin=0;
							}
							histo[bin]++;							
						}
					}
				}
			}


			int localMax=Integer.MIN_VALUE;
			int maxLocation=0;
			for(int i=0;i<4000;i++){
				if(histo[i]>localMax){
					localMax=histo[i];
					maxLocation=i;	
				}
			}
			histo=null;
			returnPair.unsuspectedDelta=0.5 + (double)maxLocation;
			returnPair.unsupsectedDeltaCount=localMax;
		}
		int deltaHit=0;  //how many times do we hit the deltaMass ?
		{
			for(MSMS tmpMSMSa:spec1.valueList){	
				for(MSMS tmpMSMSb:spec2.valueList){	
					if( Math.abs(tmpMSMSa.massToCharge - tmpMSMSb.massToCharge + dm )<detectorParameters.msmsAccuracy ){
						deltaHit++;break;
					}
				}
			}
		}
		boolean neutralLoss_on= false; 
		if(Math.abs(dm-79.966331) < detectorParameters.msAccuracy)  {neutralLoss_on=true;}

		if(!detectorParameters.neutralLossDetection){
			if(deltaHit <10 ){if(!neutralLoss_on)return null;}
		}
		else{
			//must have at least 1/3 equal hits (as measured on average number of MSMS)
			int minEqualHits=(int) ((spec1.valueList.size()+spec2.valueList.size())/6.0);
			if(equalHit  < minEqualHits ){if(!neutralLoss_on)return null;}
			//20081229 commented this if(returnPair.unsupsectedDeltaCount< (int)(equalHit/2)){if(!neutralLoss_on)return null;}		
			logger.debug("sugar detector at dm:" + dm);
		}

		double p_delta= binomialIntegrated(n1, p ,deltaHit);

		if(!detectorParameters.neutralLossDetection){
			if(p_delta>maxRandomProbability && !neutralLoss_on ){return(null);}
		}

		if(p_delta>maxRandomProbability){
			returnPair.hasWeakDeltaSignal=true;
		}

		double p_intra1_delta=0;
		int p_intra1_count=0;
		for(int f=0;f<spec1.valueList.size();f++){
			for(int g=f+1;g<spec1.valueList.size();g++){
				if( Math.abs(Math.abs(spec1.valueList.get(f).massToCharge - spec1.valueList.get(g).massToCharge) - dm )<detectorParameters.msmsAccuracy ){
					p_intra1_count++;
				}
			}
		}

		double tmpP=Math.min( (2 * detectorParameters.msmsAccuracyScoring * spec1.valueList.size()/range), 1);
		p_intra1_delta = binomialIntegrated(spec1.valueList.size(), tmpP ,p_intra1_count);	
		if(p_intra1_delta<0.0001  && !neutralLoss_on && detectorParameters.polymerFiltering){
			return(null);
		}		

		double localEqualOverlap=0;
		double localDeltaOverlap=0;
		double equalOverlap=0;
		double shiftOverlap=0;
		double totalOverlap=0;
		boolean equalFound,shiftFound;
		{
			for(MSMS tmpMSMSa:spec1.valueList){	
				equalFound=shiftFound=false;
				localEqualOverlap=0;
				localDeltaOverlap=0;
				for(MSMS tmpMSMSb:spec2.valueList){	
					if(Math.abs(tmpMSMSa.massToCharge - tmpMSMSb.massToCharge)<detectorParameters.msmsAccuracy){
						localEqualOverlap=Math.max(localEqualOverlap,Math.min( tmpMSMSa.intensity , tmpMSMSb.intensity ));
						equalFound=true;				}
					if( Math.abs(tmpMSMSa.massToCharge - tmpMSMSb.massToCharge + dm )<detectorParameters.msmsAccuracy ){
						localDeltaOverlap=Math.max(localDeltaOverlap,Math.min( tmpMSMSa.intensity , tmpMSMSb.intensity ));
						shiftFound=true;
					}
				}
				if(equalFound){
					equalHit++;
					equalOverlap+=localEqualOverlap;
				}
				if(shiftFound){
					deltaHit++;
					shiftOverlap+=localDeltaOverlap;
				}
			}
		}

		if(equalOverlap>1){equalOverlap=1.0;} else if(equalOverlap<0){equalOverlap=0;}//security
		if(shiftOverlap>1){shiftOverlap=1.0;} else if(shiftOverlap<0){shiftOverlap=0;}//security
		totalOverlap=equalOverlap+shiftOverlap;
		if(totalOverlap>1){totalOverlap=1.0;}//security

		p=Math.max(p_equal, p_delta);//Official version since 20061108
		if(p==0)p=p_equal;
		if(p==0)p=p_delta;
		if(neutralLoss_on)p=Math.min(p_equal,p_delta);
		
		//INTEGRATION_CONTROL comment warnings below for release
		if(p==0){p=maxRandomProbability;//last resort, tragic. TODO fixme
			//logger.warn("p==0");
		}
		if(p<0){
			//logger.warn("negative p:"+p);
			p=maxRandomProbability/100;//20070317 frankp we did have negative probabilities}
		}

		returnPair.spectrumA=spec1;
		returnPair.spectrumB=spec2;
		returnPair.p=p;
		returnPair.equalOverlap=equalOverlap;
		returnPair.shiftOverlap=shiftOverlap;
		returnPair.totalOverlap=totalOverlap;
		returnPair.score=totalOverlap;
		returnPair.deltaMass=dm;//this might be different from the precursor mass differences !!!!!
		return returnPair;
	}

	public static double binomialIntegrated(int N, double p, int k){
		double probability=0;
		boolean trouble=false;
		BinomialDistributionImpl BDist = new BinomialDistributionImpl(N,p);
		try {
			probability=1-BDist.cumulativeProbability(k);
		} catch (MathException e) {
			logger.error("MathException:"+e.toString());
			trouble=true;
		}
		if(trouble || probability==0){
			double mu    = N * p;
			double sigma = Math.sqrt(N * (1-p) * p);
			NormalDistributionImpl ND = new NormalDistributionImpl();
			ND.setMean(mu);
			ND.setStandardDeviation(sigma);

			try {
				probability=1.0-ND.cumulativeProbability((double)k);
			}
			catch (MathException e){
				trouble=true;
				logger.error("MathException:"+e.toString());
			}	
			if(probability==0 || trouble){
				probability=0;
				mu    = N * p;
				sigma = Math.sqrt(N * (1-p) * p);
				double expFac=-1.0/(2.0*sigma*sigma);
				for(double x=k-1;x<k+1000;x+=1.0){
					probability+=Math.exp(expFac*(x-mu)*(x-mu));				
				}
				probability=probability/(Math.sqrt(2.0*Math.PI)*sigma);

				double fixme=Math.pow(p,k);
				//System.out.println("prob:"+probability + " N:" + N + " p:"+p+" k:"+k + " fix:"+fixme);
				//TODO CLEAN THIS UP FRANK
				if(fixme>probability)probability=fixme;
			}
		}
		return(probability);
	}

	public static boolean printPdf(Record tmpRecord, ScoreData data, ArrayList modList2, AlgoParams algoParams){
		Document document =  new Document(PageSize.A4, 45, 40, 110, 50);//left right top bottom
		document.setMarginMirroring(true);
		String peakListFile=tmpRecord.getFileName();
		String bareFilename=tmpRecord.getFileName();

		//make sure to cut away both possible separators
		bareFilename=peakListFile.split("\\\\")[peakListFile.split("\\\\").length -1];
		bareFilename=bareFilename.split("/")[bareFilename.split("/").length-1];

		try {
			String pdfFileName = peakListFile.concat(".deltaMasses.pdf");		
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
			writer.setPageEvent(new HeaderFooter());
			File f = new File(pdfFileName);
			//if(f.exists()){return false;}
			if(f.isDirectory()){return false;}
			if(! f.canWrite()){return false;}

			document.addTitle("Detectorvision AG : deltaMasses results for " + bareFilename);
			document.addSubject("Detection of protein modifications");
			document.addKeywords("PTM PTM-Detection PTM-Localisation deltaMasses Detectorvion AG Differential PTM Detection www.detectorvision.com Frank Potthast");
			document.addAuthor("deltaMasses by Detectorvision AG. User:"+System.getProperty("user.name"));
			document.open();

			Image image128 = Image.getInstance("images/blue_bar.png");//why do we do this ???????
			try{
				PdfContentByte cd = writer.getDirectContent();
				Barcode128 code128 = new Barcode128();
				String tmpString= bareFilename;

				//make sure we only have 7bit ASCII characters
				//change non-ASCII characters to underscores
				String btmpString=bareFilename;
				for(int i=0;i<tmpString.length();i++){
					if((char)tmpString.charAt(i)>127){
						char tmp = (char)tmpString.charAt(i);
						char to = (char)95;//underscore
						btmpString= btmpString.replace(tmp,to);
					}
				}
				code128.setCode(btmpString);
				image128 = code128.createImageWithBarcode(cd, null, null);
			}
			catch (Exception de) {
				logger.error("BaseAlgorithm:barcode printing:"+de.getLocalizedMessage());
			}
			document.add(image128);

			document.add(new Paragraph(tmpRecord.getFileName()));
			//source: java in a nutshell pg 222
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			String tmpString =  "deltaMasses analysis time: " + cal.getTime().toString()+ " by "+System.getProperty("user.name");
			document.add(new Paragraph(tmpString));

			if(tmpRecord.getTandemName()!=null &&tmpRecord.getTandemName().length()>4){
				Chunk chunk = new Chunk("peptide id file: "+tmpRecord.getTandemName());
				document.add(new Paragraph(chunk));
			}

			if(tmpRecord.getSearchReportHttp()!=null){
				Chunk chunk = new Chunk("search report: "+tmpRecord.getSearchReportHttp(), new Font(Font.HELVETICA, 6));
				chunk.setAnchor(tmpRecord.getSearchReportHttp());
				document.add(new Paragraph(chunk));
			}


			//			count number of peptides identified by search engine
			ArrayList<String> proteinList= new ArrayList<String>();
			{
				ArrayList<String> peptideList = new ArrayList<String>();
				for(int i=0;i<tmpRecord.getSpectrumList().size();i++){
					if(!proteinList.contains(tmpRecord.getSpectrumList().get(i).proteinAsc) && tmpRecord.getSpectrumList().get(i).proteinAsc != null){
						proteinList.add(tmpRecord.getSpectrumList().get(i).proteinAsc);
					}
				}
				for(int i=0;i<tmpRecord.getSpectrumList().size();i++){
					if(!peptideList.contains(tmpRecord.getSpectrumList().get(i).pepSequence) && tmpRecord.getSpectrumList().get(i).pepSequence != null && tmpRecord.getSpectrumList().get(i).pepSequence.length()>0){
						peptideList.add(tmpRecord.getSpectrumList().get(i).pepSequence);
					}
				}
				int tmpint = peptideList.size();
				//if(tmpint==1){tmpint=0;}//TODO Fixme
				document.add(new Paragraph("detected "+proteinList.size()+" proteins with "+tmpint+" different peptides identified"));
			}

			int countPeps=0;
			{
				countPeps=0;
				for(int i=0;i<tmpRecord.getSpectrumList().size();i++){
					if(tmpRecord.getSpectrumList().get(i).pepSequence != null && tmpRecord.getSpectrumList().get(i).pepSequence.length()>2){
						countPeps++;
					}
				}
				double percentIdentified = 100.0*(double)countPeps/tmpRecord.getSpectrumList().size();
				if(countPeps>0){
					document.add(new Paragraph("spectra with peptide id: "+countPeps+" = "+String.format("%.2f",percentIdentified)+"%"));
				}
			}

			//charge distribution info/////////////////////////////////////
			{
				int count[]=new int[6];
				int countPepId[]= new int[6];
				for(int charge=1;charge<=5;charge++){
					for(int i=0;i<tmpRecord.getSpectrumList().size();i++){
						if(tmpRecord.getSpectrumList().get(i).charge == charge){
							count[tmpRecord.getSpectrumList().get(i).charge-1]++;
							if(tmpRecord.getSpectrumList().get(i).pepSequence != null){
								if(tmpRecord.getSpectrumList().get(i).pepSequence.length()>2){
									countPepId[charge-1]++;
								}
							}
						}	
						else if(tmpRecord.getSpectrumList().get(i).charge >= 6){
							count[5]++;
						}
					}
				}


				float[] proteinWidths = { 3f, 1f, 1f, 1f, 1f, 1f, 1f};
				PdfPTable chargeTable = new PdfPTable(proteinWidths);
				chargeTable.setHorizontalAlignment(Element.ALIGN_LEFT);
				chargeTable.setSpacingBefore(10f);//what actualy does 10f mean ?
				chargeTable.setSpacingAfter(10f);
				PdfPCell cell = new PdfPCell(new Paragraph("charge characteristics of "+tmpRecord.getSpectrumList().size()+" MSMS spectra"));
				cell.setColspan(7);
				cell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
				chargeTable.addCell(cell);


				cell = new PdfPCell(new Paragraph("charge"));
				cell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
				cell.setColspan(1);
				chargeTable.addCell(cell);	
				chargeTable.addCell("1");
				chargeTable.addCell("2");
				chargeTable.addCell("3");
				chargeTable.addCell("4");
				chargeTable.addCell("5");
				chargeTable.addCell(">5");

				chargeTable.addCell("spectra");
				chargeTable.addCell(""+count[0]);
				chargeTable.addCell(""+count[1]);
				chargeTable.addCell(""+count[2]);
				chargeTable.addCell(""+count[3]);
				chargeTable.addCell(""+count[4]);
				chargeTable.addCell(""+count[5]);

				chargeTable.addCell("% spectra");
				chargeTable.addCell(String.format("%.2f",100.0*(double)count[0]/tmpRecord.getSpectrumList().size()));
				chargeTable.addCell(String.format("%.2f",100.0*(double)count[1]/tmpRecord.getSpectrumList().size()));
				chargeTable.addCell(String.format("%.2f",100.0*(double)count[2]/tmpRecord.getSpectrumList().size()));
				chargeTable.addCell(String.format("%.2f",100.0*(double)count[3]/tmpRecord.getSpectrumList().size()));
				chargeTable.addCell(String.format("%.2f",100.0*(double)count[4]/tmpRecord.getSpectrumList().size()));
				chargeTable.addCell(String.format("%.2f",100.0*(double)count[5]/tmpRecord.getSpectrumList().size()));
				chargeTable.setWidthPercentage(100);//full width table

				chargeTable.addCell("peptide ID's");
				chargeTable.addCell(""+countPepId[0]);
				chargeTable.addCell(""+countPepId[1]);
				chargeTable.addCell(""+countPepId[2]);
				chargeTable.addCell(""+countPepId[3]);
				chargeTable.addCell(""+countPepId[4]);
				chargeTable.addCell(""+countPepId[5]);

				if(countPeps>0){
					chargeTable.addCell("% peptide ID's");
					chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[0]/countPeps));
					chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[1]/countPeps));
					chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[2]/countPeps));
					chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[3]/countPeps));
					chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[4]/countPeps));
					chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[5]/countPeps));
				}

				if(countPeps>0){
					chargeTable.addCell("% ID efficiency");
					for(int m=0;m<6;m++){
						if(count[m]>0){
							chargeTable.addCell(String.format("%.2f",100.0*(double)countPepId[m]/count[m]));		
						}
						else{
							chargeTable.addCell("-/-");
						}
					}
				}
				document.add(chargeTable);			
			}


			document.add(new Paragraph("maximal accepted deltaMasses random probability: "+String.format("%.3g",maxRandomProbability)));


			//count number of spectra involved in pairing
			{
				ArrayList<String> titles= new ArrayList<String>();
				for(int i=0;i<data.spectraPairList.size();i++){
					if(!titles.contains(data.spectraPairList.get(i).spectrumA.title)){
						titles.add(data.spectraPairList.get(i).spectrumA.title);
					}
					if(!titles.contains(data.spectraPairList.get(i).spectrumB.title)){
						titles.add(data.spectraPairList.get(i).spectrumB.title);
					}

				}
				document.add(new Paragraph("detected pairs: "+data.spectraPairList.size()+" originating from "+titles.size()+" different spectra"));
			}

			//number of pairs with peptide id's
			{
				ArrayList<String> titles= new ArrayList<String>();
				for(int i=0;i<tmpRecord.getSpectrumList().size();i++){
					if(tmpRecord.getSpectrumList().get(i).pepSequence != null){
						titles.add(tmpRecord.getSpectrumList().get(i).title);
					}
				}
				//walk though all pairs
				for(int i=0;i<data.spectraPairList.size();i++){
					//A->B
					if(!titles.contains(data.spectraPairList.get(i).spectrumA.title)){
						//spectrum A does not have a pepID. Check if spec B does:
						if(data.spectraPairList.get(i).spectrumB.pepSequence!=null){
							//System.out.println("A no ID but B has ID: list does not contain:"+data.spectraPairList.get(i).spectrumB.title);
							//if specB unknown to list, put it in 
							if(!titles.contains(data.spectraPairList.get(i).spectrumA.title)){
								titles.add(data.spectraPairList.get(i).spectrumA.title);
							}
						}
					}
					//B->A
					if(!titles.contains(data.spectraPairList.get(i).spectrumB.title)){
						//spectrum B does not have a pepID. Check if spec A does:
						if(data.spectraPairList.get(i).spectrumA.pepSequence!=null){
							//if specA unknown to list, put it in 
							//System.out.println("B no ID but A has ID: list does not contain:"+data.spectraPairList.get(i).spectrumA.title);
							if(!titles.contains(data.spectraPairList.get(i).spectrumB.title)){
								titles.add(data.spectraPairList.get(i).spectrumB.title);
							}
						}
					}
				}
				double tmpPercent = 100.0*(double)titles.size()/tmpRecord.getSpectrumList().size();
				int idByPair=titles.size()-countPeps;
				document.add(new Paragraph("spectra with peptide ID including ID by pairing: "+titles.size()+" = "+String.format("%.2f",tmpPercent)+"%"));
				if(idByPair>0 && countPeps>0){
					double percentage=100.0*(double)idByPair/(double)countPeps;
					document.add(new Paragraph("spectra ID'd by pairing: "+idByPair + " improvement: "+String.format("%.2f",percentage)+"%"));
				}
			}

			//pairs with nothing ID'd: count involved spectra
			{
				ArrayList<String> titles= new ArrayList<String>();
				for(int i=0;i<data.spectraPairList.size();i++){
					if(data.spectraPairList.get(i).spectrumA.pepSequence==null && data.spectraPairList.get(i).spectrumA.pepSequence==null){
						if(!titles.contains(data.spectraPairList.get(i).spectrumA.title)){
							titles.add(data.spectraPairList.get(i).spectrumA.title);
						}
						if(!titles.contains(data.spectraPairList.get(i).spectrumB.title)){
							titles.add(data.spectraPairList.get(i).spectrumB.title);
						}
					}
				}
				document.add(new Paragraph("paired spectra without pair having any peptide ID: " + titles.size()));
			}
			//			unimod modifications
			{
				int[] countMod = new int[modList2.size()+1];//last element contains the none-thingies
				for(int j=0;j<data.spectraPairList.size();j++){
					boolean found=false;
					for(int i=0;i<modList2.size();i++){
						if(data.spectraPairList.get(j).knownModification != null && data.spectraPairList.get(j).knownModification.fullName == ((Modification)modList2.get(i)).fullName ){
							countMod[i]++;
							found=true;
						}
					}
					if(!found){countMod[modList2.size()]++;}
				}
				PdfPTable table = new PdfPTable(4);
				table.setHorizontalAlignment(Element.ALIGN_LEFT);
				table.setSpacingBefore(10f);//what actualy does 10f mean ?
				table.setSpacingAfter(10f);
				PdfPCell cell = new PdfPCell(new Paragraph("potential modification signals and attempted explanations"));
				cell.setColspan(4);
				cell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
				table.addCell(cell);

				table.addCell("delta mass ");	
				table.addCell("composition");
				table.addCell("short name");
				table.addCell("number of pairs");
				table.setHeaderRows(2);//top 2 rows above get repeated after page breaks ...

				table.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell("-");	
				table.setHorizontalAlignment(Element.ALIGN_LEFT);
				table.addCell("-");
				table.addCell("unknown");
				table.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(""+countMod[modList2.size()]);

				for(int i=0;i<modList2.size();i++){
					if(countMod[i]!=0){
						table.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table.addCell(""+String.format("%9.5f",((Modification)modList2.get(i)).monoisotopic));	
						table.setHorizontalAlignment(Element.ALIGN_LEFT);
						table.addCell(((Modification)modList2.get(i)).composition);	
						table.addCell(((Modification)modList2.get(i)).shortName);
						table.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table.addCell(""+countMod[i]);	
					}
				}

				PdfPCell cell2 = new PdfPCell(new Paragraph("Listed are the nearest modifications in terms of delta mass. If you find e.g. 100 pairs, they might result from 10 unmodified spectra and 10 modified spectra from the same peptide."));
				cell2.setColspan(4);
				cell2.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
				table.addCell(cell2);
				table.setWidthPercentage(100);//full width table
				document.add(table);
				countMod=null;
			}
			for(int i=0;i<proteinList.size();i++){
				//for(int i=0;i<10;i++){


				float[] widths = { 2f, 2f, 2f, 7f, 1f, 1f, 2f, 4f};
				PdfPTable proteinTable = new PdfPTable(widths);

				proteinTable.setHorizontalAlignment(Element.ALIGN_LEFT);
				proteinTable.setSpacingBefore(10f);//what actualy does 10f mean ?
				proteinTable.setSpacingAfter(10f);



				String tmpProteinDesc=null;//find the proteinDescription for this guy ...
				double tmpProteinMass=0;
				int tmpProteinMatches=0;
				double tmpProteinScore=0;
				for(int k=0;k<tmpRecord.getSpectrumList().size();k++){
					if(tmpRecord.getSpectrumList().get(k).proteinAsc != null){
						if(tmpRecord.getSpectrumList().get(k).proteinAsc.equals(proteinList.get(i))){
							tmpProteinDesc=tmpRecord.getSpectrumList().get(k).proteinDesc;
							tmpProteinMass=tmpRecord.getSpectrumList().get(k).proteinMass;
							tmpProteinMatches=tmpRecord.getSpectrumList().get(k).proteinMatches;
							tmpProteinScore=tmpRecord.getSpectrumList().get(k).proteinScore;
							break;
						}
					}
				}

				Chunk descChunk =	new Chunk("protein: "+proteinList.get(i), new Font(Font.HELVETICA, 8));
				//proteinTable.addCell(new Paragraph(chunk));
				if(proteinList.get(i).startsWith("gi")){
					descChunk.setAnchor("http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val="+proteinList.get(i));
				}
				else if(proteinList.get(i).matches("[A-Z0-9]+_[A-Z]+")){
					descChunk.setAnchor("http://www.ebi.uniprot.org/uniprot-srv/uniProtView.do?proteinId="+proteinList.get(i));
				}
				else if(proteinList.get(i).matches("Y[A-Z0-9]+")){
					descChunk.setAnchor("http://db.yeastgenome.org/cgi-bin/locus.pl?locus="+proteinList.get(i));
				}

				PdfPCell DescCell = new PdfPCell(new Paragraph(descChunk));
				DescCell.setColspan(3);
				DescCell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
				proteinTable.addCell(DescCell);


				if(tmpProteinDesc==null){tmpProteinDesc="";}
				descChunk= new Chunk(tmpProteinDesc,new Font(Font.HELVETICA, 8));
				PdfPCell proteinCell = new PdfPCell(new Paragraph(descChunk));
				proteinCell.setColspan(5);
				proteinCell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
				proteinTable.addCell(proteinCell);

				if(!algoParams.isDiscoveryEdition){
					Chunk  redChunk =	new Chunk(" This superfluous red rectangle is not present in Discovery Edition ", new Font(Font.HELVETICA, 12));	
					PdfPCell redCell = new PdfPCell(new Paragraph(redChunk));
					redCell.setColspan(8);
					redCell.setBackgroundColor(new Color(0xFF, 0xBB, 0xBB));	
					proteinTable.addCell(redCell);
				}

				{
					String tmpText="protein score:"+tmpProteinScore;
					if(tmpProteinMass>0){//is negative if unknown, e.g. for X!Tandem peptide ID's.
						tmpText+="  mass:"+tmpProteinMass;
					}
					tmpText +="   spectra with matched peptide:"+tmpProteinMatches;
					Chunk  redChunk =	new Chunk(tmpText, new Font(Font.HELVETICA, 8));	
					PdfPCell redCell = new PdfPCell(new Paragraph(redChunk));
					redCell.setColspan(8);
					redCell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));	
					proteinTable.addCell(redCell);
				}

				Chunk chunk2;  
				chunk2 =	new Chunk("mass", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("error", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("expect", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("peptide", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("start", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("end", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("#", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));

				chunk2 =	new Chunk("modification", new Font(Font.HELVETICA, 8));
				proteinTable.addCell(new Paragraph(chunk2));


				Chunk chunk = null;  

				Font font = new Font(Font.COURIER, 10, Font.BOLD);
				for(int j=0;j<tmpRecord.getSpectrumList().size();j++){	
					if(tmpRecord.getSpectrumList().get(j).proteinAsc !=null){

						if(tmpRecord.getSpectrumList().get(j).proteinAsc.equals(proteinList.get(i))){
							chunk =	new Chunk(String.format("%.4f",tmpRecord.getSpectrumList().get(j).pepMass), new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));

							chunk =	new Chunk(String.format("%.4f",tmpRecord.getSpectrumList().get(j).pepError), new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));

							chunk =	new Chunk(""+tmpRecord.getSpectrumList().get(j).pepScore, new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));

							chunk =	new Chunk(tmpRecord.getSpectrumList().get(j).pepSequence, new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));

							chunk =	new Chunk(""+tmpRecord.getSpectrumList().get(j).pepStart, new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));

							chunk =	new Chunk(""+tmpRecord.getSpectrumList().get(j).pepEnd, new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));

							chunk =	new Chunk(""+tmpRecord.getSpectrumList().get(j).queryID, new Font(Font.HELVETICA, 8));
							proteinTable.addCell(new Paragraph(chunk));


							String modString="";
							boolean hasDeltaMod=false;
							ArrayList<Double> tmpList = new ArrayList<Double>();
							for(int l=0;l<data.spectraPairList.size();l++){
								if(tmpRecord.getSpectrumList().get(j).title != null){//crashed null blocker ... why ????
									if(data.spectraPairList.get(l).spectrumA.title.equals(tmpRecord.getSpectrumList().get(j).title)){
										boolean seenBefore=false;
										for(int z=0;z<tmpList.size();z++){
											if(Math.abs(tmpList.get(z)-data.spectraPairList.get(l).deltaMass)<1.5){
												seenBefore=true;
											}
										}
										if(!seenBefore){
											tmpList.add(data.spectraPairList.get(l).deltaMass);
										}
									}
								}
							}				
							for(int l=0;l<data.spectraPairList.size();l++){
								if(tmpRecord.getSpectrumList().get(j).title != null){//crashed null blocker ... why ????
									if(data.spectraPairList.get(l).spectrumB.title.equals(tmpRecord.getSpectrumList().get(j).title)){
										boolean seenBefore=false;
										for(int z=0;z<tmpList.size();z++){
											if(Math.abs(tmpList.get(z)+data.spectraPairList.get(l).deltaMass)<1.5){
												seenBefore=true;
											}
										}
										if(!seenBefore){
											tmpList.add(-data.spectraPairList.get(l).deltaMass);
										}
									}
								}
							}				
							int modCount=0;
							for(int z=0;z<tmpList.size();z++){
								if(modCount>0){modString+="\r\n";}//formatting
								modString+="deltaMod:"+String.format("%.4f",tmpList.get(z));
								hasDeltaMod=true;
								modCount++;
							}
							if(tmpRecord.getSpectrumList().get(j).pepMod!=null){
								if(modCount>0 && tmpRecord.getSpectrumList().get(j).pepMod.length()>0 && modCount>0){
									modString+="\r\n";
								}//unelegant formatting
								modString += tmpRecord.getSpectrumList().get(j).pepMod;
							}
							chunk =	new Chunk(modString, new Font(Font.HELVETICA, 8));
							//proteinTable.addCell(new Paragraph(chunk));

							PdfPCell modCell = new PdfPCell(new Paragraph(chunk));
							proteinCell.setColspan(1);
							if(hasDeltaMod)modCell.setBackgroundColor(new Color(0x99, 0xEE, 0xFF));
							proteinTable.addCell(modCell);
						}
					}
				}
				if(algoParams.isDiscoveryEdition){
					proteinTable.setHeaderRows(3);//top 3 rows above get repeated after page breaks ...
				}
				else{
					proteinTable.setHeaderRows(4);//top 4 rows above get repeated after page breaks ...	
				}

				proteinTable.setWidthPercentage(100);
				document.add(proteinTable);
			}




		} catch (DocumentException de) {
			logger.error("BaseAlgorithm:DocumentException:"+de.toString());
		} catch (IOException ioe) {
			logger.error("BaseAlgorithm:IOException:"+ioe.toString());
		}
		document.close();
		data=null;
		tmpRecord=null;
		modList2=null;
		document=null;
		return(true);
	}
}


