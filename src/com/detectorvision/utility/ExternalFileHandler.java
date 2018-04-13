/* $Id: ExternalFileHandler.java 245 2009-01-02 15:53:33Z frank $ */

package com.detectorvision.utility;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

public class ExternalFileHandler {

	/**
		 Start an external file reader. Returns zero value on success,
		 non-zero value otherwise according to the list below

		 Value: Explanation

		 1: When an IllegalArgumentException or NullPointerException is
		 catched then the specified file doesn't exist

		 2: When an UnsupportedOperationException is catched then
		 Desktop.Action.OPEN action is not supported by the platform.

		 3: When an IOException is catched the the specified file has no
		 associated application or the associated application fails to be
		 launched

		 -1: For all other caught exceptions (such as SecurityException
      and URISyntaxException).

		 See Java documentation for java.awt.Desktop for more information.
	 */
	
	static Logger logger = Logger.getLogger(ExternalFileHandler.class.getName());
	
	public static int open(File file) {
		logger.info("ExternalFileHandler:open:"+file.getAbsolutePath());
		return privateOpen(file);
	}

	public static int open(String path) {
		try {			
			File file = new File(path);
			logger.info("open(String) absolute path: "+file.getAbsolutePath());
			return privateOpen(file);
		} catch (NullPointerException e) {
			logger.error("ExternalFileHandler:open:String:"+e.toString());
			e.printStackTrace();
			return 1;
		}
	}

	private static int privateOpen(File file) {
		try{
			String targetFile=file.getAbsoluteFile().toURI().toString();
			Process p = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "start " + targetFile });
			InputStream stdOut = p.getInputStream();
			if(stdOut.read() != -1){
				logger.error("ExternalFileHandler:privateOpen: Acrobat Reader not installled or no file association for pdf");
				return -1;
			}
			else{//worked ok
			return 0;
			}
			
		}catch(Exception exception ){
			logger.info("privateOpen file has an exception. OK if not a Windows OS:"+exception.toString());
			 try {
				    //code below should only run on non-windows machines.
				    //bad news - this does not work on some windows machines....
					//see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6457572
					//see http://forums.sun.com/thread.jspa?threadID=682870&messageID=3979401
					Desktop.getDesktop().open(file);
				} catch (IllegalArgumentException e) {
					logger.error("ExternalFileHandler: " + e.getLocalizedMessage());
					return 1;
				} catch (NullPointerException e) {
					logger.error("ExternalFileHandler: " + e.getLocalizedMessage());
					return 1;
				} catch (UnsupportedOperationException e) {
					logger.error("ExternalFileHandler: " + e.getLocalizedMessage());
					return 2;
				} catch (IOException e) {
					logger.error("ExternalFileHandler: " + e.getLocalizedMessage());
					return 3;
				} catch (Exception e) {
					logger.error("ExternalFileHandler: " + e.getLocalizedMessage());
					e.printStackTrace();
					return -1;
				}
				return 0;
		}
		} 
}


