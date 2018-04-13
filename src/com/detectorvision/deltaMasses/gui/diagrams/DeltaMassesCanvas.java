/* $Id: DeltaMassesCanvas.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.deltaMasses.gui.diagrams;

import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.Spectrum;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Formatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * This specialised canvas draws the delta precursorMass historgram of a given 
 * record. 
 * 
 * @author Raphael
 *
 */
public class DeltaMassesCanvas extends Diagram {
	
	// graphical tuning parameters
	boolean drawScale = true;
	int numScaleDivisions = 4;
	int borderTop = 30;
	int borderBottom = 70;	
	int borderLeft = 60;
	int borderRight = 40;
		
	// graphical control parameters
	double zoomFactor = 1.0;
	boolean needRecalculation = true;
	int scrollMin = 0;
	int scrollMax = 9999;
	
	// data parameters
	double precursorMassMin = Double.MAX_VALUE;
	double precursorMassMax = Double.MIN_VALUE;
	double deltaPrecursorMassMax = Double.MIN_VALUE;
	double deltaPrecursorMassMin = Double.MAX_VALUE;
	int currentMaxCount = Integer.MIN_VALUE;
	
	int[] histYValues;
	
	double bufferXOffset = -1;
	int indexStart;
	int indexEnd;
	
	public DeltaMassesCanvas(Composite parent, int style) {
		super(parent, style);
		
		ScrollBar horizontal = getHorizontalBar();
		horizontal.setEnabled(true);
	    
		horizontal.setMinimum(this.scrollMin);
		horizontal.setMaximum(this.scrollMax);
	}
	
	/**
	 *  The zoom factor may be set trough this method. 
	 *  The diagram will repaint itself after any zoom operation
	 * 
	 */
	public void setZoom(double factor){
		
		this.zoomFactor = Math.abs(factor);
		this.needBufferUpdate = true;
		
		redraw();
	}	
	
	/**
	 * This method performs all necessary calculations to paint the diagram.
	 * It does, however, not paint the diagram data.
	 */
	private boolean recalculate(){
		
		// make sure everything we need is available
		if(this.records == null || this.records[0] == null | this.canvasHeight <= 0 || this.canvasWidth <= 0){
			return false;
		}	
		
		int numBins;

		Record rec = this.records[0];
		ArrayList<Spectrum> spectraList = rec.getSpectrumList();
		if(spectraList==null){return false;}//http://chiralcomp.dlinkddns.com/deltamasses/ticket/78
		
		// set some sane defaults
		this.precursorMassMin = Double.MAX_VALUE;
		this.precursorMassMax = Double.MIN_VALUE;
		this.deltaPrecursorMassMax = Double.MIN_VALUE;
		double deltaPrecursorMassMin = Double.MAX_VALUE;
		
		// iterate over the list and get minimum and maximum precursorMass
		for(Spectrum s : spectraList){
			precursorMassMax = Math.max(precursorMassMax, s.precursorMass);
			precursorMassMin = Math.min(precursorMassMin, s.precursorMass);
	
		}
		
		this.deltaPrecursorMassMax = this.precursorMassMax - this.precursorMassMin;

		// the values get distributed into bins
		numBins = (int)(100 * this.deltaPrecursorMassMax);
		
		// allocate the needed memory for the bins
		if(numBins < 0){
			// hrm. something's wierd
			return false;
		}
		this.histYValues = new int[numBins];
		
		
		// for all spectra pairs: calculate the precursorMass distance and
		// count how many times a certain distance occures
		for(int a = 0; a < spectraList.size(); a++){
			
			Spectrum specA = spectraList.get(a);
			
			// b= a +1 : only do one half of the matrix, the data in the other is identical	
			for(int b = a + 1; b < spectraList.size(); b++){
			
				Spectrum specB = spectraList.get(b);
				double deltaPrecursorMass = Math.abs(specB.precursorMass - specA.precursorMass);

				//get the precursorMass delta
				int index = (int)((numBins - 1) * (deltaPrecursorMass / this.deltaPrecursorMassMax));
				
				//there _should_ be no upper or lower bounds mismatches. however; better to be sure
				if(index >= numBins) {
					index = numBins - 1;
				}
				if(index < 0){
					index = 0;
				}
				
				// increment the "found count" of the identified precursorMass delta
				this.histYValues[index]++;

			}
		}

		// the buffer needs update
		this.needRecalculation = false;
		this.needBufferUpdate = true;
		
		return true;
	}
	
	private void paintBuffer(){
		
		if(this.needRecalculation){
			recalculate();
		}
		if(histYValues == null){
			//now _THAT_ shouldn't happen!
			return;
		}
		int numBins = histYValues.length;
		
		int bWidth = (int)((this.canvasWidth - this.borderLeft - this.borderRight ) );
		int bHeight = this.canvasHeight - this.borderTop - this.borderBottom;
		int numBinsToDraw = (int)(numBins / this.zoomFactor);
		
		this.bufferXOffset = numBins * ((double)this.scrollPosition / this.scrollMax);
		
		Image buffer = new Image(getDisplay(), bWidth, bHeight);
		GC gc = new GC(buffer);
		
		int realX, realY;
		
		float signalWidth = (float)((bWidth) / (float)numBinsToDraw);

		if (signalWidth < 1){
			signalWidth = 1;
		}
		
		gc.setForeground(new Color( getDisplay(), 105, 133 ,213) );
		gc.setBackground(new Color( getDisplay(), 105, 133 ,213) );
		
		int lastX = 0;
		int lastY = 0;

		//
		this.indexStart = (int)(this.bufferXOffset );
		this.indexEnd = indexStart + numBinsToDraw ;
		
		
		//failsafe
		if(indexStart < 0){
			indexStart = 0;
		}
		
		if(indexEnd >= this.histYValues.length){
			indexEnd = this.histYValues.length;
		}
		
		// Performance increase by not drawing every possible garbage
		//int step = (int)(numBinsToDraw/(bWidth * 10) );
		int step = 1;
		//if(step < 1){
		//	step = 1;
		//}
		
		
		//System.out.println("Step: " + step);
		
		this.currentMaxCount = Integer.MIN_VALUE;			
		for(int x = indexStart; x < indexEnd; x += step){
			this.currentMaxCount = Math.max(this.currentMaxCount, this.histYValues[x]);
		}
		//System.out.println("is: " + indexStart + " ie: " + indexEnd + " length:" + histYValues.length);
		//System.out.println("Start: " + ((bWidth ) * (indexStart - indexStart / (float)(indexEnd - indexStart) ))  + " width" + bWidth  );
		//System.out.println("Width: " + bWidth + "Starting at: " + bWidth * (indexStart / (double)histYValues.length) + " ending at: " + bWidth * (indexEnd / (double)histYValues.length));
		
		for(int x = indexStart; x < indexEnd; x += step){
			
			float y = (float)histYValues[x];
			
			int xCoord = (int)((bWidth ) * ((x - indexStart) / (double)numBinsToDraw ));			
			int yCoord = (int)((bHeight) * (y / (1.1 * (double)this.currentMaxCount)));

			
			realX = xCoord;
			realY = bHeight - yCoord;
			
			//gc.drawLine(realX, bHeight, realX, realY);
			if(signalWidth > 1){
				gc.drawLine(lastX, lastY, lastX, realY);
				gc.drawLine(realX, realY, realX + (int)signalWidth, realY);
			
				lastX = realX + (int)signalWidth;
				lastY = realY;
			}
			else{
				gc.drawLine(realX, bHeight, realX, realY);
			}
		}
		
		/*
		gc.setForeground(new Color( getDisplay(), 0, 0 ,0) );
		gc.setBackground(new Color( getDisplay(), 0, 0 ,0) );

		gc.setLineWidth(1);
		gc.setForeground( new Color(getDisplay(), 255,0,0 ) );
		
		gc.drawLine(0, 0, bWidth, bHeight);
		gc.drawLine(0, bHeight, bWidth, 0);
		*/

		if(this.imageBuffer != null){
			this.imageBuffer.dispose();
		}
		this.imageBuffer = buffer;

		this.needBufferUpdate = false;
		gc.dispose();
	}
	
	public void paint(PaintEvent e) {

		if(records == null){
			return;
		}
		
		Record record = records[0];		
		Canvas canvas = (Canvas) e.widget;
		
		// if either height or width of the image have changed, the buffer needs to be re-drawn.
		if(this.canvasHeight != canvas.getSize().y || this.canvasWidth != canvas.getSize().x){

			this.canvasWidth = canvas.getSize().x;
			this.canvasHeight = canvas.getSize().y;
			
			this.needBufferUpdate = true;
		}
		
		if( record == null ){
			return;
		}
		
		e.gc.setBackground(new Color( e.display, 255, 255 ,255) );
		e.gc.setForeground(new Color( e.display, 255, 255 ,255) );
		e.gc.fillRectangle(0,0, this.canvasWidth, this.canvasHeight);

		e.gc.setForeground(new Color( e.display, 0, 0, 0) );
		

		if(this.needBufferUpdate){
			paintBuffer();
		}

		if(this.imageBuffer == null){
			// this really shouldn't happen!
			return;
		}
		
		int height = this.canvasHeight - this.borderBottom - this.borderTop;
		int width = this.canvasWidth - this.borderRight - this.borderLeft;

		//int xOffset = (int)(((double)(bufferWidth - width) / this.scrollMax) * this.scrollPosition );
		e.gc.drawImage(this.imageBuffer, 0, 0, width, height, this.borderLeft, this.borderTop, width, height);
		
		if(this.drawScale){
			
			double scaleHeightSteps = height / this.numScaleDivisions;
			double scaleWidthSteps = width / this.numScaleDivisions;
			int fontOffset = e.gc.getFontMetrics().getHeight() / 2;
			
			// draw scales and grid
			// Both vertical and horizontal scales/lines are drawn in one iteration
			for(int i = 0; i <= numScaleDivisions; i++){
				Formatter form = new Formatter();
				Formatter form1 = new Formatter();
				
				//calculate the positions of the yScaleString and the xScaleString
				//build 61 added the (double)casts below to fix uggly scale mistake....
				double xScaleStringRaw = ((((double)this.indexEnd - (double)this.indexStart) / (double)this.numScaleDivisions) * (double)i + (double)this.indexStart) / 100.00;
				//logger.info("indexStart:" +indexStart+ " indexEnd:"+indexEnd+ " numScale:" + numScaleDivisions);
				
				int yScaleStringRaw = (this.currentMaxCount / this.numScaleDivisions) * i;
				
				// Format the strings
				//build 61 String xScaleString = form.format("%1.1f", xScaleStringRaw).toString();
				String xScaleString = form.format("%.4f", xScaleStringRaw).toString();
				String yScaleString = form1.format("%1d", yScaleStringRaw).toString(); 

				// calculate the width of both vertical and horizontal scale string
				int xScaleStringWidth = e.gc.textExtent(xScaleString).x;
				int yScaleStringWidth = e.gc.textExtent(yScaleString).x;

				//draw scale strings
				e.gc.setForeground(new Color( e.display, 0, 0, 0) );
				e.gc.drawString(yScaleString, this.borderLeft - yScaleStringWidth - (this.borderLeft / 10), this.canvasHeight - this.borderBottom - (int)(i * scaleHeightSteps) - fontOffset);
				e.gc.drawString(xScaleString, this.borderLeft + (int)(i * scaleWidthSteps) - (int)(xScaleStringWidth / 2), this.canvasHeight - this.borderBottom + (this.borderBottom / 10));
				
				// draw horizontal and vertical line
				e.gc.setForeground(new Color( e.display, 225, 225, 225) );
				e.gc.drawLine(this.borderLeft, this.canvasHeight - this.borderBottom - (int)(i * scaleHeightSteps), this.canvasWidth - this.borderRight, this.canvasHeight - this.borderBottom - (int)(i * scaleHeightSteps));
				e.gc.drawLine(this.borderLeft + (int)(i * scaleWidthSteps), this.borderTop, this.borderLeft + (int)(i * scaleWidthSteps) , this.canvasHeight - this.borderBottom );
				
			}
			e.gc.setFont(new Font(e.display, this.fontName, this.fontBaseSize, SWT.BOLD ));
			String precursorMassDeltaTitle = "precursorMass delta";
			String precursorMassDeltaCountTitle = "count";
			int rdTitleWidth = e.gc.textExtent(precursorMassDeltaTitle).x;
			int rdcTitleWidth = e.gc.textExtent(precursorMassDeltaCountTitle).x;
			int rdTitleHeight = e.gc.textExtent(precursorMassDeltaTitle).y;
			
			e.gc.setForeground(new Color( e.display, 0, 0, 0) );
			e.gc.drawString(precursorMassDeltaTitle, this.borderLeft + width - rdTitleWidth + (this.borderRight / 2), this.canvasHeight - (this.borderBottom / 2) - (rdTitleHeight / 2) );
			e.gc.drawString(precursorMassDeltaCountTitle, (this.borderLeft / 2) - (rdcTitleWidth / 2), (this.borderTop / 2) - (rdTitleHeight / 2));
			//e.gc.drawString(xScaleString, this.borderLeft + (i * scaleWidthSteps) - (xScaleStringWidth / 2), this.canvasHeight - this.borderBottom + (this.borderBottom / 10));
			
		}
		
		if( this.drawBorder ){
			e.gc.setForeground(new Color( e.display, 0, 0, 0) );
			//e.gc.drawLine(this.borderLeft, this.borderTop, this.canvasWidth - this.borderRight, this.borderTop);
			//e.gc.drawLine(this.canvasWidth - this.borderRight, this.borderTop, this.canvasWidth - this.borderRight, this.canvasHeight - this.borderBottom);
			e.gc.drawLine(this.canvasWidth - this.borderRight, this.canvasHeight - this.borderBottom, this.borderLeft, this.canvasHeight - this.borderBottom);
			e.gc.drawLine(this.borderLeft, this.canvasHeight - this.borderBottom, this.borderLeft, this.borderTop );
		}	
		syncScrollBars();
	}
	
}
