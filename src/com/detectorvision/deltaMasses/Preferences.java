/* $Id: Preferences.java 226 2008-08-19 14:05:40Z frank $ */

package com.detectorvision.deltaMasses;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Loads and saves the deltaMasses preferences.
 * @author lehmamic
 */
public class Preferences {
	
	// Attributs
	private String 			fileName;
	
	// Preference names
	public final static String PREF_FILE_UNIMOD = "file.unimod";
	public final static String PREF_CLASS_ALGORITHM = "class.algorithm";
	
	// Preferences
	private String 			uniModFile;
	private String			algorithmClass;
	
	// XML Data
	private SAXBuilder 		builder;
	private Document 		doc;

	// Logging with log4j
	static Logger logger = Logger.getLogger(Preferences.class.getName());
	
	// Constructors
	/**
	 * Constructor loads the preferences data from the XML file.
	 * 
	 * @param fileName Filename of the preferences file.
	 * @throws IOException Throws an Exception if the method is unable to read the file.
	 */
	public Preferences(String fileName)throws IOException{
		this.fileName = fileName;
		java.io.File f = new java.io.File(fileName);
		// initialize the xml-api
		try{
			this.builder = new SAXBuilder();
			this.doc = this.builder.build(f);
		}
		catch(JDOMException e){
			logger.error("JDOMException:"+e.toString());
			throw new IOException(e.getMessage());
		}
		this.loadPreferences();
	}
	
	// Methods
	/**
	 * Loads the preferences data from the XML file.
	 * 
	 * @throws IOException Throws an Exception if the method is unable to read the file.
	 */
	private void loadPreferences() throws IOException {
		// root element
		Element deltaMasses = this.doc.getRootElement();
		if(deltaMasses == null || !deltaMasses.getName().equals("deltaMasses")){
			logger.error("preferences error");
			throw new IOException("Incorrect deltaMasses preferences file!");
		}
		// get a list of settings elements
		List settingList = deltaMasses.getChildren("setting");
		if(settingList == null || settingList.size() == 0)
			throw new IOException("No preference data available!");
		
		// iterate thru the list an save the preferences
		for(int i=0; i<settingList.size(); i++){
			// get the setting element
			Element setting = (Element) settingList.get(i);
			
			// get the preference
			Element name = setting.getChild("name");
			Element value = setting.getChild("value");
			if(name == null || value == null)
				throw new IOException("Incorrect preference values!");
			
			// store the preference
			if(name.getTextTrim().equals(Preferences.PREF_FILE_UNIMOD)){
				this.uniModFile = value.getTextTrim();
			}
			
			// store algorythm archive
			else if(name.getTextTrim().equals(Preferences.PREF_CLASS_ALGORITHM)){
				this.algorithmClass = value.getTextTrim();
			}		
		}	
	}
	
	/**
	 * Returns the filename of the unimod database dump.
	 * @return Unimod database dump filename.
	 */
	public String getUniModFile(){
		logger.info("Trying to get uniModFile:"+this.uniModFile);
		return this.uniModFile;
	}
	
	/**
	 * Returns the classname of the algorithm.
	 * @return Classname as string.
	 */
	public String getAlgorithmClass(){
		return this.algorithmClass;
	}
}