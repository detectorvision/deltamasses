/* $Id: PmcCanvas.java 235 2008-12-31 15:08:24Z frank $ */

package com.detectorvision.deltaMasses.gui.diagrams;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.detectorvision.deltaMasses.SettingsDialogHandler;
import com.detectorvision.massspectrometry.biodata.MSMS;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import org.apache.log4j.Logger;

public class PmcCanvas extends Diagram {
	Record record;
	SpectraPair pair;

	int borderLeft = 0; 
	int borderRight = 0;
	int borderTop = 0;
	int borderBottom = 20;

	int seperatorBarHeight = 30;
	int bottomBarHeight = 25;
	int topBarHeight = bottomBarHeight;

	int maxPeaks = 100;

	boolean needRecalculation = true;

	int ticksPerDivisor = 50; // for the scale

	SpectraPair currentPair;

	ArrayList<MSMS> msmsA;
	ArrayList<MSMS> msmsB;

	ArrayList<ArrayList<Double>> equalHits;
	ArrayList<ArrayList<Double>> deltaHits;
	ArrayList<ArrayList<Double>> z2Hits;//charge 2 hits build 65
	ArrayList<ArrayList<Double>> phospho98Hits; boolean isPhosphoPair=false;//build 61

	double zoomFactor = 1.0;

	double[] massToChargeA;
	double[] massToChargeB;

	double massToChargeMax;
	double massToChargeMin;

	double intensityMaxB;
	double intensityMaxA;

	double deltaMass;

	Color diagramBackgroundColor = new Color(getDisplay(), 255, 255, 255 );
	Color canvasBackgroundColor =  new Color(getDisplay(), 244, 244, 244 );

	Color upperSpectrumColor = new Color(getDisplay(), 0, 0, 76 );
	Color lowerSpectrumColor = upperSpectrumColor;

	Color diagramScaleColor = new Color( getDisplay(), 225, 225 , 225);

	Color topBarColor = new Color(getDisplay(), 149, 203, 239 );
	Color bottomBarColor = topBarColor;

	Color equalHitColor  = new Color(getDisplay(), 50 , 255,  50 );
	Color deltaHitColor  = new Color(getDisplay(), 255,  75,  75 );
	Color multiHitColor  = new Color(getDisplay(), 255, 165,   0 );
	Color phospho98Color = new Color(getDisplay(), 255, 255,  75 );//build 61
	Color z2HitColor     = new Color(getDisplay(), 255, 255,  255 );//build 65

	Color separatorBarColor =  new Color(getDisplay(), 185, 181, 170);

	Color fontColor = new Color(getDisplay(), 0,0,0);
	Color fontColorgrey = new Color(getDisplay(),66,66,66);

	Font largeFont = new Font(getDisplay(), this.fontName, this.fontBaseSize, 0 );
	Font smallFont = new Font(getDisplay(), this.fontName, (int)(this.fontBaseSize / 1.5), 0 );
	
	static Logger logger = Logger.getLogger(PmcCanvas.class.getName());
	

	public PmcCanvas(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 *  The zoom factor may be set trough this method. 
	 *  The diagram will repaint itself after any zoom operation
	 * 
	 */
	public void setZoom(double factor){

		this.zoomFactor = Math.abs(factor);
		this.needRecalculation = true;

		redraw();
	}	

	/**
	 *  This method can be used to force the diagram to repaint itself.
	 *  Recalculation of the buffered values is forced.
	 */
	public void redraw(){
		this.needRecalculation = true;
		super.redraw();
	}

	/**
	 * This method performs all necessary calculations to paint the diagram.
	 * It does, however, not paint the diagram data.
	 *
	 */
	private void recalculate(){

		msmsA = this.currentPair.spectrumA.valueList;
		msmsB = this.currentPair.spectrumB.valueList;

		this.deltaHits = new ArrayList<ArrayList<Double>>();
		this.equalHits = new ArrayList<ArrayList<Double>>();
		this.z2Hits = new ArrayList<ArrayList<Double>>();//build 65
		this.phospho98Hits = new ArrayList<ArrayList<Double>>();//build 61


		this.massToChargeMax = Double.MIN_VALUE;
		this.massToChargeMin = Double.MAX_VALUE;

		intensityMaxB = Double.MIN_VALUE;
		intensityMaxA = Double.MIN_VALUE;

		Collections.sort(msmsA);
		Collections.reverse(msmsA);

		Collections.sort(msmsB);
		Collections.reverse(msmsB);

		this.deltaMass = this.currentPair.spectrumA.precursorMass - this.currentPair.spectrumB.precursorMass;
		//Good to know: deltaMass is negative because spectrumA always lighter than spectrumB
		double deltaMassHalf=deltaMass/2;//build 65
		if(this.deltaMass>0){
			
			logger.fatal("Error: deltaMass not negative:"+this.deltaMass);
			return;
		}
		//else {
		//	System.out.println("deltaMass negative:"+this.deltaMass);
		//}

		isPhosphoPair=false;//build 61
		double tmpcrap=Math.abs(this.deltaMass + 79.966331);
		if( tmpcrap < this.record.getMsPrecision()){
			isPhosphoPair=true;
			logger.info("isPhosphoPair ture:"+tmpcrap);
		}
		
		for(MSMS msms : msmsA){
			ArrayList<Double> thisDeltaHits     = new ArrayList<Double>();
			ArrayList<Double> thisEqualHits     = new ArrayList<Double>();
			ArrayList<Double> thisZ2Hits        = new ArrayList<Double>();//build 65
			ArrayList<Double> thisPhospho98Hits = new ArrayList<Double>();//build 61


			this.massToChargeMax = Math.max(massToChargeMax, msms.massToCharge);
			this.massToChargeMin = Math.min(massToChargeMin, msms.massToCharge);
			this.intensityMaxA = Math.max(intensityMaxA, msms.intensity);

			
			boolean seen=false;
			for(MSMS msmsOther: msmsB){
				if(Math.abs(msms.massToCharge - msmsOther.massToCharge) < this.record.getMsmsPrecision()){
					thisEqualHits.add(msmsOther.massToCharge);
					seen=true;
				}
				if( Math.abs(msms.massToCharge - msmsOther.massToCharge - this.deltaMass ) < this.record.getMsmsPrecision()){
					thisDeltaHits.add(msmsOther.massToCharge);
					seen=true;
				}
				if(isPhosphoPair && Math.abs(msms.massToCharge - msmsOther.massToCharge - 18.010565) < this.record.getMsmsPrecision()){//build 61
					thisPhospho98Hits.add(msmsOther.massToCharge);
					seen=true;
				}
			}

//			build 65 new below
			if(!seen){
				for(MSMS msmsOther: msmsB){
					if(Math.abs(msms.massToCharge - msmsOther.massToCharge - deltaMassHalf ) < this.record.getMsmsPrecision()){
						thisZ2Hits.add(msmsOther.massToCharge);
					}			
				}
			}
		
			deltaHits.add(thisDeltaHits);
			equalHits.add(thisEqualHits);
			z2Hits.add(thisZ2Hits);//build 65
			phospho98Hits.add(thisPhospho98Hits);
		}

		for(MSMS msms : msmsB){
			this.massToChargeMax = Math.max(massToChargeMax, msms.massToCharge);
			this.massToChargeMin = Math.min(massToChargeMin, msms.massToCharge);
			this.intensityMaxB = Math.max(intensityMaxB, msms.intensity);
		}

	}

	/**
	 * paintBuffer repaints the buffer
	 *
	 */
	private void paintBuffer(){
		if(this.needRecalculation){
			recalculate();
		}

		int bWidth = (int)(this.canvasWidth * this.zoomFactor);
		int bHeight = this.canvasHeight - this.borderTop - this.borderBottom;

		Image buffer = new Image(getDisplay(), bWidth, bHeight);


		int spectrumWidth = bWidth -1;       // avoid off-by-one errors
		int spectrumHeight = (bHeight - 1 - this.topBarHeight - this.bottomBarHeight - this.seperatorBarHeight ) / 2;

		GC gc = new GC(buffer);

		//fill background
		gc.setBackground( diagramBackgroundColor );
		gc.setForeground( diagramBackgroundColor );
		gc.fillRectangle(0,0, bWidth, bHeight);

		double intensityMax = Math.max(intensityMaxA, intensityMaxB);
		int deltaMZ = (int)this.massToChargeMax - (int)this.massToChargeMin;

		//draw the middle seperator bar
		gc.setForeground( this.separatorBarColor );
		gc.setBackground( this.separatorBarColor );
		gc.fillRectangle( 0, this.topBarHeight + spectrumHeight , spectrumWidth, this.seperatorBarHeight );

		//draw the top bar
		gc.setForeground( this.topBarColor );
		gc.setBackground( this.topBarColor );
		gc.fillRectangle(0, 0, spectrumWidth, this.topBarHeight);

		//draw the bottom bar
		gc.setForeground( this.bottomBarColor );
		gc.setBackground( this.bottomBarColor );
		gc.fillRectangle( 0, bHeight - this.bottomBarHeight , spectrumWidth, this.bottomBarHeight );

		int minCount = ((int)this.massToChargeMin / ticksPerDivisor ) * ticksPerDivisor;
		int maxCount = ((int)this.massToChargeMax / ticksPerDivisor ) * ticksPerDivisor;

		// draw the scale seperators
		gc.setLineWidth((int)this.zoomFactor);

		for(int count = minCount; count < maxCount + minCount; count = count + ticksPerDivisor ){

			gc.setForeground( this.diagramScaleColor );
			int xPos = (int)(bWidth * ( (count - this.massToChargeMin ) / deltaMZ));
			gc.drawLine(xPos, this.bottomBarHeight , xPos, bHeight - this.bottomBarHeight);

			gc.setForeground( this.fontColor );
			gc.setBackground( this.topBarColor );

			gc.setFont(largeFont);

			// print scale text
			gc.drawString(new Integer(count).toString(), xPos, 0);
			gc.drawString(new Integer(count).toString(), xPos, bHeight - this.bottomBarHeight);
		}



		// if applicapable draw the subticks
		if(((double)(this.zoomFactor) / 10) > 0.5 ){
			int subticks = ticksPerDivisor / 10;
			gc.setLineWidth((int)this.zoomFactor / 10);
			minCount = ((int)this.massToChargeMin / subticks) * subticks;
			maxCount = ((int)this.massToChargeMax / subticks) * subticks;

			// draw the subtick seperators
			gc.setForeground( this.diagramScaleColor );
			for(int count = minCount; count < maxCount + minCount; count = count + subticks ){
				if(count%ticksPerDivisor == 0){ // don't draw the scale if there is already a label from up above
					continue;
				}

				gc.setForeground( this.diagramScaleColor );
				int xPos = (int)(bWidth * ( (count - this.massToChargeMin ) / deltaMZ));
				gc.drawLine(xPos, this.bottomBarHeight , xPos, bHeight - this.bottomBarHeight);


				// print scale text
				gc.setForeground( this.fontColor );
				gc.setBackground( this.topBarColor );

				gc.setFont(smallFont);

				gc.drawString(new Integer(count).toString(), xPos, 0);
				gc.drawString(new Integer(count).toString(), xPos, bHeight - this.bottomBarHeight);

			}
		}

		/* 
		 *  Draw upper spectrum
		 */
		gc.setForeground( upperSpectrumColor );
		gc.setBackground( upperSpectrumColor );
		gc.setLineWidth(1);

		for(MSMS msms: msmsA){
			int lineHeightA = (int)((double)spectrumHeight * 2 * (msms.intensity / (2 * intensityMaxA))  ) ;
			int xPosA = (int)(bWidth * ( (msms.massToCharge - this.massToChargeMin ) / deltaMZ));	
			int groundline = spectrumHeight + this.topBarHeight;

			gc.drawLine(xPosA, groundline , xPosA, groundline - lineHeightA);
		}

		//frankp20061019
		gc.setBackground(diagramBackgroundColor);
		gc.setForeground(fontColorgrey);
		gc.setFont(largeFont);
		gc.drawString("" + this.currentPair.spectrumA.title + "         " + String.format("%.5f",this.currentPair.spectrumA.precursorMass) + " [Da]" + "     z=" + this.currentPair.spectrumA.charge + "    protein:" + currentPair.spectrumA.proteinAsc + "    peptide:" + currentPair.spectrumA.pepSequence , 2 ,this.topBarHeight + 2);
		gc.drawString("" + this.currentPair.spectrumB.title + "         " + String.format("%.5f",this.currentPair.spectrumB.precursorMass) + " [Da]" + "     z=" + this.currentPair.spectrumB.charge + "    protein:" + currentPair.spectrumB.proteinAsc + "    peptide:" + currentPair.spectrumB.pepSequence , 2 ,this.topBarHeight + this.seperatorBarHeight + spectrumHeight + 3 +spectrumHeight -  gc.getFontMetrics().getHeight()-3);

		gc.setBackground(separatorBarColor);
		gc.drawString( "\u0394m = " + String.format("%.5f",Math.abs(this.deltaMass)) + " [Da]", 2 ,this.topBarHeight + spectrumHeight + (int)(((int)(this.seperatorBarHeight / 2))-gc.getFontMetrics().getHeight()/2));
		this.deltaMass = this.currentPair.spectrumA.precursorMass - this.currentPair.spectrumB.precursorMass;


		//build 61 paint spectrumA b-ion series-------------------------------------------------------------start
		if(currentPair.spectrumA.pepSequence != null)
		{  
			double[] amino_mass = new double[23];
			amino_mass[0]=71.037110; //A
			amino_mass[1]=114.534940;//B
			amino_mass[2]=103.009180; //C
			amino_mass[3]=115.026940; //D
			amino_mass[4]=129.042590; //E
			amino_mass[5]=147.068410; //F
			amino_mass[6]=57.021460; //G
			amino_mass[7]=137.058910; //H
			amino_mass[8]=113.084060;//I
			amino_mass[9]=128.094960; //K
			amino_mass[10]=113.084060; //L
			amino_mass[11]=131.040480; //M
			amino_mass[12]=114.042930; //N
			amino_mass[13]=97.052760; //P
			amino_mass[14]=128.058580; //Q
			amino_mass[15]=156.101110; //R
			amino_mass[16]=87.032030; //S
			amino_mass[17]=101.047680; //T
			amino_mass[18]=150.953630; //U
			amino_mass[19]=99.068410; //V
			amino_mass[20]=186.079310; //W
			amino_mass[21]=163.063330; //Y
			amino_mass[22]=128.550590; //Z
			char[] amino = new char[23];
			amino[0]='A';
			amino[1]='B';
			amino[2]='C';
			amino[3]='D';
			amino[4]='E';
			amino[5]='F';
			amino[6]='G';
			amino[7]='H';
			amino[8]='I';
			amino[9]='K';
			amino[10]='L';
			amino[11]='M';
			amino[12]='N';
			amino[13]='P';
			amino[14]='Q';
			amino[15]='R';
			amino[16]='S';
			amino[17]='T';
			amino[18]='U';
			amino[19]='V';
			amino[20]='W';
			amino[21]='Y';
			amino[22]='Z';

			double bNow=1.007825;
			for(int i=0;i<currentPair.spectrumA.pepSequence.length();i++){
				//make ticks
				boolean hasEqualHit=false;
				boolean hasDeltaHit=false;
				boolean hasNoHit=true;
				int xPosA = (int)( bWidth * (( bNow - this.massToChargeMin ) / deltaMZ ));
				for(int j=0;j<amino.length;j++){
					if(amino[j]== currentPair.spectrumA.pepSequence.charAt(i)){
						bNow+=amino_mass[j];
						//check SpectrumB for signals at m/z bNow 			
					}
				}
				for(MSMS msms: msmsB){
					if( Math.abs(msms.massToCharge-bNow)<record.getMsmsPrecision()){
						hasNoHit=false;
						hasEqualHit=true;
					}
					if(Math.abs( Math.abs(msms.massToCharge-bNow)+this.deltaMass)<record.getMsmsPrecision()){
						hasNoHit=false;
						hasDeltaHit=true;
					}
				}
				gc.setForeground(diagramScaleColor);
				if(hasEqualHit && hasDeltaHit){gc.setForeground(multiHitColor);}
				else if(hasEqualHit){gc.setForeground(equalHitColor);}
				else if(hasDeltaHit){gc.setForeground(deltaHitColor);}

				int xPosB = (int)( bWidth * (( bNow - this.massToChargeMin ) / deltaMZ ));
				int yMid=this.topBarHeight + 2 + gc.getFontMetrics().getHeight() + gc.getFontMetrics().getHeight();
				int yLow=yMid+4;
				int yHigh=yMid-4;

				gc.drawLine(xPosA, yLow, xPosA, yHigh );//vertical ticks
				gc.drawLine(xPosB, yLow, xPosB, yHigh);//TODO almost all ticks are painted twice ... fix

				gc.drawLine(xPosB-7, yMid+3, xPosB-7, yMid-3 );//arrow right vertical part
				gc.drawLine(xPosB-7, yMid+3, xPosB  , yMid   );//TODO almost all ticks are painted twice ... fix
				gc.drawLine(xPosB-7, yMid-3, xPosB  , yMid   );//TODO almost all ticks are painted twice ... fix


				gc.drawLine(xPosA, yMid, xPosB-7, yMid );//horizontal connectors
				gc.setBackground(diagramBackgroundColor);
				String crap= ""+currentPair.spectrumA.pepSequence.charAt(i);
				gc.drawString(crap,(int)((xPosA+xPosB)/2),yMid-(int)gc.getFontMetrics().getHeight()/2);
			}
			//double yNow=17.002740;
			double yNow=19.01784;//OH3 monoisotopic
			for(int i=currentPair.spectrumA.pepSequence.length()-1;i>=0;i--){
				boolean hasEqualHit=false;
				boolean hasDeltaHit=false;
				boolean hasNoHit=true;
				//make ticks

				int xPosA = (int)( bWidth * (( yNow - this.massToChargeMin ) / deltaMZ ));
				for(int j=0;j<amino.length;j++){
					if(amino[j]== currentPair.spectrumA.pepSequence.charAt(i)){
						yNow+=amino_mass[j];
					}
				}
				for(MSMS msms: msmsB){
					if( Math.abs(msms.massToCharge-yNow)<record.getMsmsPrecision()){
						hasNoHit=false;
						hasEqualHit=true;
					}
					if(Math.abs( Math.abs(msms.massToCharge-yNow)+this.deltaMass)<record.getMsmsPrecision()){
						hasNoHit=false;
						hasDeltaHit=true;
					}
				}
				gc.setForeground(diagramScaleColor);
				if(hasEqualHit && hasDeltaHit){gc.setForeground(multiHitColor);}
				else if(hasEqualHit){gc.setForeground(equalHitColor);}
				else if(hasDeltaHit){gc.setForeground(deltaHitColor);}



				int xPosB = (int)( bWidth * (( yNow - this.massToChargeMin ) / deltaMZ ));
				int yMid=this.topBarHeight + 2 + 3*gc.getFontMetrics().getHeight() + 2;
				int yLow=yMid+4;
				int yHigh=yMid-4;

				gc.drawLine(xPosA, yLow, xPosA, yHigh );//vertical ticks
				gc.drawLine(xPosB, yLow, xPosB, yMid );//TODO almost all ticks are painted twice ... fix
				gc.drawLine(xPosA+7, yMid, xPosB, yMid );//horizontal ticks

				gc.drawLine(xPosA+7, yMid+3, xPosA+7, yMid-3 );//arrow right vertical part
				gc.drawLine(xPosA+7, yMid+3, xPosA, yMid );//TODO almost all ticks are painted twice ... fix
				gc.drawLine(xPosA+7, yMid-3, xPosA, yMid );//TODO almost all ticks are painted twice ... fix

				gc.setBackground(diagramBackgroundColor);
				String crap= ""+currentPair.spectrumA.pepSequence.charAt(i);
				gc.drawString(crap,(int)((xPosA+xPosB)/2),yMid-(int)gc.getFontMetrics().getHeight()/2);
			}	



		}
		//build 61 paint the b-ion series--------------------------------------------------------------end		

		/*
		 *  Draw lower spectrum
		 */
		gc.setForeground( lowerSpectrumColor );
		gc.setBackground( lowerSpectrumColor );

		for(MSMS msms: msmsB){
			int lineHeightB = (int)((double)spectrumHeight *  2 * ( msms.intensity / ( 2 * intensityMaxB )));
			int xPosB = (int)( bWidth * (( msms.massToCharge - this.massToChargeMin ) / deltaMZ ));
			int groundline = spectrumHeight + this.topBarHeight + this.seperatorBarHeight;

			gc.drawLine(xPosB, groundline , xPosB, groundline + lineHeightB );
		}

		/*
		 *  Paint the equal and delta hits
		 */
		
		for(int i = 0; i < this.z2Hits.size(); i++ ){
			ArrayList<Double> z2Hits = this.z2Hits.get(i);
			int startXPos = (int)(bWidth * ( (msmsA.get(i).massToCharge - this.massToChargeMin ) / deltaMZ));
			int groundline = spectrumHeight + this.topBarHeight;
			int endline = spectrumHeight + this.topBarHeight + this.seperatorBarHeight;
			for(Double hit: z2Hits){//build 65
				int endXPos = (int)(bWidth * ( (hit - this.massToChargeMin ) / deltaMZ)); 
				gc.setForeground(this.z2HitColor);
				gc.drawLine(startXPos, groundline , endXPos, endline );
			}	
		}
		if(this.isPhosphoPair){//build 61
			for(int i = 0; i < this.phospho98Hits.size(); i++ ){
				ArrayList<Double> phospho98Hits = this.phospho98Hits.get(i);//build 61
				int startXPos = (int)(bWidth * ( (msmsA.get(i).massToCharge - this.massToChargeMin ) / deltaMZ));
				int groundline = spectrumHeight + this.topBarHeight;
				int endline = spectrumHeight + this.topBarHeight + this.seperatorBarHeight;
				for(Double hit: phospho98Hits ){
					int endXPos = (int)(bWidth * ( (hit - this.massToChargeMin ) / deltaMZ)); 
					gc.setForeground(this.phospho98Color);
					gc.drawLine(startXPos, groundline , endXPos, endline );
				}
			}
		}
		for(int i = 0; i < this.deltaHits.size(); i++ ){
			ArrayList<Double> deltaHits = this.deltaHits.get(i);
			int startXPos = (int)(bWidth * ( (msmsA.get(i).massToCharge - this.massToChargeMin ) / deltaMZ));
			int groundline = spectrumHeight + this.topBarHeight;
			int endline = spectrumHeight + this.topBarHeight + this.seperatorBarHeight;
			for(Double hit: deltaHits){
				int endXPos = (int)(bWidth * ( (hit - this.massToChargeMin ) / deltaMZ)); 
				gc.setForeground(this.deltaHitColor);
				gc.drawLine(startXPos, groundline , endXPos, endline );
			}	
		}
		for(int i = 0; i < this.equalHits.size(); i++ ){
			ArrayList<Double> equalHits = this.equalHits.get(i);
			int startXPos = (int)(bWidth * ( (msmsA.get(i).massToCharge - this.massToChargeMin ) / deltaMZ));
			int groundline = spectrumHeight + this.topBarHeight;
			int endline = spectrumHeight + this.topBarHeight + this.seperatorBarHeight;
			for(Double hit: equalHits){
				int endXPos = (int)(bWidth * ( (hit - this.massToChargeMin ) / deltaMZ));
				gc.setForeground(this.equalHitColor);
				gc.drawLine(startXPos, groundline , endXPos, endline );
			}
		}
		

		

		if(this.imageBuffer != null){
			this.imageBuffer.dispose();
		}
		this.imageBuffer = buffer;
		this.needBufferUpdate = false;

		gc.dispose();

	}

	/**
	 *  Method called by a event handler. This method should not be called manualy,
	 *  use redraw if you want to repaint the diagram
	 */
	public void paint(PaintEvent e) {

		if(records == null){
			return;
		}

		this.record = this.records[0];
		Canvas canvas = (Canvas) e.widget;

		// if either height or width of the image have changed, the buffer needs to be re-drawn.
		if(this.canvasHeight != canvas.getSize().y || this.canvasWidth != canvas.getSize().x){

			this.canvasWidth = canvas.getSize().x;
			this.canvasHeight = canvas.getSize().y;

			this.needBufferUpdate = true;
		}


		// clear the whole canvas
		/*e.gc.setBackground( canvasBackgroundColor );
		e.gc.setForeground( canvasBackgroundColor );
		e.gc.fillRectangle(0,0, this.canvasWidth, this.canvasHeight);*/

		if(this.records[0] == null || this.records[0].getCurrentSpectraPair() == null){
			// nothing to draw: boil out
			return;
		}

		// now we can be sure that everything we need is available 
		this.currentPair = record.getCurrentSpectraPair();

		if(this.needBufferUpdate){
			paintBuffer();
		}

		if(this.imageBuffer == null){
			// this really shouldn't happen
			return;
		}

		int height = this.canvasHeight - this.borderBottom - this.borderTop;
		int width = this.canvasWidth - this.borderRight - this.borderLeft;

		int bufferWidth = this.imageBuffer.getBounds().width;

		int xOffset = (int)(((double)(bufferWidth - width) / this.scrollMax) * this.scrollPosition );


		e.gc.drawImage(this.imageBuffer, xOffset, 0, width, height, this.borderLeft , this.borderTop, width , height);

		syncScrollBars();
	}

}
