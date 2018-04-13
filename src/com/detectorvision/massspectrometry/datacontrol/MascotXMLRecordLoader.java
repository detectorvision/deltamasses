/* $Id: MascotXMLRecordLoader.java 363 2010-07-20 19:58:19Z frank $ */

package com.detectorvision.massspectrometry.datacontrol;

import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.PeptideID;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.Spectrum;
import com.detectorvision.massspectrometry.datacontrol.RecordLoader;
import com.detectorvision.massspectrometry.datacontrol.ValidateXML;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * This Loader is able to handle Mascot XML exports
 *  
 * @author frank.potthast@detectorvision.com
 * 
 * 
 */ 
public class MascotXMLRecordLoader implements RecordLoader {

	/**
	 * Loads the mass spectrometry measurement into the data structure.
	 * 
	 * @param fileName Filename of the peakfile, must be in xml format
	 * @return Returns a Record
	 * @throws IOException Throws an exception if the method is unable to load the data.
	 */

	private ProgressListener progressListener = null;
	/*private SAXBuilder 		builder;
	private Document 		doc;*/

	// Logging with log4j
	static Logger logger = Logger.getLogger(MascotXMLRecordLoader.class.getName());

	public Record loadRecord(String fileName, ProgressListener listener) throws IOException{
		Record tmpRecord = new Record();
		logger.info("MascotXMLRecordLoader active. Loading:" + fileName);
		tmpRecord=loadMascotRecord(fileName,progressListener);
		tmpRecord.setOriginMethod("mascot");
		return(tmpRecord);
	}
	public void setProgressListener(ProgressListener progress){
		this.progressListener = progress;
	}

	@SuppressWarnings("unchecked")
	public static Record  loadMascotRecord(String fileName, ProgressListener listener) throws IOException{
		int maxNumberOfSpectrums=200000;
		int maxPeaksPerSpectrum = 75;  
		Record tmpRecord = new Record();
		tmpRecord.setOriginMethod("mascot");
		ArrayList<PeptideID> pepList = new ArrayList();
		ArrayList<Spectrum> spectrumList = new ArrayList<Spectrum>();
		ArrayList<MSMS> tmpMSMSList = null;
       try{
		if(listener != null){
		listener.updateProgress(5, "checking validity of the mascot xml format - can take a minute ....");
		}
				
		if(!ValidateXML.validateMascot(fileName)){
			logger.info("MascotXMLRecordLoader:invalid XML for:"+fileName);
		}
		else{
			logger.info("MascotXMLRecordLoader:valid XML for:"+fileName);
			if(listener  != null)
				listener.updateProgress(8, ".mascot xml format is valid");
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
			return tmpRecord;
		}
		
		if(listener != null)
		listener.updateProgress(10, "reading  mascot xml format ");
		
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
	    	return null;
	    }
		logger.info("MascotXMLRecordLoader::namespace determination okay");
	    
		Element mascot_result = doc.getRootElement();
		if(mascot_result == null || !mascot_result.getName().equals("mascot_search_results")){
			logger.error("Incorrect Mascot xml file:"+fileName);
			throw new IOException("Incorrect mascot xml file:" + fileName);	
		}

		Element mascot_header = mascot_result.getChild("header",  Namespace.getNamespace(mascotNameSpace));
		Element mascot_uri = mascot_header.getChild("URI",  Namespace.getNamespace(mascotNameSpace));
		tmpRecord.setSearchReportHttp(mascot_uri.getText());
		logger.info("searchReportHttp:"+tmpRecord.getSearchReportHttp());
		
		Element mascot_queries = mascot_result.getChild("queries",  Namespace.getNamespace(mascotNameSpace));

		if(listener != null)
		listener.updateProgress(20, "got mascot queries ");
		
		if(mascot_queries == null){
			logger.error("mascot_query_error");
			throw new IOException("mascot_query error");
		}
		List list_query = mascot_queries.getChildren("query", Namespace.getNamespace(mascotNameSpace));
		
		if(list_query.size()>=maxNumberOfSpectrums){
			if(listener != null){
				listener.updateProgress(5, "too many spectra in input file");
				}
			logger.error("too many spectra.");
			return tmpRecord;
		}

		if(list_query == null || list_query.size() == 0){
			logger.error("no querries available");
			throw new IOException("No queries available!");
		}
		
		/////////////////////////////////////////////////////////start 
		double precMass_tmp[] = new double[maxNumberOfSpectrums];
		int    precCharge_tmp[] = new int[maxNumberOfSpectrums];
		String pepSeq_tmp[] = new String[maxNumberOfSpectrums];
		String proteinAcc_tmp[] = new String[maxNumberOfSpectrums];
		String prot_desc_tmp[]= new String[maxNumberOfSpectrums];
		double prot_score_tmp[] = new double [maxNumberOfSpectrums];
		double prot_mass_tmp[] = new double[maxNumberOfSpectrums];
		int prot_matches_tmp[]=new int [maxNumberOfSpectrums];
		double pep_expect_tmp[]= new double[maxNumberOfSpectrums];
		
		int    pepStart_tmp[] = new int[maxNumberOfSpectrums];
		int    pepEnd_tmp[] = new int[maxNumberOfSpectrums];
		
		double pep_delta_tmp[] = new double[maxNumberOfSpectrums];
		double pep_exp_mz_tmp[] = new double[maxNumberOfSpectrums];
		
		String pepMod_tmp[]= new String[maxNumberOfSpectrums];
		
		for(int i=0;i<maxNumberOfSpectrums;i++){//TODO this is unnecessary, is it?
			precMass_tmp[i]=0;
			precCharge_tmp[i]=0;
		}
		
		Element mascot_hits = mascot_result.getChild("hits",  Namespace.getNamespace(mascotNameSpace));
		if(mascot_queries == null){//TODO  should this be mascot_hits ??????
			logger.error("mascot_hits error");
			throw new IOException("mascot_hits error");
		}
		List list_hits = mascot_hits.getChildren("hit", Namespace.getNamespace(mascotNameSpace));
		if(list_hits != null){
			logger.debug("MascotXMLRecordLoader::got Hits");
			for(int i=0; i<list_hits.size(); i++){
				int process = (int) (100*(double)i/(double)list_hits.size());
				if(listener != null)
				listener.updateProgress(process, "processing hit "+i);
				
				Element this_hit = (Element) list_hits.get(i);
				Element this_protein = this_hit.getChild("protein", Namespace.getNamespace(mascotNameSpace));
				List list_peptide = this_protein.getChildren("peptide", Namespace.getNamespace(mascotNameSpace));
				if(list_peptide == null || list_peptide.size() == 0)
					throw new IOException("No peptides available!");
				for(int pep=0; pep<list_peptide.size(); pep++){
					//<peptide query="3">
					Element this_pep_query = (Element) list_peptide.get(pep);
					int query_index=Integer.parseInt(this_pep_query.getAttributeValue("query"));
					//System.out.println("a query:" + query_index);

					Element pep_exp_mz= this_pep_query.getChild("pep_exp_mz", Namespace.getNamespace(mascotNameSpace));	
					//System.out.println("and the pep_exp_mz is:"+pep_exp_mz.getText());
					pep_exp_mz_tmp[query_index]=Double.parseDouble(pep_exp_mz.getText());

					Element pep_exp_mr= this_pep_query.getChild("pep_exp_mr", Namespace.getNamespace(mascotNameSpace));	
					//System.out.println("and the pep_exp_mr is:"+pep_exp_mr.getText());
					precMass_tmp[query_index]=Double.parseDouble(pep_exp_mr.getText());

					Element pep_exp_z= this_pep_query.getChild("pep_exp_z", Namespace.getNamespace(mascotNameSpace));	
					//System.out.println("charge:"+this_pep_query.getChildText("pep_exp_z"));	
					precCharge_tmp[query_index]=Integer.parseInt(pep_exp_z.getText());

					Element pep_delta= this_pep_query.getChild("pep_delta", Namespace.getNamespace(mascotNameSpace));	
					//System.out.println("and pep_delta is:"+pep_delta.getText());
					double tmpErr=Double.parseDouble(pep_delta.getText());
					tmpErr /= precCharge_tmp[query_index];
					pep_delta_tmp[query_index]=tmpErr;

					Element pep_seq= this_pep_query.getChild("pep_seq", Namespace.getNamespace(mascotNameSpace));	
					//System.out.println("pep_seq:"+this_pep_query.getChildText("pep_seq"));	
					pepSeq_tmp[query_index]=pep_seq.getText();
					proteinAcc_tmp[query_index]=this_protein.getAttributeValue("accession");

					Element mod_seq= this_pep_query.getChild("pep_var_mod", Namespace.getNamespace(mascotNameSpace));	
					//System.out.println("pep_seq:"+this_pep_query.getChildText("pep_seq"));	
					if(mod_seq.getText()!=null){
					pepMod_tmp[query_index]=mod_seq.getText();
					}
					
					Element this_prot_desc = this_protein.getChild("prot_desc", Namespace.getNamespace(mascotNameSpace));
					//System.out.println(this_protein.getAttributeValue("accession"));
					prot_desc_tmp[query_index]=this_prot_desc.getText();
					
					Element this_prot_score = this_protein.getChild("prot_score", Namespace.getNamespace(mascotNameSpace));
					//System.out.println(this_protein.getAttributeValue("accession"));
					prot_score_tmp[query_index]=Double.parseDouble(this_prot_score.getText());
					
					Element this_prot_mass = this_protein.getChild("prot_mass", Namespace.getNamespace(mascotNameSpace));
					//System.out.println(this_protein.getAttributeValue("accession"));
					prot_mass_tmp[query_index]=Double.parseDouble(this_prot_mass.getText());
										
					Element this_prot_matches = this_protein.getChild("prot_matches", Namespace.getNamespace(mascotNameSpace));
					//System.out.println(this_protein.getAttributeValue("accession"));
					prot_matches_tmp[query_index]=Integer.parseInt(this_prot_matches.getText());
					
					Element pepStart= this_pep_query.getChild("pep_start", Namespace.getNamespace(mascotNameSpace));	
					pepStart_tmp[query_index]=Integer.parseInt(pepStart.getText());

					Element pepEnd= this_pep_query.getChild("pep_end", Namespace.getNamespace(mascotNameSpace));		
					pepEnd_tmp[query_index]=Integer.parseInt(pepEnd.getText());
					
					Element pepExpect= this_pep_query.getChild("pep_expect", Namespace.getNamespace(mascotNameSpace));		
					pep_expect_tmp[query_index]=Double.parseDouble(pepExpect.getText());
				} 
			}
		}
		
		//unassigned queries/////////////////////////////////////////////////////////
		Element mascot_unassigned=mascot_result.getChild("unassigned",  Namespace.getNamespace(mascotNameSpace));

		if(listener != null)
		listener.updateProgress(100, "processing unassigned queries");
		
		if(mascot_unassigned == null)
		{
			logger.warn("unassinged hits issue - you might want to check if unassigned querries have been exported.");
			//throw new IOException("unassigned_hits error");
		}
		else{
			List list_u_peptide = mascot_unassigned.getChildren("u_peptide", Namespace.getNamespace(mascotNameSpace));
			if(list_u_peptide == null || list_u_peptide.size() == 0)
			{
				logger.error("No u_peptide available");
				throw new IOException("No u_peptide available");
			}
			for(int i=0; i<list_u_peptide.size(); i++){
				Element this_u_peptide = (Element) list_u_peptide.get(i);
				int tmpIndex = Integer.parseInt(this_u_peptide.getAttributeValue("query"));
				//System.out.println("\n-------------------\nunassigned query number:" + tmpIndex);
				Element this_u_pep_exp_mz = this_u_peptide.getChild("pep_exp_mz", Namespace.getNamespace(mascotNameSpace));
				//System.out.println("u_pep_exp_mz:" + this_u_pep_exp_mz.getText());
				pep_exp_mz_tmp[tmpIndex]=Double.parseDouble(this_u_pep_exp_mz.getText());

				//<pep_exp_mr>1421.5654</pep_exp_mr>
				Element this_u_pep_exp_mr = this_u_peptide.getChild("pep_exp_mr", Namespace.getNamespace(mascotNameSpace));
				//System.out.println("u_pep_exp_mr:" + this_u_pep_exp_mr.getText());
				//<pep_exp_z>2</pep_exp_z>
				Element this_u_charge = this_u_peptide.getChild("pep_exp_z",Namespace.getNamespace(mascotNameSpace));
				//System.out.println("charge:" + this_u_charge.getText());
				precMass_tmp[tmpIndex]=Double.parseDouble(this_u_pep_exp_mr.getText());
				precCharge_tmp[tmpIndex]=Integer.parseInt(this_u_charge.getText());
				//System.out.println("charge issy:"+this_u_charge.getText());
			}
		}
		/////////////////////////////////////////////////////////the end
		logger.debug("MascotXMLRecordLoader::starting list_query_loop");
		for(int i=0; i<list_query.size(); i++){
			Spectrum tmpSpectrum= new Spectrum();

			Element this_query = (Element) list_query.get(i);
			tmpSpectrum.queryID= this_query.getAttributeValue("number");//new 20070115
			//System.out.println("\n-------------------\nquery_number:" + this_query.getAttributeValue("number"));
			Element thisTitle = this_query.getChild("StringTitle",  Namespace.getNamespace(mascotNameSpace));
			if(thisTitle==null)
			{
				tmpSpectrum.title="query:"+tmpSpectrum.queryID;//Holger Kramer Defekt holger.kramer@linacre.ox.ac.uk 20080611
			}
			else{
				if(thisTitle.getText().length()==0){tmpSpectrum.title="query:"+tmpSpectrum.queryID;}
				else{
					tmpSpectrum.title=thisTitle.getText();
				}
			}
	

			Element thisNumVals = this_query.getChild("NumVals",  Namespace.getNamespace(mascotNameSpace));
			//System.out.println("NumVals:" + thisNumVals.getText());

			String[] mzPairs=null;
			Element thisStringIons1=null;
			try{
			thisStringIons1 = this_query.getChild("StringIons1",  Namespace.getNamespace(mascotNameSpace));
			mzPairs=thisStringIons1.getText().split(",");	
			}catch(Exception e){
				if(listener != null){
					listener.updateProgress(100, "Mascot: no MSMS info found (no StringIons1). Please check your mascot.xml file export");
					}
				logger.error("load error:no MSMS info found (no StringIonds1).");
				return tmpRecord;
			}
			
			tmpMSMSList = new ArrayList<MSMS>();
			for(int j=0;j<mzPairs.length;j++){
				MSMS newMSMS = new MSMS();
				String[] tmpMz=mzPairs[j].split(":");
				if(tmpMz.length!=2){
					logger.debug("MascotXMLRecordLoader:msms signal parse error:" +
											 tmpMz + ":" + tmpMz.length + ":" +
											 thisStringIons1.getText() + " tmpquerid:" +
											 tmpSpectrum.queryID + " mzpairs[j]:" + mzPairs[j]);
					break;
				}
				//System.out.println("msms:"+tmpMz[0] + " " + tmpMz[1]);
				newMSMS.intensity = Double.parseDouble(tmpMz[1]);
				newMSMS.massToCharge= Double.parseDouble(tmpMz[0]);
				tmpMSMSList.add(newMSMS);
			}
			tmpSpectrum.queryID=this_query.getAttributeValue("number");
			int qn=Integer.parseInt(this_query.getAttributeValue("number"));//to avoid long names
			/////////////////////////////////////////////////get the precursor mass and the charge

			tmpSpectrum.precursorMass=precMass_tmp[qn];
			tmpSpectrum.pepMz=pep_exp_mz_tmp[qn];

			tmpSpectrum.charge=precCharge_tmp[qn];
			if(pepSeq_tmp[qn] != null && proteinAcc_tmp[qn] != null){
				tmpSpectrum.pepSequence=pepSeq_tmp[qn];

				
				
				String tmp=prot_desc_tmp[qn];
				if(tmp.length()>75){
					tmp=tmp.substring(0,75);
					tmp+="...";
				}
				tmpSpectrum.proteinDesc=tmp;
				tmpSpectrum.proteinScore=prot_score_tmp[qn];
				tmpSpectrum.proteinMass=prot_mass_tmp[qn];
				tmpSpectrum.proteinMatches=prot_matches_tmp[qn];
				tmpSpectrum.pepScore=pep_expect_tmp[qn];
				
				tmp=proteinAcc_tmp[qn];
				if(tmp.length()>35){
					tmp=tmp.substring(0,35);
					tmp+="...";
				}
				tmpSpectrum.proteinAsc=tmp; 
				
				tmpSpectrum.pepMass=precMass_tmp[qn];//frankp 20070321
				tmpSpectrum.pepError=pep_delta_tmp[qn];
				tmpSpectrum.pepStart=pepStart_tmp[qn];
				tmpSpectrum.pepEnd=pepEnd_tmp[qn]; 
				tmpSpectrum.pepMod=pepMod_tmp[qn];//frankp 20070321
			}

			ArrayList<MSMS> valueList = new ArrayList<MSMS>();

			Collections.sort(tmpMSMSList);
			Collections.reverse(tmpMSMSList);

			//total ion current (tic) normalization
			double tic=0;
			for(int n = 0; n < tmpMSMSList.size(); n++){
				 tic+=tmpMSMSList.get(n).intensity;
				}
			for(int n = 0; n < tmpMSMSList.size(); n++){
				 tmpMSMSList.get(n).intensity/=tic;
				 }
			tmpSpectrum.tic=tic;
			double minMZ=Double.MAX_VALUE;double maxMZ=Double.MIN_VALUE;		
			for(int n = 0; n < maxPeaksPerSpectrum; n++){if(n < tmpMSMSList.size()){
				valueList.add(tmpMSMSList.get(n));
				minMZ = Math.min(minMZ, tmpMSMSList.get(n).massToCharge);
				maxMZ = Math.max(maxMZ, tmpMSMSList.get(n).massToCharge);
			}}

			maxMZ = Math.max(0, maxMZ); //<0 protection
			minMZ = Math.max(0, minMZ); //<0 protection
			tmpSpectrum.minMZ=minMZ;
			tmpSpectrum.maxMZ=maxMZ;
			tmpSpectrum.rangeMZ = Math.max(0,maxMZ - minMZ);
			tmpSpectrum.valueList = valueList;
			if(valueList.size()>=10){
				spectrumList.add(tmpSpectrum);
				int progress = (int) (100.0*(double)spectrumList.size()/(double)list_query.size());
				if(listener != null)
				listener.updateProgress(progress, ""+spectrumList.size()+ " MSMS spectra loaded");
			}
		}	
		precMass_tmp = null;
		precCharge_tmp =  null;
		pepSeq_tmp =  null;
		proteinAcc_tmp =  null;
		doc = null;	
		
		//bundle it all together
		tmpRecord.setHasRetention(false);
		tmpRecord.setPeptideIDList(pepList);
		tmpRecord.setSpectrumList(spectrumList);
		tmpRecord.setFileName(fileName);
		tmpRecord.setOriginMethod("mascot");
		if(tmpRecord.getMsmsPrecision()<= tmpRecord.getMsmsPrecision()){
			tmpRecord.msmsBetterPrecisionThanMs=true;
		}
		else{
			tmpRecord.msmsBetterPrecisionThanMs=false;
		}
		return tmpRecord;
	}catch(Exception ex){
		if(listener != null){
			listener.updateProgress(100, "Mascot load error:"+ex.toString());
			}
		logger.error("load error:"+ex.toString()+ex.getStackTrace().toString());
		return tmpRecord;
	}
	}

}
