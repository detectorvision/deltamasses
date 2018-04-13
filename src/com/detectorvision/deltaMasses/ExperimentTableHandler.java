
package com.detectorvision.deltaMasses;

import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.DeltaMassBase_table_experiment;

import java.util.ArrayList;
import java.util.Map;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


public class ExperimentTableHandler extends SelectionAdapter{

	// Logging with log4j
	static Logger logger = Logger.getLogger(RecordTableHandler.class.getName());
	public static Table ExperimentTable=null;
	//public CCombo[] loadComboExperimentId = null;
	TableEditor editor = null;
	TableItem tableItem = null;

	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public ExperimentTableHandler(){
		//this.uniMod = uniMod;
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){

		// get the mainshell
		logger.info("ExperimentTableHandler:Start:"+e.toString());
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();

		Map openWidgets = null;
		final Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/ExperimentDialog.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			logger.error("UniModHandler:XSWTException:"+error.toString());
		}
		dialogShell.pack();
		
		// objectreferences
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		Button storeExperimentButton = (Button)openWidgets.get("storeExperimentButton");
		
		if(!DeltaMasses.isDiscoveryEdition){
			storeExperimentButton.setEnabled(false);
		}
	
		ExperimentTable = (Table)openWidgets.get("ExperimentTable");
		refreshTable();

		//cancel Button
		SelectionAdapter closeDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				// get the shell object
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				dialogComposite.getShell().close();
			}
		};
		cancelButton.addSelectionListener(closeDialogEvent);
		
		
		//store Button
		SelectionAdapter storeExperimentDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				ArrayList<Integer> expid = getExperimentIDsOfSelectedRecords();
				ArrayList<String>  expNames = getExperimentNamesOfSelectedRecords();
				logger.info("selected "+expid.size()+" experiments for updation");
				for(int i=0;i<expid.size();i++){
						DeltaMasses.logger.info("storing experiment id:"+expid.get(i));
						if(DeltaMassBase.storeExperiment(expid.get(i),expNames.get(i))){
							DeltaMasses.statusLabel.setText("Stored experiment_id:"+expid.get(i));
							refreshTable();
						}
						else{
							logger.error("ExperimentTableHandler:error while storing experiment_id:"+expid.get(i));
							DeltaMasses.statusLabel.setText("error while storing experiment_id:"+expid.get(i));
							refreshTable();
						}
					}
				refreshTable();
			}
		};
		storeExperimentButton.addSelectionListener(storeExperimentDialogEvent);
		
		for(int i=0;i<ExperimentTable.getColumnCount(); i++)
			ExperimentTable.getColumn(i).addListener(SWT.Selection, new TableSortHandler(i));		
		// show the screen
		dialogShell.open();	
	
	}
	
	boolean  refreshTable(){
		try{
			
			ArrayList<DeltaMassBase_table_experiment> table_experiments= new ArrayList<DeltaMassBase_table_experiment>();
			if(DeltaMasses.DMBcanBeReached){
				table_experiments=null;
				ExperimentTable.clearAll();
				ExperimentTable.removeAll();
				table_experiments=DeltaMassBase.get_Experiments();
			    
				for(int i=0; i<table_experiments.size(); i++){
					tableItem = new TableItem(ExperimentTable,SWT.None);//never move the record_id from first place!!!!!
					tableItem.setText(new String[] {""+table_experiments.get(i).experiment_id,
							""+table_experiments.get(i).experimentname});
				}
	
			    //Event handler - To make experiment name as editable
			    final TableEditor editor1 = new TableEditor(ExperimentTable);
			    editor1.horizontalAlignment = SWT.LEFT;
			    editor1.grabHorizontal = true;

			    ExperimentTable.addListener(SWT.MouseDown, new Listener() {
				      public void handleEvent(Event event) {
				        Rectangle clientArea = ExperimentTable.getClientArea();
				        Point pt = new Point(event.x, event.y);
				        int index = ExperimentTable.getTopIndex();
				        while (index < ExperimentTable.getItemCount()) {
				          boolean visible = false;
				          final TableItem item = ExperimentTable.getItem(index);
				       	  Rectangle rect = item.getBounds(1);
				       	  if (rect.contains(pt)) 
				       	  {
				       		  final int column = 1;
				       		  final Text text = new Text(ExperimentTable, SWT.NONE);
				        	  Listener textListener = new Listener(){
				        		  public void handleEvent(final Event e){
				        			  switch (e.type) {
				        			  case SWT.FocusOut:
				        				  item.setText(column, text.getText());
				        				  text.dispose();
				        				  break;
				        			  case SWT.Traverse:
				        				  switch (e.detail) {
				        				  	case SWT.TRAVERSE_RETURN:
				        				  		item.setText(column, text.getText());
				        				  	case SWT.TRAVERSE_ESCAPE:
				        				  		text.dispose();
				        				  		e.doit = false;
				        				  }
				        			  break;
				        		  }
				        	  }};
				        	  text.addListener(SWT.FocusOut, textListener);
				        	  text.addListener(SWT.Traverse, textListener);
				        	  editor1.setEditor(text, item, 1);
				        	  text.setText(item.getText(1));
				        	  text.selectAll();
				        	  text.setFocus();
				        	  return;
				            }
				            if (!visible && rect.intersects(clientArea)) {
				              visible = true;
				            }
				            if (!visible)
				            	return;
				            index++;
				        }
				      }
				    });
	
			}
		}
		catch(Exception ee){
			logger.error("ExperimentTableHandler- refreshTable error in :"+ee.toString());
			return false;
		}
		return true;
	}
		
	ArrayList<Integer> getExperimentIDsOfSelectedRecords(){
		//returns the experiment IDs of the selected records.
		ArrayList<Integer> selectedRecords = new ArrayList<Integer>();
		if(ExperimentTable.getSelectionCount() < 1){
			logger.info("getExperimentIDsOfSelectedRecords:Nothing selected");
			return selectedRecords;
		}
		else{
			TableItem[] selection = ExperimentTable.getSelection();
			for (int l_i = 0; l_i < selection.length; l_i++){
				try{
					selectedRecords.add(Integer.parseInt(selection[l_i].getText(0)));
				}
				catch(Exception e){
					DeltaMasses.logger.error("selection error:"+e.toString());
					selectedRecords.clear();
					return selectedRecords;	
				}
			}
			return selectedRecords;
		}
	}
	
	ArrayList<String> getExperimentNamesOfSelectedRecords(){
		//returns the Experiment names of the selected records.
		ArrayList<String> selectedRecords = new ArrayList<String>();
		if(ExperimentTable.getSelectionCount()<1){
			logger.info("getExperimentNamesOfSelectedRecords:Nothing selected");
			return selectedRecords;
		}
		else{
			TableItem[] selection = ExperimentTable.getSelection();
			for (int l_i = 0; l_i < selection.length; l_i++){
				try{
					selectedRecords.add(selection[l_i].getText(1));
				}
				catch(Exception e){
					DeltaMasses.logger.error("selection error:"+e.toString());
					selectedRecords.clear();
					return selectedRecords;	
				}
			}
			return selectedRecords;
		}
	}
	
}