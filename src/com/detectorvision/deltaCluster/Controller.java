package com.detectorvision.deltaCluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.detectorvision.utility.FileOperations;
import com.detectorvision.utility.currentDirectory;
import org.apache.log4j.Logger;

public class Controller extends Thread {

	static Logger logger = Logger.getLogger(Controller.class.getName());
	public static int  cluster_id;
	public static File clusterBase=null;
	public static File clusterDir =null;
	public static File outputFile=null;
	public static int maxBlock=0;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Controller control= new Controller(1);
		control.cleanClusterDir();
		control.getParameters();
		control.exportFromDeltaMassBase();

		//TODO threading for block below
		for(int b1=0;b1<=maxBlock;b1++){
			for(int b2=0;b2<=b1;b2++){
				String block1=clusterDir.toString()+"\\block_"+b1+".data";
				String block2=clusterDir.toString()+"\\block_"+b2+".data";
				String tmpOutFile=clusterDir.toString()+"\\result_"+b1+"_"+b2+".out";
				run(	
						block1, 
						block2,
						tmpOutFile,
						clusterDir.toString());
			}	
		}		
		
		control.checkAfterRun();
		control.mergeResultFiles();
		System.out.println("starting import2deltaMassBase");
		control.import2deltaMassBase();
		//control.zipClusterDir();
		//call peptideNet Calculation
		System.out.println("deltaClusterOK");
	}

	
	private boolean zipClusterDir(){
		//doesnt work yet.
		System.out.println("zipClusterDir active:"+clusterDir.toString());
		try {
			ClusterCompress.zipDirectory(clusterDir.toString(), clusterDir.toString()+ ".zip");
		} catch (IllegalArgumentException e) {
			System.err.println("zipClsuterDir error:"+e.toString());
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("zipClsuterDir error:"+e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return true;
	}
	
	private boolean mergeResultFiles() {
		if(com.detectorvision.utility.FileOperations.mergeDoneFiles(clusterDir.toString(), outputFile.toString())){
			outputFile= new File(outputFile.toString()+".done");
			if(! outputFile.exists()){
				System.err.println("outputFile does not exist:"+outputFile.toString());
				System.exit(1);
			}
		}
		else{
			System.out.println("merging of files okay.");
		}
		return true;
	}

	//constructor
	Controller(int aCluster_id){
		Controller.cluster_id=aCluster_id;
		try{
			currentDirectory currDir = new currentDirectory();
			System.out.println("starting in :"+currDir.getCurrentDirectory());

			//baseDir of the Cluster	
			//TODO put into preferences file
			String clusterBaseDirectory="E:/clusterBase/";

			//never change below
			clusterDir=new File(clusterBaseDirectory+"deltaCluster_00"+cluster_id+"/");
			System.out.println("cluster_dir:"+clusterDir.toString());
			clusterBase = new File(clusterBaseDirectory);
			if(!clusterDirsOK()){
				System.err.println("root dir not ok. exit.");
				System.exit(1);
			}
			outputFile=new File(clusterDir.toString()+"\\cluster_"+cluster_id+"_results.out");
			System.out.println("outputFile:"+outputFile.toString());

			//get the status of this cluster
			System.out.println("controler ok");
		}catch(Exception ex){
			System.err.println("Controller constructor failes:"+ex.toString());
		}
	}

	public boolean clusterDirsOK(){
		if(!clusterBase.toString().contains("clusterBase")){
			System.err.println("clusterBase does not contain String clusterBase"+clusterBase.toString());
			return false;
		}
		if(!clusterBase.exists()){
			System.err.println("clusterBase does not exist:"+clusterBase.toString());
			return false;
		}
		if(! (clusterBase.canRead() && clusterBase.canWrite()) ){
			System.err.println("clusterBase does not exist:"+clusterBase.toString());
			return false;
		}
		if(!clusterDir.toString().contains("clusterBase")){
			System.err.println("clusterBase does not contain String clusterBase"+clusterBase.toString());
			return false;
		}
		if(!clusterDir.exists()){
			System.err.println("clusterBase does not exist:"+clusterBase.toString());
			return false;
		}
		if(! (clusterDir.canRead() && clusterBase.canWrite()) ){
			System.err.println("clusterBase does not exist:"+clusterBase.toString());
			return false;
		}
		return true;
	}

	private boolean cleanClusterDir(){
		boolean allFine=true;
		//security control
		if(!clusterDir.toString().contains("clusterBase\\deltaCluster")){
			System.err.println("cleanClusterDir:clusterDir error:"+clusterDir.toString());
			System.exit(1);
		}
		
		File[] listing = clusterDir.listFiles();
		for(int i=0;i<listing.length;i++){
			if(listing[i].isFile()){
				System.out.println("deleting:"+listing[i].toString());
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
				try{listing[i].delete();}
				catch(Exception ex){
					System.err.println("cannot delete:"+ex.toString());
					return false;
				}
			}
		}
		System.out.println("controller.clean returns ok");
		return allFine;
	}
/**
 * Copies the default_parameter_settings to the cluster_dir.
 * 
 * @param  none
 * @return boolean	true if operation succesful, false if not. In most cases, an error will result in a system.exit.
 */
	private boolean getParameters() {
		File parameterSettings = new File("default_parameter_settings");
		if(! parameterSettings.exists()){
			System.err.println("parameter-file not found:"+parameterSettings.getAbsolutePath().toString());
			System.exit(1);
		}
		File target = new File(clusterDir+"\\default_parameter_settings");
		if( target.exists()){
			System.err.println("target file exists"+target.getAbsolutePath().toString());
			System.exit(1);
		}
		try{
			FileOperations.copy(parameterSettings,target);
		}catch(IOException ex){
			System.err.println("could not copy:"+ex.toString());
			System.exit(1);
		}
		return true;
	}

	private boolean exportFromDeltaMassBase(){
		maxBlock=deltaMassBase2Cluster.dump2Cluster(clusterDir.toString());
		if(maxBlock<0){
			System.err.println("blockNumber error:"+maxBlock);
			System.exit(1);
		}
		System.out.println("maxBlock:"+maxBlock);
		return true;
	}

	private boolean checkAfterRun() {
		//count number of *done files in clusterDir.
		int filesDone=0;
		int datFiles=0;
		int toDoFiles=0;
		
		try{
			if(!clusterDir.exists()){System.err.println("clusterDir does not exist. Exit."+clusterDir.toString());System.exit(1);}
			File[] listing = clusterDir.listFiles();
			for(int i=0;i<listing.length;i++){
				if(listing[i].toString().endsWith("done")){
					filesDone++;
				}
				if(listing[i].toString().endsWith("data")){
					datFiles++;
				}	
			}
			toDoFiles=datFiles + (datFiles*(datFiles-1)/2);
			System.out.println("datFiles:\t"+datFiles);
			System.out.println("filesDone:\t"+filesDone);
			System.out.println("toDoFiles:\t"+toDoFiles);
			if(toDoFiles!=filesDone){
				System.err.println("not finished yet.");
				System.exit(1);
			}
		}catch(Exception ex){
			System.err.println("checkAfterRun Error:"+ex.toString());
		}
		
		
		
		boolean allFine=true;	
		return allFine;
	}

	private boolean import2deltaMassBase()
	{
		deltaMassBase2Cluster.import2DMB(outputFile.toString());
		return true;	
	}
	
	
	public static void run(String file1,String file2,String outFileTarget,String clusterDir){
		
		File doneFile = new File(outFileTarget +".done");
	    if(doneFile.exists()){
	    	System.out.println("results allready computed:"+outFileTarget);
	    	return;
	    }
	
		boolean allOK=true;
	    check_arguments(file1,file2);		

		File inFileOne = new File(file1);
		File inFileTwo = new File(file2);

		File outFile = new File(outFileTarget);
		File logFile = new File(outFileTarget+".log");
		
		if(outFile.exists()){
			outFile.delete();
			clusterLog("outFile :"+outFile.toString() + "------------------does exist, deleting it",false);
			//System.exit(1);
		}
		//System.out.println("now processing\nin1: "+inFileOne + "\nin2: "+inFileTwo);//+ "\nout:"+outFile.toString()+ "\nlog:"+logFile.toString());
		ClusterData   clusterData = new ClusterData();
		System.out.println("================================================================================");
		clusterData.readInFile(inFileOne.toString(),1);
		clusterData.readInFile(inFileTwo.toString(),2);

		boolean block1EqualBlock2=false;
		if(inFileOne.toString().equals(inFileTwo.toString())){
			System.out.println("equal blocks:"+inFileOne.toString()+" "+inFileTwo.toString());
			block1EqualBlock2=true;
		}

		clusterData.searchForPtm(block1EqualBlock2);
		if(allOK){
			//System.out.println("all fine, exporting to:"+outFile);
			clusterData.printTofile(outFile.toString());
			if(!FileOperations.addFileEnding(outFile, "done")){
				System.err.println("cannot addFileEnding done to:"+outFile.toString());
				System.exit(1);
			}
		}
	}
	
	
	static String check_arguments(String file1, String file2){
		File inFileOne = new File(file1);
		File inFileTwo = new File(file2);
		if(!inFileOne.toString().contains("block_")){
			clusterLog("inFileOne has wrong file beginning:"+inFileOne.toString(),true);
		}
		if(!inFileTwo.toString().contains("block_")){
			clusterLog("inFileTwo has wrong file beginning:"+inFileTwo.toString(),true);
		}
		if(!inFileOne.toString().endsWith(".data")){
			clusterLog("inFileOne has wrong file ending:"+inFileOne.toString(),true);
		}
		if(!inFileTwo.toString().contains(".data")){//TODO put a .endsWith in here
			clusterLog("inFileTwo has wrong file ending:"+inFileTwo.toString()+"+++",true);
		}
		if(!inFileOne.exists()){
			clusterLog("inFileOne "+inFileOne.toString() + " does not exist",true);
		}
		if(!inFileOne.canRead()){
			clusterLog("inFileOne "+inFileOne.toString() + " reading failed",true);
		}
		if(!inFileTwo.exists()){
			clusterLog("inFileTwo "+inFileTwo.toString() + " does not exist",true);
		}
		if(!inFileTwo.canRead()){
			clusterLog("inFileTwo "+inFileTwo.toString() + " reading failed",true);
		}
		String tmp1=inFileOne.toString().replace(".data", "");
		String tmp1b=tmp1.replace("block", "result");//stupid replace function ....
		String tmp2=inFileTwo.toString().replace(".data", "");
		String tmp2b=tmp2.replace("block", "");
		return(tmp1b+tmp2b);
	}
	static void clusterLog(String message, boolean fatal){
		//TODO improve this 
		System.err.println("clusterLog Error:"+message);
		if(fatal){System.exit(1);}
		return;
	}
	
}
