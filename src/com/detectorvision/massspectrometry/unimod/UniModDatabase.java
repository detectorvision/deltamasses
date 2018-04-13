/* $Id: UniModDatabase.java 406 2010-11-06 16:02:21Z frank $ */

package com.detectorvision.massspectrometry.unimod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Logger;

import com.detectorvision.deltaMasses.DeltaMasses;

/**
 * Singelton class UniModDatabase represents the public online portal UniMod, whitch provides
 * a database containing all known modification infromations.
 * 
 * @author lehmamic
 */
public class UniModDatabase {
	
	// Attributs
	private String fileName;
	
	// Modifications
	private ArrayList<Modification> modificationList;
	
	// XML Data
	private SAXBuilder 		builder;
	private Document 		doc;
	
	static Logger logger = Logger.getLogger(UniModDatabase.class.getName());
	
	// Constructors
	/**
	 * The constructor loads the modifications from the UniMod XML database dump into the virtual data structure.
	 * @param fileName Filename of the databasedump.
	 * @throws IOException
	 */
	public UniModDatabase(String fileName) throws IOException{//TODO argument is not used anymore, remove in references.
		fileName=DeltaMasses.preferences.getUniModFile();
		java.io.File f = new java.io.File(fileName);
		logger.info("unimod file to be loaded:" + f.getAbsolutePath());
		this.modificationList = new ArrayList<Modification>();
		// initialize the xml-api
		try{
			this.builder = new SAXBuilder();
			this.doc = this.builder.build(f);
		}
		catch(JDOMException e){
			logger.error("UniModDatabase:JDOMException:"+e.toString());
			throw new IOException(e.getMessage());
		}
		// load the modifications
		try{
			logger.info("starting to load unimod");
			this.loadModifications();
			logger.info("UniModDatabase:loading OK");
		}
		catch(Exception e){
			logger.error("UniModDatabase:Exception:"+e.toString());
		}
	}
		
	// Methods
	/**
	 * Loads the peptide modification informations into the virtual data structure.
	 * @param fileName Filename of the databasedump.
	 * @throws IOException
	 */
	private void loadModifications() throws IOException{
		try{
			
		this.addPeptides();
			
		Element unimod = this.doc.getRootElement();
		if(unimod == null || !unimod.getName().equals("unimod")){
			logger.error("Incorrect unimod database file! No unimod node available: " + unimod.getName());
			throw new IOException("Incorrect unimod database file! No unimod node available: " + unimod.getName());
		}
		// modification element
		Element modifications = unimod.getChild("modifications", Namespace.getNamespace("http://www.unimod.org/xmlns/schema/unimod_2"));
		if(modifications == null){
			logger.error("UnidModDatabase:loadModifications:modifications node not available:");
			throw new IOException("No umod:modifications node available");
		}
		
		// Modifications_row element
		List modList = modifications.getChildren("mod", Namespace.getNamespace("http://www.unimod.org/xmlns/schema/unimod_2"));
		if(modList == null || modList.size() == 0){
			logger.error("no mod available");
			throw new IOException("No mod available!");
		}
		// iterate and load modifications
		for(int i=0; i<modList.size(); i++){
			Element mod_row = (Element) modList.get(i);
			Modification mod = new Modification();
			Element delta = (Element) mod_row.getChild("delta", Namespace.getNamespace("http://www.unimod.org/xmlns/schema/unimod_2"));
			mod.monoisotopic=Double.parseDouble(delta.getAttributeValue("mono_mass"));
			mod.average=Double.parseDouble(delta.getAttributeValue("avge_mass"));
			mod.composition=delta.getAttributeValue("composition");
			mod.fullName=mod_row.getAttributeValue("full_name");
			mod.shortName=mod_row.getAttributeValue("title");			
			mod.unimodID=Integer.parseInt(mod_row.getAttributeValue("record_id"));
			mod.postedDate=mod_row.getAttributeValue("date_time_posted");
			mod.modifiedDate=mod_row.getAttributeValue("date_time_modified");
			this.modificationList.add(mod);
		}
		
		//PTMString+="(737,'PEG 2','PEG 2',88.0523,88.0443,'H(8) C(4) O(2)','comment on peg 2','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		Modification mod1 = new Modification();
		mod1.monoisotopic=88.0443;
		mod1.average=88.0523;
		mod1.composition="H(8) C(4) O(2)";
		mod1.fullName="PEG 2";
		mod1.shortName="PEG 2";			
		mod1.unimodID=100001;
		this.modificationList.add(mod1);	
		
		//PTMString+="(738,'PEG 3','PEG 3',132.07845,132.06645,'H(12) C(6) O(3)','comment on peg 3','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		Modification mod2 = new Modification();
		mod2.monoisotopic=132.06645;
		mod2.average=132.07845;
		mod2.composition="H(12) C(6) O(3)";
		mod2.fullName="PEG 3";
		mod2.shortName="PEG 3";			
		mod2.unimodID=100002;
		this.modificationList.add(mod2);		
		
		//PTMString+="(739,'PEG 4','PEG 4',176.1046,176.0886,'H(16) C(8) O(4)','comment on peg 4','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		Modification mod3 = new Modification();
		mod3.monoisotopic=176.0886;
		mod3.average=176.1046;
		mod3.composition="H(16) C(8) O(4)";
		mod3.fullName="PEG 4";
		mod3.shortName="PEG 4";			
		mod3.unimodID=100003;
		this.modificationList.add(mod3);	
		
		//PTMString+="(740,'PEG 5','PEG 6',,,'H(20) C(10) O(5)','comment on peg 5','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		Modification mod4 = new Modification();
		mod4.monoisotopic=220.11075;
		mod4.average=220.13075;
		mod4.composition="H(20) C(10) O(5)";
		mod4.fullName="PEG 5";
		mod4.shortName="PEG 5";			
		mod4.unimodID=100004;
		this.modificationList.add(mod4);	
		
		//PTMString+="(741,'PEG 6','PEG 6',264.1569,264.1329,'H(24) C(12) O(6)','comment on peg 6','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0)";
		Modification mod5 = new Modification();
		mod5.monoisotopic=264.1329;
		mod5.average=264.1569;
		mod5.composition="H(24) C(12) O(6)";
		mod5.fullName="PEG 6";
		mod5.shortName="PEG 6";			
		mod5.unimodID=100005;
		this.modificationList.add(mod5);	
	}catch(Exception e){
		logger.error("unidmod load error:"+e.toString());
		}
	}
		
	
	/**
	 * Returns a list of all loaded modifiaction
	 * @return returns an ArrayList if some modifiacations are loaded, else it returns null.
	 */
	public ArrayList<Modification> getModifications(){
		if(this.modificationList.size() > 0)
			return this.modificationList;
		else
			return null;
	}
	
	private void addPeptides(){
		double[] amino_mass = new double[23];
		amino_mass[0]=71.037110; //A
		amino_mass[1]=114.534940;//B
		amino_mass[2]=103.009180; //C
		amino_mass[3]=115.026940; //D
		amino_mass[4]=129.042590; //E
		amino_mass[5]=147.068410; //F
		amino_mass[6]=57.021460; //G
		amino_mass[7]=137.058910; //H
		amino_mass[8]=113.084060;//I
		amino_mass[9]=128.094960; //K
		amino_mass[10]=113.084060; //L
		amino_mass[11]=131.040480; //M
		amino_mass[12]=114.042930; //N
		amino_mass[13]=97.052760; //P
		amino_mass[14]=128.058580; //Q
		amino_mass[15]=156.101110; //R
		amino_mass[16]=87.032030; //S
		amino_mass[17]=101.047680; //T
		amino_mass[18]=150.953630; //U
		amino_mass[19]=99.068410; //V
		amino_mass[20]=186.079310; //W
		amino_mass[21]=163.063330; //Y
		amino_mass[22]=128.550590; //Z
		char[] amino = new char[23];
		amino[0]='A';
		amino[1]='B';
		amino[2]='C';
		amino[3]='D';
		amino[4]='E';
		amino[5]='F';
		amino[6]='G';
		amino[7]='H';
		amino[8]='I';
		amino[9]='K';
		amino[10]='L';
		amino[11]='M';
		amino[12]='N';
		amino[13]='P';
		amino[14]='Q';
		amino[15]='R';
		amino[16]='S';
		amino[17]='T';
		amino[18]='U';
		amino[19]='V';
		amino[20]='W';
		amino[21]='Y';
		amino[22]='Z';
		
		for(int i=0;i<23;i++){
			Modification tmpMod=new Modification();
			tmpMod.monoisotopic=amino_mass[i];
			tmpMod.shortName="Amino "+amino[i];
			tmpMod.fullName="Amino Acid:"+amino[i];
			tmpMod.unimodID=100006+i;
			this.modificationList.add(tmpMod);
		}
	}
}
