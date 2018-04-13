/* $Id: DeltaUtils.java 324 2010-05-23 11:24:03Z frank $ */

package com.detectorvision.deltaMasses.analyzationmanagement;

import com.detectorvision.utility.DeltaMassBase;

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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class DeltaUtils {
	
	// Logging with log4j
	static Logger logger = Logger.getLogger(DeltaUtils.class.getName());

	public static String DMBgetVersionInfoString(int dbVersionDeltaMasses){

		if (!DeltaMassBase.isCreated())
			return "No database, please create the database";

		String infoString="";
		int dbVersion=0;
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement getInfo;
		try {
			getInfo = conn.prepareStatement("SELECT db_schema_version from meta_db");
			ResultSet result = getInfo.executeQuery();
			while (result.next()) {
				dbVersion=result.getInt(1);
			}
			conn.close();
			logger.info("DMBgetVersionInfoString: " + dbVersionDeltaMasses + ":" +
									dbVersion );
		} catch (SQLException e) {
			logger.error("SQLException: " + e.toString());
			if (dbVersionDeltaMasses==14) {
				infoString="Please create the database";
				return(infoString);
			}
		}
		if(dbVersionDeltaMasses > dbVersion){
			infoString="Please re-create the database";
			return infoString;
		}
		else if(dbVersionDeltaMasses==dbVersion){
			infoString="database version ok: "+dbVersion;
			return infoString;
		}
		else{
			logger.error("DMBgetVersionInfoString:error:dbVersion:" + dbVersion +
									 ":  dbVersionDeltaMasses:" + dbVersionDeltaMasses);
			infoString="";//chosen not to tell the user.
		}
		return infoString;
	}
	
	public static boolean DMBspecnetIsUpToDate(){
		boolean RC=true;
		Connection conn = DeltaMassBase.getCleanConnection();
		PreparedStatement get;
		try {
			get = conn.prepareStatement("SELECT lastmodificationdate,lastspecnetdate from meta_db");
			ResultSet result = get.executeQuery();
			while (result.next()) {
				if(result.getTimestamp(1).after(result.getTimestamp(2))){
					RC=false;
				}
				else{RC=true;}
			}
		} catch (SQLException e) {
			logger.error("DMBspecnetIsUpToDate:SQLException:" + e.toString());
			return true;//uuuuggly
		}
		return RC;
	}

	public static ArrayList<String> DMBgetProteinsOfNet(int specnet_id){
		ArrayList<String> protList= new ArrayList<String>();	
		ArrayList<Integer> specList = new ArrayList<Integer>();
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement get;
		try {
			//populate specList (all spectra being member of that specnet)
			get = conn.prepareStatement("SELECT fk_l_spectrum_id, fk_h_spectrum_id FROM deltamass where lfk_specnet_id = ?");
			get.setInt(1, specnet_id);
			ResultSet result = get.executeQuery();
			while (result.next()) {
				int specLight=result.getInt(1);
				int specHeavy=result.getInt(2);
				if(!specList.contains(specLight)){specList.add(specLight);}
				if(!specList.contains(specHeavy)){specList.add(specHeavy);}
			}
			
			//populate protList with proteins belonging to that net
			get = conn.prepareStatement("SELECT proteinasc,fk_spectrum_id FROM peptide");
			result = get.executeQuery();
			while (result.next()) {
				int spec_id=result.getInt(2);
				if(specList.contains(spec_id)){
					String tmpProt=result.getString(1);
					if(!protList.contains(tmpProt)){
						protList.add(tmpProt);
					}
				}
			}
			conn.close();
		} catch (SQLException e) {
			logger.error("DMBgetProteinsOfNet:" + specnet_id + ":SQLException:" +
									 e.toString());
		}
		specList.clear();
		return protList;
	}
	
	
	public static ArrayList<DMBpeptideShort> DMBgetPepTideInfoOfNet(int specnet_id){
		ArrayList<DMBpeptideShort> DMBpeptideList = new ArrayList<DMBpeptideShort>();
		ArrayList<Integer> specList = new ArrayList<Integer>();
		ArrayList<String> protList = new ArrayList<String>();
		
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement get;
		try {
			//populate specList (all spectra being member of that specnet)
			get = conn.prepareStatement("SELECT fk_l_spectrum_id, fk_h_spectrum_id FROM deltamass where lfk_specnet_id = ?");
			get.setInt(1, specnet_id);
			ResultSet result = get.executeQuery();
			while (result.next()) {
				int specLight=result.getInt(1);
				int specHeavy=result.getInt(2);
				if(!specList.contains(specLight)){specList.add(specLight);}
				if(!specList.contains(specHeavy)){specList.add(specHeavy);}
			}
			
			//populate DMBpeptideList with peptides belonging to this net
			get = conn.prepareStatement("SELECT peptide_id,proteinasc,pepsequence,pepstart,pepend,fk_spectrum_id,pepmass FROM peptide ORDER BY pepmass");
			result = get.executeQuery();
			while (result.next()) {
				int spec_id=result.getInt(6);
				if(specList.contains(spec_id)){
					DMBpeptideShort tmpPep=new DMBpeptideShort();
					tmpPep.peptide_id=result.getInt(1);
					tmpPep.proteinasc=result.getString(2);
					tmpPep.pepsequence=result.getString(3);
					tmpPep.pepstart=result.getInt(4);
					tmpPep.pepend=result.getInt(5);
					tmpPep.pepmass=result.getDouble(7);
					boolean isNotInList=true;
					for(int j=0;j<DMBpeptideList.size();j++){
						if(DMBpeptideShort.pepsAreEqual(tmpPep, DMBpeptideList.get(j))){
							isNotInList=false;
							break;
						}
					}
					if(isNotInList){
						DMBpeptideList.add(tmpPep);
					}
				}
			}
			conn.close();
		} catch (SQLException e) {
			logger.error("SQLException:"+e.toString());
			try {
				if(!conn.isClosed()){
					conn.close();
				}
			} catch (SQLException e1) {
			  logger.error("SQLException:"+e1.toString());
				e1.printStackTrace();
			} catch(Exception ee){
				logger.error("Exception:"+e.toString());
			}
			
			logger.error("DMBgetProteinsOfNet:" + specnet_id + ":SQLException:" +
									 e.toString());
		}
		specList.clear();
		protList.clear();
		return DMBpeptideList;
	}
	
	
	
	
	public static ArrayList<Integer> DMBgetSpecnetIDsOfNetHavingProteinId(int specnet_id){	
		ArrayList<Integer> specList = new ArrayList<Integer>();
		ArrayList<Integer> identifiedSpectra = new ArrayList<Integer>();
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement get;
		try {
			//populate specList (all spectra being member of that specnet)
			get = conn.prepareStatement("SELECT fk_l_spectrum_id, fk_h_spectrum_id FROM deltamass where lfk_specnet_id = ?");
			get.setInt(1, specnet_id);
			ResultSet result = get.executeQuery();
			while (result.next()) {
				int specLight=result.getInt(1);
				int specHeavy=result.getInt(2);
				if(!specList.contains(specLight)){specList.add(specLight);}
				if(!specList.contains(specHeavy)){specList.add(specHeavy);}
			}
			
			//populate protList with proteins belonging to that net
			get = conn.prepareStatement("SELECT fk_spectrum_id FROM peptide");
			result = get.executeQuery();
			while (result.next()) {
				int spec_id=result.getInt(1);
				if(specList.contains(spec_id)){
					if(!identifiedSpectra.contains(spec_id)){
						identifiedSpectra.add(spec_id);
					}
				}
			}
			conn.close();
		} catch (SQLException e) {
			logger.error("DMBgetProteinsOfNet:" + specnet_id + ":SQLException:" +
									 e.toString());
		}
		specList.clear();
		return identifiedSpectra;
		
	}
	
	public static int DMBgetNumDeltaMasses(){
		int numDeltaMasses=0;
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement getNum;
		try {
			getNum = conn.prepareStatement("SELECT count(*) from deltamass");
			ResultSet result = getNum.executeQuery();
			while (result.next()) {
				numDeltaMasses=result.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("DMGgetnumDeltaMasses:SQLException:" + e.toString());
		}
		return numDeltaMasses;
	}
	
}
