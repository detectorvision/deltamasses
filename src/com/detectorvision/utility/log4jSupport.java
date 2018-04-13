/* $Id: log4jSupport.java 160 2008-07-06 07:50:18Z jari $ */

package com.detectorvision.utility;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class log4jSupport {

	static Logger
		logger = Logger.getLogger("com.detectorvision.deltaMasses.DeltaMasses");

	public static String getLogFile(String appender) {
		Appender a=logger.getAppender(appender);
		if ((a!=null) && (a instanceof FileAppender))
			return ((FileAppender)a).getFile();
		return null;
	}

	public static String getLogFile(Logger alogger, String appender) {
		Appender a=alogger.getAppender(appender);
		if ((a!=null) && (a instanceof FileAppender))
			return ((FileAppender)a).getFile();
		return null;
	}

}
