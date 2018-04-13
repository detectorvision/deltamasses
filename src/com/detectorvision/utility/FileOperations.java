package com.detectorvision.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class FileOperations  {
	public static void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Changes the Filename of File to File.anEnding
	 * Returns true if OK false otherwise. anEnding must have length 4. Otherwise, 
	 * function returns false.
	 * 
	 * @param  File		aFile which names will be appended with param 2
	 * @param  String	anEnding Ending that will be appended to aFile. Result: aFile.anEnding
	 * @return boolean	true if renaming ok, false otherwise.
	 */
	public static boolean addFileEnding(File aFile, String anEnding){
		if(anEnding.length()!=4){
			System.err.println("addFileEnding error. length is not 4:"+anEnding.toString());
			return false;
		}
		if(! aFile.exists() || ! aFile.canWrite()){
			System.err.println("addFileEnding problems with file:"+aFile.toString());
			return false;
		}
		System.out.println("renaming "+aFile.toString() +" to "+aFile.toString()+ "."+ anEnding);
		File file2 = new File(aFile.toString()+ "."+ anEnding);
		// Rename file (or directory)
		boolean success = aFile.renameTo(file2);
		if (!success) {return false;}
		return true;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		 if(!destFile.exists()) {
		  destFile.createNewFile();
		 }

		 FileChannel source = null;
		 FileChannel destination = null;
		 try {
		  source = new FileInputStream(sourceFile).getChannel();
		  destination = new FileOutputStream(destFile).getChannel();
		  destination.transferFrom(source, 0, source.size());
		 }
		 finally {
		  if(source != null) {
		   source.close();
		  }
		  if(destination != null) {
		   destination.close();
		  }
		}
	}
	
	public static boolean mergeDoneFiles(String aTargetDir,String aTargetFile){
		boolean allFine=true;
		ArrayList<File> mergedFiles = new ArrayList<File>();
		
		System.out.println("mergeDoneFiles:"+aTargetDir+" to:"+aTargetFile);
		System.out.println("merging "+aTargetDir+" to "+aTargetFile);
		try{
			File targetFile=new File(aTargetFile);
			if(targetFile.exists()){System.err.println("target file exists. Exit.");System.exit(1);}
			File targetDir=new File(aTargetDir);
			File[] listing = targetDir.listFiles();
			for(int i=0;i<listing.length;i++){
				if(listing[i].isFile()){
					if(listing[i].toString().endsWith(".done")){
						//System.out.println("merge loop "+i+" "+listing[i].toString());
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return false;
						}
						try{//append listing[i] to targetFile

							BufferedWriter out = new BufferedWriter(new FileWriter(aTargetFile, true));
							BufferedReader in = new BufferedReader(new FileReader(listing[i]));
							String str;
							while ((str = in.readLine()) != null) {
								out.write(str+"\n");
							}
							in.close();
							out.close();
							mergedFiles.add(listing[i]);
						}
						catch(Exception ex){
							System.err.println("cannot append:"+ex.toString());
							System.exit(1);
							//return false;
						}
					}
				}
			}
			FileOperations.addFileEnding(targetFile, "done");	
		}catch(Exception ex){
			System.err.println("mergeOutFiles error:exit:"+ex.toString());
			System.exit(1);
		}
		
		for(int i=0;i<mergedFiles.size();i++){
		try{
			mergedFiles.get(i).delete();
		}catch(Exception ex){
			System.err.println("mergeDoneFiles:cannot delete:"+ex.toString());
			System.exit(1);
		}	
			
		}
		System.out.println("mergeOutFiles returns ok");
		return allFine;
	}
}
