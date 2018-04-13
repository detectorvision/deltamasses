/* $Id: postSQL.java 383 2010-08-25 16:46:23Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.AutomationControl.AsyncEventListenerRunnable;
import com.detectorvision.deltaMasses.AutomationControl.AsyncUpdateRunnable;
import com.detectorvision.deltaMasses.analyzationmanagement.DMBpeptideShort;
import com.detectorvision.deltaMasses.analyzationmanagement.DeltaUtils;
import com.detectorvision.deltaMasses.analyzationmanagement.PeptideNet;
import com.detectorvision.deltaMasses.statemachine.Event;
import com.detectorvision.deltaMasses.statemachine.EventListener;
import com.detectorvision.massspectrometry.analyzation.ProgressListener;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.ExternalFileHandler;
import com.detectorvision.utility.GaussianFit;
import com.detectorvision.utility.pdf.HeaderFooter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lowagie.text.Anchor;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

public final class postSQL implements Runnable, ProgressListener{

	/**
	 * @param args
	 */
	private ProgressListener progressListener;
	private EventListener eventListener;
	private Display display;
	
	static public int MAX_PEPNETS_PERSONAL_EDITION = 10;
	static boolean isStoppedFlag = false;
	private double lowMass ;
	private double highMass;
	private String barcodeString;
	public Thread thread = null;

	// Logging with log4j
	static Logger logger = Logger.getLogger(postSQL.class.getName());
	public static void main(String[] args) {
		int record_id=0;
		int spectrum_id=0;
		int deltaMass_id=0; 
		int peptide_id=0;
		String sqlQuery="";
		Statement s = null;
		String barcodeString="someFunnyBarcodeString";
		logger.debug("Checking mascot log");
		postSQL ps = new postSQL();
		if (!ps.DMBgetDeltaMassFingerPrint(0,5000,barcodeString))
			logger.fatal("could not calculate deltaProtein fingerprint. Exit.");
	}

	postSQL(){
		int record_id=0;
		int spectrum_id=0;
		int deltaMass_id=0; 
		int peptide_id=0;
		String sqlQuery="";
		Statement s = null;
	}

	postSQL(EventListener eventListener, ProgressListener progressListener){
		int record_id=0;
		int spectrum_id=0;
		int deltaMass_id=0; 
		int peptide_id=0;
		String sqlQuery="";
		Statement s = null;
		
		this.progressListener = progressListener;
		this.eventListener = eventListener;
		this.display = Display.getCurrent();
		

	}
	
	public void run()
	{
		AsyncEventListenerRunnable async;
		updateProgress(0 , "Started creating report...");
		async = new AsyncEventListenerRunnable(Event.DETECTBEGIN, this.eventListener);
		display.asyncExec(async);	
		try{
		Thread.sleep(750);
		logger.debug("running process");
		DMBgetDeltaMassFingerPrint(this.lowMass,this.highMass,this.barcodeString);
		this.updateProgress(100, "processing completed");
		async = new AsyncEventListenerRunnable(Event.DETECTEND, this.eventListener);
		display.asyncExec(async);
		}
		catch(InterruptedException ex){
			logger.error("Error in Thread processing : startDeltaMassFingerPrint" + ex.toString());
		}
	}
	public static boolean DMBgetMascotLog(){//returns true if all ok
		try {
			BufferedReader in = new BufferedReader(new FileReader("automation/mascot/searches.log"));
			String str;

			while ((str = in.readLine()) != null) {
				logger.debug(str);
				String[] words = str.split("\t");
				if(! words[0].equals("Job#")){
					for(int i=0;i<words.length;i++){
						logger.debug("item:"+i+":"+words[i]);
					}	
				}
			}
			in.close();
		} catch (IOException e) {
			logger.error("Trouble in DMBgetMascotLog: " + e.toString());
		}
		return false;
	}

	public void startDeltaMassFingerPrint(double lowMass, double highMass, String barcodeString){
		this.lowMass = lowMass;
		this.highMass = highMass;
		this.barcodeString = barcodeString;
		if(this.thread == null || isStoppedFlag == true){
			this.thread = new Thread(this);
			this.thread.start();
		}
	}
	class AsyncEventListenerRunnable implements Runnable{
		Event e; 
		EventListener listener;

		public AsyncEventListenerRunnable(Event e, EventListener listener){
			this.e = e;
			this.listener = listener;
		}

		public void run() {

			if(this.listener == null){
				return;
			}
			this.listener.updateEvent(e, null);
		}	
	}

	public void stopDeltaMassFingerPrint()
	{
		isStoppedFlag = true;
		if(this.thread != null){
			this.thread.stop();
		}
			AsyncEventListenerRunnable async;
			async = new AsyncEventListenerRunnable(Event.DETECTSTOP, this.eventListener);
			display.asyncExec(async);
			this.updateProgress(0, "Processing stopped by user");
	}
	public  boolean DMBgetDeltaMassFingerPrint(double lowMass, double highMass,String barcodeString){
		int[] countBin = new int[200000];//C-Style ....
		ArrayList<GaussianFit> GaussFits = new ArrayList<GaussianFit>();
		boolean[] blocked = new boolean[200000];
		int[] checkMe = new int[20];
		for(int i=0;i<200000;i++){
			countBin[i]=0;blocked[i]=false;
		}
		try{	
			Connection conn = DeltaMassBase.getConnection();
			Statement s = conn.createStatement ();

			ResultSet result = s.executeQuery("SELECT dm,sim,p from deltaMass where dm > " +lowMass + " AND dm < "+highMass);
			double dm=0;
			int bin=0;
			int countPairs=0;
			this.updateProgress(5," Report generation in progress .. ");
			while (result.next()) {
				dm  = Math.abs(result.getDouble(1));
				if(dm<2000){
					bin=(int)(dm*100);
					if(bin>199999 || bin <0){
						logger.fatal("System error:wrong bin:"+bin);
						System.exit(1);
					}
					countPairs++;
					countBin[bin]++;
				}
			}
			for(int j=0;j<25;j++){
				int max=0;
				int maxIndex=0;
				for(int i=0;i<200000;i++){
					if(countBin[i]>max && blocked[i]==false){max=countBin[i];maxIndex=i;}
				}
				for(int i=Math.max(maxIndex-6, 0);i<Math.min(maxIndex+6, 200000);i++)blocked[i]=true;
				double d=((double)maxIndex)/100.0;
				if(max>50){
					GaussFits.add(DMBgetGaussianFit(d, 0.04, 20));
				}
				
			}
			this.updateProgress(10," Report generation in progress .. ");
			conn.close();
			printDMBPdf(barcodeString, GaussFits, lowMass, highMass);
		    logger.info("printDMBPdf ok");
		}catch(Exception ex){
			logger.error("DMBgetDeltaMassFingerprint: " + ex);
			updateProgress(0, "Processing stopped due to some Error");
			return false;
		}

		return true;
	}
	
	public static GaussianFit DMBgetGaussianFit(double mass, double windowWidth, int bins){
		double minMass= mass - windowWidth/2.0;
		double maxMass= mass + windowWidth/2.0;
		double binWidth = windowWidth/bins;
		GaussianFit GaussFit=null;

		int[] countBin = new int[bins];//C-Style ....
		for(int i=0;i<bins;i++){
			countBin[i]=0;
		}
		try{
			Connection conn = DeltaMassBase.getConnection();
			Statement s = conn.createStatement ();

			ResultSet result = s.executeQuery("SELECT dm from deltaMass WHERE dm < "+maxMass+" AND dm > "+minMass);
			double dm=0;
			int bin=0;
			int countPairs=0;
			while (result.next()) {
				dm  = Math.abs(result.getDouble(1));
				bin=(int)((-minMass+dm)/(binWidth));
				if(bin==bins){bin=bins-1;}
				if(bin>=bins+1 || bin <0){
					logger.fatal("System error:wrong bin:"+bin);
					System.exit(1);
				}
				countPairs++;
				countBin[bin]++;
			}
			conn.close();
			int sum=0;
			for(int i=0;i<bins;i++){
				if(countBin[i]>0){
					double d=(minMass+  ((double)((double)i+0.5)*(binWidth)));
					//System.out.println("detail: "+String.format("%.8g",d)+"\t"+countBin[i]);
					sum+=countBin[i];
				}
			}
			//System.out.println("total count:"+sum);
			double exp=mass;
			double sigma=windowWidth;
			double maxCount=0;
			for(int i=0;i<bins;i++){
				if(countBin[i]>maxCount){maxCount=countBin[i];}
			}
			//System.out.println("maxCount:"+maxCount);

			//below, we do a quadratic error minimization fit of a gaussian to the data in countBin[].
			//four variables: 
			//exp     center of the Gaussian)
			//sigma   sigma .... of course ...)
			//base    a constant added to the signal
			//height  height of the gaussian
			//method works by continuously zooming into the 4-dimensional point of the minimum.

			double baseMin=0;
			double baseMax=maxCount/2;

			double sigmaMin=0.0001;
			double sigmaMax=0.1;

			double hMin=0;
			double hMax=maxCount*2.0;

			double expMin = mass - (windowWidth/2.0);
			double expMax = mass + (windowWidth/2.0);;

			double base=0;double bestExp=0;double bestSigma=0;double bestH=0;double bestBase=0;double bestErr=Double.MAX_VALUE;
			for(int loop=0;loop<=20;loop++){
				double expStep=(expMax-expMin)/10.0;
				double sigmaStep=(sigmaMax-sigmaMin)/10.0;
				double  baseStep=(baseMax-baseMin)/10.0;
				double  hStep=(hMax-hMin)/10.0;
				for(exp=expMin; exp<=expMax;exp+=expStep){
					for(sigma=sigmaMin;sigma<=sigmaMax;sigma+=sigmaStep){
						double twoSsq=-1.0/(sigma*sigma*2.0);
						for(base=baseMin;base<=baseMax;base+=baseStep){
							for(double h=hMin; h< hMax;h+=hStep){
								double err=0;double tmp=0;
								for(int i=0;i<bins;i++){double m=(minMass+  ((double)((double)i+0.5)*(binWidth)));tmp=countBin[i] - (base + h*Math.exp(twoSsq*((m-exp)*(m-exp))));tmp*=tmp;err+=tmp;}
								err /= bins;
								if(err<bestErr){bestErr=err;bestExp=exp;bestSigma=sigma;bestBase=base;bestH=h;
								//System.out.println("b:"+base+" h:"+h+" err:"+bestErr);
								}
							}
						}	
					}
				}
				expMin=bestExp - 2.5*expStep;expMax=bestExp+2.5*expStep;
				sigmaMin=bestSigma - 2.5*sigmaStep;sigmaMax=bestSigma+2.5*sigmaStep;
				baseMin=bestBase - 2.5*baseStep;baseMax=bestBase+2.5*baseStep;
				hMin=bestH - 2.5*hStep;hMax=bestH+2.5*hStep;
			}
			bestSigma=Math.abs(bestSigma);
			double y[]= new double[bins];
			double x[]= new double[bins];
			for(int i=0;i<bins;i++){
				x[i]=(minMass+  ((double)((double)i+0.5)*(binWidth)));
				y[i]= (double)countBin[i];
			}
			double numUnderCurve=bestH*bestSigma*Math.sqrt(2.0*Math.PI)/binWidth;
			GaussFit=new GaussianFit(bestExp,bestH,bestBase,bestSigma,x,y,
						 numUnderCurve,bestErr);

		}catch(Exception ex){
			logger.error("DMBgetGaussianFit:error:"+ex);
		}

		return GaussFit;
	}
	
	public  boolean printDMBPdf(String title, ArrayList<GaussianFit> GaussFits, double lowMass, double highMass){
		boolean isDiscoveryEdition=DeltaMasses.isDiscoveryEdition;
		try{
		Document document =  new Document(PageSize.A4, 45, 40, 110, 50);//left right top bottom
		document.setMarginMirroring(true);
			/* chapter08/FontMetrics.java */
			Font fontSmall = new Font(Font.HELVETICA, 8);
			Font fontTiny = new Font(Font.HELVETICA, 5);
			Font fontBold = new Font(Font.HELVETICA,14,Font.BOLD);
			
			String pdfFileName = "tmp/deltaProtein_report.pdf";
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
			writer.setPageEvent(new HeaderFooter());
			File f = new File(pdfFileName);
			if(f.isDirectory()){return false;}
			if(! f.canWrite()){return false;}

			document.addTitle("deltaProtein peptideNet: "+ title);
			document.addSubject("Detection of protein modifications");
			document.addKeywords("PTM PTM-Detection PTM-Localization deltaMasses Detectorvion AG Differential PTM Detection www.detectorvision.ch");
			document.addAuthor("deltaMasses. Scientist:"+System.getProperty("user.name"));
			document.open();

			Image image128 = Image.getInstance("images/blue_bar.png");//why do we do this ???????
			try{
				PdfContentByte cd = writer.getDirectContent();
				Barcode128 code128 = new Barcode128();
				String tmpString=title;

				//make sure we only have 7bit ASCII characters
				//change non-ASCII characters to underscores
				String btmpString=title;
				for(int i=0;i<tmpString.length();i++){
					if((char)tmpString.charAt(i)>127){
						char tmp = (char)tmpString.charAt(i);
						char to = (char)95;//underscore
						btmpString= btmpString.replace(tmp,to);
					}
				}
				code128.setCode(btmpString);
				image128 = code128.createImageWithBarcode(cd, null, null);
			}
			catch (Exception de) {
				logger.error("postSQL:barcode printing:" + de);
			}
			document.add(image128);

			//source: java in a nutshell pg 222
			document.add(new Paragraph(" "));

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			String tmpString =  "deltaProtein peptideNet report";
			document.add(new Paragraph(tmpString,fontBold));
			
			document.add(new Paragraph(" "));
			
			tmpString =  "" + cal.getTime().toString()+ " by "+System.getProperty("user.name");
			document.add(new Paragraph(tmpString));
			
			
			
			
			tmpString="inspected deltaMass range: "+lowMass + " - "+highMass + " [Dalton] ";
			document.add(new Paragraph(tmpString));

			int numDeltaMasses=DeltaUtils.DMBgetNumDeltaMasses();
			if(numDeltaMasses>0){tmpString= "number of pairs in deltaMassBase:"+numDeltaMasses;}
			document.add(new Paragraph(tmpString));
	
			document.add(new Paragraph(" "));
			document.add(new Paragraph("deltaMasses PTM fingerprint section",fontBold));
			
			PdfPTable table = new PdfPTable(5);
			table.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.setSpacingBefore(10f);//what actualy does 10f mean ?
			table.setSpacingAfter(10f);
			table.setWidthPercentage(100);


			PdfPCell cell=null;

			if(!isDiscoveryEdition){
				cell = new PdfPCell(new Paragraph("In Discovery Edition, you can see all data blocked in this report"));
				cell.setColspan(5);
				cell.setBackgroundColor(new Color(0xCC, 0xFF, 0xCC));
				table.addCell(cell);
			}

			cell = new PdfPCell(new Paragraph("Gaussian fits to deltaMass pairs"));
			cell.setColspan(5);
			cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("deltaMass"));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("pairs"));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("height"));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("sigma"));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			table.addCell(cell);

			//cell = new PdfPCell(new Paragraph("rms"));
			//cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			//cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			//table.addCell(cell);

			cell = new PdfPCell(new Paragraph("base"));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
			table.addCell(cell);

			cell.setHorizontalAlignment(cell.ALIGN_RIGHT);
			
			for(int i=0;i<GaussFits.size();i++){
				GaussianFit gf = GaussFits.get(i);

				cell = new PdfPCell(new Paragraph(String.format("%.6f", gf.getExp())));
				cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);

				cell = new PdfPCell(new Paragraph(String.format("%.0f", gf.getNumUnderCurve())));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(String.format("%.1f", gf.getH())));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph(String.format("%.4f", gf.getSigma())));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				
				//cell = new PdfPCell(new Paragraph(String.format("%.1f", gf.error)));
				//cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				//table.addCell(cell);
				
				cell = new PdfPCell(new Paragraph(String.format("%.1f", gf.getBase())));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
			}
			document.add(table);

		    document.add(new Paragraph(" "));
			
			String tmp="Origin of the deltaMasses PTM fingerprint: All detected deltaMasses in deltaMassBase were binned at bin width 0.01 Dalton. ";
			tmp +=  "For bins with a count of more than 50, a Gaussian curve was fitted to another binned histogram of width 0.04 Dalton consisting of 20 bins i.e. with a bin width of 0.002 Dalton. ";
			tmp +=  "The resulting parameters are deltaMass, height, base, and sigma. ";
			tmp +=  "The number of pairs is derived from the curve under the fitted Gaussians.";
			tmp +=  "If there are more than 25 signals, the 25 most intense are reported.";
			Paragraph  par = new Paragraph(tmp);
			par.setAlignment(Paragraph.ALIGN_JUSTIFIED);
			document.add(par);
			
			Anchor anchor = new Anchor("This approach is similar to the Mass Distance Fingerprint, see PUB-MED-ID : 17513179 (Click)",fontSmall);
			anchor.setReference("http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&Cmd=ShowDetailView&TermToSearch=17513179");
			anchor.setName("");
			document.add( anchor);			
			
			tmp=" ";
		    document.add(new Paragraph(tmp));
			tmp=" ";
		    document.add(new Paragraph(tmp));
		    
			tmp="The following records have been used for this deltaProtein report:";
				
		    document.add(new Paragraph(tmp,fontSmall));
		    tmp="";
			
			Connection conn = DeltaMassBase.getConnection();
			logger.debug("Connected!");
			Statement s;
			tmp="";
			try {
								
				s = conn.createStatement ();
				ResultSet result = s.executeQuery("SELECT filename FROM  record order by record_id");
				int count=0;
				while (result.next()){
					String tmp1="";
					String tmp2=result.getString(1);
					//tmp=tmp+result.getString(1)+ " ";
					count++;
				    Pattern p = Pattern.compile(".*/");
				    Matcher matcher = p.matcher(tmp2);
				    tmp = tmp + matcher.replaceFirst("");
				    tmp = tmp + " ";
				    
				}
				Paragraph  par5 = new Paragraph(tmp,fontTiny);
				par5.setAlignment(Paragraph.ALIGN_JUSTIFIED);
				document.add(par5);
				
				tmp="That are " + count + " records in total.";
			    document.add(new Paragraph(tmp,fontSmall));
			    tmp="";
				
			} catch (SQLException e) {
				logger.error("postSQL:SQLException: " + e.toString());
			}


			document.newPage();
			int countLocal=0;
			for(int i=0;i<GaussFits.size();i++){

				PdfContentByte cb = writer.getDirectContent();
				PdfTemplate tp = cb.createTemplate(150, 150);
				Graphics2D g3 = tp.createGraphicsShapes(150, 150);
				paintGaussFit(g3, GaussFits.get(i));
				cb.addTemplate(tp, 410, 550-countLocal*150);
				g3.dispose();

				PdfContentByte cb2 = writer.getDirectContent();
				PdfPTable table2=new PdfPTable(3);
				float[] rows = { 117f,117f,117f };
				table2.setTotalWidth(rows);


				cell = new PdfPCell(new Paragraph(String.format("experimental deltaMass: ")));
				cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setColspan(2);
				table2.addCell(cell);

				cell = new PdfPCell(new Paragraph(String.format("%.6f",GaussFits.get(i).getExp()) + " [Da]"));
				cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setColspan(1);
				table2.addCell(cell);
				
				cell = new PdfPCell(new Paragraph("sigma:"+String.format("%.4f", GaussFits.get(i).getSigma())+"  height:"+ String.format("%.2f", GaussFits.get(i).getH()) + " base:"+String.format("%.2f", GaussFits.get(i).getBase())+ String.format(" pairs: %.0f", GaussFits.get(i).getNumUnderCurve())));
				cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setColspan(3);
				table2.addCell(cell);

				try {
					double minMass=GaussFits.get(i).getExp()-0.01;
					double maxMass=GaussFits.get(i).getExp()+0.01;
					logger.debug("SELECT mono_mass,code_name,composition from multimod WHERE mono_mass < "+maxMass+" AND mono_mass > "+minMass);
					
					PreparedStatement getMods = conn.prepareStatement("SELECT mono_mass,code_name,composition from multimod WHERE mono_mass < ? AND mono_mass > ?");
					getMods.setDouble(1, maxMass);
					getMods.setDouble(2, minMass);
					ResultSet result = getMods.executeQuery();
					int count=0;
					
					cell = new PdfPCell(new Paragraph("modification"));
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setColspan(1);
					table2.addCell(cell);
					
					cell = new PdfPCell(new Paragraph("exact deltaMass [Da]"));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setColspan(1);
					table2.addCell(cell);
					
					cell = new PdfPCell(new Paragraph("deviation [Da]"));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setColspan(1);
					table2.addCell(cell);
					
					
					while (result.next() && count<6) {
						double ddm=GaussFits.get(i).getExp()-result.getDouble(1);
	                    String tmpSign ="";
	                    if(ddm>0){tmpSign="+";}
	                    
						cell = new PdfPCell(new Paragraph(""+result.getString(2)+" "+result.getString(3)));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setColspan(1);
						table2.addCell(cell);
						
						cell = new PdfPCell(new Paragraph(""+result.getDouble(1)));
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setColspan(1);
						table2.addCell(cell);
						
						cell = new PdfPCell(new Paragraph(tmpSign+String.format("%.6f", ddm)));
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setColspan(1);
						table2.addCell(cell);
						
						count++;
					}

					if(count==0){
						cell = new PdfPCell(new Paragraph("no modification known within +/- 0.01 Dalton"));
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setColspan(3);
						table2.addCell(cell);
					}
					if(count>=6){
						cell = new PdfPCell(new Paragraph("more annotated deltamasses within +/- 0.01 Dalton - not reported"));
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setColspan(3);
						table2.addCell(cell);	
					}
				} catch (SQLException e) {
					logger.error("Database error:SQLException: " + e.toString());
				}

				table2.writeSelectedRows(0,6, 50,550-(countLocal-1)*150, cb2);

				countLocal++;
				if(countLocal>=4){countLocal=0;document.newPage();}
			}
			//////////////////////////////////////////////print the network-pdf////////////
			
			PreparedStatement pSt = conn.prepareStatement("SELECT count(specnet_id) from specnet");
			ResultSet res = pSt.executeQuery();
			int totalRec = 0;
			if(res.next())
			{
				totalRec = res.getInt(1);
			}
			pSt.close();
			res.close();
			
			logger.info("Printing deltaProtein pdf");
			PreparedStatement getNets = conn.prepareStatement("SELECT specnet_id,numspecs,numpairs from specnet order by numspecs desc");
			ResultSet result = getNets.executeQuery();
			
			int count=0;
			int MAX_TO_REPORT=1000;
			if(!isDiscoveryEdition){MAX_TO_REPORT=MAX_PEPNETS_PERSONAL_EDITION;}
			
			PeptideNet pepNet = new PeptideNet();
			pepNet.loadSpec2Experiment();
			int x= 0;
			int progress = 0;
			while (result.next() && count<MAX_TO_REPORT) {
				x++;
				progress = (int)(100.00*(double)x/(double)totalRec);
				if(progress > 10)
					this.updateProgress(progress," Report generation in progress ..");
				int specnet_id=result.getInt(1);
				//PeptideNet pepNet = new PeptideNet();
				pepNet.clearNet();
		
				pepNet.loadNet(specnet_id,DeltaMasses.isDiscoveryEdition);
				if(pepNet.numpairs<=0){
					logger.error("printDMBFingerprint:SYSTEM_FAILURE:numpairs is zero");
					continue;
					}
				
				if(count==0){//print the first page
					document.newPage();
					
					document.add(new Paragraph("Protein identification by peptideNet analysis",fontBold));
					
					document.add(new Paragraph(" "));
					
					
					if(!isDiscoveryEdition){
						document.add(new Paragraph("In personal edition, maximal "+ MAX_PEPNETS_PERSONAL_EDITION +" peptideNets are presented in this report. This limitation is not present in Discovery Edition."));
						document.add(new Paragraph(" "));
					}
					
					tmp="The peptideNets on the following pages are colored according to this scheme:";
					par = new Paragraph(tmp);
					par.setAlignment(Paragraph.ALIGN_LEFT);
					document.add(par);
										
					PdfPTable table2 = new PdfPTable(4);
					table2.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.setSpacingBefore(10f);//what actualy does 10f mean ?
					table2.setSpacingAfter(10f);
					table2.setWidthPercentage(100);

					PdfPCell cell2=null;

					cell2 = new PdfPCell(new Paragraph("peptideNet color table"));
					cell2.setColspan(4);
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					table2.addCell(cell2);
					
					cell2 = new PdfPCell(new Paragraph("elemental composition"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("deltaMass"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("name"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("color"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					cell2.setColspan(1);
					table2.addCell(cell2);
					
					cell2 = new PdfPCell(new Paragraph("H O3 P"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("79.966331"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("phosphorylation"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("yellow"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					
					cell2 = new PdfPCell(new Paragraph("O"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("15.994915"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("oxidation"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("blue"));
					cell2.setColspan(1);
					table2.addCell(cell2);
								
					cell2 = new PdfPCell(new Paragraph("H2 C"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("14.01565"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("methylation"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("green"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					
					cell2 = new PdfPCell(new Paragraph("H3 C2 N O"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("57.021464"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("carbamidomethylation"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("red"));
					cell2.setColspan(1);
					table2.addCell(cell2);
			
					cell2 = new PdfPCell(new Paragraph("H C N O"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("43.005814"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("carbamylation"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("cyan"));
					cell2.setColspan(1);
					table2.addCell(cell2);
							
					/*cell2 = new PdfPCell(new Paragraph("C6 H14 N2 O"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("128.09496"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("Lysine (K)"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("black"));
					cell2.setColspan(1);
					table2.addCell(cell2);*/
					
					cell2 = new PdfPCell(new Paragraph("C2 H4"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("28.031300"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("Dimethylation"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("orange"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					
					
					cell2 = new PdfPCell(new Paragraph("weak delta signal"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("---"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("---"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("black"));
					cell2.setColspan(1);
					table2.addCell(cell2);
					document.add(table2);	
					
					document.add(new Paragraph(" "));
					
					tmp="The peptideNets are orderd by the number of spectra they contain. At most 1000 nets are reported. In the graphical representation of a peptideNet, the y-axis corresponds to the weight of the peptide with mass decreasing upwards; the heaviest peptide is at the bottom of the plot. On te y-axis, the peptides are ordered by their spectrum id. If a spectrum has been annotated with an identification, it is marked with a circular symbol. The concept of peptideNets was first published in ";
					tmp += "Journal of Chromatography B Volume 817, Issue 2, 25 March 2005, pages 225-230";
					Paragraph  par3 = new Paragraph(tmp);
					par3.setAlignment(Paragraph.ALIGN_JUSTIFIED);
					document.add(par3);
					
					Anchor anchor3 = new Anchor("http://dx.doi.org/10.1016/j.jchromb.2004.12.009 (Click)",fontSmall);
					anchor3.setReference("http://dx.doi.org/10.1016/j.jchromb.2004.12.009");
					anchor3.setName("");
					document.add( anchor3);
					
					document.add(new Paragraph(" "));
					
					tmp="A more elaborate paper on peptideNets has recently been published: ";
					tmp += "N. Bandeira, D. Tsur, A. Frank, and P.A. Pevzner: Protein identification by spectral network analysis. PNAS, 10 April 2007, vol. 104, no. 15, pages 6140-6145.";
					Paragraph  par4 = new Paragraph(tmp);
					par4.setAlignment(Paragraph.ALIGN_JUSTIFIED);
					document.add(par4);
					Anchor anchor4 = new Anchor("http://www.pnas.org/cgi/content/abstract/104/15/6140 (Click)",fontSmall);
					anchor4.setReference("http://www.pnas.org/cgi/content/abstract/104/15/6140");
					anchor4.setName("");
					document.add( anchor4);
					document.newPage();
				}
				
				
				PdfPTable table2 = new PdfPTable(2);
				table2.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.setSpacingBefore(10f);//what actualy does 10f mean ?
				table2.setSpacingAfter(10f);
				table2.setWidthPercentage(100);

				PdfPCell cell2=null;

				cell2 = new PdfPCell(new Paragraph("peptideNet ID : "+specnet_id));
				cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
				cell2.setColspan(2);
				table2.addCell(cell2);
				
				
				
				cell2 = new PdfPCell(new Paragraph("spectra in net : "+pepNet.numspecs));
				cell2.setColspan(1);
				table2.addCell(cell2);
			
				cell2 = new PdfPCell(new Paragraph("pairs in net : "+pepNet.numpairs));
				cell2.setColspan(1);
				table2.addCell(cell2);
			
				
				Double tmpDouble=pepNet.maxmass-pepNet.minmass;
				Formatter form3 = new Formatter();
				cell2 = new PdfPCell(new Paragraph("mass width of peptideNet : "+form3.format("%.4f", tmpDouble).toString() +" [Da]"));
				cell2.setColspan(1);
				table2.addCell(cell2);
				
				if(pepNet.numphospho>0){
					cell2 = new PdfPCell(new Paragraph("phosphorylation pairs : "+pepNet.numphospho));
					cell2.setColspan(1);
					table2.addCell(cell2);
				}
				else{
					cell2 = new PdfPCell(new Paragraph(" "));
					cell2.setColspan(1);
					table2.addCell(cell2);
				}
				
				
				
				
				
				Formatter form1 = new Formatter();
				cell2 = new PdfPCell(new Paragraph("minimal mass : "+form1.format("%.4f", pepNet.minmass).toString() +" [Da]"));
				cell2.setColspan(1);
				table2.addCell(cell2);
					
				Formatter form2 = new Formatter();
				cell2 = new PdfPCell(new Paragraph("maximal mass : "+form2.format("%.4f", pepNet.maxmass).toString() +" [Da]"));
				cell2.setColspan(1);
				table2.addCell(cell2);
					
				document.add(table2);
				
				//--------------------------------------peptide table-------------------------------
				ArrayList<DMBpeptideShort> pepList = DeltaUtils.DMBgetPepTideInfoOfNet(specnet_id);
				if(pepList.size()>0){
					PdfPTable pepTable = new PdfPTable(5);
					float[] widths2 = { 3f , 1f , 1f , 6f , 2f };
					try {
						pepTable.setWidths(widths2);
					} catch (DocumentException e1) {
						e1.printStackTrace();
					}
					pepTable.setHorizontalAlignment(Element.ALIGN_LEFT);
					pepTable.setSpacingBefore(10f);//what actualy does 10f mean ?
					pepTable.setSpacingAfter(10f);
					pepTable.setWidthPercentage(100);

					cell2 = new PdfPCell(new Paragraph("protein"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("start"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("end"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("peptide"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("@mass"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);

					for(int i=0;i<pepList.size();i++){
						
						if(DeltaMasses.isDiscoveryEdition){
							cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).proteinasc));
							cell2.setColspan(1);
							pepTable.addCell(cell2);
						}
						else{
							Font fontCourier = new Font(Font.COURIER, 10);
							cell2 = new PdfPCell(new Paragraph("---------",fontCourier));
						    cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell2.setColspan(1);
							pepTable.addCell(cell2);
							
						}
						
						cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).pepstart));
						cell2.setColspan(1);
						pepTable.addCell(cell2);

						cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).pepend));
						cell2.setColspan(1);
						pepTable.addCell(cell2);

						
						Font fontCourier;
						if(pepList.get(i).pepsequence.length()>26){
							fontCourier = new Font(Font.COURIER, 8);	
						}
						else {
							fontCourier = new Font(Font.COURIER, 11);	
						}
						
						if(DeltaMasses.isDiscoveryEdition){
							cell2 = new PdfPCell(new Paragraph(pepList.get(i).pepsequence, fontCourier));
							cell2.setColspan(1);
							pepTable.addCell(cell2);
						}
						else{
							fontCourier = new Font(Font.COURIER, 10);
							cell2 = new PdfPCell(new Paragraph("---NOT BLOCKED IN DISCOVERY EDITION---",fontCourier));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell2.setColspan(1);
							pepTable.addCell(cell2);	
						}
						
						cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).pepmass));
						cell2.setColspan(1);
						pepTable.addCell(cell2);
					}
					document.add(pepTable);
				}
				
				if(pepNet.numpairs>1){//dont show idot-grapics to the user ...
					//insert the graphics
					PdfContentByte cb = writer.getDirectContent();
					PdfTemplate tp = cb.createTemplate(510, 510);
					Graphics2D g3 = tp.createGraphicsShapes(510, 510);
					//paintNet(g3, specnet_id);
					pepNet.paintNet(g3,510);
					cb.addTemplate(tp, document.leftMargin(), 80);
					g3.dispose();
					count++;
					document.newPage();
				}
				else{
					count++;
					document.add(new Paragraph(" "));
				}
			}
			

			int c=0;
			while(result.next()){c++;}//count number of nets not printed yet

			if(c>0 && !isDiscoveryEdition )
			{
				document.newPage();
				document.add(new Paragraph("In Discovery Edition, you could see "+c+" more peptideNets.",fontBold));
				document.add(new Paragraph(" "));
				document.add(new Paragraph("In personal edition, maximal "+ MAX_PEPNETS_PERSONAL_EDITION +" peptideNets are presented. This limitation is not present in Discovery Edition."));
				document.add(new Paragraph(" "));
			}

			conn.close();
			document.close();
			document=null;
		 } catch (DocumentException de) {
			logger.error("BaseAlgorithm:DocumentException: " + de.toString());
			updateProgress(0, "Error caused during report generation !!");
		} catch (IOException ioe) {
			logger.error("BaseAlgorithm:IOException: " + ioe.toString());
			updateProgress(0, "Error caused during report generation!!");
		} catch (SQLException e) {
			logger.error("PDF report: SQLException: " + e.toString());
			updateProgress(0, "Error caused during report generation!!");
		} catch (Exception e){
			updateProgress(0, "Error caused during report generation!!");
		    logger.error("PDF report:exception:"+e.toString());
		}
		return(true);
	}	
	
	public static void paintGaussFit(Graphics2D g, GaussianFit gaussFit){
		//graphics has a size of 150*150 pixels
		Graphics2D g2 = (Graphics2D) g;

		g2.setBackground(Color.LIGHT_GRAY);		
        Color detectorvisionBlue= new Color(188,188,188);
		g2.setColor(detectorvisionBlue);
		
		double minX=Double.MAX_VALUE;
		double maxX=Double.MIN_VALUE;
		double minY=Double.MAX_VALUE;
		double maxY=Double.MIN_VALUE;

		int y_length=gaussFit.getY().length;
		for (int i=0; i<y_length; ++i) {
			minX=Math.min(gaussFit.getX()[i],minX);
			maxX=Math.max(gaussFit.getX()[i],maxX);
			minY=Math.min(gaussFit.getY()[i],minY);
			maxY=Math.max(gaussFit.getY()[i],maxY);
		}
		double xrange=maxX-minX;
		double yrange=maxY-minY;
		for (int i=0; i<y_length; ++i) {
			int x=1+20+i*6;
			int y=100- (int)(100.0*(gaussFit.getY()[i]/maxY));
			int h=(int)(100.0*(gaussFit.getY()[i]/maxY));
			g2.fillRect(x, y, 3,h);//width was 4 before 20080122
		}
		//coordinate baseline
		g2.setColor(Color.BLACK);
		g2.drawLine(20, 100,140,100);


		g2.setColor(Color.GRAY);
		g2.drawLine(20,  120, 20, 130);
		g2.drawLine(140, 120,140, 130);

		g2.drawLine(20, 130, 50, 130);
		g2.drawLine(110,130,140, 130);
		g2.drawString("0.04 Da", 60, 135);

		//top left corner
		g2.drawLine(20,0, 30,  0);
		g2.drawLine(20,0, 20, 10);

		//top right corner
		g2.drawLine(130,0, 140,  0);
		g2.drawLine(140,0, 140, 10);
		g2.drawString(String.format("%.0f",maxY), 24,12);

		//base line of fit
		int y=100- (int)(100.0*((gaussFit.getBase())/maxY));	
		g2.setColor(Color.GREEN);
		if(gaussFit.getBase()>=0){
			g2.drawLine(20, y,140,y);
		}

		for(int i=0;i<120 ;i++){
			int x=20+i;
			int x2=x+1;
			double xx=minX+((double)i/120)*xrange;
			double gy=gaussFit.getBase()+gaussFit.getH()*Math.exp(-( (xx-gaussFit.getExp())*(xx-gaussFit.getExp()) )/(2*gaussFit.getSigma()*gaussFit.getSigma()));
			xx=minX+((double)(i+1)/120)*xrange;
			double gy2=gaussFit.getBase()+gaussFit.getH()*Math.exp(-( (xx-gaussFit.getExp())*(xx-gaussFit.getExp()) )/(2*gaussFit.getSigma()*gaussFit.getSigma()));
			y=100- (int)(100.0*(gy/maxY));
			int y2=100- (int)(100.0*(gy2/maxY));
			g2.setColor(Color.GREEN);
			g2.drawLine(x, y, x2,y2);
		}

		//mark the maximum value of the fit with a green vertical line
		y=100- (int)(100.0*((gaussFit.getBase()+gaussFit.getH())/maxY));
		int x=20+  (int)(120.0*(gaussFit.getExp()-minX)/xrange);
		g2.setColor(Color.GREEN);
		g2.drawLine(x, 105, x,y);
		g2.setColor(Color.GRAY);
		g2.drawString(""+String.format("%.4f", gaussFit.getExp()), x-30, 115);
		
		if(!DeltaMasses.isDiscoveryEdition){
			g2.setColor(Color.GREEN);
			g2.drawLine(20, 0, 140, 130);
			g2.drawLine(20,130, 140,0);
		}
	}
	
	
	public static boolean printSpecNet2Pdf(String title, int specNet_id, boolean pop_pdf){
		boolean isDiscoveryEdition=DeltaMasses.isDiscoveryEdition;
		String pdfFileName="";
		try{
		Document document =  new Document(PageSize.A4, 45, 40, 110, 50);//left right top bottom
		document.setMarginMirroring(true);
			Font fontSmall = new Font(Font.HELVETICA, 8);
			Font fontTiny = new Font(Font.HELVETICA, 5);
			Font fontBold = new Font(Font.HELVETICA,14,Font.BOLD);
			
			pdfFileName = "tmp/deltaProtein_pepNet_"+specNet_id+"_report.pdf";
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
			writer.setPageEvent(new HeaderFooter());
			File f = new File(pdfFileName);
			if(f.isDirectory()){return false;}
			if(! f.canWrite()){return false;}

			document.addTitle("deltaProtein peptideNet:"+specNet_id+" "+ title);
			document.addSubject("Detection of protein modifications");
			document.addKeywords("PTM PTM-Detection PTM-Localization deltaMasses Detectorvion AG Differential PTM Detection www.detectorvision.com");
			document.addAuthor("deltaMasses user:"+System.getProperty("user.name"));
			document.open();

			Image image128 = Image.getInstance("images/blue_bar.png");
			try{
				PdfContentByte cd = writer.getDirectContent();
				Barcode128 code128 = new Barcode128();
				String tmpString=title;

				//make sure we only have 7bit ASCII characters
				//change non-ASCII characters to underscores
				String btmpString=title;
				for(int i=0;i<tmpString.length();i++){
					if((char)tmpString.charAt(i)>127){
						char tmp = (char)tmpString.charAt(i);
						char to = (char)95;//underscore
						btmpString= btmpString.replace(tmp,to);
					}
				}
				code128.setCode(btmpString);
				image128 = code128.createImageWithBarcode(cd, null, null);
			}
			catch (Exception de) {
				logger.error("postSQL:barcode printing:" + de);
			}
			document.add(image128);

			//source: java in a nutshell pg 222
			document.add(new Paragraph(" "));

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			String tmpString =  "deltaProtein :: specNet_ID: "+specNet_id;
			document.add(new Paragraph(tmpString,fontBold));
			document.add(new Paragraph(" "));
			
			tmpString =  "" + cal.getTime().toString()+ " by "+System.getProperty("user.name");
			document.add(new Paragraph(tmpString));

			//////////////////////////////////////////////print the network-pdf////////////
			logger.info("Printing deltaProtein pdf");
			Connection conn = DeltaMassBase.getConnection();
			PreparedStatement getNets = conn.prepareStatement("SELECT specnet_id,numspecs,numpairs from specnet where specnet_id = ?");
			getNets.setInt(1, specNet_id);
			ResultSet result = getNets.executeQuery();
			
			int count=0;
			int MAX_TO_REPORT=1000;
			if(!isDiscoveryEdition){MAX_TO_REPORT=MAX_PEPNETS_PERSONAL_EDITION;}
			
			while (result.next() && count<MAX_TO_REPORT) {
				int specnet_id=result.getInt(1);
				PeptideNet pepNet = new PeptideNet();
				pepNet.loadNet(specnet_id,DeltaMasses.isDiscoveryEdition);
				if(pepNet.numpairs<=0){
					logger.error("printDMBFingerprint:SYSTEM_FAILURE:numpairs is zero");
					continue;
					}
				
				PdfPTable table2 = new PdfPTable(2);
				table2.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.setSpacingBefore(10f);//what actualy does 10f mean ?
				table2.setSpacingAfter(10f);
				table2.setWidthPercentage(100);

				PdfPCell cell2=null;

				cell2 = new PdfPCell(new Paragraph("peptideNet ID : "+specnet_id));
				cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
				cell2.setColspan(2);
				table2.addCell(cell2);
				
				
				
				cell2 = new PdfPCell(new Paragraph("spectra in net : "+pepNet.numspecs));
				cell2.setColspan(1);
				table2.addCell(cell2);
			
				cell2 = new PdfPCell(new Paragraph("pairs in net : "+pepNet.numpairs));
				cell2.setColspan(1);
				table2.addCell(cell2);
			
				
				Double tmpDouble=pepNet.maxmass-pepNet.minmass;
				Formatter form3 = new Formatter();
				cell2 = new PdfPCell(new Paragraph("mass width of peptideNet : "+form3.format("%.4f", tmpDouble).toString() +" [Da]"));
				cell2.setColspan(1);
				table2.addCell(cell2);
				
				if(pepNet.numphospho>0){
					cell2 = new PdfPCell(new Paragraph("phosphorylation pairs : "+pepNet.numphospho));
					cell2.setColspan(1);
					table2.addCell(cell2);
				}
				else{
					cell2 = new PdfPCell(new Paragraph(" "));
					cell2.setColspan(1);
					table2.addCell(cell2);
				}
				
				
				
				
				
				Formatter form1 = new Formatter();
				cell2 = new PdfPCell(new Paragraph("minimal mass : "+form1.format("%.4f", pepNet.minmass).toString() +" [Da]"));
				cell2.setColspan(1);
				table2.addCell(cell2);
					
				Formatter form2 = new Formatter();
				cell2 = new PdfPCell(new Paragraph("maximal mass : "+form2.format("%.4f", pepNet.maxmass).toString() +" [Da]"));
				cell2.setColspan(1);
				table2.addCell(cell2);
					
				document.add(table2);
				
				//--------------------------------------peptide table-------------------------------
				ArrayList<DMBpeptideShort> pepList = DeltaUtils.DMBgetPepTideInfoOfNet(specnet_id);
				if(pepList.size()>0){
					PdfPTable pepTable = new PdfPTable(5);
					float[] widths2 = { 3f , 1f , 1f , 6f , 2f };
					try {
						pepTable.setWidths(widths2);
					} catch (DocumentException e1) {
						e1.printStackTrace();
					}
					pepTable.setHorizontalAlignment(Element.ALIGN_LEFT);
					pepTable.setSpacingBefore(10f);//what actualy does 10f mean ?
					pepTable.setSpacingAfter(10f);
					pepTable.setWidthPercentage(100);

					cell2 = new PdfPCell(new Paragraph("protein"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("start"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("end"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("peptide"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);
					cell2 = new PdfPCell(new Paragraph("@mass"));
					cell2.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					pepTable.addCell(cell2);

					for(int i=0;i<pepList.size();i++){
						
						if(DeltaMasses.isDiscoveryEdition){
							cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).proteinasc));
							cell2.setColspan(1);
							pepTable.addCell(cell2);
						}
						else{
							Font fontCourier = new Font(Font.COURIER, 10);
							cell2 = new PdfPCell(new Paragraph("---------",fontCourier));
						    cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell2.setColspan(1);
							pepTable.addCell(cell2);
							
						}
						
						cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).pepstart));
						cell2.setColspan(1);
						pepTable.addCell(cell2);

						cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).pepend));
						cell2.setColspan(1);
						pepTable.addCell(cell2);

						
						Font fontCourier;
						if(pepList.get(i).pepsequence.length()>26){
							fontCourier = new Font(Font.COURIER, 8);	
						}
						else {
							fontCourier = new Font(Font.COURIER, 11);	
						}
						
						if(DeltaMasses.isDiscoveryEdition){
							cell2 = new PdfPCell(new Paragraph(pepList.get(i).pepsequence, fontCourier));
							cell2.setColspan(1);
							pepTable.addCell(cell2);
						}
						else{
							fontCourier = new Font(Font.COURIER, 10);
							cell2 = new PdfPCell(new Paragraph("---NOT BLOCKED IN DISCOVERY EDITION---",fontCourier));
							PdfPCell cell=null;
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell2.setColspan(1);
							pepTable.addCell(cell2);	
						}
						
						cell2 = new PdfPCell(new Paragraph(""+pepList.get(i).pepmass));
						cell2.setColspan(1);
						pepTable.addCell(cell2);
					}
					document.add(pepTable);
				}
				
				if(pepNet.numpairs>1){//dont show idot-grapics to the user ...
					//insert the graphics
					PdfContentByte cb = writer.getDirectContent();
					PdfTemplate tp = cb.createTemplate(510, 510);
					Graphics2D g3 = tp.createGraphicsShapes(510, 510);
					//paintNet(g3, specnet_id);
					pepNet.paintNet(g3,510);
					cb.addTemplate(tp, document.leftMargin(), 80);
					g3.dispose();
					count++;
					document.newPage();
				}
				else{
					count++;
					document.add(new Paragraph(" "));
				}
			}
			
			conn.close();
			document.close();
			document=null;
			
			if(pop_pdf){//pop the pdf to the user
				try {//wait a bit before opening the doc ... 
					long numMillisecondsToSleep = 100; // 10th of a second
					Thread.sleep(numMillisecondsToSleep);
				} catch (InterruptedException eee) {
					logger.error("sleep problems:"+eee.toString());
				}
				if (ExternalFileHandler.open(pdfFileName)!=0){
					logger.error("printSpecNet2Pdf:Acrobat Reader not installled or file association for pdf is not there. File:"+pdfFileName);}
			}		
			
			
		 } catch (DocumentException de) {
			logger.error("BaseAlgorithm:DocumentException: " + de.toString());
		} catch (IOException ioe) {
			logger.error("BaseAlgorithm:IOException: " + ioe.toString());
		} catch (SQLException e) {
			logger.error("PDF report: SQLException: " + e.toString());
		} catch (Exception e){
		    logger.error("PDF report:exception:"+e.toString());
		}
		

		
		return(true);
	}
	class AsyncUpdateRunnable implements Runnable{
		int progress;
		String text;
		ProgressListener listener;

		public AsyncUpdateRunnable(int progress, String text, ProgressListener listener){
			this.progress = progress;
			this.text = text;
			this.listener = listener;
		}

		public void run() {
			if(this.listener == null){
				return;
			}
			this.listener.updateProgress(this.progress, this.text);
		}	
	}
	public void updateProgress(int progress, String text) {
		AsyncUpdateRunnable async = new AsyncUpdateRunnable(progress, text, this.progressListener);
		if(display == null){
			return;
		}
		logger.debug("updateprogress:"+text);
		display.asyncExec(async);
	}	
	
	
	
	
	
	
	
	
}	
