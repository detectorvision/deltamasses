/* $Id: Record.java 341 2010-06-06 11:40:25Z frank $ */

package com.detectorvision.massspectrometry.biodata;

import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.datacontrol.PeptideIDLoader;
import com.detectorvision.massspectrometry.datacontrol.RecordLoader;
import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.GaussianFit;
import com.detectorvision.utility.Protein;
import com.detectorvision.utility.ProteinList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.omg.PortableServer.IdAssignmentPolicy;

/**
 * Mass spectrometry record data structure. A record describes a mass spectrometry measurement
 * and contains all spectras with their msms-pairs data of a peakfile.
 * 
 * @author frank.potthast@detectorvision.com
 * (c) 2006-2007 Detectorvision AG, Zurich www.detectorvision.com
 */ 
public class Record {
	// Attributes
	private String fileName;
	private String tandemName;
	private float msPrecision;
	private float msmsPrecision;
	private MassUnit msPrecisionUnit;
	private MassUnit msmsPrecisionUnit;
	private boolean hasRetention = false;
	private SpectraPair currentPair = null;
	private ArrayList<Spectrum> spectrumList;
	private ArrayList<PeptideID> peptideIDList;
	public ArrayList<SpectraPair> pairList;//V2.1
	public String searchReportHttp;//frankp 20070322 complete http link to the search result including "http://"
	public boolean msmsBetterPrecisionThanMs;//frankp 20070326 for Q-Tof type data, this is true
	public boolean hasBeenStored=false;
	private int recordID=0;
	
	public enum load_type_enum {
		NORMAL_RECORD,LOADED_FROM_DB,LOADED_FOR_DELTA_CLUSTER
	}
	public load_type_enum load_type;

	//20070129 frank
	private String originMethod="mgf";  //should be an enum: random mascot xtandem mgf  mascot-xtandem used by pmc only (block xtandem)

	// Logging with log4j
	static Logger logger = Logger.getLogger(Record.class.getName());

	// Constructors

	/**
	 * Default constructor to allow initialisation of an object without reading a peakfile.
	 */
	public Record(){
		this.originMethod="";
		this.load_type=load_type.NORMAL_RECORD;

	}
	/**
	 * Constructor of th record class. Loads and stores the mass spectrometry data of a peak file.
	 * @param peakFile Filename of the peakfile.
	 * @param recordLoader Recordloader class whitsch imports the RecordLoader interface.
	 * @throws IOException Throws an exception if the loader is unable to load the data.
	 */
	public Record(String peakfilename, RecordLoader recordLoader, ProgressListener listener) throws IOException{	
		Record tmp = recordLoader.loadRecord(peakfilename,listener);//alarm ,tandemName before

		this.hasRetention = tmp.hasRetention();

		this.spectrumList = tmp.spectrumList;
		this.fileName = tmp.fileName;
		this.msmsPrecision = tmp.msmsPrecision;
		this.msmsPrecisionUnit = tmp.msmsPrecisionUnit;
		this.msPrecision = tmp.msPrecision;
		this.msPrecisionUnit = tmp.msPrecisionUnit;
		this.peptideIDList = tmp.peptideIDList;
		this.tandemName = tmp.tandemName;
		this.originMethod=tmp.originMethod;
		this.pairList = tmp.pairList;//V2.1
		this.searchReportHttp=tmp.searchReportHttp;//frankp20070322

		/*for(MSMS msms : this.spectrumList.get(0).valueList){
			System.out.println(" m/z: " + msms.massToCharge + " int:" + msms.intensity );
		}*/
	}

	/**
	 * Constructor of th record class. Loads the mass spectrometry data of a peak file.
	 * Further it loads additional informations saved in a tandem file.
	 * @param peakFile Filename of the peakfile.
	 * @param idFile Filename of the tandemfile.
	 * @param recordLoader Recordloader class whitsch imports the RecordLoader interface.
	 * @param petideIDLoader PeptideIDloader class whitsch imports the PeptideIDLoader interface.
	 * @throws IOException Throws an exception if the loader is unable to load the data.
	 */
	public Record(String peakfilename, RecordLoader recordLoader, String peptidefilename,  PeptideIDLoader petideIDLoader,ProgressListener listener) throws IOException{

		/**
		 *  This constructor's arguments are not yet used. Maybe in version 2, who knows...
		 */
		this(peakfilename, recordLoader,listener);
	}

	// Methods
	/**
	 * Loads the specified peakfile and stors the data in the record class.
	 * @param fileName Filename of the peakfile.
	 * @param loader Fileloader for this kind of filestructure.
	 * @throws IOException Throws an exception if the loader is unable to load the data.
	 */
	public void loadPeakfile(String peakfilename, RecordLoader recordLoader,ProgressListener listener) throws IOException{
		Record tmp = recordLoader.loadRecord(peakfilename, listener);//alarm: ,tandemName before

		this.hasRetention = tmp.hasRetention;
		this.spectrumList = tmp.spectrumList;
		this.fileName = tmp.fileName;
		this.msmsPrecision = tmp.msmsPrecision;
		this.msmsPrecisionUnit = tmp.msmsPrecisionUnit;
		this.msPrecision = tmp.msPrecision;
		this.msPrecisionUnit = tmp.msPrecisionUnit;
		this.peptideIDList = tmp.peptideIDList;
		this.tandemName = tmp.tandemName;
		this.originMethod = tmp.originMethod;
		this.pairList=tmp.pairList;//V2.1
		this.searchReportHttp=tmp.searchReportHttp;//frankp 20070322
	}

	/**
	 * Loads the additional peptide informations of a peakfile an stors the data in de record class.
	 * @param fileName FIlename of the tandemfile.
	 * @param loader Fileloader for this kind of filestructure.
	 * @throws IOException Throws an exception if the loader is unable to load the data.
	 */
	public void loadTandemFile(String fileName, PeptideIDLoader loader) throws IOException{
	}

	/**
	 * Return the spectrum list.
	 * @return spectrum list
	 */

	public int getSpectrumNumber(){
		int numSpectra=0;
		try{
			ArrayList<Spectrum> sl = this.getSpectrumList();
			numSpectra=sl.size();
		}
		catch(Exception e){
			logger.error("error in getSpectrumNumber:"+e.toString());
			return 0;
		}
		return numSpectra;
	}

	public ArrayList<Spectrum> getSpectrumList(){
		return this.spectrumList;
	}

	/**
	 * Return the PeptideID list
	 * @return peptide id list
	 */
	public ArrayList<PeptideID> getPeptideIDList(){
		return this.peptideIDList;
	}

	/**
	 * Return the peakfile name.
	 * @return filename
	 */
	public String getFileName(){
		return this.fileName;
	}

	/**
	 * Return the tandemfile name
	 * @return filename
	 */
	public String getTandemName(){
		return this.tandemName;
	}

	/**
	 * Return the ms precision.
	 * @return msPrecisison
	 */
	public float getMsPrecision(){
		return this.msPrecision;
	}

	/**
	 * Return the msms precision.
	 * @return msmsPrecision
	 */
	public float getMsmsPrecision(){
		return this.msmsPrecision;
	}

	/**
	 * Return the ms precision unit.
	 * @return ms precision unit
	 */
	public MassUnit getMsPrecisionUnit(){
		return this.msPrecisionUnit;
	}

	/**
	 * Return the msms precision unit.
	 * @return msms precision unit
	 */
	public MassUnit getMsmsPrecisionUnit(){
		return this.msmsPrecisionUnit;
	}

	/**
	 * Return the origin Method.
	 * @return originMethod
	 */
	public String getOriginMethod(){
		return this.originMethod;
	}


	/**
	 * Return the origin Method.
	 * @return originMethod
	 */
	public String getSearchReportHttp(){
		return this.searchReportHttp;
	}



	/**
	 * Return the PairList
	 * @return Arraylist<SpectraPair> 
	 */
	public ArrayList<SpectraPair> getPairlist()
	{
		return this.pairList;
	}

	public void setOriginMethod(String oriMethod){
		this.originMethod=oriMethod;
	}	
	/**
	 * Sets the peakfile name.
	 * @param fileName Filename
	 */
	public void setFileName(String fileName){
		this.fileName = fileName;
	}


	/**
	 * Sets the searchReportHttp
	 * @param String searchReportHttp
	 */
	public void setSearchReportHttp(String searchReportHttp){
		this.searchReportHttp= searchReportHttp;
	}


	/**
	 * Sets the tandemfile name.
	 * @param tandemFile Filename
	 */
	public void setTandemName(String tandemFile){
		this.tandemName = tandemFile;
	}

	/**
	 * Sets the ms precision
	 * @param msPrecision ms precision
	 */
	public void setMsPrecision(float msPrecision){
		this.msPrecision = msPrecision;
	}

	/**
	 * Sets the msms precision
	 * @param msmsPrecision msmsm precision
	 */
	public void setMsmsPrecision(float msmsPrecision){
		this.msmsPrecision = msmsPrecision;
	}

	/**
	 * Sets the ms precision unit
	 * @param msPrecisionUnit ms precision unit
	 */
	public void setMsPrecisionUnit(MassUnit msPrecisionUnit){
		this.msPrecisionUnit =  msPrecisionUnit;
	}

	/**
	 * Sets the msms precision unit
	 * @param msmsPrecisionUnit msms precision unit
	 */
	public void setMsmsPrecisionUnit(MassUnit msmsPrecisionUnit){
		this.msmsPrecisionUnit = msmsPrecisionUnit;
	}
	/**
	 * Set hasRetention
	 * 
	 * @param hasRetention
	 */
	public void setHasRetention(boolean hasRetention){
		this.hasRetention = hasRetention;
	}

	/**
	 * Returns true, if the racord has a retention
	 * @return
	 */
	public boolean hasRetention(){
		return this.hasRetention;
	}

	/**
	 * Sets a new spectrum list.
	 * @param spectrumList a list of spectren
	 */
	public void setSpectrumList(ArrayList<Spectrum> spectrumList){
		this.spectrumList = spectrumList;
	}

	/**
	 * Sets a new peptide id list
	 * @param peptideIDList a list of peptideID's
	 */
	public void setPeptideIDList(ArrayList<PeptideID> peptideIDList){
		this.peptideIDList = peptideIDList;
	}

	/**
	 * Sets a new pairList
	 * @param ArrayList<SpectraPair>
	 */
	public void setPairList(ArrayList<SpectraPair> pairList){
		this.pairList = pairList;
	}

	/**
	 * sets the current spectra pair
	 * @param pair
	 */
	public void setCurrentSpectraPair(SpectraPair pair){
		this.currentPair = pair;
	}

	/**
	 * Returns the current spectrapair
	 * @return
	 */
	public SpectraPair getCurrentSpectraPair(){
		return this.currentPair;
	}

	public boolean printXML(String xmlOutFile,ArrayList <SpectraPair>pairItemList){//new build 65
		logger.info("Record:printXML:"+xmlOutFile);
		Element rootElement = new Element("differentialPtmDetection");
		Document myDocument = new Document(rootElement);

		Element analysis = new Element("analysis");
		rootElement.addContent("");
		rootElement.addContent(analysis);

		analysis.addContent(new Element("data_source_spectrum").addContent(this.getFileName()));
		analysis.addContent(new Element("data_source_peptide_id").addContent(this.getTandemName()));
		analysis.addContent(new Element("name").addContent("deltaMasses"));
		analysis.addContent(new Element("version").addContent("version 2.0 build 68"));


		long now = System.currentTimeMillis();
		String tmp=null;
		Date d= new Date(now);

		tmp = String.format("%tD", d) + " " + String.format("%tR",now);//TODO improve the date format
		analysis.addContent(new Element("date").addContent(tmp));

		analysis.addContent(new Element("investigator").addContent(System.getProperty("user.name")));
		//see platform dependency comment on http://java.sun.com/docs/books/tutorial/essential/environment/env.html

		analysis.addContent(new Element("MsPrecision").addContent(String.format("%.2f",this.getMsPrecision())));
		analysis.addContent(new Element("MsMsPrecision").addContent(String.format("%.2f",this.getMsmsPrecision())));
		analysis.addContent(new Element("numberOfSpectra").addContent(Integer.toString(this.getSpectrumList().size())));
		analysis.addContent(new Element("peptideIdOriginMethod").addContent(this.getOriginMethod()));

		Element pairList = new Element("pairList");
		rootElement.addContent(pairList);

		int pairId=0;
		for(SpectraPair pairItem:pairItemList){
			Element pair = new Element("pair");
			pairId++;
			pair.setAttribute("id",Integer.toString(pairId));
			pair.setAttribute("lightSpec",pairItem.spectrumA.title);
			pair.setAttribute("heavySpec",pairItem.spectrumB.title);
			double dm = pairItem.spectrumB.precursorMass - pairItem.spectrumA.precursorMass;
			pair.setAttribute("dm",String.format("%.6f",dm));
			pair.setAttribute("p",String.format("%.3g",pairItem.p));
			pair.setAttribute("sim",String.format("%.2g",pairItem.score));

			if(pairItem.knownModification != null){
				Element potentialMod = new Element("potentialMod");
				potentialMod.setAttribute("modName",pairItem.knownModification.shortName);
				potentialMod.setAttribute("composition",pairItem.knownModification.composition);
				potentialMod.setAttribute("monoisotopic",String.format("%.6f",pairItem.knownModification.monoisotopic));
				potentialMod.setAttribute("unimodID",Integer.toString(pairItem.knownModification.unimodID));
				pair.addContent(potentialMod);
			}

			if(pairItem.spectrumA.pepSequence != null){
				Element lightPeptide = new Element("lightPeptide");
				lightPeptide.setAttribute("Pep",pairItem.spectrumA.pepSequence);
				lightPeptide.setAttribute("Prot",pairItem.spectrumA.proteinAsc);
				lightPeptide.setAttribute("PepStart",Integer.toString(pairItem.spectrumA.pepStart));
				lightPeptide.setAttribute("PepEnd",Integer.toString(pairItem.spectrumA.pepEnd));
				pair.addContent(lightPeptide);
			}
			if(pairItem.spectrumB.pepSequence != null){
				Element heavyPeptide = new Element("heavyPeptide");
				heavyPeptide.setAttribute("Pep",pairItem.spectrumB.pepSequence);
				heavyPeptide.setAttribute("Prot",pairItem.spectrumB.proteinAsc);
				heavyPeptide.setAttribute("PepStart",Integer.toString(pairItem.spectrumB.pepStart));
				heavyPeptide.setAttribute("PepEnd",Integer.toString(pairItem.spectrumB.pepEnd));
				pair.addContent(heavyPeptide);
			}

			Element QualityControl= new Element("qualityControl");
			QualityControl.setAttribute("humanControlled","no");
			QualityControl.setAttribute("quality","auto");
			QualityControl.setAttribute("comment","");
			{
				Date tmpDate = new Date(System.currentTimeMillis());
				SimpleDateFormat tmpDf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
				String tmpDeltaDate=tmpDf.format( tmpDate );
				QualityControl.setAttribute("date",tmpDeltaDate);
			}

			QualityControl.setAttribute("name","deltaMasses");
			pair.addContent(QualityControl);

			pairList.addContent(pair);
		}

		try {
			XMLOutputter outputter = new XMLOutputter();
			FileWriter writer = new FileWriter(xmlOutFile);
			outputter.output(myDocument, writer);
			writer.close();
		} catch (java.io.IOException e) {
			logger.error("Record:XmlOutputter:IOException:"+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}	
		return true;
	}

	public boolean printRecalibratedMgf(double a, double b, String targetFile){
		logger.info("Record:printRecalibratedMgf: a=" + a + " b=" + b +
				": mgf to:" + targetFile);
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(targetFile));
			for(int i=0;i<this.spectrumList.size();i++){
				out.write("BEGIN IONS\n");
				out.write("TITLE="+this.spectrumList.get(i).title+"\n");
				out.write("CHARGE="+this.spectrumList.get(i).charge+"+\n");
				double betterMass=this.spectrumList.get(i).pepMz -a - b* this.spectrumList.get(i).pepMz;
				out.write("PEPMASS="+betterMass+"\n");

				int nofMSMS=this.spectrumList.get(i).valueList.size();
				MSMS [] tmpMSMSList = new MSMS[nofMSMS];
				for(int j=0; j<nofMSMS; ++j) {
					MSMS newMSMS = new MSMS();
					//warning - reversed logic below!!!!!
					newMSMS.intensity=this.spectrumList.get(i).valueList.get(j).massToCharge;
					newMSMS.massToCharge=this.spectrumList.get(i).valueList.get(j).intensity;
					tmpMSMSList[j]=newMSMS;
				}
				Arrays.sort(tmpMSMSList);
				double tic=this.spectrumList.get(i).tic;
				for(int j=0; j<nofMSMS; ++j) {
					//warning still reversed logic so the sort works straight ahead) TODO:fix
					out.write(tmpMSMSList[j].intensity+"\t"+String.format("%.6g",tmpMSMSList[j].massToCharge * tic)+"\n");
				}		
				out.write("END IONS\n\n");
			}
			out.close();
		} catch (IOException e) {
			logger.error("Record:.mgf calibration:IOException: " +
					e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean printTSV(String outFile,ArrayList <SpectraPair>pairItemList){
		logger.info("Record:exporting record to " + outFile);
		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter(outFile));
			int pairId=0;
			out.write("deltaMass\trandom probability\tsimilarity\tlight protein\tlight peptide\tstart\tend\tlight query\theavy protein\theavy peptide\tstart\tend\theavy query\tlight title\theavy title\n");
			for(SpectraPair pairItem:pairItemList){
				//System.out.println("exporting" + pairId);
				pairId++;
				double dm = pairItem.spectrumB.precursorMass - pairItem.spectrumA.precursorMass;
				out.write(dm + "\t");
				//out.write(pairId + "\t");
				out.write(pairItem.p + "\t");
				out.write(pairItem.score + "\t");
				//if(pairItem.knownModification.shortName != null)
				//		{out.write(pairItem.knownModification.shortName + "\t");}
				//else{out.write("-\t");}
				if(pairItem.spectrumA.pepSequence != null){
					out.write(pairItem.spectrumA.proteinAsc + "\t");
					out.write(pairItem.spectrumA.pepSequence + "\t");
					out.write(pairItem.spectrumA.pepStart + "\t");
					out.write(pairItem.spectrumA.pepEnd + "\t");
					out.write(pairItem.spectrumA.queryID + "\t");
				}
				else{out.write("-\t"+"-\t "+"-\t"+"-\t"+pairItem.spectrumA.queryID+"\t");}
				if(pairItem.spectrumB.pepSequence != null){
					out.write(pairItem.spectrumB.proteinAsc + "\t");
					out.write(pairItem.spectrumB.pepSequence + "\t");
					out.write(pairItem.spectrumB.pepStart + "\t");
					out.write(pairItem.spectrumB.pepEnd + "\t");
					out.write(pairItem.spectrumB.queryID + "\t");
				}
				else{out.write("-\t "+"-\t"+"-\t"+"-\t" + pairItem.spectrumB.queryID+"\t");}

				out.write(pairItem.spectrumA.title + "\t");
				out.write(pairItem.spectrumB.title);
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			logger.error("Record:tsvExport:IOException: " + e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public static boolean DMBFileExists(String fileName){//returns true if fileName is stored in DMB false otherwise
		String sqlQuery="";
		Statement s=null;
		Connection conn =DeltaMassBase.getConnection();	
		try {
			PreparedStatement prep = conn.prepareStatement("SELECT count(*) FROM record where filename=?");
			prep.setString(1, fileName);
			ResultSet rs = prep.executeQuery();
			rs.next();
			if(rs.getInt(1)>0){
				logger.info("record for:"+fileName+" is in the database");
				return(true);
			}		
		} catch (SQLException e1) {
			logger.error("DMBFileExists:"+e1.toString());
			e1.printStackTrace();
		}

		try{
			conn.close();
		}
		catch(Exception e){
			logger.error("DMBFileExists:"+e.toString());
		}
		logger.info("record for:"+fileName+" is not in the database");
		return false;
	}

	public static ArrayList<String> DMBCheckListForExistence(ArrayList<String> datFiles){
		ArrayList<String> tmpList = new ArrayList<String>();
		String sqlQuery="";
		Statement s=null;
		Connection conn =DeltaMassBase.getConnection();	
		logger.debug("Record:DMBCheckListForExistence:checking files:"+datFiles.size());
		for(int i=0;i<datFiles.size();i++){
			String tmpString=datFiles.get(i);
			if(tmpString.endsWith(".dat")){
				String bareFile = tmpString.replaceFirst(".*/", "");
				tmpString="C:/detectorvision/deltaMasses/automation/mascot/data/"+bareFile+".xml";
			}

			sqlQuery="SELECT count(*) FROM record WHERE filename='" + tmpString + "';";
			logger.debug("Checking if filename exists:"+sqlQuery);
			try {
				s =conn.createStatement();
				s.execute(sqlQuery);
				ResultSet rs = s.getResultSet();
				rs.next ();
				if(rs.getInt(1)>0){
					logger.debug("record for:"+tmpString+" is in the database");					
				}
				else{//not in DB - put it into the return list
					tmpList.add(datFiles.get(i));
				}
			} catch (SQLException e) {
				logger.error("Record:DMBCheckListForExistence:SQLException: " +
						e.getLocalizedMessage());
			}

		}
		datFiles=null;
		return tmpList;
	}


	public String loadRecordFromDeltaMassBase(int record_id, ArrayList modList){
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("Record:loadRecordFromDeltaMassBase:"+record_id+":");
		Connection conn =DeltaMassBase.getConnection();
		if(conn==null){
			logger.error("Record:loadRecordFromDeltaMassBase:Connection==null:record not loaded");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=?");
			pst.setInt(1, record_id);
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no spectra in database associated with record_id="+record_id;
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns


			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where fk_record_id=?");
			pst.setInt(1, record_id);
			result=pst.executeQuery();

			while(result.next())
			{
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();

				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));

				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}


				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;

				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepmod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();

				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod="";//TODO where the hell is the mod string ?
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
					currentSpectrum.pepMod=result2.getString(9);
				}
				spectrumList.add(currentSpectrum);	
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass");

			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);
					tmpPair.specnet_id=result.getInt(8);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}

					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();
			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		returnString="OK";
		return returnString;
	}

	public String loadPeptideNetFromDeltaMassBase(int record_id, ArrayList modList){
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("Record:loadPeptideNetFromDeltaMassBase:"+record_id+":");
		Connection conn =DeltaMassBase.getConnection();	

		if(conn==null){
			logger.info("Record:loadPeptideNetFromDeltaMassBase:Connection==null:record not loaded");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=?");
			pst.setInt(1, 0);//TODO this doesnt make any sense if we call for a peptide net ...
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no spectra in database associated with record_id="+record_id;
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns


			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass where lfk_specnet_id=?)");
			pst.setInt(1, record_id);//TODO should be called pepnet ID
			result=pst.executeQuery();
			while(result.next())
			{
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}

			//now we do above block again, but for the light specs ....
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_l_spectrum_id from deltamass where lfk_specnet_id=?)");
			pst.setInt(1, record_id);//TODO should be called pepnet ID
			result=pst.executeQuery();
			while(result.next())
			{
				if(specListInteger.contains(result.getInt(8))){continue;}//no double loading
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass where lfk_specnet_id=?");
			pst.setInt(1, record_id);//TODO should read pepnet_id, see function call.

			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}

					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		returnString="OK";
		return returnString;
	}


	//20080522
	public String 	loadProteinFromDeltaMassBase(ArrayList modList, String get_protein_acc){

		//WARNING do not use
		logger.error("loadProteinFromDeltaMassBase should not be used yet !!!!");
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("Record:loadProteinFromDeltaMassBase:"+get_protein_acc+":");
		Connection conn = DeltaMassBase.getConnection();		

		if(conn==null){
			logger.error("Record:loadProteinFromDeltaMassBase:Connection==null:protein not loaded");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=0");
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=" ";
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=" ";
				this.tandemName=" ";
				this.searchReportHttp=" ";
			}
			else{
				returnString="no spectra in database associated with record_id="+0;
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns

			/*			pst=conn.prepareStatement("select distinct precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id " +
	                  "from   peptide,deltamass right outer join spectrum on  ( spectrum.spectrum_id=peptide.fk_spectrum_id)" +
	                  "where  proteinasc like ? " +
	                  "and    fk_spectrum_id=spectrum_id " +
	                  "and    (fk_h_spectrum_id=spectrum_id OR fk_l_spectrum_id=spectrum_id)");*/


			pst=conn.prepareStatement("select distinct precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id " +
					"from spectrum left outer join peptide on (spectrum_id=fk_spectrum_id),deltamass " +
			"where (deltamass.fk_l_spectrum_id=spectrum_id or deltamass.fk_h_spectrum_id=spectrum_id )");

			//pst.setString(1,get_protein_acc);

			logger.info("loadProteinFromDeltaMassBase:"+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				count++;
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}
			if(count==0){
				returnString="no proteins found with accession:"+get_protein_acc;
				return returnString;
			}

			//now we do above block again, but for the light specs ....
			//pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (   select spectrum_id from peptide, spectrum where proteinasc like ? and spectrum_id=fk_spectrum_id   )");
			//pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from peptide,spectrum,deltamass where proteinasc like ? and fk_spectrum_id=spectrum_id and fk_l_spectrum_id=spectrum_id");
			/* pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum");



			//pst.setString(1,get_protein_acc);
			deltaLog("loaddeltaMassFromDeltaMassBase:"+pst.toString());
	        result=pst.executeQuery();
	        while(result.next())
	        {
	        	if(specListInteger.contains(result.getInt(8))){continue;}//no double loading
	        	Spectrum currentSpectrum = new Spectrum();   
	        	ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
	        	ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
	        	ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
	        	Double minMz=Double.MAX_VALUE;
	        	Double maxMz=Double.MIN_VALUE;
	        	for(int j=0;j<tmpMzBack.size();j++){
	        		MSMS newMSMS= new MSMS();
	        		newMSMS.massToCharge = tmpMzBack.get(j);
	        		newMSMS.intensity    = tmpSignalBack.get(j);
	        		minMz=Math.min(minMz, tmpMzBack.get(j));
	        		maxMz=Math.max(maxMz, tmpMzBack.get(j));
	        		tmpMSMSList.add(newMSMS);
	        	}
	        	currentSpectrum.precursorMass =result.getDouble(1);
	        	currentSpectrum.queryID=result.getString(5);
	        	currentSpectrum.title=result.getString(3);
	        	currentSpectrum.charge=result.getInt(2);
	        	currentSpectrum.valueList=tmpMSMSList;
	        	currentSpectrum.retention=result.getFloat(4);
	        	currentSpectrum.spectrum_id=result.getInt(8);
	        	currentSpectrum.minMZ=minMz;
	        	currentSpectrum.maxMZ=maxMz;
	        	currentSpectrum.rangeMZ=maxMz-minMz;
	        	PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
	        	pst2.setInt(1, currentSpectrum.spectrum_id);
	        	ResultSet result2=pst2.executeQuery();
	        	while(result2.next())
	        	{
	        		currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
	        		currentSpectrum.proteinMatches=0;
	        		currentSpectrum.proteinScore=0;
	        		currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
	        	    currentSpectrum.pepMod=result.getString(9);//trac #57
	        		currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
	        		currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
	        		currentSpectrum.proteinAsc=result2.getString(3);
	        		currentSpectrum.pepSequence=result2.getString(4);
	        		currentSpectrum.pepStart=result2.getInt(7);
	        		currentSpectrum.pepEnd=result2.getInt(8);
	        		currentSpectrum.pepError=result2.getDouble(6);
	        		currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
	        	}
	        	spectrumList.add(currentSpectrum);	
	        }*/

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass " +
			"                  where fk_l_spectrum_id in( select spectrum_id from peptide, spectrum where proteinasc like ? and spectrum_id=fk_spectrum_id ) ");
			pst.setString(1,get_protein_acc);
			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());

			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}

					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		returnString="OK";
		return returnString;

	}

	public String loaddeltaMassFromDeltaMassBase(int record_id, ArrayList modList,double in_deltaMass,double in_MS_accuracy){
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("Record:loadDeltaMassFromDeltaMassBase:"+record_id+":");
		Connection conn =DeltaMassBase.getConnection();	

		if(conn==null){
			logger.error("Record:loadDeltaMassFromDeltaMassBase:Connection==null");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=0");
			//pst.setInt(1, 1);//TODO this doesnt make any sense if we call for a deltaMass
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no spectra in database associated with record_id="+record_id;
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns

			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass where dm > ? and dm < ? )");
			pst.setDouble(1,in_deltaMass-in_MS_accuracy);
			pst.setDouble(2,in_deltaMass+in_MS_accuracy);
			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				count++;
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}
			if(count==0){
				returnString="no pairs in database at deltaMass="+in_deltaMass;
				return returnString;
			}

			//now we do above block again, but for the light specs ....
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_l_spectrum_id from deltamass where dm > ? and dm < ? )");
			pst.setDouble(1,in_deltaMass-in_MS_accuracy);
			pst.setDouble(2,in_deltaMass+in_MS_accuracy);
			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());
			result=pst.executeQuery();
			while(result.next())
			{
				if(specListInteger.contains(result.getInt(8))){continue;}//no double loading
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass where dm > ? and dm < ?");
			pst.setDouble(1,in_deltaMass-in_MS_accuracy);
			pst.setDouble(2,in_deltaMass+in_MS_accuracy);
			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());


			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}

					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		returnString="OK";
		return returnString;
	}



	public String loadMarkedPairsFromDeltaMassBase(ArrayList modList){
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("loading marked pairs from DB");
		Connection conn =DeltaMassBase.getConnection();	

		if(conn==null){
			logger.error("Record:Connection==null");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=0");
			//pst.setInt(1, 1);//TODO this doesnt make any sense if we call for a deltaMass
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no record in database";
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns

			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass where marked = true )");
			logger.info(""+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				count++;
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}
			if(count==0){
				returnString="nothing found in database";
				return returnString;
			}

			//now we do above block again, but for the light specs ....
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_l_spectrum_id from deltamass where marked = true )");
			logger.info("sql:"+pst.toString());
			result=pst.executeQuery();
			while(result.next())
			{
				if(specListInteger.contains(result.getInt(8))){continue;}//no double loading
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass where marked = true");		
			logger.info("sql:"+pst.toString());

			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}
					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		returnString="OK";
		return returnString;
	}

	public String loadIdentifiedPairsFromDeltaMassBase(
			ArrayList<Modification> modList, String ProteinASC) {
		boolean loadAll=true;
		logger.info("loadIdentifiedPairsFromDeltaMassBase:start");

		if(ProteinASC.equals("all")){
			loadAll=true;
		}
		else{
			loadAll=false;
			
		}
		logger.info("loadIdentifiedPairsFromDeltaMassBase:loadAll:"+loadAll);
		
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		Connection conn =DeltaMassBase.getConnection();	
		ArrayList<Integer> IDsOfIdentifiedSpectra = new ArrayList<Integer>();
		ArrayList<Integer> IDsOfSpectraToLoad = new ArrayList<Integer>();
		ArrayList<Integer> IDsOfPairsToLoad = new ArrayList<Integer>();

		if(conn==null){
			logger.error("Record:Connection==null");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			//get id's of all IDs spectra: select distinct fk_spectrum_id from peptide
			if(loadAll){
				pst = conn.prepareStatement("Select distinct fk_spectrum_id from peptide");
			}
			else{
				pst = conn.prepareStatement("Select distinct fk_spectrum_id from peptide where proteinasc like '%"+ProteinASC.trim()+"%'");
			}
			logger.info("loadIdentifiedPairsFromDeltaMassBase:SQL:"+pst.toString());
			
			ResultSet result = pst.executeQuery();
			while(result.next()){
				int l_specId=result.getInt(1);
				IDsOfIdentifiedSpectra.add(l_specId);
			}
			logger.info("loadIdentifiedPairsFromDeltaMassBase:number of spectra with ID:"+IDsOfIdentifiedSpectra.size());
			if(IDsOfIdentifiedSpectra.size()==0){
				returnString="no identified pairs in database";
				return returnString;
			}

			//get list of all pairs containing an identified spectrum.
			pst = conn.prepareStatement("Select fk_l_spectrum_id, fk_h_spectrum_id,deltamass_id from deltamass");
			result = pst.executeQuery();
			while(result.next()){
				int l_light=result.getInt(1);
				int l_heavy=result.getInt(2);
				int l_deltamass_id=result.getInt(3);
				if(IDsOfIdentifiedSpectra.contains(l_light) || IDsOfIdentifiedSpectra.contains(l_heavy)) { //something is identified ...
					IDsOfPairsToLoad.add(l_deltamass_id);
					if(!IDsOfSpectraToLoad.contains(l_light)){
						IDsOfSpectraToLoad.add(l_light);
					}
					if(!IDsOfSpectraToLoad.contains(l_heavy)){
						IDsOfSpectraToLoad.add(l_heavy);
					}						
				}
			}
			logger.info("loadIdentifiedPairsFromDeltaMassBase:number of spectra to load:"+IDsOfSpectraToLoad.size());

			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record");
			result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no record in database";
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum");

			logger.info("loadIdentifiedPairsFromDeltaMassBase:SQL:"+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				int l_spectrumid=result.getInt(8);
				if(IDsOfSpectraToLoad.contains(l_spectrumid)){

					count++;
					Spectrum currentSpectrum = new Spectrum();   
					ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
					ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
					ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
					Double minMz=Double.MAX_VALUE;
					Double maxMz=Double.MIN_VALUE;
					for(int j=0;j<tmpMzBack.size();j++){
						MSMS newMSMS= new MSMS();
						newMSMS.massToCharge = tmpMzBack.get(j);
						newMSMS.intensity    = tmpSignalBack.get(j);
						minMz=Math.min(minMz, tmpMzBack.get(j));
						maxMz=Math.max(maxMz, tmpMzBack.get(j));
						tmpMSMSList.add(newMSMS);
					}
					currentSpectrum.precursorMass =result.getDouble(1);
					currentSpectrum.queryID=result.getString(5);
					currentSpectrum.title=result.getString(3);
					currentSpectrum.charge=result.getInt(2);
					currentSpectrum.valueList=tmpMSMSList;
					currentSpectrum.retention=result.getFloat(4);
					currentSpectrum.spectrum_id=result.getInt(8);
					specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
					currentSpectrum.minMZ=minMz;
					currentSpectrum.maxMZ=maxMz;
					currentSpectrum.rangeMZ=maxMz-minMz;
					PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
					pst2.setInt(1, currentSpectrum.spectrum_id);
					logger.info("SQL:"+pst2.toString());
					ResultSet result2=pst2.executeQuery();
					while(result2.next())
					{
						currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
						currentSpectrum.proteinMatches=0;
						currentSpectrum.proteinScore=0;
						currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
						currentSpectrum.pepMod=result2.getString(9);//trac #57
						currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
						currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
						currentSpectrum.proteinAsc=result2.getString(3);
						currentSpectrum.pepSequence=result2.getString(4);
						currentSpectrum.pepStart=result2.getInt(7);
						currentSpectrum.pepEnd=result2.getInt(8);
						currentSpectrum.pepError=result2.getDouble(6);
						currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
					}
					spectrumList.add(currentSpectrum);	
				}
			}
			if(count==0){
				returnString="nothing found in database";
				return returnString;
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass");		
			logger.info("loadIdentifiedPairsFromDeltaMassBase:sql:"+pst.toString());
			result=pst.executeQuery();
			while(result.next())
			{
				int l_deltamass_id=result.getInt(1);
				if(IDsOfPairsToLoad.contains(l_deltamass_id)){
					int lightSpec=result.getInt(5);
					int heavySpec=result.getInt(6);
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}
					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					this.pairList.add(tmpPair);
				}
			}
			conn.close();
		} catch (SQLException e) {
			logger.error("loadIdentifiedPairsFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("loadIdentifiedPairsFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		System.out.println("identified pairs loaded:"+this.spectrumList.size());
		logger.info("loadIdentifiedPairsFromDeltaMassBase:returning:loaded:"+this.spectrumList.size());
		returnString="OK";
		return returnString;
	}

	public String loadAllPairsFromDeltaMassBase(ArrayList modList){
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("loading all pairs from DB");
		Connection conn =DeltaMassBase.getConnection();	

		if(conn==null){
			logger.error("Record:Connection==null");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			//pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=0");
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record");
			//pst.setInt(1, 1);//TODO this doesnt make any sense if we call for a deltaMass
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no record in database";
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns

			//pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass where marked = true )");
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass)");

			logger.info(""+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				count++;
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}
			if(count==0){
				returnString="nothing found in database";
				return returnString;
			}

			//now we do above block again, but for the light specs ....
			//pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_l_spectrum_id from deltamass where marked = true )");
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_l_spectrum_id from deltamass )");
			logger.info("sql:"+pst.toString());
			result=pst.executeQuery();
			while(result.next())
			{
				if(specListInteger.contains(result.getInt(8))){continue;}//no double loading
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass");		
			logger.info("sql:"+pst.toString());

			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}
					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		System.out.println("load all spectra loaded:"+this.spectrumList.size());
		logger.info("load all spectra loaded:"+this.spectrumList.size());
		returnString="OK";
		return returnString;
	}	


	public String loadAllFromDeltaMassBase(ArrayList modList){
		String returnString="NOT_OK";
		
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("loading all pairs from DB");
		Connection conn =DeltaMassBase.getConnection();	

		if(conn==null){
			logger.error("Record:Connection==null");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			//pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=0");
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record");
			//pst.setInt(1, 1);//TODO this doesnt make any sense if we call for a deltaMass
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no record in database";
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns

			//pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass where marked = true )");
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum");

			logger.info(""+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				count++;
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}
			if(count==0){
				returnString="nothing found in database";
				return returnString;
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass");		
			logger.info("sql:"+pst.toString());

			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}
					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		System.out.println("load all spectra loaded:"+this.spectrumList.size());
		logger.info("load all spectra for deltaCluster loaded:"+this.spectrumList.size());
		returnString="OK";
		this.load_type=load_type.LOADED_FOR_DELTA_CLUSTER;
		return returnString;
	}	


	public String loadpairIDFromDeltaMassBase(int pair_id, ArrayList modList){
		String returnString="NOT_OK";
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Integer> specListInteger=new ArrayList<Integer>();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();	
		logger.info("pairID:loadpairIDFromDeltaMassBase:"+pair_id+":");
		Connection conn =DeltaMassBase.getConnection();	

		if(conn==null){
			logger.error("Record:loadDeltaMassFromDeltaMassBase:Connection==null");
			returnString="cant get db connection";
			return returnString;
		}

		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("Select filename,pepfilename,msprecision,msmsprecision,hasretention,originmethod,fk_experiment_id,url from record where record_id=0");
			//pst.setInt(1, 1);//TODO this doesnt make any sense if we call for a deltaMass
			ResultSet result = pst.executeQuery();
			if(result.next()){
				this.hasRetention=result.getBoolean(5);//TODO correct stuff below
				this.fileName=result.getString(1);
				this.msmsBetterPrecisionThanMs=false;
				this.msmsPrecision=result.getFloat(4);
				this.msPrecision=result.getFloat(3);
				this.originMethod=result.getString(6);
				this.tandemName=result.getString(2);
				this.searchReportHttp=result.getString(8);
			}
			else{
				returnString="no spectra in database associated with pair id="+pair_id;
				return returnString;
			}
			this.pairList = new ArrayList<SpectraPair>();//not done earlier in case above test returns

			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_h_spectrum_id from deltamass where deltamass_id = ? )");
			pst.setInt(1,pair_id);
			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());
			result=pst.executeQuery();
			int count=0;
			while(result.next())
			{
				count++;
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				specListInteger.add(currentSpectrum.spectrum_id);//we need this in the block below to prevent double loading
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}
			if(count==0){
				returnString="no pair in database with id:="+pair_id;
				return returnString;
			}

			//now we do above block again, but for the light specs ....
			pst=conn.prepareStatement("select precursormass, charge, title, retention,queryid,mzbase64,signalbase64,spectrum_id from spectrum where spectrum_id in (select fk_l_spectrum_id from deltamass where deltamass_id = ? )");
			pst.setInt(1,pair_id);
			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());
			result=pst.executeQuery();
			while(result.next())
			{
				if(specListInteger.contains(result.getInt(8))){continue;}//no double loading
				Spectrum currentSpectrum = new Spectrum();   
				ArrayList<MSMS> tmpMSMSList = new ArrayList<MSMS>();
				ArrayList<Double>tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(6));
				ArrayList<Double>tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(result.getString(7));
				Double minMz=Double.MAX_VALUE;
				Double maxMz=Double.MIN_VALUE;
				for(int j=0;j<tmpMzBack.size();j++){
					MSMS newMSMS= new MSMS();
					newMSMS.massToCharge = tmpMzBack.get(j);
					newMSMS.intensity    = tmpSignalBack.get(j);
					minMz=Math.min(minMz, tmpMzBack.get(j));
					maxMz=Math.max(maxMz, tmpMzBack.get(j));
					tmpMSMSList.add(newMSMS);
				}
				currentSpectrum.precursorMass =result.getDouble(1);
				currentSpectrum.queryID=result.getString(5);
				currentSpectrum.title=result.getString(3);
				currentSpectrum.charge=result.getInt(2);
				currentSpectrum.valueList=tmpMSMSList;
				currentSpectrum.retention=result.getFloat(4);
				currentSpectrum.spectrum_id=result.getInt(8);
				currentSpectrum.minMZ=minMz;
				currentSpectrum.maxMZ=maxMz;
				currentSpectrum.rangeMZ=maxMz-minMz;
				PreparedStatement pst2=conn.prepareStatement("select peptide_id,pepmass,proteinasc,pepsequence,pepmz,peperror,pepstart,pepend,pepMod  from peptide where fk_spectrum_id=?");
				pst2.setInt(1, currentSpectrum.spectrum_id);
				ResultSet result2=pst2.executeQuery();
				while(result2.next())
				{
					currentSpectrum.proteinMass=result.getDouble(1);//redundant !!!!
					currentSpectrum.proteinMatches=0;
					currentSpectrum.proteinScore=0;
					currentSpectrum.proteinDesc=result2.getString(3);//TODO this is not the description ....
					currentSpectrum.pepMod=result2.getString(9);//trac #57
					currentSpectrum.pepScore=55.55;//TODO where the hell is the pepscore ?
					currentSpectrum.pepMass=result.getDouble(1);//TODO not okay, this is an experimental value.
					currentSpectrum.proteinAsc=result2.getString(3);
					currentSpectrum.pepSequence=result2.getString(4);
					currentSpectrum.pepStart=result2.getInt(7);
					currentSpectrum.pepEnd=result2.getInt(8);
					currentSpectrum.pepError=result2.getDouble(6);
					currentSpectrum.pepMz=result2.getDouble(5);//redundant !!!!   	
				}
				spectrumList.add(currentSpectrum);	
			}

			this.setPeptideIDList(pepList);
			this.setSpectrumList(spectrumList);	

			pst=conn.prepareStatement("select deltamass_id,dm,sim,p,fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,marked,comment,weakdelta from deltamass where deltamass_id = ?");
			pst.setInt(1,pair_id);

			logger.info("loaddeltaMassFromDeltaMassBase:"+pst.toString());


			result=pst.executeQuery();

			ArrayList<Integer> specIdlist = new ArrayList<Integer>();
			for(int i=0;i<this.spectrumList.size();i++){
				specIdlist.add(this.spectrumList.get(i).spectrum_id);	
			}
			while(result.next())
			{
				int lightSpec=result.getInt(5);
				int heavySpec=result.getInt(6);
				//System.out.println("light:"+lightSpec+" heavy:"+heavySpec);
				//TODO improve speed of following line.
				if(specIdlist.contains(lightSpec) && specIdlist.contains(heavySpec)){	
					SpectraPair tmpPair = new SpectraPair();
					tmpPair.deltaMass=result.getDouble(2);
					tmpPair.p=result.getDouble(4);
					tmpPair.score=result.getDouble(3);
					tmpPair.pair_id=result.getInt(1);
					tmpPair.specnet_id=result.getInt(8);
					tmpPair.marked=result.getBoolean(9);
					tmpPair.comment=result.getString(10);
					tmpPair.hasWeakDeltaSignal=result.getBoolean(11);

					//TODO speed-improve the two loops below
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==heavySpec){
							//System.out.println("got heavy spec:"+heavySpec);
							tmpPair.spectrumB=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}
					for(int i=0;i<this.spectrumList.size();i++){
						if(this.spectrumList.get(i).spectrum_id==lightSpec){
							//System.out.println("got light spec:"+lightSpec);
							tmpPair.spectrumA=(Spectrum)this.spectrumList.get(i);
							break;
						}
					}

					//search if we have a reasonable modification
					tmpPair.knownModification=null;
					double tmpDeltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					double tmpBestMatch=Double.MAX_VALUE;
					double tmpDist;
					for(int k=modList.size()-1;k>=0;k--){
						tmpDist=Math.abs(Math.abs(((Modification)modList.get(k)).monoisotopic) - tmpDeltaMass);
						if( tmpDist<this.msPrecision){
							if(tmpDist<=tmpBestMatch){
								tmpBestMatch=tmpDist;
								tmpPair.knownModification=(Modification)modList.get(k);
							}
						}
					}

					tmpPair.deltaMass=Math.abs(tmpPair.spectrumA.precursorMass-tmpPair.spectrumB.precursorMass);
					//System.out.println("deltaMass of pair:"+tmpPair.deltaMass);
					this.pairList.add(tmpPair);
				}
			}
			specIdlist.clear();

			conn.close();
		} catch (SQLException e) {
			logger.error("Record:loadRecordFromDeltaMassBase:SQLException:"+e.toString());
			returnString="SQL Exception for record";
			e.printStackTrace();
			return returnString;
		}
		catch (Exception e){
			logger.error("Record:loadRecordFromDeltaMassBase:Exception:"+e.toString());
			returnString="Exception for record";
			e.printStackTrace();
			return returnString;
		}
		returnString="OK";
		return returnString;
	}


	public static boolean DMBstoreRecord(Record record, ArrayList<SpectraPair> pairList, boolean isDiscoveryEdition){
		int record_id=0;
		int spectrum_id=0;
		int deltaMass_id=0;
		int peptide_id=0;
		String sqlQuery="";
		Statement s = null;
		boolean storeRecord=false;
		boolean deltaClusterStore=false;

		if (record.load_type==load_type_enum.LOADED_FOR_DELTA_CLUSTER){
			deltaClusterStore=true;
		}
		else{
			deltaClusterStore=false;
		}

		logger.info("Record:DMBstoreRecord:start " +
				record.getFileName());
		Connection conn =DeltaMassBase.getConnection();		
		if(conn==null){
			logger.warn("Record:DMBSstoreRecord:Connection==null:record not stored");
			return false;
		}
		try {
			s =conn.createStatement();

			storeRecord=false;
			if(!isDiscoveryEdition){
				//if more than 4 records and personal edition - do not store this record.
				sqlQuery="SELECT COUNT(*) from record;";
				s.execute(sqlQuery);
				ResultSet rs = s.getResultSet();
				rs.next ();
				if(rs.getInt(1)<11){//Not too many...
					storeRecord=true;
				}
				else{
					logger.info("Record:DMBstoreRecord:10 records in DeltaMassBase: you cannot store more than 10 with Personal Edition.");
					conn.close();
					return false;
				}
			}
			else{
				storeRecord=true;
			}

			sqlQuery="BEGIN;";
			s.executeUpdate(sqlQuery);


			ProteinList protList= new ProteinList();
			protList.setProteinList();


			PreparedStatement prep = conn.prepareStatement("SELECT count(*) FROM record WHERE filename=?");
			prep.setString(1, record.fileName);
			//sqlQuery="SELECT count(*) FROM record WHERE filename='" + record.fileName + "';";
			logger.info("Checking if filename exists:"+prep.toString());
			//s.execute(sqlQuery);
			prep.execute();
			ResultSet rs = prep.getResultSet();
			rs.next ();
			if(rs.getInt(1)>0){
				if(record.load_type!=load_type_enum.LOADED_FOR_DELTA_CLUSTER){
					logger.warn("Record:DMBstoreRecord:not storing record because it is allready in the database: "+record.getFileName());
					return(false);
				}
				else{
					logger.info("Storage continues - record name is present but we have a deltaCluster operation");	
				}
			}			

			int DMBspectrumId[] = new int[record.spectrumList.size()];
			String DMBspectrumTitle[] = new String[record.spectrumList.size()];

			if(!deltaClusterStore){

				long now = System.currentTimeMillis();
				Date d = new Date(now);
				String tmpDeltaDate=String.format("%tD", d) + " " + String.format("%tR", now);
				SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
				tmpDeltaDate=df.format( d );
				record_id=DeltaMassBase.getSequencer("recordSeq");

				PreparedStatement ps1=conn.prepareStatement("INSERT INTO record (record_id,pepfilename,filename,userName,deltaDate,msPrecision,msmsPrecision,originMethod,fk_experiment_id,num_spectra,num_pairs) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
				ps1.setInt(1,record_id);
				ps1.setString(2,record.fileName);
				ps1.setString(3,record.fileName);
				ps1.setString(4, System.getProperty("user.name"));

				Calendar currenttime=Calendar.getInstance();
				java.util.Date currentdate=currenttime.getTime();
				java.sql.Date sqlDate = new java.sql.Date(currentdate.getTime());
				ps1.setDate(5, sqlDate);
				ps1.setDouble(6, record.getMsPrecision());
				ps1.setDouble(7, record.getMsmsPrecision());
				ps1.setString(8, record.getOriginMethod());
				ps1.setInt(9,0);
				ps1.setInt(10,record.spectrumList.size());
				ps1.setInt(11,pairList.size());
				ps1.executeUpdate();

				//int DMBspectrumId[] = new int[record.spectrumList.size()];
				//String DMBspectrumTitle[] = new String[record.spectrumList.size()];
				String stringMz="";
				String stringSignal="";

				for(int j=0;j<record.spectrumList.size();j++){
					{
						ArrayList<Double> tmpMz=new ArrayList<Double>();
						ArrayList<Double> tmpSignal=new ArrayList<Double>();

						ArrayList<Double> tmpMzBack=new ArrayList<Double>();
						ArrayList<Double> tmpSignalBack=new ArrayList<Double>();


						for(int c=0;c<record.spectrumList.get(j).valueList.size();c++){
							tmpMz.add(    record.spectrumList.get(j).valueList.get(c).massToCharge);
							tmpSignal.add(record.spectrumList.get(j).valueList.get(c).intensity);	
						}
						stringMz         = Base64.encodeObject( tmpMz, Base64.DONT_BREAK_LINES);
						stringSignal     = Base64.encodeObject( tmpSignal, Base64.DONT_BREAK_LINES);

						//online encoding/decoding for testing purposes
						/*tmpMzBack= (ArrayList<Double>)Base64.decodeToObject(stringMz);
					tmpSignalBack= (ArrayList<Double>)Base64.decodeToObject(stringSignal);
					if(! tmpMzBack.equals(tmpMz)){
						System.out.println("error - encoding decoding mz not the same");
						deltaLog("mz encoding-decoding error:j="+j);
						System.exit(1);
					}
					if(! tmpSignalBack.equals(tmpSignal)){
						System.out.println("error - encoding decoding signal not the same");
						deltaLog("signal encoding-decoding error:j="+j);
						System.exit(1);
					}
					tmpSignalBack.clear();
					tmpMzBack.clear();*/

						tmpMz.clear();
						tmpSignal.clear();
					}

					DMBspectrumId[j]=DeltaMassBase.getSequencer("spectrumSeq");
					DMBspectrumTitle[j]=record.spectrumList.get(j).title;
					//store the whole spectrum as a serialized object
					PreparedStatement ps2=conn.prepareStatement("INSERT INTO spectrum (" +
							"spectrum_id," +
							"fk_record_id," +
							"precursorMass," +
							"charge," +
							"title," +
							"queryID," +
							"mzBase64," +
							"signalBase64)" + 
					" VALUES (?,?,?,?,?,?,?,?)");
					ps2.setInt(1, DMBspectrumId[j]);
					ps2.setInt(2, record_id);
					ps2.setDouble(3, record.spectrumList.get(j).precursorMass);
					ps2.setInt(4, record.spectrumList.get(j).charge);
					ps2.setString(5, record.spectrumList.get(j).title);
					String tmpQuery="-";
					if (record.spectrumList.get(j).queryID==null) {
						// if we dont do this exercise, the not-null constraint in
						// the db freaks out.
						tmpQuery="-";
					}
					else {
						tmpQuery=record.spectrumList.get(j).queryID;
					}
					ps2.setString(6, tmpQuery);
					ps2.setString(7, stringMz);
					ps2.setString(8, stringSignal);
					ps2.executeUpdate();

					//if this spectrum has a peptide id: store it!
					if(record.spectrumList.get(j).pepSequence != null && record.spectrumList.get(j).pepSequence.length()>0){
						peptide_id=DeltaMassBase.getSequencer("peptideSeq");
						String tmpProteinAsc=record.spectrumList.get(j).proteinAsc;
						if(tmpProteinAsc.length()>35){
							tmpProteinAsc=tmpProteinAsc.substring(0,35);
							tmpProteinAsc+="...";
						}

						PreparedStatement ps=conn.prepareStatement("INSERT INTO peptide (peptide_id,fk_spectrum_id,pepMass,pepSequence,proteinAsc,pepError,pepStart,pepEnd,pepMod) VALUES (?,?,?,?,?,?,?,?,?)");


						Protein prot = new Protein();
						prot.acc=tmpProteinAsc;
						prot.description=record.spectrumList.get(j).proteinDesc;
						prot.http="http://www.google.ch/search?hl=en&q="+tmpProteinAsc;//todo put something useful in here
						prot.mass=record.spectrumList.get(j).proteinMass;
						protList.addProtein(prot);

						ps.setInt(1, peptide_id);
						ps.setInt(2, DMBspectrumId[j]);
						ps.setDouble(3,record.spectrumList.get(j).pepMass);
						ps.setString(4, record.spectrumList.get(j).pepSequence);
						ps.setString(5, tmpProteinAsc);
						ps.setDouble(6, record.spectrumList.get(j).pepError);
						ps.setInt(7, record.spectrumList.get(j).pepStart);
						ps.setInt(8, record.spectrumList.get(j).pepEnd);
						ps.setString(9, record.spectrumList.get(j).pepMod);
						ps.executeUpdate();
					}
				}
				protList.storeProteinList2DB();
			}

			int l_spec_id=-1;int h_spec_id=-1;//-1 to cause an errror if wrong in sql below ....
			logger.info("Record:DMBStore pairList.size:"+pairList.size());



			if(deltaClusterStore){
				//delete all records vom deltaMass
				logger.info("deltaClusterStore active. Deleting table deltaMass.");
				PreparedStatement ps=conn.prepareStatement("delete from deltaMass");
				ps.executeUpdate();
			}

			for(int k=0;k<pairList.size();k++){
				//TODO speed this up - idiot code
				double mass_light=0;


				if(!deltaClusterStore){
					for(int l=0;l<record.spectrumList.size();l++){
						if(DMBspectrumTitle[l].equals(pairList.get(k).spectrumA.title)){
							l_spec_id=DMBspectrumId[l];
							mass_light=pairList.get(k).spectrumA.precursorMass;
						}
						if(DMBspectrumTitle[l].equals(pairList.get(k).spectrumB.title)){
							h_spec_id=DMBspectrumId[l];
						}
					}
				}
				else{
					if(pairList.get(k).spectrumA.precursorMass<=pairList.get(k).spectrumB.precursorMass){
						l_spec_id=pairList.get(k).spectrumA.spectrum_id;
						h_spec_id=pairList.get(k).spectrumB.spectrum_id;
						mass_light=pairList.get(k).spectrumA.precursorMass;
						logger.info("A l_spec_id:"+l_spec_id+"/th_spec_id:"+h_spec_id);
					}
					else{
						l_spec_id=pairList.get(k).spectrumB.spectrum_id;
						h_spec_id=pairList.get(k).spectrumA.spectrum_id;
						mass_light=pairList.get(k).spectrumB.precursorMass;
						logger.info("B l_spec_id:"+l_spec_id+"/th_spec_id:"+h_spec_id);
					}	
				}



				//some pathologic security checks
				//double tmpDm=Math.abs(pairList.get(k).spectrumB.precursorMass-pairList.get(k).spectrumA.precursorMass) ;
				double tmpDm=pairList.get(k).spectrumB.precursorMass-pairList.get(k).spectrumA.precursorMass;
				if (tmpDm<0)
					//should never happen.
					logger.fatal("storing pairList:Error:negative deltamass:");
				if (mass_light==0)
					//should never happen.
					logger.fatal("storing pairList:Error:mass_light is 0");

				deltaMass_id=DeltaMassBase.getSequencer("deltaMassSeq");
				SpectraPair tmpPair= new SpectraPair();
				tmpPair=pairList.get(k);
				tmpPair.pair_id=deltaMass_id;
				pairList.set(k, tmpPair);
				//TODO turn statement below into a prepared statement
				sqlQuery="INSERT INTO deltaMass (deltaMass_id,fk_l_spectrum_id,fk_h_spectrum_id,dm,sim,p,lfk_specnet_id,mass_light,marked,weakdelta) " +
				"VALUES("+deltaMass_id+","+l_spec_id+","+h_spec_id+","+tmpDm+","+pairList.get(k).score+","+pairList.get(k).p+","+"0,"+mass_light+", false,"+pairList.get(k).hasWeakDeltaSignal +")";
				s.executeUpdate(sqlQuery);
			}

			//20080106 block below
			//20081228 block below replaced by setLastModificactionDate below.
			/*String INSERT_RECORD = "update meta_db set lastModificationDate = ? where meta_db_id=1";
			PreparedStatement pstmt = conn.prepareStatement(INSERT_RECORD);
			pstmt.setInt(1,1);
			java.sql.Timestamp  sqlDate2 = new java.sql.Timestamp(new java.util.Date().getTime());
			pstmt.setTimestamp(1, sqlDate2);
			pstmt.executeUpdate();*/

			sqlQuery="COMMIT;";
			s.executeUpdate(sqlQuery);
			conn.close();//20080106
			DeltaMassBase.setLastModificationDate();

			record.hasBeenStored=true;
			record.setRecordID(record_id);
			logger.debug("Record:DMBstoreRecord:COMMIT");
		} catch (SQLException e) {
			logger.error("Record:DMBstoreRecord:ROLLBACK:SQLException:error:" +
					e.getLocalizedMessage());
			sqlQuery="ROLLBACK;";
			try {
				s.executeUpdate(sqlQuery);
				s.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.fatal("Record:DMBStoreRecord:ROLLBACK:SQLException: " +
						e1.getLocalizedMessage());
				System.exit(1);
			}
			logger.error("Record:DMBstoreRecord:COMMIT PROBLEM:" +
					e.getLocalizedMessage());
			e.printStackTrace();
		}
		return true;
	}

	public GaussianFit calcPrecision(double mass, double windowWidth, int bins){
		int minimumSignals=1000;//Below this, returncode of Gaussian is set to false;
		GaussianFit GaussFit=null;
		boolean allOK=true;

		double minMass= mass - windowWidth/2.0;
		double maxMass= mass + windowWidth/2.0;
		double binWidth = windowWidth/bins;
		int[] countBin = new int[bins];//C-Style ....
		for(int i=0;i<bins;i++){
			countBin[i]=0;
		}

		int countPairs=0;

		double dm;
		int bin;

		for(int i=0;i<this.spectrumList.size();i++){
			for(int j=i+1;j<this.spectrumList.size();j++){
				dm=(spectrumList.get(i).precursorMass - spectrumList.get(j).precursorMass);
				if(dm>minMass && dm < maxMass && Math.abs(dm)>0.00001){
					bin=(int)((-minMass+dm)/(binWidth));
					if(bin==bins){bin=bins-1;}
					if(bin>=bins+1 || bin <0){
						logger.fatal("System error:wrong bin:"+bin);
						allOK=false;
					}
					countPairs++;
					countBin[bin]++;

					dm*=-1;//to make the distribution symmetric ....
					bin=(int)((-minMass+dm)/(binWidth));
					if(bin==bins){bin=bins-1;}
					if(bin>=bins+1 || bin <0){
						logger.fatal("System error:wrong bin:"+bin);
						allOK=false;
					}
					countPairs++;
					countBin[bin]++;

				}
			}
		}

		int sum=0;
		for(int i=0;i<bins;i++){
			if(countBin[i]>0){
				double d=(minMass+  ((double)((double)i+0.5)*(binWidth)));
				System.out.println("detail: "+String.format("%.8g",d)+"\t"+countBin[i]);
				sum+=countBin[i];
			}
		}
		if(sum<minimumSignals){allOK=false;}//statistics not good enough.
		//System.out.println("total count:"+sum);
		double exp=mass;
		double sigma=windowWidth;
		double maxCount=0;
		for(int i=0;i<bins;i++){
			if(countBin[i]>maxCount){maxCount=countBin[i];}
		}
		//System.out.println("maxCount:"+maxCount);

		//below, we do a quadratic error minimization fit of a gaussian to the data in countBin[].
		//four variables: 
		//exp     center of the Gaussian)
		//sigma   sigma .... of course ...)
		//base    a constant added to the signal
		//height  height of the gaussian
		//method works by continuously zooming into the 4-dimensional point of the minimum.

		double baseMin=0;
		double baseMax=maxCount/2;

		double sigmaMin=0.0001;
		double sigmaMax=0.1;

		double hMin=0;
		double hMax=maxCount*2.0;

		double expMin = mass - (windowWidth/2.0);
		double expMax = mass + (windowWidth/2.0);;

		double base=0;double bestExp=0;double bestSigma=0;double bestH=0;double bestBase=0;double bestErr=Double.MAX_VALUE;
		for(int loop=0;loop<=20;loop++){
			double expStep=(expMax-expMin)/10.0;
			double sigmaStep=(sigmaMax-sigmaMin)/10.0;
			double  baseStep=(baseMax-baseMin)/10.0;
			double  hStep=(hMax-hMin)/10.0;
			for(exp=expMin; exp<=expMax;exp+=expStep){
				for(sigma=sigmaMin;sigma<=sigmaMax;sigma+=sigmaStep){
					double twoSsq=-1.0/(sigma*sigma*2.0);
					for(base=baseMin;base<=baseMax;base+=baseStep){
						for(double h=hMin; h< hMax;h+=hStep){
							double err=0;double tmp=0;
							for(int i=0;i<bins;i++){double m=(minMass+  ((double)((double)i+0.5)*(binWidth)));tmp=countBin[i] - (base + h*Math.exp(twoSsq*((m-exp)*(m-exp))));tmp*=tmp;err+=tmp;}
							err /= bins;
							if(err<bestErr){bestErr=err;bestExp=exp;bestSigma=sigma;bestBase=base;bestH=h;
							}
						}
					}	
				}
			}
			expMin=bestExp - 2.5*expStep;expMax=bestExp+2.5*expStep;
			sigmaMin=bestSigma - 2.5*sigmaStep;sigmaMax=bestSigma+2.5*sigmaStep;
			baseMin=bestBase - 2.5*baseStep;baseMax=bestBase+2.5*baseStep;
			hMin=bestH - 2.5*hStep;hMax=bestH+2.5*hStep;
		}
		bestSigma=Math.abs(bestSigma);
		double y[]= new double[bins];
		double x[]= new double[bins];
		for(int i=0;i<bins;i++){
			x[i]=(minMass+  ((double)((double)i+0.5)*(binWidth)));
			y[i]= (double)countBin[i];
		}
		double numUnderCurve=bestH*bestSigma*Math.sqrt(2.0*Math.PI)/binWidth;
		GaussFit=new GaussianFit(bestExp,bestH,bestBase,bestSigma,x,y,
				numUnderCurve,bestErr);
		GaussFit.computationOK=allOK;
		if(GaussFit.getSigma()>windowWidth/3){GaussFit.computationOK=false;}
		GaussFit.print();
		return GaussFit;
	}
	public int getRecordId() {
		return recordID;
	}
	public void setRecordID(int arecordID){
		recordID=arecordID;
	}

}
