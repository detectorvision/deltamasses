package com.detectorvision.deltaCluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import com.detectorvision.massspectrometry.analyzation.AlgoParams;

public class ClusterData {
	static public ArrayList<ClusterSpectrum> cSpecOne = new ArrayList<ClusterSpectrum>();
	static public ArrayList<ClusterSpectrum> cSpecTwo = new ArrayList<ClusterSpectrum>();
	static public ArrayList<ClusterPair> cPair = new ArrayList<ClusterPair>();
	//static double MsPrecision=0.01;
	//static double MsmsPrecision=0.4;
	static int 	  compareCounter=0;
	static int	  deepCompareCounter=0;
	static double globalSim=0.0;
	static AlgoParams algoParams= new AlgoParams();

	public static void readInFile(String inFile,int channel){		
		System.out.println("reading "+inFile + " to channel "+ channel);

		File file = new File(inFile);
		if(!file.exists()){
			System.out.println("inFile "+file+" does not exist. Channel:"+channel);
			logIssue("", "ClusterData: inFile does not exist:"+inFile+" channel:"+channel, true);
		}
		try {
			//not empty in some cases, therefore we have to clean the arraylist ...
			if(channel==1){cSpecOne=new ArrayList<ClusterSpectrum>();}
			if(channel==2){cSpecTwo=new ArrayList<ClusterSpectrum>();;}
			cPair = new ArrayList<ClusterPair>();
			//System.out.println("trying to split ...");
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			String inLine;
			String inMz;
			String inSignal;
			while ((inLine = in.readLine()) != null) {
				ClusterSpectrum tmpSpec = new ClusterSpectrum();

				String[] words= inLine.split("\t");
				if(words.length!=5){
					//TODO what is this about ?? check it 
					//System.out.println("error: number of words:"+words.length+ " "+inLine);
				}

				{//read and decode the mz double list-----------------------
					ArrayList<Double> decodedList = new ArrayList<Double>();
					inMz=in.readLine();
					decodedList = (ArrayList<Double>)Base64.decodeToObject(inMz);	
					tmpSpec.mz=decodedList;
					//for(int i=0;i<tmpSpec.mz.size();i++){
					//	System.out.println("mz i:"+i+" value:"+tmpSpec.mz.get(i));
					//}
				}

				{//read and decode the signal list--------------------------
					ArrayList<Double> decodedList = new ArrayList<Double>();
					inSignal=in.readLine();
					decodedList = (ArrayList<Double>)Base64.decodeToObject(inSignal);
					tmpSpec.signal=decodedList;
					//for(int i=0;i<tmpSpec.signal.size();i++){
					//	System.out.println("signal i:"+i+" value:"+tmpSpec.signal.get(i));
					//}
				}

				tmpSpec.minMz=Double.MAX_VALUE;
				tmpSpec.maxMz=Double.MIN_VALUE;

				double tic=0;
				for(int j=0;j<tmpSpec.mz.size();j++){
					if(tmpSpec.mz.get(j)>tmpSpec.maxMz)tmpSpec.maxMz=tmpSpec.mz.get(j);
					if(tmpSpec.mz.get(j)<tmpSpec.minMz)tmpSpec.minMz=tmpSpec.mz.get(j);
					tic+=tmpSpec.signal.get(j);
				}
				for(int j=0;j<tmpSpec.signal.size();j++){//normalize
					tmpSpec.signal.set(j, tmpSpec.signal.get(j)/tic);
				}

				//block_num record_id spectrum_id precursor_mass charge \n mz \n signal
				tmpSpec.precursorMass=Double.parseDouble(words[3]);
				tmpSpec.charge=Integer.parseInt(words[4]);
				tmpSpec.range=tmpSpec.maxMz-tmpSpec.minMz;
				tmpSpec.id=Integer.parseInt(words[2]);
				tmpSpec.record_id=Integer.parseInt(words[1]);
				if(channel==1){cSpecOne.add(tmpSpec);}
				else if(channel==2){cSpecTwo.add(tmpSpec);}
				else{System.out.println("wrong channel:"+channel);System.exit(1);}
				//System.out.println("charge:"+tmpSpec.charge + " precMass:"+tmpSpec.precursorMass+" record_id:"+tmpSpec.record_id+
				//		" specId:"+tmpSpec.id+" channel:"+channel);
			}
			in.close();
		} catch (IOException ex) {
			System.out.println("clusterData.java:error while reading:"+inFile+" "+ex.getLocalizedMessage());
			logIssue("","ClusterData.java:error while reading "+inFile+" "+ex.toString(),true);
		}
		if(true){
			sortMsMsByMass(channel);
		}
	}

	public static void sortMsMsByMass(int channel){
		//sort cSpecOne and cSpecTwo
		if(channel==1){
			ArrayList<MSMS> MSMSList = new ArrayList<MSMS>();
			for(int i=0;i<cSpecOne.size();i++){
				for(int j=0;j<cSpecOne.get(i).mz.size();j++){
					MSMS tmpmsms= new MSMS();
					tmpmsms.mz=  cSpecOne.get(i).mz.get(j);
					tmpmsms.sig= cSpecOne.get(i).signal.get(j);
					//System.out.println(j+" "+tmpmsms.mz+ ""+tmpmsms.sig);
					MSMSList.add(tmpmsms);
				}
				Collections.sort(MSMSList);
				for(int k=0;k<MSMSList.size();k++){
					//System.out.println("k:"+k+"\t"+MSMSList.get(k).mz+"\t"+MSMSList.get(k).sig);
					cSpecOne.get(i).mz.set(k,MSMSList.get(k).mz );
					cSpecOne.get(i).signal.set(k,MSMSList.get(k).sig );
				}
				//System.out.println("---------------------------");
				MSMSList.clear();
			}
		}
		else if(channel==2){
			ArrayList<MSMS> MSMSList = new ArrayList<MSMS>();
			for(int i=0;i<cSpecTwo.size();i++){
				for(int j=0;j<cSpecTwo.get(i).mz.size();j++){
					MSMS tmpmsms= new MSMS();
					tmpmsms.mz=  cSpecTwo.get(i).mz.get(j);
					tmpmsms.sig= cSpecTwo.get(i).signal.get(j);
					//System.out.println(j+" "+tmpmsms.mz+ ""+tmpmsms.sig);
					MSMSList.add(tmpmsms);
				}
				Collections.sort(MSMSList);
				for(int k=0;k<MSMSList.size();k++){
					//System.out.println("k:"+k+"\t"+MSMSList.get(k).mz+"\t"+MSMSList.get(k).sig);
					cSpecTwo.get(i).mz.set(k,MSMSList.get(k).mz );
					cSpecTwo.get(i).signal.set(k,MSMSList.get(k).sig );
				}
				//System.out.println("---------------------------");
				MSMSList.clear();
			}		
		}
		else{
			System.out.println("ClusterData error: Channel must be 1 or 2 but is:"+channel);
			System.exit(1);	
		}


	}


	public static void readInFileTwo(String inFile, int channel){
		System.out.println("reading "+inFile + " to channel "+ channel);
		//random version of readInFile
		Random rand = new Random();	
		for(int i=0;i<707;i++){
			ClusterSpectrum tmpSpec = new ClusterSpectrum();
			tmpSpec.minMz=Double.MAX_VALUE;
			tmpSpec.maxMz=Double.MIN_VALUE;

			double tic=0;
			for(int j=0;j<75;j++){
				double m=200+rand.nextDouble()*1000;
				double s=rand.nextDouble();
				if(m>tmpSpec.maxMz)tmpSpec.maxMz=m;
				if(m<tmpSpec.minMz)tmpSpec.minMz=m;
				tmpSpec.mz.add(m);
				tmpSpec.signal.add(s);
				tic+=s;
			}
			for(int j=0;j<tmpSpec.signal.size();j++){//normalize
				tmpSpec.signal.set(j, tmpSpec.signal.get(j)/tic);
			}
			tmpSpec.precursorMass=500+rand.nextDouble()*1000;
			tmpSpec.charge=1+rand.nextInt(3);
			tmpSpec.range=tmpSpec.maxMz-tmpSpec.minMz;
			tmpSpec.id=rand.nextInt(240000);
			if(channel==1){cSpecOne.add(tmpSpec);}
			else if(channel==2){cSpecTwo.add(tmpSpec);}
			else{System.out.println("wrong channel:"+channel);System.exit(1);}
		}
	}

	public static void searchForPtm(boolean block1EqualBlock2){
		//search all pairs between cSpecOne and cSpecTwo but NOT within a class
		double dm=0;double range;double p;
		System.out.println("comparing:"+cSpecOne.size()+" x "+cSpecTwo.size()+" pairs for PTMs");
		for(int i1=0;i1<cSpecOne.size();i1++){
			for(int i2=0;i2<cSpecTwo.size();i2++){
				compareCounter++;
				if(cSpecOne.get(i1).charge==cSpecTwo.get(i2).charge){
					range=  Math.min(cSpecOne.get(i1).maxMz, cSpecTwo.get(i2).maxMz)-
					Math.max(cSpecOne.get(i1).minMz,cSpecTwo.get(i2).minMz);
					if(range>500){
						dm=cSpecOne.get(i1).precursorMass-cSpecTwo.get(i2).precursorMass;
						if(Math.abs(dm)>13.5){//low deltaMass is just a mess - skip it ...
							if( ! (block1EqualBlock2 && i1>i2) ){//do not process in-block pairs twice ....
								p=calc_deltamass_score(i1,i2,dm,range);
								if(p<ClusterConstants.maxRandomProbability){
									ClusterPair tmpPair = new ClusterPair();
									tmpPair.p=p;
									tmpPair.sim=globalSim;
									if(cSpecOne.get(i1).record_id==cSpecTwo.get(i2).record_id){
										tmpPair.fromSameRecord=true;
									}
									else{
										tmpPair.fromSameRecord=false;
									}
									if(dm>0){
										tmpPair.id_heavy=cSpecOne.get(i1).id;
										tmpPair.id_light=cSpecTwo.get(i2).id;
										tmpPair.dm=dm;
									}
									else{
										tmpPair.id_light=cSpecOne.get(i1).id;
										tmpPair.id_heavy=cSpecTwo.get(i2).id;
										tmpPair.dm=-dm;
									}
									cPair.add(tmpPair);
								}
							}
						}
					}
				}
			}
		}
		double quota=(double)deepCompareCounter/(double)compareCounter;
		System.out.println("pairs compared:"+compareCounter+" deep comparisons:"+deepCompareCounter+" quota:"+quota);
	}
	static double calc_deltamass_score(int i1, int i2, double dm,double range){
		double dmAbs=Math.abs(dm);
		deepCompareCounter++;
		int equalHit=0;
		for(int fake=0;fake<1;fake++){
			{
				equalHit=0;
				boolean changedI=false;
				int jold=-1;//so we start at zero below ...
				for(int i=0;i<cSpecOne.get(i1).mz.size();i++){
					for(int j=0;j<cSpecTwo.get(i2).mz.size();j++){
						j=Math.min(jold+1, cSpecTwo.get(i2).mz.size()-1);
						changedI=false;
						while(changedI==false && cSpecTwo.get(i2).mz.get(j)-cSpecOne.get(i1).mz.get(i) > algoParams.msmsAccuracy
								&& i<cSpecOne.get(i1).mz.size()-1 ){
							i++;
							changedI=true;
							i=Math.min(i, cSpecOne.get(i1).mz.size());
						}
						if(changedI==true){
							while(cSpecOne.get(i1).mz.get(i)-cSpecTwo.get(i2).mz.get(j)<algoParams.msmsAccuracy && j>0){
								j--;
							}
						}
						if(Math.abs(cSpecOne.get(i1).mz.get(i) - cSpecTwo.get(i2).mz.get(j))<algoParams.msmsAccuracy){
							equalHit++;break;
						}
						jold=j;
					}
				}
			}
		}
		if(equalHit<10){return(1.0);}//trash hit

		double p=0;       //probability for a random hit
		int n1=cSpecOne.get(i1).mz.size();
		int n2=cSpecTwo.get(i2).mz.size();
		p=Math.min( (2 * algoParams.msmsAccuracy * n2)/range, 1);	

		double  p_equal = binomialIntegrated(n1, p ,equalHit);
		if(p_equal>ClusterConstants.maxRandomProbability){return(1.0);}

		int deltaHit=0;  //how many times do we hit the deltaMass ?

		for(int i=0;i<cSpecOne.get(i1).mz.size();i++){	
			for(int j=0;j<cSpecTwo.get(i2).mz.size();j++){	
				if(Math.abs(cSpecOne.get(i1).mz.get(i) - cSpecTwo.get(i2).mz.get(j)-dm)<algoParams.msmsAccuracy){
					deltaHit++;break;//break to avoid double-counting
				}
			}
		}

		boolean neutralLoss_on= false; 
		if(Math.abs(dmAbs-79.966331) < algoParams.msAccuracy)  {neutralLoss_on=true;}
		if(deltaHit <10 ){if(!neutralLoss_on  && !algoParams.neutralLossDetection)return(1.0);}

		double p_delta= binomialIntegrated(n1, p ,deltaHit);
		if(     p_delta>ClusterConstants.maxRandomProbability && 
				!neutralLoss_on && 
				!algoParams.neutralLossDetection)
		{return(1.0);}

		{//polymer filter 
			double p_intra1_delta=0;//first spectrum
			int p_intra1_count=0;
			for(int f=0;f<cSpecOne.get(i1).mz.size();f++){
				for(int g=f+1;g<cSpecOne.get(i1).mz.size();g++){
					if( Math.abs(Math.abs(cSpecOne.get(i1).mz.get(f) - cSpecOne.get(i1).mz.get(g)) - dmAbs )<algoParams.msmsAccuracy ){
						p_intra1_count++;
					}
				}
			}
			double tmpP=Math.min( (2 * algoParams.msmsAccuracy * cSpecOne.get(i1).mz.size()/range), 1);
			p_intra1_delta = binomialIntegrated(cSpecOne.get(i1).mz.size(), tmpP ,p_intra1_count);	
			if(p_intra1_delta<0.0001  && !neutralLoss_on){
				return(1.0);
			}

			p_intra1_delta=0;//second spectrum
			p_intra1_count=0;
			for(int f=0;f<cSpecTwo.get(i2).mz.size();f++){
				for(int g=f+1;g<cSpecTwo.get(i2).mz.size();g++){
					if( Math.abs(Math.abs(cSpecTwo.get(i2).mz.get(f) - cSpecTwo.get(i2).mz.get(g)) - dmAbs )<algoParams.msmsAccuracy ){
						p_intra1_count++;
					}
				}
			}
			tmpP=Math.min( (2 * algoParams.msmsAccuracy * cSpecTwo.get(i2).mz.size()/range), 1);
			p_intra1_delta = binomialIntegrated(cSpecTwo.get(i2).mz.size(), tmpP ,p_intra1_count);	
			if(p_intra1_delta<0.0001  && !neutralLoss_on){
				return(1.0);
			}
		}

		//calculate the similarity-----------------------------------------------
		double localEqualOverlap=0;
		double localDeltaOverlap=0;
		double equalOverlap=0;
		double shiftOverlap=0;
		double totalOverlap=0;
		boolean equalFound,shiftFound;

		for(int i=0;i<cSpecOne.get(i1).mz.size();i++){
			equalFound=shiftFound=false;
			localEqualOverlap=0;
			localDeltaOverlap=0;
			for(int j=0;j<cSpecTwo.get(i2).mz.size();j++){
				if(Math.abs(cSpecOne.get(i1).mz.get(i) - cSpecTwo.get(i2).mz.get(j))<algoParams.msmsAccuracy){
					localEqualOverlap=Math.max(localEqualOverlap,Math.min( cSpecOne.get(i1).signal.get(i) , cSpecTwo.get(i2).signal.get(j) ));
					equalFound=true;				}
				if(Math.abs(cSpecOne.get(i1).mz.get(i) - cSpecTwo.get(i2).mz.get(j) -dm )<algoParams.msmsAccuracy){
					localDeltaOverlap=Math.max(localDeltaOverlap, Math.min(cSpecOne.get(i1).signal.get(i), cSpecTwo.get(i2).signal.get(j)));
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

		if(equalOverlap>1){equalOverlap=1.0;} else if(equalOverlap<0){equalOverlap=0;}//security
		if(shiftOverlap>1){shiftOverlap=1.0;} else if(shiftOverlap<0){shiftOverlap=0;}//security
		totalOverlap=equalOverlap+shiftOverlap;
		if(totalOverlap>1){totalOverlap=1.0;}//security
		globalSim=totalOverlap;
		//end of similarity calculation-----------------------------------------------------------


		//finish up and return probability--------------------------------------------------------
		p=Math.max(p_equal, p_delta);//Official version since 20061108
		if(p==0)p=p_equal;
		if(p==0)p=p_delta;
		if(neutralLoss_on)p=Math.min(p_equal,p_delta);
		if(p<=0)p=ClusterConstants.maxRandomProbability;//last resort - a research project !
		return(p);
	}

	public static void printTofile(String outFile){
		System.out.println("printToFile printing to "+outFile);
		try {	
			BufferedWriter blockOut = new BufferedWriter(new FileWriter(outFile, false));
			//p sim fk_h_spectrum fk_l_spectrum
			for(int i=0;i<cPair.size();i++){
				blockOut.write(
						cPair.get(i).id_light+"\t"+
						cPair.get(i).id_heavy+"\t"+
						cPair.get(i).fromSameRecord+"\t"+
						cPair.get(i).dm+"\t"+
						cPair.get(i).p+"\t"+
						cPair.get(i).sim+"\n"
				);
			}
			blockOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			logIssue("", "ClusterData problem writing to file:"+outFile+" "+e.toString(), true);
		}
	}
	static public double binomialIntegrated(int N, double p, int k){
		double probability=0;
		boolean trouble=false;
		BinomialDistributionImpl BDist = new BinomialDistributionImpl(N,p);
		try {
			probability=1-BDist.cumulativeProbability(k);
		} catch (MathException e) {
			//e.printStackTrace();
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
				//e.printStackTrace();
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

	public static void logIssue(String logFile,String message,boolean fatal) {
		try {
			if(logFile.length()==0){logFile="deltaMassCluster.log";}

			BufferedWriter out = new BufferedWriter(new FileWriter(logFile, true));
			Date now = new Date();
			String toLog = now.toLocaleString() + "\t"  + message +  " fatal:"+fatal+"\n";
			out.write(toLog);
			out.close();
		} catch (IOException e) {
			//TODO do something reasonable here althrough logging seems problematic .... 	
		}
	}



}

