/* $Id: PairTableSelectionHandler.java 322 2010-05-23 11:20:50Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.gui.diagrams.SpectrumCanvas;
import com.detectorvision.massspectrometry.biodata.Record;
import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.biodata.Spectrum;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class PairTableSelectionHandler extends SelectionAdapter{
	
	// record
	private Record[] records;
	private ArrayList<SpectraPair> pairList;
	private Canvas canvas;
	private ArrayList<Control> redrawListenerList = new ArrayList<Control>();

	// Logging with log4j
	static Logger logger = Logger.getLogger(PairTableSelectionHandler.class.getName());


	
	/**
	 * Constructor to submit data
	 * @param records
	 */
	public PairTableSelectionHandler(Record[] records,ArrayList <SpectraPair>pairList){
		this.records = records;
		this.pairList = pairList;
		
	}
	
	/**
	 * Adds a control to the redraw listener list
	 * @param control Control to be added to the redraw listener list
	 */
	public void addRedrawListener(Control control){
		this.redrawListenerList.add(control);
	}
	/**
	 * Removes a control from the redraw listener list
	 * @param c Control to be removed from redraw listener list
	 */
	public void removeRedrawListener(Control c){
		this.redrawListenerList.remove(c);
	}
	
	/**
	 * Selectionevent, calculate the selected spectrapair
	 * @param e Event
	 */
	public void widgetSelected(SelectionEvent e){
		// reference
		Table table = (Table)e.widget;
		TableItem selection = table.getSelection()[0];
		
		// search the selected spectrapair
		boolean foundPair=false;
		for(SpectraPair pair:pairList){	
			if(    this.getLowerPeptideMass(  pair.spectrumA, pair.spectrumB).title.equals(selection.getText(0))
					&& this.getHigherPeptideMass( pair.spectrumA, pair.spectrumB).title.equals(selection.getText(2))
			){
				this.records[0].setCurrentSpectraPair(pair);
				foundPair=true;
			}
		}
		if(!foundPair){
			logger.error("pairTableSelectionhandler:no pair found error");
		}
		try{
		for(Control c : redrawListenerList){
			c.redraw();
			DeltaMasses.pair_id_Label.setText("pair_id: "+this.records[0].getCurrentSpectraPair().pair_id);
			
			//enable/disabled marked buttons
			if(this.records[0].getCurrentSpectraPair().marked && DeltaMasses.DMBcanBeReached){
				if(DeltaMasses.pairMarkedButton!=null){
					DeltaMasses.pairMarkedButton.setEnabled(true);
					DeltaMasses.pairUnMarkedButton.setEnabled(true);
				}
			}
			else if(DeltaMasses.DMBcanBeReached){
				if(DeltaMasses.pairMarkedButton!=null){
					DeltaMasses.pairMarkedButton.setEnabled(true);
					DeltaMasses.pairUnMarkedButton.setEnabled(false);
				}
			}
			
			if(this.records[0].getCurrentSpectraPair().specnet_id != 0){
		 	   DeltaMasses.specNet_id_Label.setText("specNet_id: "+this.records[0].getCurrentSpectraPair().specnet_id);
		 	   DeltaMasses.peptideNetButton.setEnabled(true);
			}
			else{
				DeltaMasses.peptideNetButton.setEnabled(false);
				DeltaMasses.specNet_id_Label.setText("specNet_id: none");
			}
			
			if(this.records[0].getCurrentSpectraPair().comment == null){
				DeltaMasses.pairCommentText.setText("");
			}
			else{
				DeltaMasses.pairCommentText.setText(this.records[0].getCurrentSpectraPair().comment);
			}
			
			if(this.records[0].getCurrentSpectraPair().marked){
				DeltaMasses.setMarkedButton(true);			
			}
			else{
				DeltaMasses.setMarkedButton(false);
			}	
		}
		}
		catch(Exception shit){
			logger.fatal("redraw exception:"+shit.getMessage());
			logger.fatal("redraw exception:"+shit);
		}
		
		
	}
	
	/**
	 * Calculate the peptide spectrum with the lower mass.
	 * @param spec1
	 * @param spec2
	 * @return
	 */
	private Spectrum getLowerPeptideMass(Spectrum spec1, Spectrum spec2){
		
		// calculate the lower mass
		double precMass = Math.min(spec1.precursorMass, spec2.precursorMass);
		
		// return the correct spectra
		if(precMass == spec1.precursorMass)
			return spec1;
		else
			return spec2;
	}
	
	private Spectrum getHigherPeptideMass(Spectrum spec1, Spectrum spec2){
		
		// calculate the lower mass
		double precMass = Math.max(spec1.precursorMass, spec2.precursorMass);
		
		// return the correct spectra
		if(precMass == spec1.precursorMass)
			return spec1;
		else
			return spec2;
	}
}