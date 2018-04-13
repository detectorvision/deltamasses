package com.detectorvision.massspectrometry.analyzation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.AbstractList;
import java.util.ArrayList;

import com.detectorvision.graphTheory.AbstractLink;
import com.detectorvision.graphTheory.AbstractNets;
import com.detectorvision.massspectrometry.biodata.MoleculeLink;
import com.detectorvision.massspectrometry.biodata.Spectrum;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.ExternalFileHandler;
import com.detectorvision.utility.pdf.HeaderFooter;
import com.detectorvision.utility.pdf.HeaderFooterLandscape;
import com.lowagie.text.Anchor;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class DeltaDetector {
	
	
	public static void detectMain(int s1, int s2, double accuracy){
		try {
			int spec1_id=603;
			int spec2_id=360;

			//spec1_id=s1;
			//spec2_id=s2;

			
			
		
			Spectrum spec1= DeltaMassBase.getSpectrum(spec1_id);
			Spectrum spec2= DeltaMassBase.getSpectrum(spec2_id);		
			spec1.calculateMoleculeLinks(accuracy);
			System.out.println("_________________________");
			spec2.calculateMoleculeLinks(accuracy);
					
			long startTime=System.currentTimeMillis();

			double dm=0;
			int countComparisons=0;
			double minMass=-400.0;
			double maxMass= 400.0;
			double width=maxMass-minMass;
			int bins=0;
			bins = (int)((width)/accuracy);//TODO if this division leaves a rest we might get into trouble below
			double binwidth=width/bins;
			double[] histogram = new double[bins];
			double[] tmphist= new double[bins];

			System.out.println("bins:"+bins+" \tbinwidth:"+binwidth);

			int bin=0;
			countComparisons=0;int total=0;
			for(int i=0;i<spec1.valueList.size();i++){
				for(int j=0;j<spec2.valueList.size();j++){
					total++;
					dm=spec1.valueList.get(i).massToCharge-spec2.valueList.get(j).massToCharge;
					if(dm>=minMass && dm <=maxMass){
						countComparisons++;
						bin=(int)((dm-minMass)/binwidth);
						if(bin<0 || bin > bins-1){
							System.out.println("bin error. bin:"+bin);
							System.exit(1);
						}
						else{
							histogram[bin]+=spec1.valueList.get(i).intensity*spec2.valueList.get(j).intensity;
						}
					}
				}
			}

			double maxcount=Double.MIN_VALUE;
			int maxcountbin=0;
			for(int i=0;i<bins;i++){
				if(histogram[i]>maxcount){
					maxcount=(double)histogram[i];
					maxcountbin=i;
				}
			}
			System.out.println("maxcount:"+maxcount+"\t"+maxcountbin);


			//get a list of NUM_SIGNALS most intense signals/////////////////////////
			int NUM_SIGNALS=15;
			TwoDoubles[] histoSignals = new TwoDoubles[NUM_SIGNALS];
			
			//copy histogram into tmphist array
			for(int i=0;i<histogram.length;i++){
				tmphist[i]=histogram[i];
			}
			for(int i=0;i<histoSignals.length;i++){
				double max=Double.MIN_VALUE;
				int maxbin=0;
				for(int j=0;j<tmphist.length;j++){
					if(tmphist[j]>max){
						max=tmphist[j];
						maxbin=j;
					}
				}
				TwoDoubles tmpd= new TwoDoubles();
				tmpd.x=minMass+maxbin*binwidth;
				tmpd.y=max;
				histoSignals[i]=tmpd;
				for(int j=maxbin-5;j<=maxbin+5;j++){
					if(j>0 && j<bins){
						tmphist[j]=0;
					}
				}
			}
			
			
			
			
			long endTime=System.currentTimeMillis();
			//////////////////////////////////////////////////////end histogram production


			Document document =  new Document(PageSize.A4.rotate(), 45, 40, 110, 50);//left right top bottom
			document.setMarginMirroring(true);
			document.addTitle("deltaDetector PTM detection core routine");
			document.addSubject("Detection of protein modifications");
			document.addKeywords("PTM PTM-Detection PTM-Localization deltaMasses Detectorvion AG Differential PTM Detection");
			document.addAuthor("deltaMasses used by"+System.getProperty("user.name"));

			try {
				Font fontSmall = new Font(Font.HELVETICA, 8);
				int pdfPictureWidth=750;
				int pdfPictureHeight=410;
				PdfWriter writer = PdfWriter.getInstance(document,
						new FileOutputStream("tmp/deltaDetector.pdf"));
				writer.setPageEvent(new HeaderFooterLandscape());

				document.open();
				document.add(new Paragraph("SugarDetector version 0.3 in cooperation with MPI Magdeburg"));

				String tmp1=String.format("%.2f", spec1.precursorMass);
				String tmp2=String.format("%.2f", spec2.precursorMass);

				document.add(new Paragraph("Spectrum 1: "+tmp1+"[Da]    spec_id: "+spec1_id+"     "+spec1.title));
				document.add(new Paragraph("Spectrum 2: "+tmp2+"[Da]    spec_id: "+spec2_id+"     "+spec2.title));

				double dpm = spec1.precursorMass-spec2.precursorMass;
				document.add(new Paragraph("Accuracy:    "+accuracy+" [Da]    Number of bins: "+bins+ "       deltaMass : "+String.format("%.4f [Da]", dpm)));	
				document.add(new Paragraph("Maximum count: "+maxcount));

				//put in the histogram
				PdfContentByte cb = writer.getDirectContent();
				int pdfWidth=750;
				int pdfHeight=250;
				int xlow=50;
				int ylow=100;




				cb.moveTo(xlow, ylow);
				cb.lineTo(xlow, ylow+pdfHeight);
				cb.stroke();

				cb.moveTo(xlow+pdfWidth, ylow);
				cb.lineTo(xlow+pdfWidth, ylow+pdfHeight);
				cb.stroke();

				cb.moveTo(xlow, ylow);
				cb.lineTo(xlow+pdfWidth, ylow);
				cb.stroke();

				cb.moveTo(xlow, ylow+pdfHeight);
				cb.lineTo(xlow+pdfWidth, ylow+pdfHeight);
				cb.stroke();

				cb.setLineWidth((float) 0.1);
				for(int i=0;i<histogram.length;i++){
					int x=0;
					int y=0;
					x=(int)( (double)(pdfWidth*i)/(double)bins    );
					y=(int) (pdfHeight*  ((double)histogram[i]/(double)maxcount));
					cb.moveTo(xlow+x, ylow);
					cb.lineTo(xlow+x, ylow+y);
					cb.stroke();
				}

				for(int i=0;i<histoSignals.length;i++){
					cb.beginText();
					BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
					cb.setFontAndSize(bf_helv, 6);
					double tmp=minMass+i*binwidth;
					String text = ""+String.format("%.2f", histoSignals[i].x);
					int x=(int)( (double)(pdfWidth*((histoSignals[i].x)-minMass)/(maxMass-minMass))   );
					int y=(int) (pdfHeight*  ((double)histoSignals[i].y/(double)maxcount));
					cb.showTextAligned(PdfContentByte.ALIGN_LEFT, text, xlow+x+1, ylow+y-5, 0);
					cb.endText();				

				}


				for(int i=-400;i<=400;i+=100){
					int x=(int)((double)(400+i)*((double)pdfWidth/(double)800));
					int y =-10;
					cb.moveTo(xlow+x, ylow);
					cb.lineTo(xlow+x, ylow+y);
					cb.stroke();
					cb.beginText();
					BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
					cb.setFontAndSize(bf_helv, 9);
					String text = ""+i;
					cb.showTextAligned(PdfContentByte.ALIGN_CENTER, text, xlow+x, ylow-20, 0);
					cb.endText();		
				}
				document.newPage();
				document.add(new Paragraph("SugarDetector version 0.3 in cooperation with MPI Magdeburg"));
				PrintSpectrum(0,2000,spec1,cb,50,260,750,150,true,false);
				PrintSpectrum(0,2000,spec2,cb,50,100,750,150,false,false);
				
				document.newPage();
				document.add(new Paragraph("SugarDetector version 0.3 in cooperation with MPI Magdeburg"));
				PrintSpectrum(0,2000,spec1,cb,50,100,750,150,true,true);
				
				document.newPage();
				document.add(new Paragraph("SugarDetector version 0.3 in cooperation with MPI Magdeburg"));
				PrintSpectrum(0,2000,spec2,cb,50,100,750,150,true,true);
				document.close();
			}
			catch(Error e){}

			try {//wait a bit before opening the doc ... 
				long numMillisecondsToSleep = 100; // 10th of a second
				Thread.sleep(numMillisecondsToSleep);
			} catch (InterruptedException eee) {
				System.out.println("sleep problems:"+eee.toString());
			}

			if (ExternalFileHandler.open("tmp/deltaDetector.pdf")!=0){
				System.out.println("graphicsToPdfAdapter:pdf error:Acrobat Reader not installled or file association for pdf is not there");
			}

			for(int i=0;i<bins;i++){
				double delta=minMass+binwidth*(i+0.5);
				String tmp = String.format("%.2f", delta);
				//System.out.println(""+i+"\t"+tmp+"\t"+histogram[i]);
			}


			System.out.println("total:"+total+"\t"+"did "+countComparisons+" msms peak comparisons time taken in milliseoncs:"+(endTime-startTime));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	
	public static void main(String[] args) {
		int sp1=Integer.parseInt(args[0]);
		int sp2=Integer.parseInt(args[1]);
		double acc=Double.parseDouble(args[2]);
		detectMain(sp1, sp2, acc);
		System.exit(1);
	}

	private static void PrintSpectrum(double showMinX, double showMaxX,Spectrum spec1, PdfContentByte cb, int lowx, int lowy, int xWidth, int yHeight, boolean upwards,boolean printNets) {
		double x,y,showWidth;
		showWidth=showMaxX-showMinX;
		//TODO check that positive and non-null.

		cb.setLineWidth((float) 1);

		//draw surrounding Kiste
		//bottom line
		cb.moveTo(lowx, lowy);
		cb.lineTo(lowx+xWidth, lowy);
		cb.stroke();

		//top line
		cb.moveTo(lowx, lowy+yHeight);
		cb.lineTo(lowx+xWidth, lowy+yHeight);
		cb.stroke();

		//left
		cb.moveTo(lowx, lowy);
		cb.lineTo(lowx, lowy+yHeight);
		cb.stroke();

		//right line
		cb.moveTo(lowx+xWidth, lowy);
		cb.lineTo(lowx+xWidth, lowy+yHeight);
		cb.stroke();


		//Add spectrum
		cb.setLineWidth((float) 0.1);
		spec1.minMZ=Double.MAX_VALUE;
		spec1.maxMZ=Double.MIN_VALUE;
		double maxSignal = Double.MIN_VALUE;
		for(int i=0;i<spec1.valueList.size();i++){
			if(spec1.valueList.get(i).massToCharge<spec1.minMZ){spec1.minMZ=spec1.valueList.get(i).massToCharge;}
			if(spec1.valueList.get(i).massToCharge>spec1.maxMZ){spec1.maxMZ=spec1.valueList.get(i).massToCharge;}
			if(spec1.valueList.get(i).intensity>maxSignal){maxSignal=spec1.valueList.get(i).intensity;}
		}
		spec1.rangeMZ=spec1.maxMZ-spec1.minMZ;
		if(spec1.rangeMZ<=0){spec1.rangeMZ=0.01;}//security. This is a divider below.

		for(int i=0;i<spec1.valueList.size();i++){
			if(spec1.valueList.get(i).massToCharge>=showMinX && spec1.valueList.get(i).massToCharge<showMaxX){
				x= xWidth*(spec1.valueList.get(i).massToCharge-showMinX)/showWidth;
				if(upwards){
					y=yHeight*spec1.valueList.get(i).intensity/maxSignal;
					cb.moveTo(lowx+(int)x, lowy);
					cb.lineTo(lowx+(int)x, lowy+(int)y);
					cb.stroke();
					
					if(spec1.valueList.get(i).intensity>=maxSignal/3.0){
						cb.beginText();
						BaseFont bf_helv = null;
						try {
							bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
						} catch (DocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						cb.setFontAndSize(bf_helv, 4);
						String text = ""+String.format("%.2f", spec1.valueList.get(i).massToCharge);
						cb.showTextAligned(PdfContentByte.ALIGN_LEFT, text, lowx+(int)x+1, lowy+(int)y-3, 0);
						cb.endText();				
					}
				}
				else{
					y=yHeight*spec1.valueList.get(i).intensity/maxSignal;
					cb.moveTo(lowx+(int)x, lowy+yHeight);
					cb.lineTo(lowx+(int)x, lowy+yHeight-(int)y);
					cb.stroke();
					
					
					if(spec1.valueList.get(i).intensity>=maxSignal/3.0){
						cb.beginText();
						BaseFont bf_helv = null;
						try {
							bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
						} catch (DocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						cb.setFontAndSize(bf_helv, 4);
						String text = ""+String.format("%.2f", spec1.valueList.get(i).massToCharge);
						cb.showTextAligned(PdfContentByte.ALIGN_LEFT, text, lowx+(int)x+1, lowy+yHeight-(int)y+3, 0);
						cb.endText();				
					}
				}
			}
		}
		
		if(printNets){
			int yOffset=0;
			//print non-one-link nets first
			System.out.println("printNets______________________");
			for(int i=0;i<spec1.abstractnets.abstractNets.size();i++){
				if(spec1.abstractnets.abstractNets.get(i).abstractNet.size()>1){
					yOffset+=10;
					System.out.println("printNets-next---");
					for(int j=0;j<spec1.abstractnets.abstractNets.get(i).abstractNet.size();j++){
						AbstractLink tmpLink = spec1.abstractnets.abstractNets.get(i).abstractNet.get(j);
						System.out.println("Link:"+tmpLink.start+"\t"+tmpLink.end+"\t"+tmpLink.id);
						
						double xStart= xWidth*(spec1.valueList.get(tmpLink.start).massToCharge-showMinX)/showWidth;
						double xEnd  = xWidth*(spec1.valueList.get(tmpLink.end).massToCharge-showMinX)/showWidth;
						double xMiddle = Math.abs(xEnd+xStart)/2.0;
						
						
						cb.beginText();
						BaseFont bf_helv = null;
						try {
							bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
						} catch (DocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						cb.setFontAndSize(bf_helv, 8);
						String text = ""+spec1.moleculeSearch.get(1).shortName;
						text=""+tmpLink.id;
						text=String.format("%.2f",spec1.moleculeLinks.get(tmpLink.id).dm);
						cb.showTextAligned(PdfContentByte.ALIGN_CENTER, text, lowx+(int)xMiddle, lowy+yHeight+yOffset+1, 0);
						cb.endText();	
						
						
					
						cb.moveTo(lowx+(int)xStart, lowy+yHeight+yOffset);
						cb.lineTo(lowx+(int)xEnd,   lowy+yHeight+yOffset);
						cb.stroke();
						
						//cb.moveTo(lowx+(int)xStart,   lowy+yHeight+yOffset-3);
						cb.setLineWidth((float)0.1);
						cb.setLineDash((float)2,(float)2);
						cb.moveTo(lowx+(int)xStart,   lowy);
						cb.setLineWidth((float)0.5);
						cb.lineTo(lowx+(int)xStart,   lowy+yHeight+yOffset+3);
						cb.stroke();
						cb.setLineDash((float)0);
						
						//cb.moveTo(lowx+(int)xEnd,     lowy+yHeight+yOffset-3);
						cb.setLineDash((float)2,(float)2);
						cb.setLineWidth((float)0.1);
						cb.moveTo(lowx+(int)xEnd,   lowy);
						cb.setLineWidth((float)0.5);
						
						cb.lineTo(lowx+(int)xEnd,     lowy+yHeight+yOffset+3);
						cb.stroke();
						cb.setLineDash((float)0);

						//yOffset+=3;
						
						
					}
				}
			}
			
		}




	}




}

