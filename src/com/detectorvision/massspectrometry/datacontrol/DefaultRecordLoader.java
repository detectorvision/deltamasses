/* $Id: DefaultRecordLoader.java 316 2010-05-17 13:27:15Z frank $ */

package com.detectorvision.massspectrometry.datacontrol;

import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.PeptideID;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.Spectrum;
import com.detectorvision.massspectrometry.datacontrol.RecordLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * A default implementation of the RecordLoader. This Loader is able to handle MGF-Files
 * 
 * @author Raphael Bosshard
 * @author frank.potthast@detectorvision.com
 * 
 * 
 * version history------------
 * 20060922 version 0.2 frankp 
 * added mz mass conversion
 * 20060922 version 0.3 frankp  
 * added ordering of msms signals by intensity.
 * added class MyComparator.java to FileLoader
 * 20061002 v1.0.0 frankp
 * 20061006 v1.0.1 frankp minor cleanups
 * 20061007 v1.0.2 raphaelb added framework for asynchron operations
 */
public class DefaultRecordLoader implements RecordLoader {

	/** 
	 * Loads the mass spectrometry measurement into the data structure.
	 * 
	 * @param fileName Filename of the peakfile, must be in mgf-format
	 * @return Returns a List of spectras.
	 * @throws IOException Throws an exception if the method is unable to load the data.
	 */

	private ProgressListener progressListener = null;
	private SAXBuilder 		builder;
	private Document 		doc;

	static Logger logger = Logger.getLogger(DefaultRecordLoader.class.getName());
	
	public Record loadDefaultRecord(String fileName){
		Record tmpRecord= new Record();
		try {
			tmpRecord=loadRecord(fileName, progressListener);
		} catch (IOException e) {
			logger.fatal("file did not load:"+e.toString());
			e.printStackTrace();
		}
		return tmpRecord;
	}
	
	public  Record loadRecord(String fileName, ProgressListener listener) throws IOException{
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();
		Record tmpRecord = new Record();
		tmpRecord.setHasRetention(true);
		String line = "";
		FileReader file = new FileReader(fileName);
		BufferedReader buff = new BufferedReader(file);

		Spectrum currentSpectrum = new Spectrum();
		currentSpectrum.proteinAsc="-";
		currentSpectrum.pepSequence="-";
		double minMZ = Double.MAX_VALUE;
		double maxMZ = Double.MIN_VALUE;
		double rangeMZ = 0;

		//int maxNumberOfSpectrums=30000; //frankp 20061018 added this variable 
		int maxNumberOfSpectrums=200000; 
		int maxPeaksPerSpectrum = 75;   //maximum number of peaks to remain in a single MSMS spectrum
		//TODO make this dependent on the precursor mass like
		//     maxPeaksPerSpectrum=max(75,(int)rangeMZ/40);
		int maxInPeaks = 5000;         
		double tmpPepMass=0;
		ArrayList<MSMS> tmpMSMSList = null;

		boolean beginFound = false;
		boolean endFound = true;
		int numberOfSpectrums = 0;

		int i = 0;
		logger.info("DefaultRecordLoader memory free:"+Runtime.getRuntime().freeMemory()+" max:"+Runtime.getRuntime().maxMemory()+ " total:"+Runtime.getRuntime().totalMemory());
		logger.info("DefaultRecordLoader build 235 activated. Please wait.");

		String titleRegex = "^TITLE=(.*)";
		//String pepmassRegex = "^PEPMASS=([\\.0-9]+)";//build 60 can handle intensity after mz value.
		//new 20070505
		String pepmassRegex = "^PEPMASS=([\\.0-9]+).*";//build 93 can handle intensity after mz value.
		
		//old build 58  String chargeRegex = "^CHARGE=(.*).+";
		//String chargeRegex = "^CHARGE=([0-9]+).+"; //new build 59 can handle CHARGE=2, CHARGE=2+ 3+ etc..
		String chargeRegex = "^CHARGE=([0-9]).*"; //new build 62
		String retentionRegex = "(.*)\\.([0-9]+)\\.([0-9]+)\\.([0-9])\\.dta";
		//frankp 20060922 old: String dataRegex = "(.*) (.*)";
		//String dataRegex = "(.*)\\s+(.*)";//frankp 20050922 new: \\s+ = one or several whitespace characters	
		
		//String dataRegex = "(.*)[ \t\n\r]+(.*)[ \t\n\r]+(.*)";//frankp 20050922 new: \\s+ = one or several whitespace characters	
		String dataRegex = "(.*)[ \t\n\r]+(.*)[ \t\n\r]*(.*)";//frankp 20100209 new: \\s+ = zero or several whitespace characters	
		
	
		Pattern titlePattern = Pattern.compile(titleRegex);
		Pattern pepmassPattern = Pattern.compile(pepmassRegex);
		Pattern chargePattern = Pattern.compile(chargeRegex);
		Pattern retentionPattern = Pattern.compile(retentionRegex);
		Pattern dataPattern = Pattern.compile(dataRegex);

		int lineNum = 0;
		int numSpectraToRead = 1;

		Matcher titleMatcher, chargeMatcher, retentionMatcher, dataMatcher, pepmassMatcher;

//		frankp 20061020-----------------------------------------------------reading peptide ID's------start
		try{
		
		PeptideID tmpPep=new PeptideID();
		ArrayList<PeptideID> pepList = new ArrayList<PeptideID>();

		
		String fileBase=fileName.substring(0,fileName.length()-4);
		
		String mascotName = fileBase + ".xml";
		String tandemName = fileBase + ".xt.xml";

		
		logger.info("DefaultRecordloader:checking mascot file:"+mascotName);
		File mascotFile = new File(mascotName);
		
		if(mascotFile.exists() && mascotFile.canRead()){
			logger.info("DefaultRecordLoader:Reading mascotFile:"+mascotFile.getAbsolutePath());
			pepList=loadMascotPeptideID(mascotName);
		}
		else{
			if(!mascotFile.exists()){logger.info("mascotFile does not exist:"+mascotFile.getAbsoluteFile().toString());}
			}
		if(mascotFile.exists() && !mascotFile.canRead()){
			logger.warn("Cannot read mascotFile, but it exists:"+mascotFile.getAbsoluteFile().toString());
		}
		
		
		
		
		logger.info("DefaultRecordloader:checking xtandem file:"+tandemName);
		File tandemFile = new File(tandemName);

		if(tandemFile.exists() && tandemFile.canRead()){
			logger.info("DefaultRecordLoader:Reading TandemFile:"+tandemFile.getAbsolutePath());
			ArrayList<PeptideID> tmpPeplist= new ArrayList<PeptideID>();
			//pepList can allready be populated from mascot call.
			if(pepList.size()==0){
				pepList=(loadPeptideID(tandemName));
			}
			else{//there are allready some entries ....
				tmpPeplist=(loadPeptideID(tandemName));
				for(int n=0;n<tmpPeplist.size();n++){
					pepList.add(tmpPeplist.get(n));
				}
			}
		}
		else{
			if(!tandemFile.exists()){logger.info("TandemFile does not exist:"+tandemFile.getAbsoluteFile().toString());}
			}
		if(tandemFile.exists() && !tandemFile.canRead()){
			logger.warn("Cannot read tandemFile, but it exists:"+tandemFile.getAbsoluteFile().toString());
		}
		
		logger.info("DefaultRecordLoader:number of peps identified:" +
								pepList.size());
		for(int b=0; b<pepList.size(); b++){
			tmpPep=pepList.get(b);
			//System.out.println("got peptide:" + b + " " + tmpPep.peptideSequence.toString());
			break;
		}
		
		
		//frankp 20061020-----------------------------------------------------reading peptide ID's--------end

		//count lines for the progressListener
		int clines=0;
		while(line != null){
			if(line.startsWith("BEGIN IONS")){numSpectraToRead++;}
			line = buff.readLine();
			clines++;
		}
		buff.close();
		System.out.println("lines:"+clines+" spectra:"+numSpectraToRead);


		file = new FileReader(fileName);
		buff = new BufferedReader(file);

		while(true && numberOfSpectrums < maxNumberOfSpectrums){
			line = buff.readLine();
			if(line == null){ // EOF reached. Boil out of the while-loop.
				break;
			}
			
			if(!endFound && line.startsWith("BEGIN IONS")){

				// end of a spectra not yet found and still there is a new begin-block? There's something wrong. Boil out completely. 
				logger.warn("DefaultRecordLoader:Unexpected data in file " + fileName + " at line " + lineNum);
				throw(new IOException("Unexpected data in file " + fileName + " at line " + lineNum));	
			}

			//start of an MSMS spectrum
			if(line.startsWith("BEGIN IONS")){
				minMZ = Double.MAX_VALUE;
				maxMZ = Double.MIN_VALUE;
				rangeMZ = 0;

				beginFound = true;
				endFound = false;
				numberOfSpectrums++;
				if(numberOfSpectrums%1000 == 0){
					Double memPerSpec=(double)(Runtime.getRuntime().maxMemory()-Runtime.getRuntime().freeMemory())/(double)numberOfSpectrums;
					logger.info(numberOfSpectrums+"Memory information memPerSpec:["+memPerSpec+"]"+Runtime.getRuntime().freeMemory()+" max:"+Runtime.getRuntime().maxMemory()+ " total:"+Runtime.getRuntime().totalMemory());
				}
				tmpMSMSList = new ArrayList<MSMS>();
				currentSpectrum = new Spectrum();
				i = 0;

			}
			// This seems to be the end of a spectrum.
			else if(line.startsWith("#")){;}//frankp 20061021 Mascot Distiller File do nothing - a comment 
			else if(beginFound && line.startsWith("END IONS")) {
				//System.out.println("charge:"+currentSpectrum.charge+" tmppepMass:"+tmpPepMass);
				currentSpectrum.precursorMass = ( currentSpectrum.charge * tmpPepMass - 
						( ( currentSpectrum.charge - 1 ) * 1.007276 ) );

				ArrayList<MSMS> valueList = new ArrayList<MSMS>();
				beginFound = false;
				endFound = true;

				Collections.sort(tmpMSMSList);
				Collections.reverse(tmpMSMSList);

				//frankp 20061002 tic=total ion current (used for MSMS spectrum intensity normalisation
				double tic=0;
				for(int n = 0; n < maxPeaksPerSpectrum; n++){
					if(n < tmpMSMSList.size()){
						tic+=tmpMSMSList.get(n).intensity;
					}
				}
				for(int n = 0; n < maxPeaksPerSpectrum; n++){
					if(n < tmpMSMSList.size()){
						tmpMSMSList.get(n).intensity/=tic;
					}
				}
				for(int n = 0; n < maxPeaksPerSpectrum; n++){
					if(n < tmpMSMSList.size()){
						valueList.add(tmpMSMSList.get(n));
						maxMZ=Math.max(maxMZ,tmpMSMSList.get(n).massToCharge);
						minMZ=Math.min(minMZ,tmpMSMSList.get(n).massToCharge);
					}
				}
				maxMZ = Math.max(0, maxMZ); //<0 protection
				minMZ = Math.max(0, minMZ); //<0 protection
				rangeMZ = Math.max(0,maxMZ - minMZ);

				currentSpectrum.minMZ=minMZ;
				currentSpectrum.maxMZ=maxMZ;
				currentSpectrum.rangeMZ=rangeMZ;
				currentSpectrum.valueList = valueList;
				//frankp20061019
 
				for(int b=0; b<pepList.size(); b++){
					tmpPep=pepList.get(b);
					if(tmpPep.title.startsWith(currentSpectrum.title)){	
						String tmp=tmpPep.protein_acc;
						if(tmp.length()>35){
							tmp=tmp.substring(0,35);
							tmp+="...";
						}
						currentSpectrum.proteinMass=-1;//frankp 20070323 TODO how do we get the protein mass from an xTandem report ???
						currentSpectrum.proteinMatches=-1;//frankp 20070323 calculated below ... quite ugly ..

						currentSpectrum.proteinScore=tmpPep.protein_score;
						currentSpectrum.proteinDesc=tmp;//TODO clean this up ... frankp 20070323 
						currentSpectrum.pepMod=tmpPep.peptide_mod;//frankp 20070323
						currentSpectrum.pepScore=tmpPep.peptideExpect;//frankp 20070323
						currentSpectrum.pepMass=tmpPep.mass;//frankp 20070323

						currentSpectrum.proteinAsc=tmp;
						currentSpectrum.pepSequence=tmpPep.peptideSequence;
						double tmpErr=tmpPep.deltaMass;
						currentSpectrum.pepStart=tmpPep.peptideStart;
						currentSpectrum.pepEnd=tmpPep.peptideEnd;
						currentSpectrum.queryID=tmpPep.queryID;
						currentSpectrum.pepError=tmpErr/tmpPep.peptideCharge;//TESTING
						currentSpectrum.pepMz=tmpPep.mass/tmpPep.peptideCharge;//TESTING

					}
				}
				if(currentSpectrum.valueList.size()>=10){//new 20070125
					//Lif(currentSpectrum.pepSequence != null && currentSpectrum.pepSequence.length()>0){
					//{
					//	calibratePrecursor(currentSpectrum);
					//}
					//System.out.println("added spectrum:"+currentSpectrum.charge+ " "+currentSpectrum.title);
					spectrumList.add(currentSpectrum);
					int progress = (int)(100*(double)spectrumList.size()/(double)numSpectraToRead);
					if(spectrumList.size()%1000 == 0){
						if(listener != null){
							listener.updateProgress(progress, ""+spectrumList.size()+ " MSMS spectra loaded");
						}
					}
				}
			}

			else if(beginFound && line.startsWith("TITLE=")){

				titleMatcher = titlePattern.matcher(line);
				retentionMatcher = retentionPattern.matcher(line);
				if(titleMatcher.matches()){

					currentSpectrum.title = titleMatcher.group(1);
				}

				if(retentionMatcher.matches()){
					currentSpectrum.retention =  Float.parseFloat(retentionMatcher.group(3));
				}
				else {
					tmpRecord.setHasRetention(false);
				}
			}
			else if(beginFound && line.startsWith("CHARGE=")){

				chargeMatcher = chargePattern.matcher(line);
				if(chargeMatcher.matches()){
					currentSpectrum.charge = Integer.parseInt(chargeMatcher.group(1));
				}
				else{
					logger.warn("DefaultRecordLoader:charge not found in "+line);	
				}
			}
			else if(beginFound && line.startsWith("PEPMASS=")){

				pepmassMatcher = pepmassPattern.matcher(line);
				//System.out.println("line:"+line);
				if(pepmassMatcher.matches()){
					//System.out.println("matcher matches:"+pepmassMatcher.group(1));
					//convert the pepmass into the real pepmass (irritating naming convention of .mgf files)
					//invert  $MoverZ = ($MH + ($Z - 1) * 1.007276) / $Z;
					//prototype precMass[j]=((charge[j]*tmpmass) - ((charge[j]-1)*1.007276));
					tmpPepMass=Double.parseDouble( pepmassMatcher.group(1) );
				}
			}
			else if(line.startsWith("COM=")){;}//build 59 do nothing
			else if(line.length()==0){;}//build 59 do nothing
			else if(line.startsWith("RTINSECONDS")){;}//build 60 do nothing //TODO retention
			else if(line.startsWith("SCANS")){;}//build 60 do nothing //TODO retention
			else if(beginFound && i < maxInPeaks){
				// We are still in a IONS block, but this line has no header. Must be data.
				dataMatcher = dataPattern.matcher(line);	
				if(dataMatcher.matches()){
					MSMS newMSMS = new MSMS();
					try{
					newMSMS.intensity = Double.parseDouble( dataMatcher.group(2) );
					newMSMS.massToCharge= Double.parseDouble( dataMatcher.group(1) );
					}catch(Exception e){
						System.out.println("---------------------------\ngroup1:"+dataMatcher.group(1));
						System.out.println("group2:"+dataMatcher.group(2));
						System.out.println("line:"+line);
					}
					tmpMSMSList.add(newMSMS);
					i++;
				}
			}
			lineNum++;
		}
		logger.info("Lines read:"+lineNum+ " Number of spectra:"+spectrumList.size());

		//now, calculate and set currentSpectrum.proteinMatches
		ArrayList<String> tmpProtList = new ArrayList<String>();
		for(int k=0;k<pepList.size();k++){
			if(!tmpProtList.contains(pepList.get(k).protein_acc)){
				tmpProtList.add(pepList.get(k).protein_acc);
			}
		}
		//for each protein, count how many peps we have .. dumb implementation!!!
		//TODO improve tis implementation ... very dumb .....
		for(int k=0;k<tmpProtList.size();k++){
			int countPeps=0;
			for(int m=0;m<pepList.size();m++){
				if(tmpProtList.get(k).equals(pepList.get(m).protein_acc)){
					countPeps++;
				}
			}
			//System.out.println("protein:"+tmpProtList.get(k)+" has peps:"+countPeps);
			for(int m=0;m<spectrumList.size();m++){
				if(spectrumList.get(m).proteinAsc!= null && spectrumList.get(m).proteinAsc.equals(tmpProtList.get(k))){
					spectrumList.get(m).proteinMatches=countPeps;
					//System.out.println("prrrrotein:"+tmpProtList.get(k)+" has peps:"+countPeps);
				}
			}	
		}
		

		if(tmpRecord.getMsmsPrecision()<= tmpRecord.getMsmsPrecision()){
			tmpRecord.msmsBetterPrecisionThanMs=true;
		}
		else{
			tmpRecord.msmsBetterPrecisionThanMs=false;
		}
		tmpRecord.setPeptideIDList(pepList);
		tmpRecord.setSpectrumList(spectrumList);
		tmpRecord.setFileName(fileName);
		tmpRecord.setOriginMethod("xtandem");
		if(spectrumList!=null){
		   listener.updateProgress(0, "record loaded with "+spectrumList.size()+" spectra from file "+fileName);
		}
		else{
			logger.error("spectrumList is null");
		}
	}catch(Exception e){
		logger.error("DefaultRecord:outer catch exception:"+e.getMessage());
		logger.error("DefaultRecord:outer catch exception:"+e.getLocalizedMessage());
		logger.error("DefaultRecord:outer catch exception:"+e.toString());		
		logger.error(numberOfSpectrums+"DefaultRecord:outer catch:memory free:"+Runtime.getRuntime().freeMemory()+" max:"+Runtime.getRuntime().maxMemory()+ " total:"+Runtime.getRuntime().totalMemory());
	}
		return(tmpRecord);
	}

	public void setProgressListener(ProgressListener progress){
		this.progressListener = progress;
	}

	public ArrayList<PeptideID>  loadPeptideID(String fileName){

		ArrayList<PeptideID> listA = new ArrayList<PeptideID>();
		PeptideID tmpPeptideID;

		try{
			this.builder = new SAXBuilder();
			File TandemXMLFile= new File(fileName);
			this.doc = this.builder.build(TandemXMLFile);
		}
		catch(JDOMException e){
			logger.error("DefaultRecordLoader:JDOMException:"+e.getLocalizedMessage());
			return listA;
		}
		catch(IOException e){
			logger.error("DefaultRecordLoader:IOException:" + e.getLocalizedMessage());
			return listA;
		}

		Element bioml = this.doc.getRootElement();
		if(bioml == null || !bioml.getName().equals("bioml")){
			logger.warn("DefaultRecordLoader:no <bioml> xml root found:" + fileName);
		}
		//list all group elements 
		Element group = bioml.getChild("group");// Namespace.getNamespace("http://www.bioml.com/gaml/"));
		if(group == null){
			logger.error("DefaultRecordLoader:no <group> xml element found:"+fileName);
			return listA;
		}

		String originalFileName = bioml.getAttribute("label").getValue();
		originalFileName=originalFileName.replace("models from ","");
		originalFileName=originalFileName.replace("'","");

		List groupList = bioml.getChildren("group");
		if(groupList == null || groupList.size() == 0){
			logger.error("no children of <group> found:grouplist error:" + fileName);
			return listA;
		}

		// iterate <bioml><group>---------------------------------------------start
		for(int i=0; i<groupList.size()-3; i++){//-3 because three non-peptideID-group trail the xtandem  xml structure.
			// get the modification element
			Element onegroup = (Element) groupList.get(i);
			if(onegroup.getAttribute("label").getValue().equals("no model obtained")){continue;}//BUG:20070124:1
			//dive for <group><protein><peptide><domain>->start,end,sequence
			Element gppd=onegroup.getChild("protein").getChild("peptide").getChild("domain");

			String tmpModString="";
			if(onegroup.getChild("protein").getChild("peptide").getChild("domain").getChild("aa") != null){
				List aaList=onegroup.getChild("protein").getChild("peptide").getChild("domain").getChildren("aa");
			    //logger.info("trying to get modification:aaList.size:"+aaList.size());
				for(int s=0;s<aaList.size();s++){
					Element aa= (Element)aaList.get(s);
					//logger.info("got an attribute:"+aa.getAttributeValue("type")+"-"+aa.getAttributeValue("at")+":"+aa.getAttributeValue("modified"));
					if(s>0){tmpModString += " | ";}
					tmpModString += aa.getAttributeValue("type")+"-"+aa.getAttributeValue("at")+":"+aa.getAttributeValue("modified");
				}
			}
			//dive for <group>-><group>
			List groupgroupList = onegroup.getChildren("group");
			if (groupgroupList == null || groupgroupList.size() == 0) {
				logger.fatal("no <group><group> found in xtandem file");
				System.exit(1);
			}

			for(int j=0; j<groupgroupList.size(); j++){
				Element onegroupgroup = (Element) groupgroupList.get(j);
				if(onegroupgroup.getAttribute("label").getValue().equalsIgnoreCase("fragment ion mass spectrum")){
					//<note label="Description">
					Element note=(Element)onegroupgroup.getChild("note");

					//now we have the spectrum TITLE= and can stitch together everything
					tmpPeptideID=new PeptideID();
					tmpPeptideID.protein_acc=onegroup.getAttribute("label").getValue();
					tmpPeptideID.protein_desc=onegroup.getAttribute("label").getValue();
					tmpPeptideID.peptideExpect=java.lang.Double.parseDouble(onegroup.getAttribute("expect").getValue());
					tmpPeptideID.peptideSequence=gppd.getAttribute("seq").getValue();
					tmpPeptideID.deltaMass=Double.parseDouble(gppd.getAttributeValue("delta"));
					tmpPeptideID.mass=java.lang.Double.parseDouble(onegroup.getAttribute("mh").getValue());				
					tmpPeptideID.title=note.getValue();
					tmpPeptideID.peptideCharge=Integer.parseInt(onegroup.getAttribute("z").getValue());
					tmpPeptideID.peptideStart=Integer.parseInt(gppd.getAttribute("start").getValue());;
					tmpPeptideID.peptideEnd=Integer.parseInt(gppd.getAttribute("end").getValue());
					tmpPeptideID.protein_score=Double.parseDouble(onegroup.getChild("protein").getAttribute("expect").getValue());//frankp 20070323
					tmpPeptideID.peptide_mod=tmpModString;
					tmpPeptideID.queryID=gppd.getAttributeValue("id");
					listA.add(tmpPeptideID);			
					tmpPeptideID=null;//for code clairty 
				}
			}
		}	
//		iterate <bioml><group>-----------------------------------------------end
		this.doc = null;
		this.builder = null;
		return listA;
	}

	
	
	public ArrayList<PeptideID>  loadMascotPeptideID(String fileName){

		int maxNumberOfSpectrums=200000;
		int maxPeaksPerSpectrum = 75;  
		Record tmpRecord = new Record();
		tmpRecord.setOriginMethod("mascot");
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();
		ArrayList<MSMS> tmpMSMSList = null;
		try{
		logger.info("checking validity of the mascot xml format - can take a minute ....");
		if(!ValidateXML.validateMascot(fileName)){
			logger.error("MascotXMLRecordLoader:invalid XML for:"+fileName);
		}
		else{
			logger.info("MascotXMLRecordLoader:valid XML for:"+fileName);
		}
		Document doc = new Document();
		try{
			SAXBuilder builder = new SAXBuilder();
			try {
				File MascotXMLFile= new File(fileName);
				doc = builder.build(MascotXMLFile);
			} catch (IOException e) {
				logger.error("MascotXMLRecordLoader:SYSTEM_ERROR:IOException:file:" +
										 fileName + " :" + e.getMessage());
			}
			doc.getRootElement();
			builder = null;
		}
		catch(JDOMException e){
			logger.error("MascotXMLRecordLoader:JDOMException:JDOMException:file:" +
									 fileName + " :" + e.getMessage());
			return null;
		}
		
		logger.info("reading  mascot xml format ");
		
		String mascotNameSpace="error";
		try {
	        BufferedReader in = new BufferedReader(new FileReader(fileName));
	        String str;
	        for(int i=0;i<5 &&  ((str = in.readLine()) != null);i++ ){
	        	if (str.contains("http://www.matrixscience.com/xmlns/schema/mascot_search_results_2")){
	        		mascotNameSpace="http://www.matrixscience.com/xmlns/schema/mascot_search_results_2";
	        		logger.info("mascot_search_results_2 activated");
	        	}	
	        	if (str.contains("http://www.matrixscience.com/xmlns/schema/mascot_search_results_1")){
	        		mascotNameSpace="http://www.matrixscience.com/xmlns/schema/mascot_search_results_1";
	        		logger.info("mascot_search_results_1 activated");
	        	}	
	        }
	        in.close();
	    } catch (IOException e) {
	    	logger.error("MascotXMLRecordLoader:unable to read mascot namespace");
	    	return pepList;
	    }
		logger.info("MascotXMLRecordLoader::namespace determination okay");
	    
		Element mascot_result = doc.getRootElement();
		if(mascot_result == null || !mascot_result.getName().equals("mascot_search_results")){
			logger.error("Incorrect Mascot xml file:"+fileName);
			return pepList;
		}

		Element mascot_header = mascot_result.getChild("header",  Namespace.getNamespace(mascotNameSpace));
		Element mascot_uri = mascot_header.getChild("URI",  Namespace.getNamespace(mascotNameSpace));
		tmpRecord.setSearchReportHttp(mascot_uri.getText());
		logger.info("searchReportHttp:"+tmpRecord.getSearchReportHttp());
		
//		Element mascot_queries = mascot_result.getChild("queries",  Namespace.getNamespace(mascotNameSpace));
//		
//		if(mascot_queries == null){
//			logger.error("mascot_query_error");
//		}
//		
//		
//		List list_query = mascot_queries.getChildren("query", Namespace.getNamespace(mascotNameSpace));
//		
//		if(list_query.size()>=maxNumberOfSpectrums){
//			logger.error("too many spectra:"+list_query.size());
//			return pepList;
//		}
//
//		
//		if(list_query == null || list_query.size() == 0){
//			logger.error("no querries available");
//			return pepList;
//		}
		
		/////////////////////////////////////////////////////////start 
		
		Element mascot_hits = mascot_result.getChild("hits",  Namespace.getNamespace(mascotNameSpace));
//		if(mascot_queries == null){
//			logger.error("mascot_hits error");
//			return pepList;
//		}
		List list_hits = mascot_hits.getChildren("hit", Namespace.getNamespace(mascotNameSpace));
		if(list_hits != null){
			logger.info("MascotXMLRecordLoader:number of hits:"+list_hits.size());
			for(int i=0; i<list_hits.size(); i++){
				Element this_hit = (Element) list_hits.get(i);
				Element this_protein = this_hit.getChild("protein", Namespace.getNamespace(mascotNameSpace));
				List list_peptide = this_protein.getChildren("peptide", Namespace.getNamespace(mascotNameSpace));
				if(list_peptide == null || list_peptide.size() == 0)
					logger.warn("No peptides found. May be OK but what is the software or the user doing ?");
				for(int pep=0; pep<list_peptide.size(); pep++){
					//<peptide query="3">
					Element this_pep_query = (Element) list_peptide.get(pep);
					int query_index=Integer.parseInt(this_pep_query.getAttributeValue("query"));
					Element pep_exp_mr= this_pep_query.getChild("pep_exp_mr", Namespace.getNamespace(mascotNameSpace));	
					Element pep_exp_z= this_pep_query.getChild("pep_exp_z", Namespace.getNamespace(mascotNameSpace));	
					Element pep_exp_mz= this_pep_query.getChild("pep_exp_mz", Namespace.getNamespace(mascotNameSpace));	
					Element pep_expect= this_pep_query.getChild("pep_expect", Namespace.getNamespace(mascotNameSpace));	
					Element pep_delta= this_pep_query.getChild("pep_delta", Namespace.getNamespace(mascotNameSpace));	
					Element pep_seq= this_pep_query.getChild("pep_seq", Namespace.getNamespace(mascotNameSpace));	
					Element mod_seq= this_pep_query.getChild("pep_var_mod", Namespace.getNamespace(mascotNameSpace));
					Element pep_start= this_pep_query.getChild("pep_start", Namespace.getNamespace(mascotNameSpace));	
					Element pep_end= this_pep_query.getChild("pep_end", Namespace.getNamespace(mascotNameSpace));	
					Element this_prot_desc = this_protein.getChild("prot_desc", Namespace.getNamespace(mascotNameSpace));
					Element this_prot_score = this_protein.getChild("prot_score", Namespace.getNamespace(mascotNameSpace));
					Element pep_scan_title= this_pep_query.getChild("pep_scan_title", Namespace.getNamespace(mascotNameSpace));		
										
					PeptideID tmpPeptideID = new PeptideID();
					tmpPeptideID.protein_acc=this_protein.getAttribute("accession").getValue();
					tmpPeptideID.protein_desc=this_prot_desc.getText();
					tmpPeptideID.peptideExpect=Double.parseDouble(pep_expect.getText());
					tmpPeptideID.peptideSequence=pep_seq.getText();
					tmpPeptideID.deltaMass=Double.parseDouble(pep_delta.getText());
					tmpPeptideID.mass=java.lang.Double.parseDouble(pep_exp_mr.getText());				
					tmpPeptideID.title=pep_scan_title.getText();
					tmpPeptideID.peptideCharge=Integer.parseInt(pep_exp_z.getText());
					tmpPeptideID.exp_mz=Double.parseDouble(pep_exp_mz.getText());
	
					if(pep_start!=null)
					tmpPeptideID.peptideStart=Integer.parseInt(pep_start.getText());
					
					if(pep_end!=null)
					tmpPeptideID.peptideEnd=Integer.parseInt(pep_end.getText());
					tmpPeptideID.protein_score=Double.parseDouble(this_prot_score.getText());//frankp 20070323
					if(mod_seq.getText()!=null)
						tmpPeptideID.peptide_mod=mod_seq.getText();
						
					tmpPeptideID.queryID=""+query_index;
					tmpPeptideID.print();
					pepList.add(tmpPeptideID);
				} 
			}
		}
		
		//unassigned queries/////////////////////////////////////////////////////////
		Element mascot_unassigned=mascot_result.getChild("unassigned",  Namespace.getNamespace(mascotNameSpace));

		logger.info("processing unassigned queries");
		
		/////////////////////////////////////////////////////////the end
		logger.debug("MascotXMLRecordLoader::starting list_query_loop");
//		for(int i=0; i<list_query.size(); i++){
//			Spectrum tmpSpectrum= new Spectrum();
//
//			Element this_query = (Element) list_query.get(i);
//			tmpSpectrum.queryID= this_query.getAttributeValue("number");//new 20070115
//			//System.out.println("\n-------------------\nquery_number:" + this_query.getAttributeValue("number"));
//			Element thisTitle = this_query.getChild("StringTitle",  Namespace.getNamespace(mascotNameSpace));
//			if(thisTitle==null)
//			{
//				tmpSpectrum.title="query:"+tmpSpectrum.queryID;//Holger Kramer Defekt holger.kramer@linacre.ox.ac.uk 20080611
//			}
//			else{
//				if(thisTitle.getText().length()==0){tmpSpectrum.title="query:"+tmpSpectrum.queryID;}
//				else{
//					tmpSpectrum.title=thisTitle.getText();
//				}
//			}
//	
//
//			Element thisNumVals = this_query.getChild("NumVals",  Namespace.getNamespace(mascotNameSpace));
//
//			tmpSpectrum.queryID=this_query.getAttributeValue("number");
//			int qn=Integer.parseInt(this_query.getAttributeValue("number"));//to avoid long names
//			/////////////////////////////////////////////////get the precursor mass and the charge
//
//
//
//
//	
//		}	
		doc = null;	
		}catch(Exception e){
			logger.error("loadMascotPeptideID:exception:"+e.toString());
			e.printStackTrace();
		}
		return pepList;
	}
	
	public static void calibratePrecursor(Spectrum spec){
		double addedMass=0;
		int hist[]=new int[400];
		for(int i=0;i<spec.valueList.size();i++){
			for(int j=i+1;j<spec.valueList.size();j++){
				addedMass=spec.valueList.get(i).massToCharge+spec.valueList.get(j).massToCharge;
				if(Math.abs(addedMass-spec.precursorMass)<2.0){
					int bin = 200+(int)(100.0*(addedMass-spec.precursorMass));
					if (bin<0 || bin >399) {
						logger.fatal("error: bin=" + bin);
						System.exit(1);
					}
					hist[bin]++;
				}
			} 
		}
		int maxi=Integer.MIN_VALUE;
		int maxLoc=0;
		double estimateMass=0;
		int estimateCount=0;
		for(int i=0;i<400;i++){
			if(hist[i]>maxi){maxi=hist[i];maxLoc=i;}
			double m=spec.precursorMass+(double)(i-200)/100.0;
			if (hist[i]>0)
				logger.debug("i: " + i + " " + hist[i] + " m: " + m + " pMass: " +
										 spec.precursorMass);
			estimateMass+=  m*hist[i];
			estimateCount+= hist[i];

		} 
		if(estimateCount>0){
			double newMass=estimateMass/estimateCount;
			logger.debug("estimated: " + newMass + " measured: " + spec.precursorMass +
									 " delta: " + (newMass-spec.precursorMass));
		}
		logger.debug("-----------------------------------------");
	}

}

