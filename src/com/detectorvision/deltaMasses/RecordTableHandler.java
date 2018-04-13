
package com.detectorvision.deltaMasses;

import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.DeltaMassBase_table_record;

import java.util.ArrayList;
import java.util.Map;

import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


/**
 * Eventhandler for the UniMod Menu.
 * @author lehmamic 
 */
public class RecordTableHandler extends SelectionAdapter{


	// Logging with log4j
	static Logger logger = Logger.getLogger(RecordTableHandler.class.getName());
	public static Table RecordTable=null;
	public CCombo[] loadComboExperimentId = null;
	private int noRecords = 0;
	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public RecordTableHandler(){
		//this.uniMod = uniMod;
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){

		// get the mainshell
		logger.info("RecordTableHandler:Start:"+e.toString());
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();

		Map openWidgets = null;
		final Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/RecordDialog.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			logger.error("UniModHandler:XSWTException:"+error.toString());
		}
		dialogShell.pack();

		// objectreferences
		Button saveRecordButton = (Button)openWidgets.get("saveRecordButton");
		saveRecordButton.setToolTipText("save association records to experiments");
		Button cancelButton = (Button)openWidgets.get("cancelButton");
		Button loadRecordButton = (Button)openWidgets.get("loadRecordButton");
		loadRecordButton.setEnabled(false);
		loadRecordButton.setToolTipText("to be implemented in next version");
		Button deleteRecordButton = (Button)openWidgets.get("deleteRecordButton");
		deleteRecordButton.setToolTipText("Please observe: invalidates deltaProtein peptideNets, which have to be recalculated after removing a record.");
		if(!DeltaMasses.isDiscoveryEdition){
			loadRecordButton.setEnabled(false);
			deleteRecordButton.setEnabled(false);
			saveRecordButton.setEnabled(false);
			//loadRecordButton.setToolTipText("available in Discovery Edition only");
			deleteRecordButton.setToolTipText("available in Discovery Edition only");
			saveRecordButton.setToolTipText("available in Discovery Edition only");
		}
	
		RecordTable = (Table)openWidgets.get("RecordTable");
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
		
		
		//save Button
		SelectionAdapter saveDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				ArrayList<DeltaMassBase_table_record> table_records= new ArrayList<DeltaMassBase_table_record>();
				table_records=null;
				table_records=DeltaMassBase.get_filenames();
				 TableItem tableItem=null;
				
				 boolean storeOK=true; 
					for(int i=0; i<table_records.size(); i++){
						try{
							//refreshTable();
							//get the new fk_experiment_id from the table
							int tmpexpid=0;
							String tmpExperimentname="";
							RecordTable.update();
							TableItem[] items = RecordTable.getItems();
							boolean found=false;
							for(int j=0; j< items.length; j++)
							{
								if(table_records.get(i).record_id==Integer.parseInt(items[j].getText(0) )){
									//loadComboExperimentId[i].getText();
									//tmpexpid=Integer.parseInt(items[j].getText(5) );
									tmpexpid=Integer.parseInt(loadComboExperimentId[j].getText());
									tmpExperimentname=items[j].getText(6);
									found=true;
									break;
								}
							}
							if(!found){
								logger.error("storeRecordToExperiment error, record-exp not found");
							}
							
							
						DeltaMassBase.storeRecordToExperiment(	table_records.get(i).record_id, 
																tmpexpid, 
																tmpExperimentname);
						}
						catch(Exception eeee){
							storeOK=false;
							logger.error("storeRecordToExperiment error:"+eeee.toString());
						}
					}
					if(storeOK){logger.info("storeRecordToExperiment OK");}
					refreshTable();
			}
		};
		saveRecordButton.addSelectionListener(saveDialogEvent);
		
		

		//delete Button
		SelectionAdapter deleteRecordDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				ArrayList<Integer> id=getIDsOfSelectedRecords();

				MessageBox messageBox = new MessageBox(dialogShell, SWT.OK | SWT.CANCEL );
				messageBox.setText("please confirm/cancel record deleetion:");
				logger.info("selected "+id.size()+" records for deletion");
				String message="";
				for(int l_i=0;l_i<id.size();l_i++){
					message+=" "+id.get(l_i);
				}

				if(id.size()==1)
					messageBox.setMessage("click OK to delete record"+message+ " or CANCEL to skip.\nPlease note that the peptideNets constituting deltaProtein have to be recalculated if you delete a record.");
				else
					messageBox.setMessage("click OK to delete records "+message+ " or CANCEL to skip.\nPlease note that the peptideNets constituting deltaProtein have to be recalculated if you delete a record.");

				int buttonID = messageBox.open();
				if(buttonID==SWT.CANCEL){
					logger.info("user cancelled deletion of record(s).");
					return;
				}

				for(int i=0;i<id.size();i++){
						DeltaMasses.logger.info("deleting record id:"+id.get(i));
						if(DeltaMassBase.deleteRecord(id.get(i))){
							DeltaMasses.statusLabel.setText("deleted record_id:"+id.get(i));
							refreshTable();
						}
						else{
							logger.error("RecordTableHandler:error while deleting record_id:"+id.get(i));
							DeltaMasses.statusLabel.setText("error while deleting record_id:"+id.get(i));
							refreshTable();
						}
					}
				refreshTable();
			}
		};
		deleteRecordButton.addSelectionListener(deleteRecordDialogEvent);

		//load Button
		SelectionAdapter loadRecordDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				ArrayList<Integer> id=getIDsOfSelectedRecords();
				if(id.size()>0){
					DeltaMasses.logger.info("loading record id:"+id);
				}
				else{
					DeltaMasses.logger.error("something wrong when loading a record:"+id);	
				}
			}
		};
		loadRecordButton.addSelectionListener(loadRecordDialogEvent);
		for(int i=0;i<RecordTable.getColumnCount(); i++)
			RecordTable.getColumn(i).addListener(SWT.Selection, new TableSortHandler(i));		
		// show the screen
		dialogShell.open();	
	
	}
	
	void refreshCombo()
	{
		ArrayList<Integer> expIds= new ArrayList<Integer>();
		if(DeltaMasses.DMBcanBeReached){
			expIds=null;
			expIds=DeltaMassBase.getExperimentIds();
			TableItem[] items = RecordTable.getItems();
			for(int j=0; j< noRecords; j++)
			{
				int selection = 0;
				for(int i=0;i<expIds.size();i++)
				{
					loadComboExperimentId[j].add(String.valueOf(expIds.get(i)));
					if(String.valueOf(expIds.get(i)).equals(items[j].getText(5))){
						selection = i;
					}
				}
				loadComboExperimentId[j].select(selection);
			}
		}
	}

	boolean  refreshTable(){
		try{
			
			ArrayList<DeltaMassBase_table_record> table_records= new ArrayList<DeltaMassBase_table_record>();
			if(DeltaMasses.DMBcanBeReached){
				table_records=null;
				table_records=DeltaMassBase.get_filenames();
				RecordTable.clearAll();
				RecordTable.removeAll();
								    
			    TableItem tableItem=null;
	            noRecords = table_records.size();
				for(int i=0; i<table_records.size(); i++){
					tableItem = new TableItem(RecordTable,SWT.None);//never move the record_id from first place!!!!!
					tableItem.setText(new String[] {""+table_records.get(i).record_id,
							""+table_records.get(i).num_spectra  ,
							""+table_records.get(i).num_pairs  ,
							""+table_records.get(i).deltaDate.toString() ,
							""+table_records.get(i).filename,
							""+table_records.get(i).fk_experiment_id ,
							""+table_records.get(i).experimentname 
							
					});
				}
				
				TableItem[] items = RecordTable.getItems();
				loadComboExperimentId = new CCombo[items.length];
				TableEditor editor = null;
			    for (int i = 0; i < items.length; i++) {
			    	loadComboExperimentId[i] = new  CCombo(RecordTable, SWT.NONE);
			    	loadComboExperimentId[i].pack();
			    	editor = new TableEditor(RecordTable);
			    	editor.grabHorizontal = true;
			    	editor.minimumWidth = loadComboExperimentId[i].getSize ().x;
			    	editor.minimumHeight = loadComboExperimentId[i].getSize ().y ;
			    	editor.horizontalAlignment = SWT.CENTER;
			        editor.setEditor(loadComboExperimentId[i], items[i], 5);
			        loadComboExperimentId[i].addSelectionListener(RecordFromExperiment);
			    }
			    refreshCombo();
			    
			    //Event handler - To make experiment name as editable
			    final TableEditor editor1 = new TableEditor(RecordTable);
			    editor1.horizontalAlignment = SWT.LEFT;
			    editor1.grabHorizontal = true;

			    RecordTable.addListener(SWT.MouseDown, new Listener() {
				      public void handleEvent(Event event) {
				        Rectangle clientArea = RecordTable.getClientArea();
				        Point pt = new Point(event.x, event.y);
				        int index = RecordTable.getTopIndex();
				        while (index < RecordTable.getItemCount()) {
				          boolean visible = false;
				          final TableItem item = RecordTable.getItem(index);
				       	  Rectangle rect = item.getBounds(6);
				       	  if (rect.contains(pt)) 
				       	  {
				       		  final int column = 6;
				       		  final Text text = new Text(RecordTable, SWT.NONE);
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
				        	  editor1.setEditor(text, item, 6);
				        	  text.setText(item.getText(6));
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
			logger.error("refreshTable error:"+ee.toString());
			return false;
		}
		return true;
	}
	
	SelectionAdapter RecordFromExperiment = new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e){
			TableItem[] items = RecordTable.getItems();
		    for(int i = 0; i< noRecords; i++)
		    {
		    	if(e.getSource()== loadComboExperimentId[i])
		    	{
		    		int expId = Integer.parseInt(loadComboExperimentId[i].getText());
		    		//Changing experiment name whenever experiment id changes 
		    		items[i].setText(6, DeltaMassBase.getExperimentName(expId));
		    	}
		    }	
		}
	};
	
	
	
	ArrayList<Integer> getIDsOfSelectedRecords(){
		//returns the record IDs of the selected records.
		ArrayList<Integer> selectedRecords = new ArrayList<Integer>();
		if(RecordTable.getSelectionCount()<1){
			logger.info("getIDsOfSelectedRecords:Nothing selected");
			return selectedRecords;
		}
		else{
			TableItem[] selection = RecordTable.getSelection();
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
	
}