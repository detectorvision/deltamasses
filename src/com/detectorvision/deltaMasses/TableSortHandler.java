/* $Id: TableSortHandler.java 103 2008-02-24 19:33:43Z jari $ */

package com.detectorvision.deltaMasses;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableSortHandler implements Listener{

	// index of the sortable column
	private int index;
	SortComparator comparator;


	/**
	 * Constructor of the sorthandler, contains the index of the selected row.
	 * @param index Index of the selected row
	 */
	public TableSortHandler(int index){
		this.index = index;
		this.comparator  = new SortComparator();
	}

	/**
	 * Eventhandler
	 * @param e Event
	 */
	public void handleEvent(Event e) {

		// references
		TableColumn column = (TableColumn)e.widget;
		Table table = column.getParent();
		TableItem[] items = table.getItems();
		Collator collator = Collator.getInstance(Locale.getDefault());
		String[] values = new String[table.getColumnCount()];


		if(items != null && items.length >0){
			//System.out.println(comparator.compare("947.323029", "61.0523"));
			if(items[0].getText(0).equals("None"))
				this.quickSort(items, 1, items.length-1, table.getColumnCount());
			else
				this.quickSort(items, 0, items.length-1, table.getColumnCount());

			sort(items, table.getColumnCount(), table);
			//20061013 frankp check new eclipse.
			//20061102 checked new eclipse and it was okay
			table.setSortColumn(column);
			table.setSortDirection(SWT.DOWN);
		}
	}


	/**
	 * Simple but slow sort function
	 * @param data
	 * @param count
	 * @param table
	 */
	private void sort(TableItem[] data, int count, Table table){
		for (int i = 1; i < data.length; i++) {
			String value1 = data[i].getText(index);
			for (int j = 0; j < i; j++){
				String value2 = data[j].getText(index);
				if (this.comparator.compare(value1,value2) < 0) {    
					String[] tmp = new String[count]; 
					for(int k=0; k<count; k++){
						tmp[k] = data[i].getText(k);
					}
					data[i].dispose();
					TableItem item = new TableItem(table, SWT.NONE, j);
					item.setText(tmp);
					data = table.getItems();
					break;
				}
			}
		}
	}

	private void bubbleSort(TableItem[] data){
		boolean isChange = true;

		while(isChange){
			isChange = false;
			for(int i=0; i< data.length-1; i++){
				String value1 = data[i].getText(index);
				String value2 = data[i+1].getText(index);

				// if the following item is greater, then change
				if(comparator.compare(value1, value2) > 0){
					isChange = true;
					//System.out.println("change " +value1 +"and " +value2);
					data[i].setText(value2);
					data[i+1].setText(value1);
				}
			}
		}
	}

	private void quickSort(TableItem[] data, int lo, int hi, int count){
		int li = lo;
		int re = hi;
		int mid = (li+re)/2;



		// presort
		if(this.comparator.compare(data[li].getText(index).trim(), data[mid].getText(index).trim()) > 0)
			this.swap(data, li, mid, count);

		if(this.comparator.compare(data[mid].getText(index).trim(), data[re].getText(index).trim()) > 0)
			this.swap(data, mid, re, count);

		if(this.comparator.compare(data[li].getText(index).trim(), data[mid].getText(index).trim()) > 0)
			this.swap(data, li, mid, count);

		final TableItem pivot = data[mid];


		// partioning
		if((re - li) > 2){
			do{
				//System.out.println("Pivot: "+pivot.getText(index));
				while(this.comparator.compare(data[li].getText(index).trim(), pivot.getText(index).trim()) < 0){ li++;
				//System.out.println(data[li].getText(index));
				}
				//System.out.println("--");
				while(this.comparator.compare(pivot.getText(index).trim(), data[re].getText(index).trim()) < 0){ re--;
				//System.out.println(data[re].getText(index));
				}

				if(li <= re){
					//System.out.println("change: "+data[li].getText(index)+" : "+data[re].getText(index));
					this.swap(data, li, re, count);
					li++;
					re--;
				}
			}while(li <= re);

			// run recursive
			if(lo < re) this.quickSort(data, lo, re, count);
			if(li < hi) this.quickSort(data, li, hi, count);
		}
	}


	private void swap(TableItem[] data, int a, int b, int count){
		String[] tmpA = new String[count];
		String[] tmpB = new String[count];

		// Werte auslesen                	
		for(int k=0; k<count; k++){
			tmpA[k] = data[a].getText(k);
			tmpB[k] = data[b].getText(k);
		}

		data[a].setText(tmpB);
		data[b].setText(tmpA);	
	}

	private class SortComparator implements Comparator<String>{

		public int compare(String val1, String val2) {

			// variables
			double num1, num2;
			int result = 0;

			// try to cast, and compare the values
			try{
				num1 = Double.parseDouble(val1);
				num2 = Double.parseDouble(val2);

				if(num1 < num2) result = -1;
				else if(num1 == num2) result = 0;
				else if (num1 > num2) result = 1;
			}
			// if the num compair is faild, compair the strings
			catch(NumberFormatException e){
				result = val1.toLowerCase().compareTo(val2.toLowerCase());
			}
			//System.out.println(val1+" : "+val2+" = "+result);
			return result;
		}

	}

}
