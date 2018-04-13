/* $Id: deltaMassBase2Cluster.java 267 2009-02-28 16:57:56Z frank $ */

package com.detectorvision.deltaCluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.detectorvision.utility.DeltaMassBase;

import org.apache.log4j.Logger;


public class deltaMassBase2Cluster {

	public  static String SPECNET_FILE="automation/cluster/deltaCluster.peptideNet";

	// Logging with log4j
	static Logger logger = Logger.getLogger(deltaMassBase2Cluster.class.getName());

	static void import2DMB(String specnet_file_to_import){

		System.out.println("importing data to database");
		//		if ( ) {
		//			logger.fatal("no connection to DB");
		//			System.exit(1);
		//		}
		File tmpFile = new File(specnet_file_to_import);
		if(!tmpFile.exists()){
			System.err.println("Trying to import pairs to deltaMassBase not possible because the file");
			System.err.println(specnet_file_to_import);
			System.err.println("does not exist.");
			System.err.println("All *.out files need to be concatenated into the file deltaMasses.pairs");
			System.err.println("Hanging up, sorry.");
			System.exit(1);
		}
		System.out.println("now getConnection");
		Connection conn = DeltaMassBase.getConnection();
		System.out.println("getConnection OK");
		Statement s;
		try {
			s = conn.createStatement ();	
			String sqlQuery="BEGIN;";
			s.executeUpdate(sqlQuery);
			System.out.println("beginning transaction");
			PreparedStatement p = conn.prepareStatement("delete from deltaMass");
			p.executeUpdate();
			System.out.println("delete * from deltaMass");
			//			add all pairs from file to DB-----------------------------------------------------
			try {
				logger.debug("trying to import pairs from: " + specnet_file_to_import);
				System.out.println("putting pairs into DB");
				BufferedReader in = new BufferedReader(new FileReader(specnet_file_to_import));
				String inLine;
				int countpairs=0;
				while ((inLine = in.readLine()) != null) {
					countpairs++;
					//System.out.println("new line:"+inLine);
					String[] words= inLine.split("\t");
					if(words.length!=6){
						System.err.println("import2DB::pair import error::number of words: " +
								words.length + " " + inLine);
						logger.fatal("import2DB::pair import error::number of words: " +
								words.length + " " + inLine);
						System.exit(1);
					}
					int deltaMass_id=DMBgetSequencer(conn, "deltamassseq");
					PreparedStatement ps=conn.prepareStatement("INSERT INTO deltaMass " +
							"(deltaMass_id , dm , p , sim , fk_l_spectrum_id , fk_h_spectrum_id, mass_light) " +
					" VALUES (?,?,?,?,?,?,?)");
					//System.out.println(ps.toString());
					ps.setInt(1, deltaMass_id);
					ps.setDouble(2, Double.parseDouble(words[3]));//deltaMass
					ps.setDouble(3, Double.parseDouble(words[3]));//p
					ps.setDouble(4, Double.parseDouble(words[3]));//sim
					ps.setInt(5, Integer.parseInt(words[0]));//fk_l_spectrum
					ps.setInt(6, Integer.parseInt(words[1]));//fk_h_spectrum
					ps.setDouble(7, Double.parseDouble("111.11"));//mass_light
					if (deltaMass_id%1000==0)
						logger.info("Importing deltaMass_id " + deltaMass_id +
						" please wait ...");
					ps.executeUpdate();

				}
				System.out.println("added pairs to deltamasses:"+countpairs);
				in.close();
			}catch(IOException ioex){
				System.err.println(""+ioex.toString());
				logger.fatal("DeltaMassBase2Cluster:import pairs:IOException:" +
						ioex.toString());	
				System.exit(1);
			}catch(Exception EX){
				System.err.println("EX:"+EX.toString());
			}
			sqlQuery="COMMIT;";
			s.executeUpdate(sqlQuery);
			conn.close();
			System.out.println("Import finished. All OK.");
			logger.debug("Import finished. All OK.");
		}catch(SQLException ex){
			logger.fatal("checkValidity:SQLException: " + ex.toString());	
			System.exit(1);
		}
		logger.debug("addAll:-----ALL_OK-----");
	}

	static void checkValidity(){
		//check all kind of things which might be wrong here ...
		logger.debug("checking consistency of input file / deltaMassBase");
		//TODO_FATAL
		//		if (!DMBConnectionOk()) {
		//			logger.fatal("no connection to DB");
		//			System.exit(1);
		//		}
	}

	public static int dump2Cluster(String aDir){
		System.out.println("dump2Cluster running");
		logger.debug("Trying to get a db connection");
		//configuration info----------------------------------------------------
		int BLOCKSIZE=ClusterConstants.BLOCKSIZE;
		String CLUSTER_DIR=aDir;
		aDir+="/";
		String CLUSTER_DRIVER=CLUSTER_DIR + "/cluster_driver.bat";
		//some initial contromessagels------------------------------------------------
		{
			URL clusterDir=null;
			try{
				clusterDir = new File(CLUSTER_DIR).toURI().toURL();
			} catch (MalformedURLException e1) {
				logger.fatal("Malformed URL exception : " +
						e1.getLocalizedMessage());
				System.out.println("Malformed URL exception: " +
						e1.getLocalizedMessage());
				System.exit(1);
			}
			System.out.println("clusterDir is"+clusterDir.toString());

			if(false){
				//TODO check if the directory is empty - if yes, exit with warning
				System.out.println("cluster dir does not exist:"+CLUSTER_DIR);
				logger.fatal("cluster dir does not exist / is not a directory: " +
						CLUSTER_DIR);
				System.exit(1);
			}
			//TODO_FATAL
			//			if(!DMBConnectionOk()){
			//				System.out.println("cannot connect to datbase.");
			//				logger.fatal("Cannot connect to Database. Nothing exported. Finished.");
			//				System.exit(1);}
		}


		//TODO
		//check if automation/robot directory is empty. if not, exit with a warning+instruction
		Connection conn = DeltaMassBase.getConnection();
		System.out.println("starting block loop");
		Statement s;
		int blockNum=0;
		try {
			s = conn.createStatement ();		

			
			int specInBlock=1;
			int numSpecsTotal=0;

			String blockFile=CLUSTER_DIR + "/block_"+blockNum+".data";
			BufferedWriter blockOut = new BufferedWriter(new FileWriter(blockFile,false));
			System.out.println("writing to "+blockFile);

			for (int thisRecord=0;thisRecord<350;thisRecord++){
				//ResultSet result = s.executeQuery("SELECT record_id,spectrum_id,precursorMass,charge,mzBase64,signalBase64,experiment_id,fk_experiment_id from spectrum,record,experiment"+
				//		" WHERE fk_record_id=record_id AND fk_experiment_id=experiment_id AND record_id="+thisRecord);
				ResultSet result = s.executeQuery("SELECT record_id,spectrum_id,precursorMass,charge,mzBase64,signalBase64 from spectrum,record"+
						" WHERE fk_record_id=record_id AND record_id="+thisRecord);
				while (result.next()) {
					numSpecsTotal++;
					String tmpMz=result.getString(5);
					String tmpSig=result.getString(6);
					int experiment_id=0;//result.getInt(7);
					//block_num record_id spectrum_id precursor_mass charge \n mz \n signal
					String outString = ""+blockNum+"\t"+ result.getInt(1)+"\t" +result.getInt(2)+"\t"+  result.getDouble(3)+"\t"+result.getInt(4)+"\t"+experiment_id+"\n"+tmpMz + "\n" + tmpSig;
					blockOut.write(outString+"\n");

					if(specInBlock==BLOCKSIZE){
						blockNum++;specInBlock=0;
						blockOut.close();
						if (blockNum>100) {
							logger.fatal("number of blocks bigger than 100, security exit");
							System.exit(1);
						}
						blockFile=CLUSTER_DIR + "\\block_"+blockNum+".data";
						System.out.println("opening blockFile:"+blockFile);
						blockOut = new BufferedWriter(new FileWriter(blockFile, false));
						logger.debug("writing "+blockFile);
					}

					specInBlock++;
				}
			}
			System.out.println("exported "+numSpecsTotal+" spectra, number of blocks-1:"+blockNum);
			blockOut.close();

			blockNum+=1;//counting wrong otherwise

			//write the driver---------------------------------------
			int numDrivers=getNumDrivers();
			int blocksPerDriver=1+(int)((blockNum+(blockNum*(blockNum-1)/2))/numDrivers);
			if(blocksPerDriver==0)blocksPerDriver=1;
			logger.debug("Blocks per driver: " + blocksPerDriver);
			logger.debug("Block number: " + blockNum);

			String driverFile=CLUSTER_DRIVER;

			int driverIndex=0;//number on the driver file name
			driverFile=CLUSTER_DRIVER.replaceAll(".bat", "_")+driverIndex+".bat";
			BufferedWriter driverOut = new BufferedWriter(new FileWriter(driverFile, false));



			int countWritten=0;
			for(int i=0;i<=blockNum;i++){
				for(int j=i;j< blockNum;j++){
					driverOut.write("java -jar deltaCluster.jar block_"+i+".data block_"+j+".data;\n");
					countWritten++;
					if(countWritten%blocksPerDriver==0){//next driver
						driverIndex++;
						driverOut.close();
						if(   (blockNum+(blockNum*(blockNum-1)/2)) >= countWritten){
							driverFile=CLUSTER_DRIVER.replaceAll(".sh", "_")+driverIndex+".sh";
							driverOut = new BufferedWriter(new FileWriter(driverFile, false));
						}
					}

				}	
			}
			driverOut.close();
			//TODO pack all files into one .zip file

			logger.debug("all done and well");
			int numPairs=numSpecsTotal*(numSpecsTotal-1)/2;
			logger.info("prepared " + numSpecsTotal + " spectra resulting in " +
					numPairs + " pairs");
		} catch (SQLException e) {
			System.out.println(e.toString());
			logger.error(e.toString());
			System.exit(1);
		}
		catch (IOException e2){
			System.out.println(e2.toString());
			logger.error(e2.toString());
			System.exit(1);
		}
return(blockNum-1);//maxBlock number is to be returned, not the number of blocks.

	}

	public static int getNumDrivers(){
		return 4;
		}

	//TODO_MEDIUM get rid of this method in this class, belongs to com.detectorvision.deltaMassBase.
	public static int DMBgetSequencer(Connection conn, String whatSequencer){
		int retval=0;
		try {
			Statement s = conn.createStatement();

			s.executeQuery("SELECT nextval('"+whatSequencer+"');");
			ResultSet rs = s.getResultSet();
			rs.next();
			retval=rs.getInt(1);
		} catch (SQLException e) {
			logger.fatal("SQLException:SYSTEM_EXIT: " + e.getLocalizedMessage());
			System.exit(0);
		}
		return retval;
	}

}

