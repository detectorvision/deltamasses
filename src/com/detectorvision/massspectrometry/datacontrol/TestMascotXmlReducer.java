package com.detectorvision.massspectrometry.datacontrol;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


public class TestMascotXmlReducer extends DefaultHandler{

	private String tempVal;

	public TestMascotXmlReducer(){
	}

	public void runExample() {
		parseDocument();
	}

	private void parseDocument() {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(new File("C:\\detectorvision\\deltaMasses\\data\\example.mascot.xml"), this);			
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	//Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {		
		System.out.print("<"+qName);
		for(int i=0;i<attributes.getLength();i++){
			System.out.print(" "+attributes.getLocalName(i)+"="+attributes.getValue(i));
		}
		System.out.print(">");

		tempVal="";
	}


	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal = new String(ch,start,length);
		tempVal.trim();
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("StringIons1")){
			String[] mzPairs=tempVal.split(",");
			for(int i=0;i<mzPairs.length && i<75;i++){
				System.out.print(mzPairs[i]);
				if(i<74 && i<mzPairs.length-1){System.out.print(":");}
			}
		}
		else if(qName.equalsIgnoreCase("NumVals")){
			int tempnum = Integer.parseInt(tempVal);
			if(tempnum>74){System.out.print("75");}
			else{System.out.print(tempnum);}
		}
		else{
			System.out.print(tempVal);
		}
		System.out.println("</"+qName+">");	
	}

	public static void main(String[] args){
		TestMascotXmlReducer spe = new TestMascotXmlReducer();
		spe.runExample();
	}

}



