/* $Id: RetentionCanvas.java 103 2008-02-24 19:33:43Z jari $ */

package com.detectorvision.deltaMasses.gui.diagrams;

import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.Spectrum;

//import java.util.*;
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


/**
 * This specialised canvas draws the delta retention historgram of a given 
 * record. 
 * 
 * @author Raphael Bosshard
 *
 */
public class RetentionCanvas extends Diagram {

	float retentionMin = Float.MAX_VALUE;
	float retentionMax = Float.MIN_VALUE;
	float deltaRetentionMax = Float.MIN_VALUE;
	float deltaRetentionMin = Float.MAX_VALUE;
	int maxCount = Integer.MIN_VALUE;

	boolean drawScale = true;
	int numScaleDivisions = 4;
	
	int borderTop = 30;
	int borderBottom = 70;	
	int borderLeft = 60;
	int borderRight = 40;
	
	double zoomFactor = 1.0;

	int[] histYValues;
	boolean needRecalculation = true;
	
	public RetentionCanvas(Composite parent, int style) {
		super(parent, style);

	}
	
	/**
	 *  This method can be used to force the diagram to repaint itself.
	 *  Recalculation of the buffered values is forced.
	 */
	public void redraw(){
		this.needBufferUpdate = true;
		super.redraw();
	}
	
	public void setRecords(Record[] records){
		this.records = records;
		this.needRecalculation = true;
		redraw();
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
	
	private boolean recalculate(){
		//System.out.println("recalculate");
		// make sure everything we need is available
		if(this.records == null || this.records[0] == null | this.canvasHeight <= 0 || this.canvasWidth <= 0){
			return false;
		}	
		
		int numBins;
		float binWidth;
		
		Record rec = this.records[0];
		ArrayList<Spectrum> spectraList = rec.getSpectrumList();
		
		// the values get distributed into bins. since we don't need
		// more bins than we have pixels available, minimize the number of bins/buckets.
		// numBins = this.canvasWidth - this.borderLeft - this.borderRight;
		
		//
		// Fixed amount of bins, works better 
		numBins = (int)(40 * this.zoomFactor);
		
		// allocate the needed memory for the bins
		
		this.histYValues = new int[numBins * 2 -1 ];

		// set some sane defaults
		this.retentionMin = Float.MAX_VALUE;
		this.retentionMax = Float.MIN_VALUE;
		this.deltaRetentionMax = Float.MIN_VALUE;
		float deltaRetentionMin = Float.MAX_VALUE;
		this.maxCount = Integer.MIN_VALUE;
		
		// iterate over the list and get minimum and maximum retention
		for(Spectrum s : spectraList){
			retentionMax = Math.max(retentionMax, s.retention);
			retentionMin = Math.min(retentionMin, s.retention);
		}
		
		// we need to know how wide the bins are
		binWidth = ( retentionMax - retentionMin ) / numBins;
		
		// for all spectra pairs: calculate the retention distance and
		// count how many times a certain distance occures
		for(int a = 0; a < spectraList.size(); a++){
			
			Spectrum specA = spectraList.get(a);
			
			// b= a +1 : only do one half of the matrix, the data in the other is identical	
			for(int b = a + 1; b < spectraList.size(); b++){
			
				Spectrum specB = spectraList.get(b);
				float deltaRetention = Math.abs(specB.retention - specA.retention);

				this.deltaRetentionMax = Math.max(deltaRetention, this.deltaRetentionMax );
				
				//get the retention delta
				int index = (int)((numBins - 1) * (deltaRetention/ this.deltaRetentionMax));
				
				//there _should_ be no upper or lower bounds mismatches. however; better to be sure
				if(index >= numBins) {
					index = numBins - 1;
				}
				if(index < 0){
					index = 0;
				}
				
				// increment the "found count" of the identified retention delta
				this.histYValues[numBins - index]++;
			}
		}
		
		//Copy values for the negative side and find maxCount
		for(int n = 0; n < numBins; n++ ){
			this.histYValues[numBins * 2 - 2 - n] = this.histYValues[n];
			this.maxCount = Math.max(this.maxCount, this.histYValues[n]);
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
		
		int bWidth = (int)((this.canvasWidth - this.borderLeft - this.borderRight ) * this.zoomFactor);
		int bHeight = this.canvasHeight - this.borderTop - this.borderBottom;
		
		Image buffer = new Image(getDisplay(), bWidth, bHeight);
		buffer.getImageData().alpha = 64;
		GC gc = new GC(buffer, SWT.LEFT_TO_RIGHT);
		
		int realX, realY;
		
		float signalWidth = ((bWidth) / (float)(this.histYValues.length * 2));
		if (signalWidth < 1){
			signalWidth = 1;
		}
		
		//System.out.println("Width: " + width);
		
		gc.setForeground(new Color( getDisplay(), 105, 133 ,213) );
		gc.setBackground(new Color( getDisplay(), 105, 133 ,213) );
		
		int lastX = 0;
		int lastY = 0;
		
		for(int x = 0; x < this.histYValues.length; x++){
			
			float y = (float)histYValues[x];
			
			int xCoord = (int)((bWidth ) * (x / (float)this.histYValues.length ));			
			int yCoord = (int)((bHeight) * (y / (float)this.maxCount ));
			
			realX = xCoord;
			realY = bHeight - yCoord;
			
			gc.drawLine(lastX, lastY, lastX, realY);
			gc.drawLine(realX, realY, realX + (int)signalWidth * 2 , realY);
			
			lastX = realX + (int)signalWidth * 2;
			lastY = realY;
		}
		

		/*
		 * Testing
		
		 
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
		
		
		e.gc.setForeground(new Color( e.display, 0, 0, 0) );

		if( record == null || record.hasRetention() == false ){
			return;
		}
		
		// clear background
		e.gc.setBackground(new Color( e.display, 255, 255 ,255) );
		e.gc.setForeground(new Color( e.display, 255, 255 ,255) );
		e.gc.fillRectangle(0,0, this.canvasWidth, this.canvasHeight);
		
		if(this.needBufferUpdate){
			paintBuffer();
		}

		if(this.imageBuffer == null){
			// this really shouldn't happen!
			return;
		}
		
		int height = this.canvasHeight - this.borderBottom - this.borderTop;
		int width = this.canvasWidth - this.borderRight - this.borderLeft;

		int bufferWidth = this.imageBuffer.getBounds().width;

		int xOffset = (int)(((double)(bufferWidth - width) / this.scrollMax) * this.scrollPosition );
		e.gc.drawImage(this.imageBuffer, xOffset, 0, width, height, this.borderLeft, this.borderTop, width, height);
		
		if(this.drawScale){
			
			int scaleHeightSteps = height / this.numScaleDivisions;
			int scaleWidthSteps = width / this.numScaleDivisions;
			int fontOffset = e.gc.getFontMetrics().getHeight() / 2;
			float retDelta = this.retentionMax - this.retentionMin;
			
			// draw scales and grid
			// Both vertical and horizontal scales/lines are drawn in one iteration
			for(int i = 0; i <= numScaleDivisions; i++){
				Formatter form = new Formatter();

				//calculate the position of the string 
				float xScaleStringRaw = ((retDelta / bufferWidth) * ((i * scaleWidthSteps) + xOffset ) * 2) - retDelta;

				// Format the strings
				String xScaleString = form.format("%1.2f", xScaleStringRaw).toString();
				String yScaleString = new Integer((int)((this.retentionMax / (this.numScaleDivisions-1))) * i).toString();
			
				// calculate the width of both vertical and horizontal scale string
				int xScaleStringWidth = e.gc.textExtent(xScaleString).x;
				int yScaleStringWidth = e.gc.textExtent(yScaleString).x;

				//draw scale strings
				e.gc.setForeground(new Color( e.display, 0, 0, 0) );
				e.gc.drawString(yScaleString, this.borderLeft - yScaleStringWidth - (this.borderLeft / 10), this.canvasHeight - this.borderBottom - (i * scaleHeightSteps) - fontOffset);
				e.gc.drawString(xScaleString, this.borderLeft + (i * scaleWidthSteps) - (xScaleStringWidth / 2), this.canvasHeight - this.borderBottom + (this.borderBottom / 10));
				
				//draw horizontal and vertical line
				e.gc.setForeground(new Color( e.display, 225, 225, 225) );
				e.gc.drawLine(this.borderLeft, this.canvasHeight - this.borderBottom - (i * scaleHeightSteps), this.canvasWidth - this.borderRight, this.canvasHeight - this.borderBottom - (i * scaleHeightSteps));
				e.gc.drawLine(this.borderLeft + (i * scaleWidthSteps), this.borderTop, this.borderLeft + (i * scaleWidthSteps) , this.canvasHeight - this.borderBottom );
				
			}
			
			// draw the title
			e.gc.setFont(new Font(e.display, this.fontName, this.fontBaseSize, SWT.BOLD ));
			String retentionDeltaTitle = "retention delta";
			String retentionDeltaCountTitle = "count";
			
			// get title metrics
			int rdTitleWidth = e.gc.textExtent(retentionDeltaTitle).x;
			int rdcTitleWidth = e.gc.textExtent(retentionDeltaCountTitle).x;
			int rdTitleHeight = e.gc.textExtent(retentionDeltaTitle).y;
			
			// set color and draw titles
			e.gc.setForeground(new Color( e.display, 0, 0, 0) );
			e.gc.drawString(retentionDeltaTitle, this.borderLeft + width - rdTitleWidth + (this.borderRight / 2), this.canvasHeight - (this.borderBottom / 2) - (rdTitleHeight / 2) );
			e.gc.drawString(retentionDeltaCountTitle, (this.borderLeft / 2) - (rdcTitleWidth / 2), (this.borderTop / 2) - (rdTitleHeight / 2));
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
