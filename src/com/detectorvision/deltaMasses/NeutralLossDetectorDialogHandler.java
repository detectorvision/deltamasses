/* $Id: NeutralLossDetectorDialogHandler.java 310 2010-05-17 13:18:55Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.biodata.Spectrum;
import com.detectorvision.utility.ImageSelection;
import com.detectorvision.utility.Snippet156;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.rmi.server.ExportException;
import java.security.spec.MGF1ParameterSpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.jfree.chart.plot.GreyPalette;

/**
 * Eventhandler for the mdhDialog
 * @author frank@detectorvision.com
 */
public class NeutralLossDetectorDialogHandler extends SelectionAdapter{
	private Shell mainShell;
	private Record[] records;

	Image image;
	public SpectraPair currentPair;
	ArrayList<MSMS> msmsA;
	ArrayList<MSMS> msmsB;

	static double lg2=Math.log(2.0);

	// Logging with log4j
	static Logger logger = Logger.getLogger(NeutralLossDetectorDialogHandler.class.getName());

	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public NeutralLossDetectorDialogHandler(Record[] records){
		this.records = records;
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){
		// get the mainshell
		if(e.getSource() instanceof MenuItem){
			this.mainShell = ((MenuItem)e.getSource()).getParent().getShell();
		}
		else  if(e.getSource() instanceof Button) {
			this.mainShell = ((Button)e.getSource()).getParent().getShell();
		}

		logger.info("mdhDialogHandler:mdh selected");

		Map openWidgets = null;
		Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/neutralLossDetectorScreen.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			logger.error("pmcDialogHandler:XSWTException:"+error.toString());
		}
		dialogShell.pack();

		// objectreferences
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		cancelButton.setToolTipText("closes this window");

		Button pngButton = (Button)openWidgets.get("pngButton");
		pngButton.setToolTipText("export graphics to clipboard");
		pngButton.setEnabled(true);

		final Canvas pmcCanvas = (Canvas)openWidgets.get("pmcCanvas");

		Color bcol =  new Color(dialogShell.getDisplay(), 255, 255, 255 );
		Color fcol =  new Color(dialogShell.getDisplay(), 66, 66, 66 );
		Color boxcol = new Color(dialogShell.getDisplay(),142,142,142);
		Color detectorvisionBlue =  new Color(dialogShell.getDisplay(), 0, 155, 255 );
		Color personalEditionGreen =  new Color(dialogShell.getDisplay(), 55, 255, 55 );
		Color lightGreenCol =  new Color(dialogShell.getDisplay(), 20, 140, 20 );
		
		Color superlightgreyCol =  new Color(dialogShell.getDisplay(), 238, 238, 238 );
		Color lightgreyCol =  new Color(dialogShell.getDisplay(), 188, 188, 188 );
		Color lightlightgreyCol =  new Color(dialogShell.getDisplay(), 222, 222, 222 );
		Color redCol =  new Color(dialogShell.getDisplay(), 200, 21, 41 );
		Color orange =  new Color(dialogShell.getDisplay(), 255, 150, 55 );

		
		int boxwidth=800;
		int ybox=133;//repeat distance of boxes
		int boxleft=ybox-100;//distance of boxed from left border cosmetic formula
		int yb1=ybox*1;//bottom of plot 1
		int yb2=ybox*2;//bottom of plot 2
		int yb3=ybox*3;//bottom of plot 3
		int yb4=ybox*4;//bottom of plot 4
		int ybase=ybox*5;//bottom of plot
		int shiftLabel=-98;//how far the labels in the box move
		
		image = new Image(dialogShell.getDisplay(),1200, 620); 
		GC gc = new GC (image);

		gc.setForeground(superlightgreyCol);
		gc.setBackground(superlightgreyCol);
		gc.fillRectangle(boxleft,yb1-100,800,100);
		gc.fillRectangle(boxleft,yb2-100,800,100);
		gc.fillRectangle(boxleft,yb3-100,800,100);
		gc.fillRectangle(boxleft,yb4-100,800,100);
		
		gc.setForeground(fcol);
		gc.setBackground(bcol);
		

		if(this.records[0] != null && records[0].getCurrentSpectraPair() != null){
			this.currentPair=records[0].getCurrentSpectraPair();
			logger.info("filename:" + this.records[0].getFileName());
			logger.info("origin method:"+records[0].getOriginMethod());

			Spectrum spec1= this.currentPair.spectrumA;
			Spectrum spec2 = this.currentPair.spectrumB;


			{
				int histo[] = new int[800];//up to 400 Dalton in 1 Dalton bins
				{
					for(MSMS MSMSa:spec1.valueList){
						for(MSMS MSMSb:spec2.valueList){
							double delta= (MSMSb.massToCharge - MSMSa.massToCharge);
							if(delta>=-400 && delta<=400){
								int bin = (int)(400+delta);
								if(bin==800)bin=799;
								if(bin>799 || bin <0){logger.fatal("bin error:"+bin+" delta:"+delta);System.exit(1);}
								boolean isInLight=false;
								for(MSMS MSMSc:spec1.valueList){
									if(Math.abs(MSMSb.massToCharge-MSMSc.massToCharge)<records[0].getMsmsPrecision()){
										isInLight=true;break;
									}
								}
								for(MSMS MSMSc:spec2.valueList){
									if(Math.abs(MSMSa.massToCharge-MSMSc.massToCharge)<records[0].getMsmsPrecision()){
										isInLight=true;break;
									}
								}

								if(Math.abs(delta)<records[0].getMsmsPrecision()){isInLight=false;}
								if(!isInLight){
									histo[bin]++;	
								}
							}
						}
					}

					int ahisto[] = new int[800];//up to 200 Dalton in 1 Dalton bins
					for(MSMS MSMSa:spec1.valueList){
						for(MSMS MSMSb:spec1.valueList){
							double delta= (MSMSb.massToCharge - MSMSa.massToCharge);
							if(delta>=-400 && delta<=400){
								int bin = (int)(400+delta);
								if(bin==800)bin=799;
								if(bin>799 || bin <0){logger.fatal("bin error:"+bin+" delta:"+delta);System.exit(1);}
								ahisto[bin]++;							
							}
						}
					}
					for(MSMS MSMSa:spec2.valueList){
						for(MSMS MSMSb:spec2.valueList){
							double delta= (MSMSb.massToCharge - MSMSa.massToCharge);
							if(delta>=-400 && delta<=400){
								int bin = (int)(400+delta);
								if(bin==800)bin=799;
								if(bin>799 || bin <0){logger.fatal("bin error:"+bin+" delta:"+delta);System.exit(1);}
								ahisto[bin]++;							
							}
						}
					}

					int localMax=Integer.MIN_VALUE;
					int maxLocation=0;
					for(int i=0;i<800;i++){
						if(histo[i]>localMax){
							localMax=histo[i];
							maxLocation=i;	
						}
					}

					int tmpDm=(int)(400+spec2.precursorMass-spec1.precursorMass);
					int tmpDmHalf=(int)(400+((spec2.precursorMass-spec1.precursorMass)/2.0));
					int tmpDmThird=(int)(400+((spec2.precursorMass-spec1.precursorMass)/3.0));
					
					gc.setForeground(lightlightgreyCol);
					int tmpMem=gc.getLineStyle();
					gc.setLineStyle(SWT.LINE_DOT);
					gc.setForeground(redCol);
					gc.drawLine(boxleft+tmpDm, yb4, boxleft+tmpDm, yb1-100);//red channel vertical line
					
					gc.setForeground(lightgreyCol);
					gc.drawLine(boxleft+tmpDmHalf, yb4, boxleft+tmpDmHalf, yb1-100);//red channel double charge
					
					gc.setForeground(lightgreyCol);
					gc.drawLine(boxleft+tmpDmThird, yb4, boxleft+tmpDmThird, yb1-100);//red channel triple charge
				
					gc.setForeground(lightgreyCol);
					gc.drawLine(boxleft+tmpDmHalf, yb4, boxleft+tmpDmHalf, yb1-100);//red channel double charge
				
					
					gc.setForeground(lightGreenCol);
					gc.drawLine(boxleft+400, yb4, boxleft+400, yb1-100);//green channel vertical line
					gc.setForeground(redCol);
					gc.drawString(""+(int)(spec2.precursorMass-spec1.precursorMass), boxleft+tmpDm, yb4+2);
					gc.setLineStyle(tmpMem);

					//SIGNAL
					for(int i=0;i<799;i++){
						int tmp = (int)(100.0*((double)histo[i]/(double)localMax));
						int tmp3 = (int)(100.0*((double)histo[i+1]/(double)localMax));
						gc.setForeground(fcol);
						gc.drawLine(boxleft+i,yb1-tmp,boxleft+i+1,yb1-tmp3);
					}
				}
				histo=null;

				int histo2[] = new int[800];//up to 400 Dalton in 1 Dalton bins
				double deltaMass = spec2.precursorMass-spec1.precursorMass;
				for(MSMS MSMSa:spec1.valueList){
					//check if MSMSa is found in spec 2
					boolean aIsInSpec2=false;
					for(MSMS MSMSf:spec2.valueList){
						if(Math.abs(MSMSf.massToCharge-MSMSa.massToCharge)<records[0].getMsmsPrecision()){
							aIsInSpec2=true;break;
						}
					}
					for(MSMS MSMSb:spec2.valueList){
						if( Math.abs(MSMSb.massToCharge - MSMSa.massToCharge-deltaMass)  < records[0].getMsmsPrecision()){
							//check if MSMSb is found in spec1
							boolean bIsInspec1=false;
							for(MSMS MSMSe:spec1.valueList){
								if(Math.abs(MSMSe.massToCharge-MSMSb.massToCharge)<records[0].getMsmsPrecision()){
									bIsInspec1=true;break;
								}
							}
							for(MSMS MSMSC:spec2.valueList){
								double delta = MSMSC.massToCharge-MSMSa.massToCharge;
								boolean isInLightSpec=false;
								for(MSMS MSMSd:spec1.valueList){
									if(Math.abs(MSMSC.massToCharge-MSMSd.massToCharge)<records[0].getMsmsPrecision()){
										isInLightSpec=true;break;	
									}
								}
								if(!aIsInSpec2 && !bIsInspec1 && !isInLightSpec && delta>=-400 && delta<=400){
									int bin = (int)(400+delta);
									if(bin==800)bin=799;
									if(bin>799 || bin <0){logger.fatal("bin error:"+bin+" delta:"+delta);System.exit(1);}
									histo2[bin]++;							
								}
							}
						}
					}
				}
				int localMax=Integer.MIN_VALUE;
				int maxLocation=0;
				for(int i=0;i<800;i++){
					if(histo2[i]>localMax){
						localMax=histo2[i];
						maxLocation=i;	
					}
				}

				//////////////////////////////////percent hits explained start
	            int memgreen1=-1000;
	            int memred1=-1000;
	            int memgreen2=-1000;
	            int memred2=-1000;
				double [] ticTimesHits = new double[801];
				for(int i=-400;i<=400;i++){//801 numbers
					int greenBin=0;int redBin=0;  			//number of hits on green and red cannel
					double ticGreen=0; double ticRed=0;		//total ion count on green and red channels
					boolean [] specAGreenHit = new boolean[records[0].getCurrentSpectraPair().spectrumA.valueList.size()];
					boolean [] specBGreenHit = new boolean[records[0].getCurrentSpectraPair().spectrumB.valueList.size()];

					boolean [] specARedHit = new boolean[records[0].getCurrentSpectraPair().spectrumA.valueList.size()];
					boolean [] specBRedHit = new boolean[records[0].getCurrentSpectraPair().spectrumB.valueList.size()];

					double deltaLoop=(double)i;

					for(int s1=0;s1<spec1.valueList.size();s1++){
						for(int s2=0;s2<spec2.valueList.size();s2++){
							double delta= (spec2.valueList.get(s2).massToCharge - spec1.valueList.get(s1).massToCharge);
							if(delta>=-400 && delta<=400){
								int bin = (int)(400+delta);
								if(bin==800)bin=799;
								if(bin>799 || bin <0){logger.fatal("bin error:"+bin+" delta:"+delta);System.exit(1);}
								if(Math.abs(delta)<records[0].getMsmsPrecision()){
									if(!specAGreenHit[s1] && !specBGreenHit[s2]){
										ticGreen+=spec1.valueList.get(s1).intensity+ spec2.valueList.get(s2).intensity;
									}
									specAGreenHit[s1]=true;
									specBGreenHit[s2]=true;
								}						
							}
						}
					}
					for(int s1=0;s1<spec1.valueList.size();s1++){
						for(int s2=0;s2<spec2.valueList.size();s2++){
							double delta= (spec2.valueList.get(s2).massToCharge - spec1.valueList.get(s1).massToCharge);
							if(delta>=-400 && delta<=400){
								int bin = (int)(400+delta);
								if(bin==800)bin=799;
								if(bin>799 || bin <0){logger.fatal("bin error:"+bin+" delta:"+delta);System.exit(1);}
								if(Math.abs(delta-deltaLoop)<=0.5 && !specAGreenHit[s1] && !specBGreenHit[s2]){
									if(!specARedHit[s1] && !specBRedHit[s2]){
										ticRed+=spec1.valueList.get(s1).intensity+ spec2.valueList.get(s2).intensity;
									}
									specARedHit[s1]=true;
									specBRedHit[s2]=true;
								}						
							}
						}
					}

					double totalTic=0;

					for(int s1=0;s1<spec1.valueList.size();s1++){
						totalTic+=spec1.valueList.get(s1).intensity;
						if(specAGreenHit[s1])greenBin++;
						if(specARedHit[s1])redBin++;
					}
					for(int s2=0;s2<spec2.valueList.size();s2++){
						totalTic+=spec2.valueList.get(s2).intensity;
						if(specBGreenHit[s2])greenBin++;
						if(specBRedHit[s2])redBin++;
					}

					int totSignals=spec1.valueList.size()+spec2.valueList.size();
					int tmp = (int)(100.0*((double)redBin/(double)totSignals));
					
					
					
					
					gc.setForeground(redCol);
					if(memred1>-100){
					gc.drawLine(boxleft+i+400-1,yb2-memred1,boxleft+i+400,yb2-tmp);
					}
					memred1=tmp;
					
					int tmp2=tmp+(int)(100.0*((double)greenBin/(double)totSignals));
					if(memgreen1>-100){
					gc.setForeground(fcol);
					gc.drawLine(boxleft+i+400-1,yb2-memgreen1,boxleft+i+400,yb2-tmp2);
					
					//draws the horizontal green line, stupid way to do it ....
					gc.setForeground(lightGreenCol);
					int tmp322=(int)(100.0*((double)greenBin/(double)totSignals));
					gc.drawLine(boxleft+i+400-1,yb2-tmp322,boxleft+i+400,yb2-tmp322);
					
					//green channel label
					gc.setBackground(superlightgreyCol);
					gc.drawString("  "+tmp322+" % ",boxleft, yb2-tmp322-7);
					}
					memgreen1=tmp2;
							
					///////////tic explained plot
					tmp = (int)(100.0*((double)ticRed/(double)totalTic));
					
					if(memred2>-100){
					gc.setForeground(redCol);
					gc.drawLine(boxleft+i+400-1,yb3-memred2,boxleft+i+400,yb3-tmp);
					}
					memred2=tmp;
					
					tmp2=tmp+(int)(100.0*((double)ticGreen/(double)totalTic));
					if(memgreen2>-100){
					gc.setForeground(fcol);
					gc.drawLine(boxleft+i+400-1,yb3-memgreen2,boxleft+i+400,yb3-tmp2);
					}
					memgreen2=tmp2;
					
					//draws the horizontal green line, stupid way to do it ...
					gc.setForeground(lightGreenCol);
					int tmp422=(int)(100.0*((double)ticGreen/(double)totalTic));
					gc.drawLine(boxleft+i+400-1,yb3-tmp422,boxleft+i+400,yb3-tmp422);

					ticTimesHits[i+400]=((ticRed)/totalTic)*(((double)redBin)/(double)totSignals);
					
					
					//green chanel label
					gc.setBackground(superlightgreyCol);
					gc.setForeground(lightGreenCol);
					gc.drawString("  "+tmp422+" % ",boxleft, yb3-tmp422-7);
				}
				
				//draw 50% lines in match and tic panel
				gc.setForeground(detectorvisionBlue);
				int tmpMem=gc.getLineStyle();
				gc.setLineStyle(SWT.LINE_DOT);
				gc.drawLine(boxleft+0, yb3-50, boxleft+800, yb3-50);
				gc.drawLine(boxleft+0, yb2-50, boxleft+800, yb2-50);
				gc.setLineStyle(tmpMem);//reset linestyle 
				


				
				
				gc.setForeground(detectorvisionBlue);
				gc.setBackground(superlightgreyCol);
				gc.drawString("50% line",boxleft+755, yb3-57);
				gc.drawString("50% line",boxleft+755, yb2-57);
				gc.setForeground(fcol);
				gc.drawString("light-heavy spec deltaMass histogram", boxleft+5, yb1+shiftLabel);
				gc.drawString("matched signals, percentage", boxleft+5, yb2+shiftLabel);
				gc.drawString("matched tic, percentage", boxleft+5, yb3+shiftLabel);
				gc.drawString("integrated detector signal", boxleft+5, yb4+shiftLabel);
				gc.setBackground(superlightgreyCol);
				gc.drawString("information entropy:"+String.format("%.1f",informationEntropy(ticTimesHits))+"[bit]", boxleft+650, yb4+shiftLabel);
				gc.setBackground(bcol);
				
				
				double maxTicTimesHits=Double.MIN_VALUE;
				for(int i=0;i<801;i++){
					maxTicTimesHits=Math.max(maxTicTimesHits, ticTimesHits[i]);	
				}
				for(int i=0;i<800;i++){
					gc.setForeground(fcol);
					int tmp  = (int)(100.0*((double)ticTimesHits[i]/(double)maxTicTimesHits));
					int tmp2 = (int)(100.0*((double)ticTimesHits[i+1]/(double)maxTicTimesHits));
					gc.setForeground(fcol);
					gc.drawLine(boxleft+i,yb4-tmp,boxleft+i+1,yb4-tmp2);
				}
				//////////////////////////////////percent hits explained end
			}
            
			//draw boxes and tics and scales
			gc.setForeground(boxcol);
			gc.drawLine(boxleft,yb1,boxleft+boxwidth,yb1);
			gc.drawLine(boxleft,yb1-100,boxleft+boxwidth,yb1-100);
			gc.drawLine(boxleft,yb1,boxleft,yb1-100);
			gc.drawLine(boxleft+boxwidth,yb1,boxleft+boxwidth,yb1-100);
			for(int i=0;i<=800;i+=100){
				gc.drawLine(boxleft+i, yb1+5, boxleft+i,yb1);
				gc.drawString(""+(i-400), boxleft+i, yb1+10);
			}
			for(int i=10;i<=790;i+=10){
				gc.drawLine(boxleft+i, yb1+2, boxleft+i,yb1);
			}
			for(int i=50;i<=750;i+=50){
				gc.drawLine(boxleft+i, yb1+5, boxleft+i,yb1);
			}
			

			gc.drawLine(boxleft,yb2,boxleft+boxwidth,yb2);
			gc.drawLine(boxleft,yb2-100,boxleft+boxwidth,yb2-100);
			gc.drawLine(boxleft,yb2,boxleft,yb2-100);
			gc.drawLine(boxleft+boxwidth,yb2,boxleft+boxwidth,yb2-100);
			for(int i=0;i<=800;i+=100){
				gc.drawLine(boxleft+i, yb2+5, boxleft+i,yb2);
				gc.drawLine(boxleft+i, yb2-5-100, boxleft+i,yb2-100);
				gc.drawString(""+(i-400), boxleft+i, yb2+10);
			}
			for(int i=10;i<=790;i+=10){
				gc.drawLine(boxleft+i, yb2+2, boxleft+i,yb2);
				gc.drawLine(boxleft+i, yb2-2-100, boxleft+i,yb2-100);
			}
			for(int i=50;i<=750;i+=50){
				gc.drawLine(boxleft+i, yb2+5, boxleft+i,yb2);
				gc.drawLine(boxleft+i, yb2-5-100, boxleft+i,yb2-100);
			}
			
			
			gc.drawLine(boxleft,yb3,boxleft+boxwidth,yb3);
			gc.drawLine(boxleft,yb3-100,boxleft+boxwidth,yb3-100);
			gc.drawLine(boxleft,yb3,boxleft,yb3-100);
			gc.drawLine(boxleft+boxwidth,yb3,boxleft+boxwidth,yb3-100);
			for(int i=0;i<=800;i+=100){
				gc.drawLine(boxleft+i, yb3+5, boxleft+i,yb3);
				gc.drawLine(boxleft+i, yb3-5-100, boxleft+i,yb3-100);
				gc.drawString(""+(i-400), boxleft+i, yb3+10);
			}
			for(int i=10;i<=790;i+=10){
				gc.drawLine(boxleft+i, yb3+2, boxleft+i,yb3);
				gc.drawLine(boxleft+i, yb3-2-100, boxleft+i,yb3-100);
			}
			for(int i=50;i<=750;i+=50){
				gc.drawLine(boxleft+i, yb3+5, boxleft+i,yb3);
				gc.drawLine(boxleft+i, yb3-5-100, boxleft+i,yb3-100);
			}
			
		
			gc.drawLine(boxleft,yb4,boxleft+boxwidth,yb4);
			gc.drawLine(boxleft,yb4-100,boxleft+boxwidth,yb4-100);
			gc.drawLine(boxleft,yb4,boxleft,yb4-100);
			gc.drawLine(boxleft+boxwidth,yb4,boxleft+boxwidth,yb4-100);
			for(int i=0;i<=800;i+=100){
				gc.drawLine(boxleft+i, yb4+5, boxleft+i,yb4);
				gc.drawLine(boxleft+i, yb4-5-100, boxleft+i,yb4-100);
				gc.drawString(""+(i-400), boxleft+i, yb4+10);
			}
			for(int i=10;i<=790;i+=10){
				gc.drawLine(boxleft+i, yb4+2, boxleft+i,yb4);
				gc.drawLine(boxleft+i, yb4-2-100, boxleft+i,yb4-100);
			}
			for(int i=50;i<=750;i+=50){
				gc.drawLine(boxleft+i, yb4+5, boxleft+i,yb4);
				gc.drawLine(boxleft+i, yb4-5-100, boxleft+i,yb4-100);
			}
			
			
	
			
			int tbase=-7;
			int xtbase=boxleft+boxwidth+33;
			gc.drawString("light spectrum:", 10, ybase+40);
			gc.drawString(""+records[0].getCurrentSpectraPair().spectrumA.title, xtbase, tbase+40);
			gc.drawString("heavy spectrum:", 10, ybase+60);
			gc.drawString(""+records[0].getCurrentSpectraPair().spectrumB.title, xtbase, tbase+60);
			double dm=records[0].getCurrentSpectraPair().spectrumB.precursorMass-records[0].getCurrentSpectraPair().spectrumA.precursorMass;
			gc.drawString("deltaMass:",10, ybase+80);gc.drawString(""+String.format("%.6f Dalton", dm),xtbase, tbase+80);
			gc.drawString("originating from file:", 10, ybase+100);gc.drawString(""+records[0].getFileName(), xtbase, tbase+100);

			gc.setForeground(fcol);
			gc.drawString("deltaMass [Dalton]", boxleft+370, yb4+45);
			gc.drawString("histogram bin width: 1 Dalton", boxleft+647, yb4+45);
			
			
			
			if(!DeltaMasses.isDiscoveryEdition){
				gc.setForeground(personalEditionGreen);
				for(int i=-ybase;i<1200;i+=75){
					gc.drawLine(i,0,i+ybase, ybase);
				}
				gc.setBackground(bcol);
				gc.drawString("the diagonal green lines are not present in Discovery Edition", 35, 6);
			}
		}
		else{
			gc.drawString("no spectrum pair selected, please select one and try again", 10, 10);

		}

		pmcCanvas.setBackgroundImage(image);
		gc.dispose();
		pmcCanvas.redraw();

		// eventhandler
		SelectionAdapter closeDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
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

		cancelButton.addSelectionListener(closeDialogEvent);
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
	public static double informationEntropy(double[] in){
		//see http://en.wikipedia.org/wiki/Information_entropy
		//turn in[] into a probability measure (normalize)
		double totalCount=0;
		for(int i=0;i<in.length;i++){
			totalCount+=in[i];
		}
		if(totalCount != 0){
			for(int i=0;i<in.length;i++){
				in[i]/=totalCount;//now this is a probability measure
			}
		}

		//calculate the information entropy
		double entropy=0;
		for(int i=0;i<in.length;i++){
			if(in[i]>0){
				entropy -= in[i]*log2(in[i]);
			}
		}
		return entropy;
	}

	public static double log2(double d) {
		return Math.log(d)/lg2;
	}



}