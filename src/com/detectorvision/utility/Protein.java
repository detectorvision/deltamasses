/* $Id: PeptideID.java 71 2008-02-06 21:53:32Z jari $ */

package com.detectorvision.utility;

/**
 * Protein Information
 * 
 * @author frankp
 * v0.1 20081130 frankp  added attributes
 */
public class Protein {
	public String 	acc;          //accession, max length 40 characters (db limit)
	public String   description;  //description, max length 255 characters (db limit)
	public String   http;         //contains a http-link to protein information. 
		                          //max length 255 characters
	public double 	mass;         //mass of protein in Dalton

	public Protein() {
		this.http="";
		this.acc="";
		this.description="";
		this.mass=0.0;
	}
}