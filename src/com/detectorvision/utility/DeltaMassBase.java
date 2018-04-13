/* $Id: DeltaMassBase.java 411 2010-12-26 10:37:08Z frank $ */

package com.detectorvision.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.ST;
import org.postgresql.util.PSQLException;

import com.detectorvision.deltaMasses.DeltaMasses;
import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.Spectrum;
import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;
//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class DeltaMassBase { 
	private static Connection connection=null;
	private static String user=null;
	private static String password=null;
	private static String host=null;
	private static String port=null;
	private static String database=null;
	private static String url=null;
	public static int countConnections=0;
    public static boolean isReachable=false;
    public static int db_schema_version=-1;
    public static boolean doesExist=false;
    public static String connectionError="";
    public static String dbConfigFile="unknown";
	
	static Logger logger = Logger.getLogger(DeltaMassBase.class.getName());

	public static ArrayList<databaseInfo> getConfiguredDatabases(){
		ArrayList<databaseInfo> dbInfoList= new ArrayList<databaseInfo>();

		//get list of all db-config files in the db-directory
		class OnlyUnderscoreFilter implements FilenameFilter
		{
			public boolean accept(File dir, String s)
			{
				if ( s.equals("deltaMassBase.config.txt") ) return true;
				else if ( s.startsWith("deltaMassBase_")&&
						s.endsWith("config.txt")        ) return true;
				return false;
			}
		}
		String aDirectory="config";
		String liste[] = new java.io.File(aDirectory).list( new OnlyUnderscoreFilter() );

		logger.info("list of configured DBs---------------------------------:");
		for(int i=0;i<liste.length;i++){
			logger.info("loadConfig:"+liste[i]);
			loadConfig(liste[i]);
			databaseInfo tmpInfo = new databaseInfo();
			tmpInfo.filename=liste[i];
			tmpInfo.DELTAMASSBASE_NAME=database;
			tmpInfo.HOST=host;
			tmpInfo.PASSWORD=password;
			tmpInfo.PORT=port;
			tmpInfo.USER=user;
			tmpInfo.URL=url;
		    tmpInfo.isReachable=isReachable;
		    tmpInfo.db_schema_version=db_schema_version;
		    tmpInfo.exists=doesExist;
		    tmpInfo.connectionError=connectionError;
		    System.out.println("connectionError:"+connectionError+":");
		    if(connectionError.startsWith("FATAL: database") && (connectionError.endsWith("does not exist"))){
		    	logger.info("database could be created");
		    	tmpInfo.couldBeCreated=true;
		    }
		    else{
		    	tmpInfo.couldBeCreated=false;
		    }
			tmpInfo.print();
			dbInfoList.add(tmpInfo);
		}
		return dbInfoList;
	}

	public static String setDeltaMassBase(String DbNameToSet){
		//returns string OK if OK Errormessages otherwise
		//copies the deltaMassBase***.config.txt from the config directory
		//to bin/deltaMassBase_active_db.config.txt
		logger.info("setDeltaMassBase to:"+DbNameToSet);
		//check if that file exists.
		String sourceName="config\\"+DbNameToSet;

		File copyThisFile = new File(sourceName);
		if(!copyThisFile.exists()){
			logger.error("setDeltaMassBase:File does not exist:"+copyThisFile.toString());
			logger.error("setDeltaMassBase:doing nothing, returning false.");
			return "File "+copyThisFile.getAbsolutePath()+" does not exist";
		}

		String targetName="bin\\deltaMassBase_active_db.config.txt";//20101104
		File copyToFile=new File (targetName);
		if(!copyThisFile.exists()){
			logger.warn("setDeltaMassBase:File does not exist. can happen at first call after installation:"+copyToFile.toString());
			//this can happen at first time usage after an installation.
		}		
		logger.info("trying to copy "+copyThisFile.getAbsolutePath()+ " to: " + copyToFile.getAbsolutePath());
		try {
			FileOperations.copy(copyThisFile, copyToFile);
			//append filename to the file (otherwise deltaMasses cannot now which config file it was)
			try { BufferedWriter out = new BufferedWriter(new FileWriter(targetName, true)); 
			      out.write("DELTAMASSBASE_CONFIGFILE=       "+sourceName); 
			      out.close(); 
			      } 
			catch (IOException e) { 
				logger.error("setDeltaMassBase:could not append to file:"+targetName);
			}
		} catch (Exception x) {
			logger.error("setDeltaMassBase:copy:"+x.toString());
			return x.toString();
		}
		logger.info("succesfully copied "+DbNameToSet+" to "+copyThisFile.getAbsolutePath());
		loadConfig("default");
		return "OK";//all OK
	}



	public static Connection getConnection() {
		if (connection!=null)
			try {
				if (connection.isClosed())
					connection=null;
				else
					return connection;
			} catch (SQLException e) {
				logger.error("getConnection:"+e.toString());
				e.printStackTrace();
				connection=null;
			}
			loadConfig("default");
			return connection;
	}

	public static Connection getCleanConnection() {
		try{
			connection=null;
			loadConfig("default");
			return connection;
		}
		catch(Exception eee){
			logger.warn("getCleanConnection:"+eee.getMessage());
			logger.warn("getCleanConnection:"+eee);
		}
		connection=null;
		return connection;
	}


	public static int get_db_schema_version(){
		int versionOfDatabase=0;
		try {
			Statement s = getConnection().createStatement();
			s.executeQuery("SELECT db_schema_version from meta_db;");
			ResultSet rs = s.getResultSet();
			rs.next();
			versionOfDatabase=rs.getInt(1);
			rs.close();
			s.close();
		} catch (SQLException e) {
			logger.warn("DeltaMassBase:DbSchemaisEqualToVersion:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
			versionOfDatabase=-2;
		}	catch(Exception e){
			logger.warn("get_db_schema_version error:"+e.getLocalizedMessage());
			versionOfDatabase=-2;
			
		}	
		return versionOfDatabase;
	}
	
	public static ArrayList<Integer> getExperimentIds()
	{
		ArrayList<Integer> exp_ids= new ArrayList<Integer>();	
		Statement s = null;
		ResultSet rs = null;
		try {
			s = getConnection().createStatement();
			s.executeQuery("SELECT experiment_id from experiment order by experiment_id;");
			rs = s.getResultSet();
			while(rs.next()){
				exp_ids.add(rs.getInt(1));
			}
		} catch (SQLException e) {
			logger.error("get_ExperimentIds:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
		}	
		finally
		{
			try {
				if(rs != null)
					rs.close();
				if( s!= null)
					s.close();
			} catch (SQLException e) {
				logger.error("get_ExperimentIds:SYSTEM_EXIT:" +
						e.getLocalizedMessage());
			}
		}
		return exp_ids;
	}
	
	public static ArrayList<DeltaMassBase_table_experiment> get_Experiments(){
		ArrayList<DeltaMassBase_table_experiment> table_experiments= new ArrayList<DeltaMassBase_table_experiment>();
		Statement s = null;
		ResultSet rs = null;
		try {
			s = getConnection().createStatement();
			s.executeQuery("SELECT experiment_id,name from experiment e order by experiment_id;");
			rs = s.getResultSet();
			while(rs.next()){
				DeltaMassBase_table_experiment thisExperiment= new DeltaMassBase_table_experiment();
				thisExperiment.experiment_id=rs.getInt(1);
				thisExperiment.experimentname=rs.getString(2);
				table_experiments.add(thisExperiment);
			}
			s.close();
		} catch (SQLException e) {
			logger.error("get_Experiments:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
		}
		finally
		{
			try {
				if(rs != null)
					rs.close();
				if( s!= null)
					s.close();
			} catch (SQLException e) {
				logger.error("get_Experiments:SYSTEM_EXIT:" +
						e.getLocalizedMessage());
			}
		}
		return table_experiments;
	}
	
	
	public static ArrayList<DeltaMassBase_table_record> get_filenames(){
		ArrayList<DeltaMassBase_table_record> table_records= new ArrayList<DeltaMassBase_table_record>();
		Statement s = null;
		ResultSet rs = null;
		try {
			s = getConnection().createStatement();
			s.executeQuery("SELECT r.record_id,r.filename,r.pepFilename,r.userName,r.deltaDate,r.url,r.msPrecision,r.msmsPrecision,r.hasRetention,r.originMethod,r.num_spectra,r.num_pairs,r.fk_experiment_id,e.name from record r,experiment e where r.fk_experiment_id = e.experiment_id order by record_id;");
			rs = s.getResultSet();
			while(rs.next()){
				DeltaMassBase_table_record thisRecord= new DeltaMassBase_table_record();
				thisRecord.record_id=rs.getInt(1);
				thisRecord.filename=rs.getString(2);
				thisRecord.pepFilename=rs.getString(3);
				thisRecord.userName=rs.getString(4);
				thisRecord.deltaDate=rs.getDate(5);
				thisRecord.url=rs.getString(6);
				thisRecord.msPrecision=rs.getDouble(7);
				thisRecord.msmsPrecision=rs.getDouble(8);
				thisRecord.hasRetention=rs.getBoolean(9);
				thisRecord.originMethod=rs.getString(10);
				thisRecord.num_spectra=rs.getInt(11);
				thisRecord.num_pairs=rs.getInt(12);
				thisRecord.fk_experiment_id=rs.getInt(13);
				thisRecord.experimentname = rs.getString(14);
				table_records.add(thisRecord);
			}
		} catch (SQLException e) {
			logger.error("get_filenames:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
		}
		finally {
			try {
				if(rs != null)
					rs.close();
				if( s!= null)
					s.close();
			} catch (SQLException e) {
				logger.error("get_filenames:SYSTEM_EXIT:" +
						e.getLocalizedMessage());
			}
		}
		return table_records;
	}
	
	public static String getExperimentName(int experimentId)
	{
		String experimentName = null;
		Statement s = null;
		ResultSet rs = null;
		try {
			s = getConnection().createStatement();
			s.executeQuery("SELECT name from experiment where experiment_id = "+ experimentId +";");
			rs = s.getResultSet();
			if(rs.next())
			{
				experimentName = rs.getString(1);
			}
			
		}catch(SQLException e) {
				logger.error("getExperimentName:SYSTEM_EXIT:" +
						e.getLocalizedMessage());
		}
		finally {
			try {
				if(rs != null)
					rs.close();
				if( s!= null)
					s.close();
			} catch (SQLException e) {
				logger.error("getExperimentName:SYSTEM_EXIT:" +
						e.getLocalizedMessage());
			}
		}
		return experimentName;
	}
	
	public static boolean setPairMarked(int deltaMass_id,String comment,boolean marked_in){
		boolean OK=true;
		PreparedStatement pstmt = null;
		if(deltaMass_id==0){
			OK=false;
			return OK;
		}
		try {
			String myComment="";
			if (comment.length()>=100){
				myComment=comment.substring(0,98);
			}
			else{
				myComment=comment;
			}
			Connection con = getConnection();
			pstmt= con.prepareStatement("update deltamass set marked=? where deltamass_id= ?");
			pstmt.setBoolean(1, marked_in);
			pstmt.setInt(2, deltaMass_id);
			pstmt.executeUpdate(); // execute update statement

			pstmt= con.prepareStatement("update deltamass set comment=? where deltamass_id= ?");
			pstmt.setString(1, myComment);
			pstmt.setInt(2, deltaMass_id);
			pstmt.executeUpdate(); // execute update statement

			con.close();
			pstmt.close();
		} catch (SQLException e) {
			logger.error("DeltaMassBase:DbSchemaisEqualToVersion:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
			OK=false;
			return OK;
		}
		catch (Exception ee){
			logger.error("setPairmarked Error:"+ee.toString());
			OK=false;
			return OK;
		}
		return OK;
	}


	public static ArrayList<Protein> getProteinList(){
		ArrayList<Protein> l = new ArrayList<Protein>();
		String bla ="";
		try {
			Statement s = getConnection().createStatement();
			s.executeQuery("SELECT acc,description,http,mass FROM protein;");
			ResultSet rs = s.getResultSet();
			while(rs.next()){
				Protein p = new Protein();
				p.acc=rs.getString(1);
				p.description=rs.getString(2);
				p.http=rs.getString(3);
				p.mass=rs.getDouble(4);
				l.add(p);
				bla=s.toString();
			};	
			rs.close();
			s.close();
		} catch (SQLException e) {
			logger.error("DeltaMassBase:getProteinlist:SQLException:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
			logger.error("sql is:"+bla);
		}
		return l;
	}

	public static int getSequencer(String whatSequencer) {
		int retval=0;
		try {
			Statement s = getConnection().createStatement();
			s.executeQuery("SELECT nextval('"+whatSequencer+"');");
			ResultSet rs = s.getResultSet();
			rs.next();
			retval=rs.getInt(1);
			rs.close();
			s.close();
		} catch (SQLException e) {
			logger.fatal("DeltaMassBase:getSequencer:SQLException:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
			System.exit(-1);
		}
		return retval;
	}

	public static boolean isCreated() {
		PreparedStatement get = null;
		Connection conn = null ;
		ResultSet result = null;
		try {
			conn=getConnection();
			get = conn.prepareStatement("select tablename from pg_tables where tablename = 'record'");
			result = get.executeQuery();
			if (result.next())
				return true;
			else{
				logger.warn("isCreated returns no row");
				return false;
			}
		} catch (SQLException e) {
			logger.error("isCreated:error:"+e.toString());
			e.printStackTrace();
			return false;
		} catch( Exception e){
			logger.error("isCreated::error:"+e.toString());
			return false;
		}
		finally
		{
			try {
				if( result != null)
					result.close();
				if( get != null)
					get.close();
				if( conn != null)
					conn.close();
			} catch(SQLException e){
				logger.error("DeltaMassBase:isCreated:SQLException:SYSTEM_EXIT:" +
						e.getLocalizedMessage());
				return false;
			}
		}
	}

	private static void loadConfig(String inConfigFile) {
		try {
			logger.info("loadConfig:"+inConfigFile);
			String configFile="";

			if(inConfigFile.equals("default"))
			{//called to connect for operations
				logger.info("loadConfig:default");	
				configFile="bin\\deltaMassBase_active_db.config.txt";

				//check if this exist. if not, copy config/deltaMassBase.config.txt 
				java.io.File f = new java.io.File(configFile);
				if(!f.exists()){
					FileOperations.copy(new File("config\\deltaMassBase.config.txt"), new File("bin\\deltaMassBase_active_db.config.txt"));
					logger.warn("loadConfig:"+f.getAbsolutePath()+": not found. copying.");
					java.io.File src = new java.io.File("config\\deltaMassBase.config.txt");
					logger.warn("loadConfig trying to copy "+src.getAbsolutePath());
					try{
						FileOperations.copy(src, f);
					}
					catch(Error e){
						logger.error("loadConfig:error while copying:"+e.getLocalizedMessage());
						logger.error(e);
					}
				}
			}
			else if (inConfigFile.length()!=0){
				//called to connect for checking configuration
				configFile="config"+"//"+inConfigFile;
			}
			else{
				logger.fatal("loadConfig");
			}

			java.io.File f = new java.io.File(configFile);

			FileReader configFileReader=null;
			try {
				configFileReader = new FileReader(f);
			}
			catch (Exception e) {
				currentDirectory curDir = new currentDirectory();
				System.out.println(curDir.getCurrentDirectory());
				logger.error("Error while loading properties from "+f.toString()+" Error:"
						+ e.getMessage());
			}
			BufferedReader buff = new BufferedReader(configFileReader);
			String line;
			while (true) {
				line = buff.readLine();
				if (line == null) { // EOF reached. Bail out of the while-loop.
					break;
				}
				if (line.startsWith("DELTAMASSBASE_")) {
					String[] words = line.split("\\s+"); 
					if (line.startsWith("DELTAMASSBASE_NAME=")) database=words[1];
					if (line.startsWith("DELTAMASSBASE_USER=")) user=words[1];
					if (line.startsWith("DELTAMASSBASE_PASSWORD=")) password=words[1];
					if (line.startsWith("DELTAMASSBASE_HOST=")) host=words[1];
					if (line.startsWith("DELTAMASSBASE_PORT=")) port=words[1];
					if (line.startsWith("DELTAMASSBASE_CONFIGFILE=")) dbConfigFile=words[1];
					url="jdbc:postgresql://" + host + ":" + port + "/" +	database;
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			logger.fatal("error in database connection setup:deltaMassBase.config.txt not found:"+e1.toString());
			System.exit(1);
		} catch (IOException e2) {
			logger.fatal("DeltaMassBase:IOException:loadConfig:" +
					e2.toString());
			e2.printStackTrace();
			logger.fatal("error in database connection setup:IOExeption in deltaMassBase.config.txt");
			System.exit(1);
		} catch(Exception e3){
			logger.error(""+e3.getMessage());
			logger.error("Exception:"+e3);
		}

		boolean gotConnection=false;
			try {
				java.lang.Class.forName("org.postgresql.Driver");
				countConnections++;
				connection = java.sql.DriverManager.getConnection(url, user, password);
				gotConnection=true;
				doesExist=true;
				isReachable=true;
				db_schema_version=get_db_schema_version();
				connectionError="OK";
			}
			catch(PSQLException ex) {
				isReachable=false;
				if(ex.getSQLState().equals("3D000")){
					doesExist=false;
				}
				connectionError=ex.getMessage();
				logger.warn("loadConfig.getConnection:SQLState:"+ex.getSQLState());
				logger.warn("loadConfig.getConnection:ErrorCode:"+ex.getErrorCode());
				db_schema_version=-2;
				logger.warn("loadConfig:getConnection:PSQLException: " +
						ex.toString());
			    logger.warn("got no connection.");
			    try{
			    url="jdbc:postgresql://" + host + ":" + port + "/";
				connection = java.sql.DriverManager.getConnection(url, user, password);
				gotConnection=true;
				doesExist=false;
				isReachable=true;
				db_schema_version=-3;
			    }catch(Exception eee){
			    	logger.warn("connection without db failed.");
			    };
			    
			}
			catch(Exception ex) {
				isReachable=false;
				db_schema_version=-2;
				connectionError=ex.getMessage();
				logger.warn("loadConfig:getConnection:Exception: " +
						ex.toString());
			}
			if(inConfigFile.equals("default")){
				logger.info("default: setting myDataBaseInfo");
				DeltaMasses.myDatabaseInfo.connectionError=connectionError;
				DeltaMasses.myDatabaseInfo.DELTAMASSBASE_NAME=database;
				DeltaMasses.myDatabaseInfo.filename=inConfigFile;
				DeltaMasses.myDatabaseInfo.HOST=host;
				DeltaMasses.myDatabaseInfo.URL=url;
				DeltaMasses.myDatabaseInfo.PORT=port;
				DeltaMasses.myDatabaseInfo.USER=user;
				DeltaMasses.myDatabaseInfo.filename=dbConfigFile;
			}
		if(countConnections%100==0){
			logger.info("number of DB-get-connections:"+countConnections);
		}
	}

	// return false if failed true if ok
	public static boolean vacuumDataBase() {
		logger.info("DeltaMassBase:vacuumDataBase:vacuuming database");
		try {
			Connection conn = getConnection();
			Statement s = conn.createStatement();
			s.executeUpdate("VACUUM");
			s.close();
			conn.close();
			return true;
		}
		catch(Exception ex){
			logger.error("DeltaMassBase:vacuumDataBase:SYSTEM_ERROR:could not vaccum database:" +
					ex.getLocalizedMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public static final boolean setLastModificationDate(){
		Connection conn=getConnection();;
		PreparedStatement get;
		try {		
			String INSERT_RECORD = "update meta_db set lastModificationDate = ? where meta_db_id=1";
			PreparedStatement pstmt = conn.prepareStatement(INSERT_RECORD);
			pstmt.setInt(1,1);
			java.sql.Timestamp  sqlDate2 = new java.sql.Timestamp(new java.util.Date().getTime());
			int updatedRows=0;
			pstmt.setTimestamp(1, sqlDate2);
			updatedRows=pstmt.executeUpdate();

			if (updatedRows!=1){	
				logger.error("meta_db update error:"+pstmt.toString());
				return false;
			}
			else{
				logger.info("updated lastmodification date of metadb to:"+sqlDate2.toString());
			}
			pstmt.close();
			conn.close();
		}catch(Exception e){
			logger.error("setLastModificationDate error:"+e.toString());
			return false;
		}
		return true;
	}
	
public static final boolean storeExperiment(int aExperimentId, String aExperimentName){
		
		logger.info("trying to store experiment:"+aExperimentId);
		Connection conn=getConnection();;
		PreparedStatement get = null;
		ResultSet result = null;
		try {		
			//check if the experiment is present. if its not, return false.
			get = conn.prepareStatement("select experiment_id from experiment where experiment_id=?");
			get.setInt(1, aExperimentId);
			result = get.executeQuery();
			if (!result.next()){	
				logger.error("storeExperiment of experiment_id:"+aExperimentId+":failed, nothing found.");
				return false;
			}
			else{
				logger.info("Store Experiment: found experiment_id"+aExperimentId);
			}
			//begin the transaction
			get=conn.prepareStatement("BEGIN");
			get.executeUpdate();
			 
			get=conn.prepareStatement("update experiment set name = ? where experiment_id = ? ");
			get.setString(1, aExperimentName);
			get.setInt(2, aExperimentId);
			int updatedRows=get.executeUpdate();
			logger.info("No of rows stored/updated : "+updatedRows);
			
		
			get=conn.prepareStatement("COMMIT");
			get.executeUpdate();
			logger.info("updated rows in Experiment table:"+updatedRows);
			logger.info("transaction commited");
			DeltaMassBase.setLastModificationDate();

		} catch (SQLException e) {
			logger.error("storeExperiment problem. experiment_id:"+aExperimentId+":"+e.toString());
			e.printStackTrace();
			return false;
		} catch(Exception e){
			logger.error("storeExperiment Exception:"+e.toString());
			return false;
		}
		finally
		{
			try {
				if( get!= null)
					get.close();
				if( result != null)
					result.close();
			} catch (SQLException e) {
				logger.error("storeExperiment Exception:"+e.toString());
				return false;
			}
		}
		return true;
	}
	
	
	public static final boolean storeRecordToExperiment(int aRecordID,int aExperimentId, String aExperimentName){
		logger.info("trying to store record:"+aRecordID);
		Connection conn=getConnection();;
		PreparedStatement get = null;
		ResultSet result = null;
		try {		
			//check if the record is present. if its not, return false.
			get = conn.prepareStatement("select record_id from record where record_id=?");
			get.setInt(1, aRecordID);
			result = get.executeQuery();
			if (!result.next()){	
				logger.error("storeRecord of record_id:"+aRecordID+":failed, nothing found.");
				return false;
			}
			else{
				logger.info("Store Record: found record_id"+aRecordID);
			}
			//begin the transaction
			get=conn.prepareStatement("BEGIN");
			get.executeUpdate();
			 
			get=conn.prepareStatement("update experiment set name = ? where experiment_id = ? ");
			get.setString(1, aExperimentName);
			get.setInt(2, aExperimentId);
			int updatedRows=get.executeUpdate();
			logger.info("No of rows stored : "+updatedRows);
			
			get=conn.prepareStatement("update record set fk_experiment_id = ? where record_id= ? ");
			get.setInt(1, aExperimentId);
			get.setInt(2, aRecordID);
			updatedRows=get.executeUpdate();
			logger.info("No of rows stored : "+updatedRows);
			
			get=conn.prepareStatement("COMMIT");
			get.executeUpdate();
			logger.info("updated rows in Record table:"+updatedRows);
			logger.info("transaction commited");
			DeltaMassBase.setLastModificationDate();

		} catch (SQLException e) {
			logger.error("storeRecord problem. record_id:"+aRecordID+":"+e.toString());
			e.printStackTrace();
			return false;
		} catch(Exception e){
			logger.error("storedRecord Exception:"+e.toString());
			return false;
		}
		finally
		{
			try {
				if(get != null)
					get.close();
				if(result != null)
					result.close();
			} catch (SQLException e) {
				logger.error("storeRecord problem. record_id:"+aRecordID+":"+e.toString());
				return false;
			}
		}
		return true;
		
	}
	public static final boolean deleteRecord(int aRecordID){
		//TODO the transaction is not rollbacked. is that OK ?????
		logger.info("trying to delete record:"+aRecordID);
		Connection conn=getConnection();;
		PreparedStatement get = null;
		ResultSet result = null;
		try {		
			//check if the record is present. if its not, return false.
			get = conn.prepareStatement("select record_id,pepfilename from record where record_id=?");
			get.setInt(1, aRecordID);
			result = get.executeQuery();
			if (!result.next()){	
				logger.error("deleteRecord of record_id:"+aRecordID+":failed, nothing found.");
				return false;
			}
			else{
				logger.info("deleteRecord: found record_id"+aRecordID);
			}

			//begin the transaction
			get=conn.prepareStatement("BEGIN");
			get.executeUpdate();

			//Delete the peptides
			//delete from peptide  where fk_spectrum_id in (select spectrum_id from spectrum where fk_record_id=1);
			get=conn.prepareStatement("delete from peptide  where fk_spectrum_id in (select spectrum_id from spectrum where fk_record_id=?)");
			get.setInt(1, aRecordID);
			int deletedRows=get.executeUpdate();
			logger.info("deleted rows from peptide:"+deletedRows);

			//Delete the light deltamasses
			//delete from deltamass where fk_l_spectrum_id in (select spectrum_id from spectrum where fk_record_id=1);
			get=conn.prepareStatement("delete from deltamass  where fk_l_spectrum_id in (select spectrum_id from spectrum where fk_record_id=?)");
			get.setInt(1, aRecordID);
			deletedRows=get.executeUpdate();
			logger.info("deleted light deltamasses:"+deletedRows);

			//Delete the heavy deltamasses
			get=conn.prepareStatement("delete from deltamass  where fk_h_spectrum_id in (select spectrum_id from spectrum where fk_record_id=?)");
			get.setInt(1, aRecordID);
			deletedRows=get.executeUpdate();
			logger.info("deleted heavy deltamasses:"+deletedRows);			

			//delete the spectra
			get = conn.prepareStatement("delete from spectrum where fk_record_id=?");
			get.setInt(1, aRecordID);
			deletedRows=get.executeUpdate();
			logger.info("deleted "+deletedRows+" spectra");

			if(aRecordID!=0){//delete only if its not record zero - it holds accuracy information.
				//delete the record
				get = conn.prepareStatement("delete from record where record_id=?");
				get.setInt(1, aRecordID);
				deletedRows=get.executeUpdate();
				logger.info("deleted "+deletedRows+" records");
			}
			else{//aRecordID must be zero. We cannot delete this one.... 
				get = conn.prepareStatement("update record set (filename,pepfilename,url,originmethod,num_spectra,num_pairs)=('removed','removed','none','none',0,0) where record_id=0");
				deletedRows=get.executeUpdate();
				logger.info("updated the information for record 0.");
			}
			get=conn.prepareStatement("COMMIT");
			get.executeUpdate();
			logger.info("transaction commited");
			DeltaMassBase.setLastModificationDate();
			
		} catch (SQLException e) {
			logger.error("delteteRecord problem. record_id:"+aRecordID+":"+e.toString());
			e.printStackTrace();
			return false;
		} catch(Exception e){
			logger.error("deleteRecord Exception:"+e.toString());
			return false;
		}
		finally
		{
			try {
				if(get != null)
					get.close();
				if(result != null)
					result.close();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;		
	}

	public static boolean validConnection() {
		Connection testConn = getConnection();
		if (testConn == null) return false;
		try {
			testConn.close();
		} catch (SQLException e) {
			logger.error("validConnection:SQLException: " +
					e.toString());
			return false;
		} catch (Exception f){
			logger.error("validConnection:Exception: " +
					f.toString());
		}
		logger.info("validConnection:true");
		return true;
	}

	    //last relicts of getPTMList. Remove in 2011.
		//PTMString+="(737,'PEG 2','PEG 2',88.0523,88.0443,'H(8) C(4) O(2)','comment on peg 2','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		//PTMString+="(738,'PEG 3','PEG 3',132.07845,132.06645,'H(12) C(6) O(3)','comment on peg 3','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		//PTMString+="(739,'PEG 4','PEG 4',176.1046,176.0886,'H(16) C(8) O(4)','comment on peg 4','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		//PTMString+="(740,'PEG 5','PEG 6',220.13075,220.11075,'H(20) C(10) O(5)','comment on peg 5','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0),";
		//PTMString+="(741,'PEG 6','PEG 6',264.1569,264.1329,'H(24) C(12) O(6)','comment on peg 6','unimod','dvision','2007-03-27 16:21:11','2007-03-27 16:21:11','',0)";



	public static Spectrum getSpectrum(Integer spectrum_id){
		Spectrum spectrum = new Spectrum();
		ArrayList<MSMS> valuelist = new ArrayList<MSMS>();

		Connection conn = null;
		Statement s = null;
		ResultSet result = null;
		try{
			conn = com.detectorvision.utility.DeltaMassBase.getConnection();
			s = conn.createStatement ();
			result = s.executeQuery("SELECT spectrum_id,mzbase64,signalbase64,precursormass,charge,title,retention,queryid,fk_record_id,fk_specnet_id from spectrum where spectrum_id = "+spectrum_id);

			boolean found=false;
			while (result.next()){
				found=true;
				spectrum.charge=result.getInt(5);
				spectrum.title=result.getString(6);
				spectrum.retention=result.getFloat(7);
				spectrum.queryID=result.getString(8);
				spectrum.spectrum_id=result.getInt(1);
				spectrum.precursorMass=result.getDouble(4);

				////////////////////////get the MSMS data//////////////////////////////////////////////////////////
				{
					String tmpMz =result.getString(2);
					String tmpSig=result.getString(3);
					//get the decoded objects
					ArrayList<Double> mzArray = new ArrayList<Double>();
					ArrayList<Double> signalArray = new ArrayList<Double>();

					mzArray=(ArrayList<Double>)com.detectorvision.massspectrometry.biodata.Base64.decodeToObject(tmpMz);
					signalArray=(ArrayList<Double>)com.detectorvision.massspectrometry.biodata.Base64.decodeToObject(tmpSig);
					//security check if lists size are equal
					if(mzArray.size()!=signalArray.size()){
						System.out.println("fatal error: array 1 size mismatch. exiting.");
						System.exit(1);
					}

					for(int i=0;i<mzArray.size();i++){
						MSMS tmp = new MSMS();

						tmp.massToCharge=mzArray.get(i);
						tmp.intensity=signalArray.get(i);
						valuelist.add(tmp);
					}
					spectrum.valueList=valuelist;
				}
			}


		}catch(Exception e){
			//TODO put in propper logging
			e.printStackTrace();
			System.out.println("Error:"+e.getLocalizedMessage());
			System.exit(1);
		}
		finally
		{
			try {
				if(result != null)
					result.close();
				if( s!= null)
					s.close();
				if(conn != null)
					conn.close();
			} catch (Exception ex) {
				System.out.println("Error:"+ex.getLocalizedMessage());
				System.exit(1);
			}
		}
		return spectrum;
	}


	public static boolean storeProteinList(ArrayList<Protein> list) {
		ArrayList<Protein> existing = new ArrayList<Protein>();
		existing=DeltaMassBase.getProteinList();
		PreparedStatement p = null;
		try {
			String query="insert into protein (acc,description,http,mass) values(?,?,?,?)";
			p = getConnection().prepareStatement(query);
			for(int i=0;i<list.size();i++){
				boolean store_it=true;
				for(int j=0;j<existing.size();j++){
					if(existing.get(j).acc.equals(list.get(i).acc)){
						store_it=false;
					}
				}
				if(store_it){
					p.setString(1, list.get(i).acc);
					p.setString(2, list.get(i).description);
					p.setString(3, list.get(i).http);
					p.setDouble(4, list.get(i).mass);
					p.execute();
				}	
			}
		} catch (SQLException e) {
			logger.error("DeltaMassBase:storeProteinList:SQLException:SYSTEM_EXIT:" +
					e.getLocalizedMessage());
			return false;
		} catch(Exception g){
			logger.error("DeltaMassBase:storeProteinList:Exception:SYSTEM_EXIT:" +
					g.getLocalizedMessage());
			return false;
		} finally {
			try {
				if(p != null)
					p.close();
			} catch (SQLException ex) {
				logger.error("DeltaMassBase:storeProteinList:Exception:SYSTEM_EXIT:" +
						ex.getLocalizedMessage());
				return false;
			}
		}
		
		return true;
	}
	
	// return false if failed true if ok
	public static boolean formatDatabase() {
		logger.info("DeltaMassBase:createDataBase:creating database");
		try {
			logger.info("Trying to get a Connection");
			Connection conn = getConnection();
			logger.info("Connected!");
			Statement s = conn.createStatement (); 
			int count;

			s.executeUpdate("BEGIN"); //all of this is run as one transaction

			s.executeUpdate ("DROP TABLE IF EXISTS meta_db");
			logger.info("dropping table meta_db");

			s.executeUpdate ("DROP TABLE IF EXISTS peptide");
			logger.info("dropping table peptide");

			s.executeUpdate ("DROP TABLE IF EXISTS elements");
			logger.info("dropping table elements");

			s.executeUpdate ("DROP TABLE IF EXISTS multimod");
			logger.info("dropping table multiMod");

			s.executeUpdate ("DROP TABLE IF EXISTS deltaComp");
			logger.info("dropping table deltaComp    new version");

			s.executeUpdate ("DROP INDEX IF EXISTS deltaMass_id_index");
			logger.info("dropping INDEX deltaMass_id_index");

			s.executeUpdate ("DROP INDEX IF EXISTS fk_l_spectrum_id");
			logger.info("dropping INDEX fk_l_spectrum_id");

			s.executeUpdate ("DROP INDEX IF EXISTS fk_h_spectrum_id");
			logger.info("dropping INDEX fk_h_spectrum_id");

			s.executeUpdate ("DROP TABLE IF EXISTS deltaMass");
			logger.info("dropping table deltaMass");

			s.executeUpdate ("DROP INDEX IF EXISTS spectrum_spectrum_id_index");
			logger.info("dropping INDEX spectrum_id_index");

			s.executeUpdate ("DROP INDEX IF EXISTS spectrum_precursorMass_index");
			logger.info("dropping INDEX spectrum_precursorMass_index");

			s.executeUpdate ("DROP TABLE IF EXISTS spectrum");
			logger.info("dropping table spectrum");

			s.executeUpdate ("DROP TABLE IF EXISTS specnet");
			logger.info("dropping table specnet");

			s.executeUpdate ("DROP INDEX IF EXISTS specnet_id_index");
			logger.info("dropping INDEX specnet_id_index");

			s.executeUpdate ("DROP INDEX IF EXISTS record_id_index");
			logger.info("dropping INDEX record_id_index");

			s.executeUpdate ("DROP TABLE IF EXISTS record");
			logger.info("dropping table record");

			s.executeUpdate ("DROP INDEX IF EXISTS experiment_id_index");
			logger.info("dropping INDEX experiment_id_index");

			s.executeUpdate ("DROP TABLE IF EXISTS experiment");
			logger.info("dropping table experiment");

			s.executeUpdate ("DROP INDEX IF EXISTS protein_id_index");
			logger.info("dropping INDEX protein_id_index");

			s.executeUpdate ("DROP TABLE IF EXISTS protein");
			logger.info("dropping table protein");	
			
			s.executeUpdate ("DROP TABLE IF EXISTS fingerprint");
			logger.info("dropping table fingerprint");	

			String sqlString="CREATE TABLE experiment (experiment_id int primary key,";
			sqlString += "name VARCHAR(255) NOT NULL default '-'";
			sqlString += ");";
			s.executeUpdate(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(0, 'default')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(1, 'healthy')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(2, 'diseased')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(3, 'reference')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(4, 'exp_4')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(5, 'exp_5')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(6, 'exp_6')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(7, 'exp_7')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(8, 'exp_8')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(9, 'exp_9')";
			s.execute(sqlString);
			sqlString="INSERT INTO experiment (experiment_id,name) VALUES(10, 'exp_10')";
			s.execute(sqlString);
	
			sqlString="CREATE TABLE meta_db (";
			sqlString += "meta_db_id           INT NOT NULL,";
			sqlString += "db_schema_version    INT NOT NULL,";
			sqlString += "createDate           TIMESTAMP NOT NULL,";
			sqlString += "lastModificationDate TIMESTAMP NOT NULL,";
			sqlString += "lastSpecnetDate      TIMESTAMP NOT NULL,";
			sqlString += "export2clusterDate   TIMESTAMP NOT NULL,";
			sqlString += "msAccuracy FLOAT NOT NULL default 0.01,";
			sqlString += "msmsAccuracy FLOAT NOT NULL default 0.4";
			sqlString += ");";
			s.executeUpdate(sqlString);

			logger.info("-----creating export2clusterDate");

			//20080106 block below
			String INSERT_RECORD = "INSERT INTO meta_db(meta_db_id,db_schema_version,createDate,lastModificationDate,lastSpecnetDate,export2clusterDate) VALUES(?,?,?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(INSERT_RECORD);

			pstmt.setInt(1,1);
			pstmt.setInt(2,22); //INTEGRATION_CONTROL
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
			pstmt.setTimestamp(3, sqlDate);
			pstmt.setTimestamp(4, sqlDate);
			pstmt.setTimestamp(5, sqlDate);
			pstmt.setTimestamp(6, sqlDate);
			pstmt.executeUpdate();

			sqlString="CREATE TABLE record (record_id int primary key,";		//serial: postgresql pg 95
			sqlString += "filename VARCHAR(255) NOT NULL default '-',";   			//from where the spectrum data originates
			sqlString += "pepFilename VARCHAR(255) NOT NULL default '-',";			//from where the peptides originate
			sqlString += "userName VARCHAR(255) NOT NULL default '-',";       		//username (as taken from OS)
			sqlString += "deltaDate TIMESTAMP NOT NULL,";								//date of run of deltaMasses
			sqlString += "url VARCHAR(255) NOT NULL default '-',";					//if there is a url pointing to this resource
			sqlString += "msPrecision FLOAT NOT NULL default 0.01,";
			sqlString += "msmsPrecision FLOAT NOT NULL default 0.4,";
			sqlString += "hasRetention boolean NOT NULL default false,";
			sqlString += "originMethod VARCHAR(20) NOT NULL default '-',";
			sqlString += "num_spectra    INT NOT NULL,";
			sqlString += "num_pairs      INT NOT NULL,";
			sqlString += "fk_experiment_id int references experiment";
			sqlString += ");";
			s.executeUpdate(sqlString);

			sqlString="CREATE INDEX record_id_index ON record (record_id)";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE specnet (specnet_id int primary key,";
			sqlString += "numspecs INT NOT NULL default 1,";
			sqlString += "numpairs INT NOT NULL default 1,";
			sqlString +="minmass FLOAT NOT NULL default 0.0,";
			sqlString +="maxmass FLOAT NOT NULL default 0.0,";
			sqlString +="numphospho INT NOT NULL default 1,";
			sqlString += "fk_experiment_id int references experiment"; //to be removed
			sqlString += ");";
			s.executeUpdate(sqlString);

			sqlString="CREATE INDEX specnet_id_index ON specnet (specnet_id)";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE spectrum (spectrum_id int primary key,";
			sqlString +="fk_record_id int NOT NULL references record,";
			sqlString += "precursorMass FLOAT NOT NULL default 0.0,";
			sqlString += "charge INT NOT NULL default 1,";
			sqlString += "title VARCHAR(255) NOT NULL default '',";
			sqlString += "retention FLOAT NOT NULL default 0.0,";
			sqlString += "queryID VARCHAR(100) NOT NULL default '',";
			sqlString += "mzBase64 TEXT NOT NULL,";
			sqlString += "signalBase64 TEXT NOT NULL,";
			//20080104 sqlString += "fk_specnet_id int references specnet";
			sqlString += "fk_specnet_id int";
			sqlString += ");";
			s.executeUpdate(sqlString);

			sqlString="CREATE INDEX spectrum_id_index ON spectrum (spectrum_id)";
			s.executeUpdate(sqlString);

			sqlString="CREATE INDEX spectrum_precursorMass_index ON spectrum (precursorMass)";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE deltaMass (deltaMass_id int primary key,";
			sqlString +="dm FLOAT NOT NULL default 0.0,";
			sqlString +="sim FLOAT NOT NULL default 0.0,";
			sqlString +="p FLOAT NOT NULL default 0.0,";
			sqlString +="fk_l_spectrum_id int NOT NULL references spectrum,";
			sqlString +="fk_h_spectrum_id int NOT NULL references spectrum,";
			sqlString +="mass_light FLOAT NOT NULL default 0.0,";
			sqlString +="marked boolean NOT NULL default false,";
			sqlString +="comment VARCHAR(100) NOT NULL default '',";
			sqlString +="weakdelta boolean NOT NULL default false,";
			sqlString +="lfk_specnet_id int";

			sqlString += ");";
			s.executeUpdate(sqlString);

			sqlString="CREATE INDEX deltaMass_id_index ON deltaMass (deltaMass_id)";
			s.executeUpdate(sqlString);
			sqlString="CREATE INDEX deltaMass_fk_l_index ON deltaMass (fk_l_spectrum_id)";
			s.executeUpdate(sqlString);
			sqlString="CREATE INDEX deltaMass_fk_h_index ON deltaMass (fk_h_spectrum_id)";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE peptide (peptide_id int primary key,";
			sqlString +="pepMass FLOAT NOT NULL default 0.0,";	
			sqlString +="proteinAsc VARCHAR(40) NOT NULL default '',";
			sqlString +="pepSequence VARCHAR(200) NOT NULL default '',";
			sqlString +="pepMz FLOAT NOT NULL default 0.0,";
			sqlString +="pepError FLOAT NOT NULL default 0.0,";
			sqlString +="pepStart int NOT NULL default 0,";
			sqlString +="pepEnd int NOT NULL default 0,";
			sqlString +="pepMod VARCHAR(60) NOT NULL default '',";
			sqlString +="fk_spectrum_id int references spectrum";
			sqlString += ");";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE deltaComp (deltaComp_id int primary key,";
			sqlString +="deltaCompMass FLOAT NOT NULL default 0.0,";
			sqlString +="deltaCompAbsMass FLOAT NOT NULL default 0.0,";
			sqlString +="deltaComposition VARCHAR(100) NOT NULL default '',";
			sqlString +="isUniMod BOOLEAN NOT NULL default 'true'";
			sqlString += ");";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE elements (elements_id int primary key,";
			sqlString +="element VARCHAR(255) NOT NULL default '',";
			sqlString +="full_name VARCHAR(255) NOT NULL default '',";
			sqlString +="mono_mass FLOAT NOT NULL default 0.0,";
			sqlString +="avg_mass FLOAT NOT NULL default 0.0";
			sqlString += ");";
			s.executeUpdate(sqlString);
			sqlString=("INSERT INTO elements VALUES " +
					"( 1,'H'  ,'Hydrogen'   ,  1.007825035,  1.00794)," +
					"( 2,'2H' ,'Deuterium'  ,  2.014101779,  2.014101779)," +
					"( 3,'Li' ,'Lithium'    ,  7.016003   ,  6.941)," +
					"( 4,'C'  ,'Carbon'     , 12          , 12.0107)," +
					"( 5,'13C','Carbon13'   , 13.00335483 , 13.00335483)," +
					"( 6,'N'  ,'Nitrogen'   , 14.003074   , 14.0067)," +
					"( 7,'15N','Nitrogen15' , 15.00010897 , 15.00010897)," +
					"( 8,'O'  ,'Oxygen'     , 15.99491463 , 15.9994)," +
					"( 9,'18O','Oxygen18'   , 17.9991603  , 17.9991603)," +
					"(10,'F'  ,'Fluorine'   , 18.99840322 , 18.9984032)," +
					"(11,'Na' ,'Sodium'     , 22.9897677  , 22.98977)," +
					"(12,'P'  ,'Phosphorous', 30.973762   , 30.973761)," +
					"(13,'S'  ,'Sulfur'     , 31.9720707  , 32.065)," +
					"(14,'Cl' ,'Chlorine'   , 34.96885272 , 35.453)," +
					"(15,'K'  ,'Potassium'  , 38.9637074  , 39.0983)," +
					"(16,'Ca' ,'Calcium'    , 39.9625906  , 40.078)," +
					"(17,'Fe' ,'Iron'       , 55.9349393  , 55.845)," +
					"(18,'Ni' ,'Nickel'     , 57.9353462  , 58.6934)," +
					"(19,'Zn' ,'Zinc'       , 63.9291448  , 65.409)," +
					"(20,'Se' ,'Selenium'   , 79.9165196  , 78.96)," +
					"(21,'Br' ,'Bromine'    , 78.9183361  , 79.904)," +
					"(22,'Ag' ,'Silver'     ,106.905092   ,107.8682)," +
					"(23,'Hg' ,'Mercury'    ,201.970617   ,200.59)," +
					"(24,'Au' ,'Gold'       ,196.966543   ,196.96655)," +
					"(25,'I'  ,'Iodine'     ,126.904473   ,126.90447)," +
					"(26,'Mo' ,'Molybdenum' , 97.9054073  , 95.94)," +
					"(27,'Cu' ,'Copper'     , 62.9295989  , 63.546)," +
			"(28,'e'  ,'electron'   ,  0.000549   ,  0.000549)");
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE multimod (multimod_id int primary key,";
			sqlString +="full_name varchar(255) NOT NULL default '',";
			sqlString +="code_name varchar(255) NOT NULL default '',";
			sqlString +="mono_mass FLOAT default NULL,";
			sqlString +="avge_mass FLOAT default NULL,";
			sqlString +="composition varchar(255) default NULL,";
			sqlString +="misc_notes text,";
			sqlString +="username_of_poster varchar(255) NOT NULL default '',";
			sqlString +="group_of_poster varchar(255) NOT NULL default '',";
			sqlString +="date_time_posted varchar(100) NOT NULL default '2000-01-00 00:00:00',";
			sqlString +="date_time_modified varchar(100) NOT NULL default '2000-01-00 00:00:00',";
			sqlString +="ex_code_name varchar(255) default NULL,";
			sqlString +="approved int default 0,";
			sqlString +="fk_deltaComp_id int default 8888);";
			s.executeUpdate(sqlString);

			sqlString="CREATE TABLE protein (acc varchar(40) primary key,";
			sqlString +="description varchar(255) NOT NULL default '-',";
			sqlString +="http varchar(255) NOT NULL default '-',";  //TODO put in a google-type default search here.
			sqlString +="mass FLOAT default NULL)";
			s.executeUpdate(sqlString);
			
			sqlString="CREATE TABLE fingerprint (fingerprint_id int primary key,";
			sqlString += "exp_mass  FLOAT NOT NULL,";
			sqlString += "height    FLOAT NOT NULL,";
			sqlString += "base      FLOAT NOT NULL,";
			sqlString += "sigma  FLOAT NOT NULL,";
			sqlString += "numUnder  FLOAT NOT NULL,";
			sqlString += "xy        VARCHAR(5000) NOT NULL,";
			sqlString += "error     FLOAT   NOT NULL)";
			s.executeUpdate(sqlString);

			//warning: fk_deltaComp_id from above is not used in the insert statements below ...
			UniModDatabase uModDb= new UniModDatabase("trash");//argument not needed anymore
			ArrayList<Modification> mList = uModDb.getModifications();
			uModDb=null;
			for(int i=0;i<mList.size();i++){
				PreparedStatement ps=conn.prepareStatement("INSERT INTO multimod " +
						"(multimod_id," +
						"full_name," +
						"code_name," +
						"mono_mass," +
						"avge_mass," +
						"composition" +
						//"date_time_posted," +
						//"date_time_modified" +
						") "+
				" VALUES (?,?,?,?,?,?)");
				ps.setInt(1, mList.get(i).unimodID);
				ps.setString(2, mList.get(i).fullName);
				ps.setString(3, mList.get(i).shortName);
				ps.setDouble(4, mList.get(i).monoisotopic);
				ps.setDouble(5, mList.get(i).average);
				ps.setString(6, mList.get(i).composition);
				ps.executeUpdate();
			}
			uModDb=null;

			s.executeUpdate("DROP SEQUENCE IF EXISTS recordSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS spectrumSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS deltaMassSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS peptideSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS deltaCompSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS experimentSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS specnetSeq");
			s.executeUpdate("DROP SEQUENCE IF EXISTS proteinSeq");

			sqlString="CREATE SEQUENCE recordSeq MINVALUE 0;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE spectrumSeq MINVALUE 0;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE deltaMassSeq MINVALUE 1;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE peptideSeq MINVALUE 0;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE deltaCompSeq MINVALUE 0;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE experimentSeq MINVALUE 0;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE specnetSeq MINVALUE 0;";
			s.executeUpdate(sqlString);
			sqlString="CREATE SEQUENCE proteinSeq MINVALUE 0;";
			s.executeUpdate(sqlString);

			//first, we take the positive masses ...			
			ResultSet result = s.executeQuery("SELECT mono_mass,composition,multimod_id from multimod where mono_mass >0");
			while (result.next()) {
				double tmpMono = result.getDouble(1);
				String tmpComp = result.getString(2);
				int tmpMultimod_id = result.getInt(3);

				Statement s2 = conn.createStatement ();
				ResultSet result2 = s2.executeQuery("SELECT deltaComp_id,deltaCompAbsMass from deltaComp WHERE deltaCompAbsMass = "+Math.abs(tmpMono));
				if (result2.next()) { //exists ...
					int tmpDeltaComp_id=result2.getInt(1);
					s2.executeUpdate("UPDATE multimod SET fk_deltaComp_id = "+tmpDeltaComp_id+" where multimod_id = "+tmpMultimod_id);
				}
				else {
					int deltaComp_id=getSequencer("deltaCompSeq");
					s2.executeUpdate("INSERT INTO deltaComp (deltaComp_id,deltaCompMass,deltaCompAbsMass,deltaComposition,isUniMod) VALUES ("+deltaComp_id+","+tmpMono+","+Math.abs(tmpMono)+",'"+tmpComp+"','true')");
					s2.executeUpdate("UPDATE multimod SET fk_deltaComp_id = "+deltaComp_id+" where multimod_id = "+tmpMultimod_id);
				}
				s2.close();
			}

			result = s.executeQuery("SELECT mono_mass,composition,multimod_id from multimod where mono_mass <=0");
			while (result.next()) {
				double tmpMono = result.getDouble(1);
				String tmpComp = result.getString(2);
				int tmpMultimod_id = result.getInt(3);

				Statement s2 = conn.createStatement ();
				ResultSet result2 = s2.executeQuery("SELECT deltaComp_id,deltaCompAbsMass from deltaComp WHERE deltaCompAbsMass = "+Math.abs(tmpMono));
				if (result2.next()) { //exists ...
					int tmpDeltaComp_id=result2.getInt(1);
					s2.executeUpdate("UPDATE multimod SET fk_deltaComp_id = "+tmpDeltaComp_id+" where multimod_id = "+tmpMultimod_id);
				}
				else {
					int deltaComp_id=getSequencer("deltaCompSeq");
					s2.executeUpdate("INSERT INTO deltaComp (deltaComp_id,deltaCompMass,deltaCompAbsMass,deltaComposition,isUniMod) VALUES ("+deltaComp_id+","+tmpMono+","+Math.abs(tmpMono)+",'"+tmpComp+"','true')");
					s2.executeUpdate("UPDATE multimod SET fk_deltaComp_id = "+deltaComp_id+" where multimod_id = "+tmpMultimod_id);
				}
				s2.close();
			}

			sqlString="COMMIT;";
			s.executeUpdate(sqlString);
			logger.info("DeltaMassBase:Database created:all fine");

			return true;
		} catch(Exception ex) {
			logger.error("DeltaMassBase:SYSTEM_ERROR:could not create deltaMasses:" +
					ex.getLocalizedMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean createDatabase(String nameToBeCreated) {
		logger.info("createDatabase: start");
		try {
			logger.info("Trying to get a Connection");
			Connection conn = getConnection();
			logger.info("Connected. Trying to create databaseName="+nameToBeCreated);
			Statement s = conn.createStatement();
            s.executeUpdate("CREATE DATABASE \""+nameToBeCreated+"\" ENCODING='UTF8'");
            logger.info("SQL:"+s.toString());
			conn.close();
			return true;
		}catch(Exception e){
			logger.error("createDatabase:"+e.toString());
			logger.error("createDatabase:"+e);
			return false;
		}
		
	}
	public static boolean isFingerPrintTableExists() {
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("select count(*) from information_schema.tables where table_name = 'fingerprint'");
		    ResultSet rs = pst.executeQuery();
		    if (rs.next())
		    {
		    	if(rs.getInt(1) != 0) // If table exists
		    	{
		    		return true;
		    	}
		    }
		    rs.close();
		    pst.close();
		    conn.close();
		} catch (SQLException e) {
			logger.error("PeptideNet:isFingerPrintTableExists:"+e.toString());
		}
		return false;
	}
	
	
}
