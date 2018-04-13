/* $Id: Footer.java 295 2010-05-13 12:00:11Z frank $ */

package com.detectorvision.utility.pdf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.detectorvision.deltaMasses.RecordTableHandler;

public class Footer {
	static Logger logger = Logger.getLogger(RecordTableHandler.class.getName());

	public static ArrayList<String> getInfo() {
		// returns an array-string of length four as printed
		// on the bottom of the pdfs.
		ArrayList<String> footerArray= new ArrayList<String>();
		footerArray.add("Detectorvision AG");
		footerArray.add("Zurich");
		footerArray.add("Switzerland");
		footerArray.add("www.detectorvision.ch");
		String tmp1="",tmp2="",tmp3="",tmp4="";
		try {
			FileReader DMBConfFileReader = new FileReader("bin/deltaMassBase_active_db.config.txt");
			BufferedReader buff = new BufferedReader(DMBConfFileReader);
			String line;
			
			while(true){
				line = buff.readLine();
				if (line == null) {
					// EOF reached. Bail out of the while-loop.
					break;
				}
				if (line.startsWith("FOOTER_FIELD_")) {
					String[] words = line.split("\\s+");
					if(line.startsWith("FOOTER_FIELD_1=")){tmp1="";if(words.length>1)for(int i=1;i<words.length;i++)tmp1+=words[i]+" ";}
					if(line.startsWith("FOOTER_FIELD_2=")){tmp2="";if(words.length>1)for(int i=1;i<words.length;i++)tmp2+=words[i]+" ";}
					if(line.startsWith("FOOTER_FIELD_3=")){tmp3="";if(words.length>1)for(int i=1;i<words.length;i++)tmp3+=words[i]+" ";}
					if(line.startsWith("FOOTER_FIELD_4=")){tmp4="";if(words.length>1)for(int i=1;i<words.length;i++)tmp4+=words[i]+" ";}
				}
			}
			if(tmp1.length()>0 && tmp2.length()>0 && tmp3.length()>0 && tmp4.length()>0){
				footerArray.clear();
				footerArray.add(tmp1);
				footerArray.add(tmp2);
				footerArray.add(tmp3);
				footerArray.add(tmp4);
			}
		} catch (FileNotFoundException e1) {
			logger.error("getFooterInfo:FileNotFoundException:"+e1.toString());
			e1.printStackTrace();
		} catch (IOException e2) {
 			 logger.error("getFooterInfo:"+e2.toString());
			e2.printStackTrace();
		}
		catch (Exception e){
 			logger.error("DeltaUtils:getFooterInfo:Exception:"+e.getMessage());
			e.printStackTrace();
		}
		logger.info("Footer.java returns:"+footerArray.toString());
		return footerArray;
	}
}
