/* $Id: ModTableSelectionHandler.java 245 2009-01-02 15:53:33Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.biodata.SpectraPair;
import com.detectorvision.massspectrometry.biodata.Spectrum;

import java.util.ArrayList;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Eventhandler for tableselection
 * @author lehmamic
 */
public class ModTableSelectionHandler extends SelectionAdapter{
	
	// spectrapair
	private Table pairTable;
	private ArrayList <SpectraPair>pairList;
	private ArrayList<Control> redrawListenerList = new ArrayList<Control>();
	
	/**
	 * Constructor with the pairTable as parameter
	 * @param pairTable table with spectrapairs
	 */
	public ModTableSelectionHandler (Table pairTable, ArrayList <SpectraPair>pairList){
		this.pairTable = pairTable;
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
	 * Eventmethod calucates the new table
	 */
	public void widgetSelected(SelectionEvent e){
		// reference
		Table table = (Table)e.widget;
		TableItem selection = table.getSelection()[0];
		TableItem tableItem ;
		
		// clear the table
		pairTable.removeAll();
		
		
		
		// check if its a modification
		if(!selection.getText(0).equals("None")){
			// get the modification id
			int modID = Integer.parseInt(selection.getText(4));

			// fill in the table
			for(SpectraPair pair:this.pairList){
				if(pair.knownModification != null && pair.knownModification.unimodID == modID){
					double dm = Math.abs(pair.spectrumA.precursorMass-pair.spectrumB.precursorMass);
					tableItem = new TableItem(this.pairTable, 0);
					
					String weakDeltaString="";
					if(pair.hasWeakDeltaSignal){weakDeltaString="y";}
					
					tableItem.setText(new String[] { 
							                         "" + this.getLowerPeptideMass(  pair.spectrumA  , pair.spectrumB).title,
							                         "" + String.format("%.4f",this.getLowerPeptideMass(  pair.spectrumA  , pair.spectrumB).precursorMass),
							                         "" + this.getHigherPeptideMass( pair.spectrumA  , pair.spectrumB).title,
							                         "" + String.format("%.4f",dm),
							                         "" + String.format("%.1e",pair.p),
							                         "" + String.format("%.2f",pair.score),
							                         "" + this.getLowerPeptideMass(  pair.spectrumA , pair.spectrumB).proteinAsc,
							                         "" + this.getHigherPeptideMass( pair.spectrumA , pair.spectrumB).proteinAsc,
							                         "" + weakDeltaString
													});
				}
			}
			
		}
		// fill in the spectras without a known modifications
		else{
			for(SpectraPair pair:this.pairList){
				if(pair.knownModification == null){
					double dm = Math.abs(pair.spectrumA.precursorMass-pair.spectrumB.precursorMass);
					tableItem = new TableItem(this.pairTable, 0);
					
					String weakDeltaString="";
					if(pair.hasWeakDeltaSignal){weakDeltaString="y";}
					
					tableItem.setText(new String[] { 
	                         "" + this.getLowerPeptideMass(pair.spectrumA, pair.spectrumB).title,
	                         "" + String.format("%.4f",this.getLowerPeptideMass(pair.spectrumA,pair.spectrumB).precursorMass),
	                         "" + this.getHigherPeptideMass(pair.spectrumA , pair.spectrumB).title,
	                         "" + String.format("%.4f",dm),
	                         "" + String.format("%.1e",pair.p),
	                         "" + String.format("%.2f",pair.score),
	                         "" + this.getLowerPeptideMass(  pair.spectrumA , pair.spectrumB).proteinAsc,
	                         "" + this.getHigherPeptideMass( pair.spectrumA , pair.spectrumB).proteinAsc,
	                         "" + weakDeltaString
					});
				}
			}
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
	
	// calculate the higher mass
	double precMass = Math.max(spec1.precursorMass, spec2.precursorMass);
	
	// return the correct spectra
	if(precMass == spec1.precursorMass)
		return spec1;
	else
		return spec2;
}

}
