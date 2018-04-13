package com.detectorvision.utility;

import java.io.IOException;

public class currentDirectory {
	public String getCurrentDirectory(){
		String bla="";
		 java.io.File f = new java.io.File(".");
		 try {
			bla=f.getCanonicalPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return bla;
		}
		return bla;
	}
}
