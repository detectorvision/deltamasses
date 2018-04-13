/* $Id: Diagram.java 92 2008-02-22 18:47:35Z jari $ */

package com.detectorvision.deltaMasses.gui.diagrams;

import com.detectorvision.massspectrometry.biodata.Record;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;


/**
 * The ZoomCanvas implements a zoomable canvas.
 * 
 * @author Raphael Bosshard
 *
 */
abstract public class Diagram extends Canvas {
	
	Record[] records;
	
	Image imageBuffer;
	
	boolean drawGrid = false;
	int gridSpacing = 50;
	
	boolean drawBorder = true;
	int borderLeft = 10;
	int borderRight = 10;
	int borderTop = 10;
	int borderBottom = 10;
	
	int canvasHeight = -1;
	int canvasWidth = -1;
	
	double zoomFactor = 1.0;
	int scrollPosition = 1;
	int scrollMin = 0;
	int scrollMax = 99;
	
	String fontName = "Helvetica";
	int fontBaseSize = 10; 
	
	boolean needBufferUpdate = true;
	
	public void redraw(){
		this.needBufferUpdate = true;
		super.redraw();
	}
	
	
	public Diagram(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL );

		// paint listener 
		addPaintListener(new PaintListener() { 
			public void paintControl(PaintEvent event) {
				paint(event);
			}
		});
		
		// resize listener
		addControlListener(new ControlAdapter() { 
			public void controlResized(ControlEvent event) {
				syncScrollBars();
			}
		});
		initScrollBars();
	}

	
	public void setRecords(Record[] records){
		this.records = records;
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
	

	public void syncScrollBars(){
		ScrollBar horizontal = getHorizontalBar();
		if(this.imageBuffer instanceof Image && canvasWidth > 0){
			//horizontal.setThumb((this.scrollMax - this.scrollMin) * this.canvasWidth / this.imageBuffer.getBounds().width );

			// TODO: if the thumb of the scrollbar is larger than 1, the maxValue cannot be reached. Why?
			
			horizontal.setThumb(1);
			horizontal.setVisible(true);
		}
		else {
			horizontal.setVisible(false);
		}
	};
	
	void initScrollBars(){
		ScrollBar horizontal = getHorizontalBar();
		horizontal.setEnabled(true);
	    
		horizontal.setMinimum(scrollMin);
		horizontal.setMaximum(scrollMax);
	    
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				//scrollHorizontally(event.widget);
				ScrollBar bar = (ScrollBar)event.widget;
				scrollPosition = bar.getSelection();

				// the image has to be redrawn
				redraw();
	        }
	    });	
	}
	abstract public void paint(PaintEvent e);
}
