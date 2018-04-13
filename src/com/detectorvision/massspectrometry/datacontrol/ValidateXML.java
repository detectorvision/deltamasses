/* $Id: ValidateXML.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.massspectrometry.datacontrol;

import com.detectorvision.massspectrometry.analyzation.DeltaMassesScore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ValidateXML {

	// Logging with log4j
	static Logger logger = Logger.getLogger(ValidateXML.class.getName());

	public static boolean validate(String document, String schemaString) throws IOException {

		File documentFile = new File(document);  
		File schemaFile = new File(schemaString);   

		// Get a parser to parse W3C schemas.  Note use of javax.xml package
		// This package contains just one class of constants.
		SchemaFactory factory =
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Now parse the schema file to create a Schema object
		Schema schema = null;
		try { schema = factory.newSchema(schemaFile); }
		catch(SAXException e) { fail(e); return false;}

		// Get a Validator object from the Schema.
		Validator validator = schema.newValidator();

		// Get a SAXSource object for the document
		// We could use a DOMSource here as well
		SAXSource source =
			new SAXSource(new InputSource(new FileReader(documentFile)));

		// Now validate the document
		try { validator.validate(source); }
		catch(SAXException e) { 
			fail(e);
			return false;
		}
		logger.info("Document is valid");
		return true;
	}

	static void fail(SAXException e) {
		if (e instanceof SAXParseException) {
			SAXParseException spe = (SAXParseException) e;
			logger.error(spe.getSystemId() + ":" + spe.getLineNumber() + ":" +
									 spe.getColumnNumber() + ": " + spe.getMessage());
		}
		else {
			logger.error(e.getMessage());
		}
	}

	public static boolean validateMascot(String document){
		boolean isValidXML=false;
		try {
			if(ValidateXML.validate(document,"config/mascot_search_results_2.xsd"))isValidXML=true;

			if(!isValidXML){
				if(ValidateXML.validate(document,"config/mascot_search_results_1.xsd"))isValidXML=true;
			}
		}
		catch (IOException e) {
			logger.error("validateMascot:IOException:file: " + document);
		}
		return isValidXML;	
	}

}
