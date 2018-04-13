/* $Id: AutomationContainer.java 299 2010-05-13 12:11:42Z frank $ */

package com.detectorvision.massspectrometry.analyzation;

import com.detectorvision.massspectrometry.biodata.Record;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class AutomationContainer {
	public ArrayList<String> FilesToProcess;

	// Logging with log4j
	static Logger logger = Logger.getLogger(AutomationContainer.class.getName());

	public AutomationContainer(){
		this.refresh();
	}
	public void refresh(){
		ArrayList<String> datFiles = new ArrayList<String>();
		File tmpFile = new File("automation/mascot/searches.log.txt");
		//------------------------------------------------add .dat files
		if(tmpFile.exists()){
			try {
				BufferedReader in = new BufferedReader(new FileReader("automation/mascot/searches.log.txt"));
				String str;
				Pattern pattern = Pattern.compile("^../data/*[0-9]*/[A-Za-z0-9._]+.dat$");

				while ((str = in.readLine()) != null) {
					String[] words = str.split("\\s+");//any amount of whitespace
					if(! words[0].equals("Job#")){
						for(int i=0;i<words.length;i++){
							Matcher matcher = pattern.matcher(words[i]);
							while (matcher.find()) {
								datFiles.add(matcher.group());
								logger.debug("adding::"+matcher.group());
							}
						}	
					}
				}
				in.close();
			} catch (IOException e) {
				logger.error("no searches.log.txt file? IOException: " +
										 e.getLocalizedMessage());
			}
			logger.debug("returning from datFiles reading\n-------------------\n");
		}
		else{
			logger.info("searches.log.txt not found");
		}

		//------------------------------------------------add .mgf files
		File dir = new File("automation/mgf/data");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				logger.debug("checking mgf file " + children[i]);
				if(children[i].endsWith(".mgf")){
					datFiles.add("automation/mgf/data/" +children[i]);
					logger.debug("mgf: " + children[i]);
				}
			}
		}
		else{
			logger.info("automation/mgf/data directory not present .... ");
		}
		logger.debug("ready with adding .mgf files");
//		------------------------------------------------add .mascot.xml files
		dir = new File("automation/mascot/data");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				logger.debug("checking mascot.xml file " + children[i]);
				if(children[i].endsWith(".xml")){
					datFiles.add("automation/mascot/data/" + children[i]);
					logger.debug(".xml:" + children[i]);
				}
			}
		}
		else{
			logger.info("automation/mgf/data directory not present .... ");
		}
		logger.debug("Ready with adding .mascot.xml files:found: " +datFiles.size());

		//---------------------check if files are allready in DMB. If yes: remove!
		try{
		FilesToProcess=Record.DMBCheckListForExistence(datFiles);
		}catch(Exception e){
			logger.error("DMBCheckListForExistence error:"+e.toString());
			return;
		}
		logger.info("number of files after DBCheck:"+FilesToProcess.size());
		datFiles=null;
	}

	
	public int getNumDat(){
		int count=0;
		for(int i=0;i<FilesToProcess.size();i++){
			if(FilesToProcess.get(i).endsWith(".dat")){count++;}
		}
		return count;
	}

	public int getNumMascotXml(){
		int count=0;
		for(int i=0;i<FilesToProcess.size();i++){
			if(FilesToProcess.get(i).endsWith(".xml")){count++;}
		}
		return count;
	}

	public int getNumMgf(){
		int count=0;
		for(int i=0;i<FilesToProcess.size();i++){
			if(FilesToProcess.get(i).endsWith(".mgf")){count++;}
		}
		return count;
	}

	public int getNumDatAtLocalComputer(){
		int numImportedFiles=0;
		for(int loop=0;loop<FilesToProcess.size();loop++){
			if(FilesToProcess.get(loop).endsWith(".dat")){
				String outFile=FilesToProcess.get(loop)+ ".xml";
				outFile = outFile.replaceFirst(".*/", "");
				outFile = "automation/mascot/data/" + outFile;
				logger.debug("Checking local presence of: " + outFile);
				File testFile = new File(outFile);
				if(testFile.exists()){
					numImportedFiles++;
				}
			}
		}
		return numImportedFiles;
	}

	public int getNumDatToFetch(){
		int numImportedFiles=0;
		int numDatFiles=0;
		for(int loop=0;loop<FilesToProcess.size();loop++){
			if(FilesToProcess.get(loop).endsWith(".dat")){
				numDatFiles++;
				String outFile=FilesToProcess.get(loop)+ ".xml";
				logger.debug("Checking local presence of: " + outFile);
				File testFile = new File(outFile);
				if(testFile.exists()){
					numImportedFiles++;
				}
			}
		}
		return (numDatFiles-numImportedFiles);
	}

}
