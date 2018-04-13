/* $Id: pmcDialogHandler.java 284 2010-05-08 09:15:29Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.utility.ImageSelection;
import com.detectorvision.utility.Snippet156;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.rmi.server.ExportException;
import java.security.spec.MGF1ParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


/**
 * Eventhandler for the HelpDialog Menu.
 * @author lehmamic
 */
public class pmcDialogHandler extends SelectionAdapter{
	private Record[] records;
	double meanDeltaBef=0;
	double meanDeltaAft=0;
	double absCutBef=0;
	double absCutAft=0;
	double absCut3Bef=0;
	double absCut3Aft=0;
	double devBef=0;
	double devAft=0;
	int devImpFac=0;
	int qualImpFac=0;
	int countPeps=0;
	double meanDeltaReady = 0;
	double devReady = 0;
	double absCut3Ready = 0;
	double fitA=0;
	double fitB=0;
	double fit2A=0;
	double fit2B=0;
	Image image;

	// Logging with log4j
	static Logger logger = Logger.getLogger(pmcDialogHandler.class.getName());


	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public pmcDialogHandler(Record[] records){
		this.records = records;
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		// get the mainshell
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();
		
		logger.info("pmc selected");
		
		Map openWidgets = null;
		Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/PmcScreen.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			logger.error("XSWTException:"+error.toString());
			error.printStackTrace();
		}
		dialogShell.pack();

		// objectreferences
		
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		cancelButton.setToolTipText("closes this window");
		
		Button pmcButton = (Button)openWidgets.get("pmcButton");

		
		Button pngButton = (Button)openWidgets.get("pngButton");
		pngButton.setToolTipText("export calibration graphics to clipboard");
		
		//Button pdfButton = (Button)openWidgets.get("pdfButton");
		//pdfButton.setToolTipText("export to .pdf format");
		
		Button clipButton = (Button)openWidgets.get("clipButton");
		clipButton.setToolTipText("export calibration data to clipboard");
		
		
		
		
		pmcButton.setEnabled(DeltaMasses.isDiscoveryEdition);
		if(DeltaMasses.isDiscoveryEdition){
			pmcButton.setToolTipText("create a calibrated .mgf; for each MSMS, export 75 most intense MSMS signals.");
			pmcButton.setText("export calibrated .mgf");
		}
		else{
		pmcButton.setText("export calibrated .mgf");
		pngButton.setEnabled(false);
		//pdfButton.setEnabled(false);
		clipButton.setEnabled(false);
		}
		
		final Canvas pmcCanvas = (Canvas)openWidgets.get("pmcCanvas");
		
		Color bcol =  new Color(dialogShell.getDisplay(), 255, 255, 255 );
		Color fcol =  new Color(dialogShell.getDisplay(), 66, 66, 66 );
		Color lightBlueCol =  new Color(dialogShell.getDisplay(), 155, 155, 255 );
		Color lightGreenCol =  new Color(dialogShell.getDisplay(), 122, 255, 122 );
		Color lightgreyCol =  new Color(dialogShell.getDisplay(), 188, 188, 188 );
		Color redCol =  new Color(dialogShell.getDisplay(), 255, 111, 111 );
		
		boolean mode_ppm=false;//if false we have mode_absolute_mz
		String devUnit="[m/z]";
		if(mode_ppm){
			devUnit="[ppm]";
		}

		countPeps=0;
		if(this.records[0] != null && records[0].getSpectrumList() != null){
			for(int i=0;i<records[0].getSpectrumList().size();i++){
				if(records[0].getSpectrumList().get(i).pepSequence != null){
					countPeps++;
				}
			}
		}
		else{
			 MessageBox messageBox = new MessageBox(dialogShell, SWT.OK );
			 messageBox.setText("post measurement callibration");
		     messageBox.setMessage("No data loaded, cannot perform post measurement calibration.");
		     messageBox.open();
			logger.error("null error in record or record.getSpectrumList. returning. User informed by dialog.");
			return;
		}
		double datax[] = new double[countPeps];
		double datay[] = new double[countPeps];
		
		
		countPeps=0;
		if(this.records[0] != null){
			for(int i=0;i<records[0].getSpectrumList().size();i++){
				if(records[0].getSpectrumList().get(i).pepSequence != null){
					datax[countPeps]=records[0].getSpectrumList().get(i).pepMz;
					if(mode_ppm){
						datay[countPeps]=1000000*records[0].getSpectrumList().get(i).pepError/records[0].getSpectrumList().get(i).pepMz;
					}
					else{
						datay[countPeps]=records[0].getSpectrumList().get(i).pepError;
					}
					countPeps++;
				}
			}
		}

		
		
//		start drawing ....
		image = new Image(dialogShell.getDisplay(),450, 700);
		//Image image = new Image(dialogShell.getDisplay(),450, 550);
		GC gc = new GC (image);
	    
		gc.setForeground(fcol);
		gc.setBackground(bcol);
		
		if(this.records[0] != null){
			SimpleRegression regression = new SimpleRegression();
			countPeps=0;
			double minMass=Double.MAX_VALUE;
			double maxMass=Double.MIN_VALUE;
			double minDelta=Double.MAX_VALUE;
			double maxDelta=Double.MIN_VALUE;

			logger.info("filename:" + this.records[0].getFileName());
			for(int i=0;i<records[0].getSpectrumList().size();i++){
				if(records[0].getSpectrumList().get(i).pepSequence != null && records[0].getSpectrumList().get(i).pepError!=0){//added pepError 20070124
					//see http://jakarta.apache.org/commons/math/userguide/stat.html#1_4_Simple_regression for the regression
					regression.addData(datax[countPeps],datay[countPeps]);
					if(datax[countPeps] > maxMass){maxMass=datax[countPeps];}
					if(datax[countPeps] < minMass){minMass=datax[countPeps];}
					if(datay[countPeps] > maxDelta){maxDelta=datay[countPeps];}
					if(datay[countPeps] < minDelta){minDelta=datay[countPeps];}
					countPeps++;
				}
			}
			logger.info("origin method:"+records[0].getOriginMethod());
			if(countPeps>=10 && records[0].getOriginMethod().equals("mascot")){
				
				//#################################################################################################
				double yrange=0; 
				double xrange=0;
				double x1=0;double x2=0; double y1=0;double y2=0;

				//fit is fitA + fitB*m
				fitA=regression.getIntercept(); 
				fitB=regression.getSlope();
//				#################################################################################################
				//recalibration preparation		
				double fy[] = new double[countPeps];//after calibration

				int c=0;
				for(int i=0;i<records[0].getSpectrumList().size();i++){
					if(records[0].getSpectrumList().get(i).pepSequence != null && records[0].getSpectrumList().get(i).pepError!=0){
						fy[c]= datay[c] - fitA - fitB*datax[c];
						minDelta=Math.min(minDelta, fy[c]);
						maxDelta=Math.max(maxDelta, fy[c]);
						c++;
					}
				}
				
				double allDelta=Math.max(Math.abs(maxDelta), Math.abs(minDelta));

				minDelta=-allDelta;
				maxDelta=+allDelta;

				yrange=maxDelta-minDelta;
				xrange=maxMass-minMass;

				gc.setForeground(lightBlueCol);
				gc.drawLine (225, 300+50, 225, 300+250);
				gc.drawText (String.format("%.3g",maxDelta), 228,300+50);
				gc.drawText (String.format("%.3g",minDelta), 228,300+250-gc.getFontMetrics().getHeight());
				gc.drawLine (50,300+150,400,300+150);
				gc.drawText(String.format("%.3g",maxMass),402, 300+152);
				gc.drawText(String.format("%.3g",minMass),52 , 300+152);
//				#################################################################################################
				//uncallibrated plot
				c=0;
				for(int i=0;i<records[0].getSpectrumList().size();i++){
					if(records[0].getSpectrumList().get(i).pepSequence != null && records[0].getSpectrumList().get(i).pepError!=0){
						double x= 50+350*((datax[c] - minMass)/xrange);
						double y= 250-200*((datay[c] -minDelta)/yrange);
						gc.setForeground(fcol);
						if(records[0].getSpectrumList().get(i).proteinAsc.contains("reversed"))gc.setForeground(redCol);
						gc.drawLine((int)x-3, (int)y-3,(int)x+3, (int)y+3);
						gc.drawLine((int)x-3, (int)y+3,(int)x+3, (int)y-3);
						c++;
					}
				}

				DescriptiveStatistics stats = DescriptiveStatistics.newInstance();
				for( int i = 0; i < fy.length; i++) {
					stats.addValue(fy[i]);
				}
				meanDeltaAft = stats.getMean();
				devAft = stats.getStandardDeviation();
				absCutAft = (2*devAft + Math.abs(meanDeltaAft));
				absCut3Aft = (3*devAft + Math.abs(meanDeltaAft));
				stats = null;

				DescriptiveStatistics stats2 = DescriptiveStatistics.newInstance();
				for( int i = 0; i < fy.length; i++) {
					stats2.addValue(datay[i]);
				}	
				meanDeltaBef = stats2.getMean();
				devBef = stats2.getStandardDeviation();
				absCutBef = (2*devBef+ Math.abs(meanDeltaBef));
				absCut3Bef = (3*devBef+ Math.abs(meanDeltaBef));

				
				//Final step: calculate deviation within +/- 3 sigma
				DescriptiveStatistics stats3 = DescriptiveStatistics.newInstance();
				SimpleRegression regression2 = new SimpleRegression();
				
				for( int i = 0; i < fy.length; i++) {
					//Consider only values within 3 sigma to recalculate the values
					if(devAft==0)devAft=0.000001;//bullshit protection
					double deviation=Math.abs(fy[i]/devAft);
					if(deviation<=3.0){
						stats3.addValue(fy[i]);
						regression2.addData(datax[i],fy[i]);
					}
				}
				double devFin = stats3.getStandardDeviation();
				double abs3CutFin = (3*devFin + Math.abs(meanDeltaAft));
				
				//Do the second regression
				//fit is fitA + fitB*m
				fit2A=regression2.getIntercept(); 
				fit2B=regression2.getSlope();
//				#################################################################################################
				//recalibration preparation		
				double ffy[] = new double[countPeps];//after calibration
				c=0;
				for(int i=0;i<records[0].getSpectrumList().size();i++){
					if(records[0].getSpectrumList().get(i).pepSequence != null && records[0].getSpectrumList().get(i).pepError!=0){
						ffy[c]= fy[c] - fit2A - fit2B*datax[c];
						stats3.addValue(ffy[c]);
						c++;
					}
				}
				meanDeltaReady = stats3.getMean();
				devReady = stats3.getStandardDeviation();
				absCut3Ready = (3*devReady);
				
//				#################################################################################################
				//recalibrated plot
				/*gc.setForeground(lightgreyCol);
				c=0;
				for(int i=0;i<records[0].getSpectrumList().size();i++){
					if(records[0].getSpectrumList().get(i).pepSequence != null){
						double x= 50+350*((datax[c] - minMass)/xrange);
						double y= 300 + 250-200*((fy[c] -minDelta)/yrange);

						gc.drawLine((int)x-3, (int)y-3,(int)x+3, (int)y+3);
						gc.drawLine((int)x-3, (int)y+3,(int)x+3, (int)y-3);
						c++;
					}
				}*/
				
				//rerecalibrated plot
				
				c=0;
				for(int i=0;i<records[0].getSpectrumList().size();i++){
					if(records[0].getSpectrumList().get(i).pepSequence != null && records[0].getSpectrumList().get(i).pepError!=0){
						double x= 50+350*((datax[c] - minMass)/xrange);
						double y= 300 + 250-200*((ffy[c] -minDelta)/yrange);
						gc.setForeground(fcol);
						if(records[0].getSpectrumList().get(i).proteinAsc.contains("reversed"))gc.setForeground(redCol);
						gc.drawLine((int)x-3, (int)y-3,(int)x+3, (int)y+3);
						gc.drawLine((int)x-3, (int)y+3,(int)x+3, (int)y-3);
						c++;
					}
				}
				
				devImpFac = (int)(100.0*(-1 + devBef/devReady));
				qualImpFac= (int)(100.0*(-1 + absCut3Bef/absCut3Ready));
				stats2=null;

				gc.drawText ("m/z [Dalton/Charge]", 170, 258);
				gc.drawText ("m/z [Dalton/Charge]", 170, 300+258);

				gc.drawText ("delta\n"+devUnit, 2, 150-gc.getFontMetrics().getHeight());
				gc.drawText ("delta\n"+devUnit, 2, 300+150-gc.getFontMetrics().getHeight());

				//gc.drawText("Measurement:" + records[0].getFileName(), 2, 5);
				
				if(DeltaMasses.isDiscoveryEdition)gc.drawText("Discovery Edition  V"+ DeltaMasses.version + " " + DeltaMasses.build, 2, 15);
				else gc.drawText("Personal Edition  V"+ DeltaMasses.version + " " + DeltaMasses.build, 2, 15);
				
				gc.drawText("Peptides used for Calibration:"+ countPeps, 2, 15+gc.getFontMetrics().getHeight());

				gc.drawText ("\u2206 before calibration \u2206          \u2207 after calibration \u2207", 102, 310);

				gc.setForeground(lightBlueCol);
				gc.drawLine(5, 590, 445, 590);

				gc.setForeground(fcol);

				gc.drawText("mean delta before: " + String.format("%.2g %s",meanDeltaBef,devUnit), 5, 600);
				gc.drawText("mean delta after: "+String.format("%.2g %s",meanDeltaReady,devUnit), 5, 600+ 1*gc.getFontMetrics().getHeight());

				gc.drawText("\u03c3 before: "+String.format("%.2g %s",devBef,devUnit), 225, 600+ 0*gc.getFontMetrics().getHeight());
				gc.drawText("\u03c3 after: "+String.format("%.2g %s",devReady,devUnit), 225, 600+ 1*gc.getFontMetrics().getHeight());
				
				gc.drawText("3\u03c3 cut before: "+String.format("%.2g %s",absCut3Bef,devUnit), 5, 600+ 3*gc.getFontMetrics().getHeight());
				gc.drawText("3\u03c3 cut after: "+String.format("%.2g %s",absCut3Ready,devUnit), 5, 600+ 4*gc.getFontMetrics().getHeight());
				

				//gc.drawText("3\u03c3 cut before: "+absCut3Bef, 5, 600+ 5*gc.getFontMetrics().getHeight());
				//gc.drawText("3\u03c3 cut after:  "+absCut3Aft, 5, 600+ 6*gc.getFontMetrics().getHeight());

				gc.drawText("\u03c3 deviation difference: "+devImpFac +"%", 225, 600+ 3*gc.getFontMetrics().getHeight());
				gc.drawText("3\u03c3 quality difference: "+qualImpFac +"%",   225, 600+ 4*gc.getFontMetrics().getHeight());
				gc.drawText(String.format("Calibration: %.5g + %.5g * m/z", fitA+fit2A, fitB+fit2B), 5, 600+ (int)(6.5*gc.getFontMetrics().getHeight()));
				
				/*gc.setForeground(lightgreyCol);
				x1=50;
				x2=400;
				y1= 300+250-200*((( fit2A +  fit2B * minMass)-minDelta)/yrange);
				y2= 300+250-200*((( fit2A +  fit2B * maxMass)-minDelta)/yrange);
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	*/
				
				gc.setForeground(lightGreenCol);
				x1=50;
				x2=400;
				y1= 250-200*((( (fitA+fit2A) + (fitB+fit2B)*minMass   )-minDelta)/yrange);
				y2= 250-200*((( (fitA+fit2A) + (fitB+fit2B)*maxMass   )-minDelta)/yrange);
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	
				
				gc.setForeground(lightGreenCol);
				x1=50;
				x2=400;
				y1= 300+250-100;
				y2= 300+250-100;
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	
				
//				3sigma lines after
				gc.setForeground(lightgreyCol);
				x1=50;
				x2=400;
				y1= 300 + 250-200*((3*devFin -minDelta)/yrange);
				y2= y1;
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	
				gc.setForeground(lightgreyCol);
				x1=50;
				x2=400;
				y1= 300 + 250-200*((-3*devFin -minDelta)/yrange);
				y2= y1;
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	
				
//				3sigma lines before
				gc.setForeground(lightgreyCol);
				x1=50;
				x2=400;
				y1= 250-200*((Math.abs(meanDeltaBef) + 3*devBef -minDelta)/yrange);
				y2= y1;
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	
				gc.setForeground(lightgreyCol);
				x1=50;
				x2=400;
				y1= 250-200*((-Math.abs(meanDeltaBef)-3*devBef -minDelta)/yrange);
				y2= y1;
				gc.drawLine((int)x1,(int)y1,(int)x2,(int)y2);	
				gc.setForeground(lightBlueCol);
				gc.drawLine (225, 50, 225, 250);
				gc.drawText (String.format("%.3g",maxDelta), 228,50);
				gc.drawText (String.format("%.3g",minDelta), 228,250-gc.getFontMetrics().getHeight());
				gc.drawLine (50,150,400,150);	
				gc.drawText(String.format("%.3g",maxMass),402, 152);
				gc.drawText(String.format("%.3g",minMass),52 , 152);

			}
			else{
				gc.drawText("Need at least 10 mascot peptide identifications for calibration", 10, 30);
				if(countPeps>0)gc.drawText("This dataset has "+countPeps + " peptide identifications", 10, 50);
				gc.drawText("Please load a *.mascot xml file to use this feature", 10, 70);
				clipButton.setEnabled(false);
				pngButton.setEnabled(false);
				pmcButton.setEnabled(false);
				Toolkit.getDefaultToolkit().beep();
			}
		}
		else{
			gc.drawText("Please load a mascot xml file to use this feature", 10, 90);
		}
		pmcCanvas.setBackgroundImage(image);
		gc.dispose();
		pmcCanvas.redraw();
		
		// eventhandler
		SelectionAdapter closeDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){

				// get the shell object
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				dialogComposite.getShell().close();
			}
		};
		
		
		SelectionAdapter pngToClipboardDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
			imageToClipboard(image);	
			}
		};
		
		SelectionAdapter toClipboardDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
			    String export ="";
			    String devUnit="[m/z]";
			    export +=  "Datafile: "+records[0].getFileName()+"\n";
			    export +=  "Number of spectra: "+records[0].getSpectrumList().size()+"\n";
			    export +=  "Peptides used for Calibration: "+ countPeps+"\n" ;
			    export +=  String.format("Calibration: %.5g + %.5g * m/z\n", fitA+fit2A, fitB+fit2B) ;
				export +=  "Mean delta before: " + String.format("%.2g %s\n",meanDeltaBef,devUnit) ;
				export +=  "Mean delta after: "+String.format("%.2g %s\n",meanDeltaReady,devUnit) ;
				export +=  "\u03c3 before: "+String.format("%.2g %s\n",devBef,devUnit) ;
				export +=  "\u03c3 after: "+String.format("%.2g %s\n",devReady,devUnit) ;
				export +=  "3\u03c3 cut before: "+String.format("%.2g %s\n",absCut3Bef,devUnit) ;
				export +=  "3\u03c3 cut after: "+String.format("%.2g %s\n",absCut3Ready,devUnit) ;
				export +=  "\u03c3 deviation difference: "+devImpFac +"%\n" ;
				export +=  "3\u03c3 quality difference: "+qualImpFac +"%\n" ;
				
				long now = System.currentTimeMillis();
				Date d = new Date(now);
				String tmpDate=String.format("%tD", d) + " " + String.format("%tR", now);
				SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
				tmpDate=df.format( d );
				export += "deltaMasses version: "+DeltaMasses.version+" "+DeltaMasses.build+"\n";
				export += "user: "+System.getProperty("user.name") + " date: " + tmpDate ;
				
				setClipboard(export);
				logger.info("Post Measurement Calibration exported to ClipBoard");
			}
		};
		
		SelectionAdapter pmcButtonEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				// get the shell object
				//final Button button = (Button)e.getSource();
				//final Composite dialogComposite = (Composite)button.getParent();	
				//FileDialog dialog = new FileDialog (dialogComposite.getShell(), SWT.SAVE);
				//dialog.setText("save calibrated .cal.mgf");
				//dialog.setFilterNames (new String [] {".mgf Files", ".mgf Files (*.mgf*)"});
				//dialog.setFilterExtensions (new String [] {"*.mgf", "*.*"}); //Windows wild cards
				//dialog.setFilterPath ("C:\\"); // or is  it  \\Windows path
				String tmpFile=records[0].getFileName();//TODO put the "filename before mascot" in here !
				tmpFile+=".cal.mgf";
				//tmpFile.replaceFirst(".*/","");
				//dialog.setFileName (tmpFile);
				//if(dialog.open()!=null){
				logger.info("exporting calibrated .mgf to:"+tmpFile);
				records[0].printRecalibratedMgf(fitA+fit2A,fitB+fit2B,tmpFile);
				logger.info("export finished.");
				Toolkit.getDefaultToolkit().beep();
				}
		};
		
		cancelButton.addSelectionListener(closeDialogEvent);
		clipButton.addSelectionListener(toClipboardDialogEvent);
		pmcButton.addSelectionListener(pmcButtonEvent);
		pngButton.addSelectionListener(pngToClipboardDialogEvent);

		// show the screen
		dialogShell.open();	
	}
	public static void setClipboard(String str) {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
        Toolkit.getDefaultToolkit().beep();
    }
	public static void imageToClipboard(Image img) {
		//convert incomming swt image to an awt image first
		org.eclipse.swt.graphics.ImageData imData = img.getImageData();
		java.awt.image.BufferedImage buffIm = Snippet156.convertToAWT(imData);
		java.awt.Image awtImage=null;
		awtImage=(java.awt.Image)buffIm;
		ImageSelection.copyImageToClipboard(awtImage);
		Toolkit.getDefaultToolkit().beep();
    }
}