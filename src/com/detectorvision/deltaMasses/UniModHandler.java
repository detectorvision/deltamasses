/* $Id: UniModHandler.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.deltaMasses;

import com.detectorvision.deltaMasses.gui.diagrams.SpectrumCanvas;
import com.detectorvision.massspectrometry.unimod.Modification;
import com.detectorvision.massspectrometry.unimod.UniModDatabase;
import com.detectorvision.utility.ExternalFileHandler;
import com.detectorvision.utility.pdf.HeaderFooter;
import com.detectorvision.utility.pdf.HeaderFooterLandscape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;

import com.lowagie.text.Anchor;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.swtworkbench.community.xswt.XSWT;
import com.swtworkbench.community.xswt.XSWTException;

import org.apache.log4j.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


/**
 * Eventhandler for the UniMod Menu.
 * @author lehmamic 
 */
public class UniModHandler extends SelectionAdapter{

	// Attributes
	private UniModDatabase uniMod;

	// Logging with log4j
	static Logger logger = Logger.getLogger(UniModHandler.class.getName());


	/**
	 * Constructor width a reference to the unimod database object
	 * @param uniMod unimod database
	 */
	public UniModHandler(UniModDatabase uniMod){
		this.uniMod = uniMod;
	}

	/**
	 * Event method, open the unimod screen.
	 * @param e eventdata.
	 */
	public void widgetSelected(SelectionEvent e){

		// get the mainshell
		final MenuItem menuItem = (MenuItem)e.getSource();
		final Menu menu = menuItem.getParent();
		final Shell mainShell = menu.getShell();

		// open the fileopen screen
		Map openWidgets = null;
		Shell dialogShell = new Shell(mainShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		try {
			openWidgets = 
				XSWT.create(this.getClass().getResourceAsStream("gui/UnimodDialog.xswt"))
				.parse(dialogShell);
		}
		catch (XSWTException error) {
			logger.error("UniModHandler:XSWTException:"+error.toString());
		}
		dialogShell.pack();

		// objectreferences
		Button cancelButton    = (Button)openWidgets.get("cancelButton");
		Button unimodPdfButton = (Button)openWidgets.get("unimodPdfButton");
		final Table unimodTable = (Table)openWidgets.get("unimodTable");

		// create the table
		ArrayList modList = this.uniMod.getModifications();
		for(int i=0; i<modList.size(); i++){
			// get the modification from the list
			Modification mod = (Modification)modList.get(i);
			// set table data for ths modification
			final TableItem tableItem = new TableItem(unimodTable, 0);
			tableItem.setText(new String[] {mod.shortName, ""+mod.monoisotopic, mod.composition, mod.fullName, mod.postedDate, mod.modifiedDate, ""+mod.unimodID, ""+Math.abs(mod.monoisotopic)});
		}

		SelectionAdapter closeDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){

				// get the shell object
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				dialogComposite.getShell().close();
			}
		};
		cancelButton.addSelectionListener(closeDialogEvent);

		SelectionAdapter unimodPdfDialogEvent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){

				// get the shell object
				final Button button = (Button)e.getSource();
				final Composite dialogComposite = (Composite)button.getParent();
				logger.info("starting to dynamically print the unimod-pdf");
				
				Document document =  new Document(PageSize.A4, 45, 40, 110, 50);//left right top bottom
				document.setMarginMirroring(true);
				document.addTitle("deltaMasses: UNIMOD modifications");
				document.addSubject("Detection of protein modifications");
				document.addKeywords("PTM PTM-Detection PTM-Localization deltaMasses Detectorvion AG Differential PTM Detection");
				document.addAuthor("deltaMasses used by"+System.getProperty("user.name"));

				try {
					Font fontSmall = new Font(Font.HELVETICA, 8);
					int pdfPictureWidth=750;
					int pdfPictureHeight=410;
					PdfWriter writer = PdfWriter.getInstance(document,
							new FileOutputStream("tmp/UNIMOD.pdf"));
					writer.setPageEvent(new HeaderFooter());
					writer.setPageEvent(new HeaderFooter());
					document.open();
					Anchor anchor = new Anchor("http://www.unimod.org (Click)");
					anchor.setReference("http://www.unimod.org");
					anchor.setName("");
					document.add( anchor);
					document.add(new Paragraph("Unimod: Protein modifications for mass spectrometry. Proteomics. 2004 Jun;4(6):1534-6."));
					document.add(new Paragraph("Unimod is a database of protein modifications for use in mass spectrometry applications, especially protein identification and de novo sequencing. It contains accurate and verifiable values, derived from elemental compositions, for the mass differences introduced by both natural and artificial modifications."));
					Anchor anchor3 = new Anchor("http://www.ncbi.nlm.nih.gov/pubmed/15174123 (Click)");
					anchor3.setReference("http://www.ncbi.nlm.nih.gov/pubmed/15174123");
					anchor3.setName("");
					document.add( anchor3);
					document.add(new Paragraph(" "));
					
					
					PdfPTable table = new PdfPTable(5);
					table.setHeaderRows(1);
					float[] widths2 = { 6f , 4f , 9f , 9f , 2f };
					try {
						table.setWidths(widths2);
					} catch (DocumentException e1) {
						logger.error("unimod-pdf error:"+e1.toString());
						e1.printStackTrace();
					}
					table.setHorizontalAlignment(Element.ALIGN_LEFT);
					table.setSpacingBefore(10f);//what actualy does 10f mean ?
					table.setSpacingAfter(10f);
					table.setWidthPercentage(100);
					
					//t5 able headings
					PdfPCell cell=null;
					
					cell = new PdfPCell(new Paragraph("modification"));
					cell.setColspan(1);
					cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					table.addCell(cell);

					cell = new PdfPCell(new Paragraph("deltaMass"));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					table.addCell(cell);

					cell = new PdfPCell(new Paragraph("composition"));
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					table.addCell(cell);

					cell = new PdfPCell(new Paragraph("full name"));
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					table.addCell(cell);

					cell = new PdfPCell(new Paragraph("id"));
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setBackgroundColor(new Color(0xDD, 0xDD, 0xDD));
					table.addCell(cell);
					
					//add the data to the table
					logger.info("unimodtable.getitemcount:"+unimodTable.getItemCount());
					for(int i=0;i<unimodTable.getItemCount();i++){
						cell = new PdfPCell(new Paragraph(unimodTable.getItem(i).getText(0),fontSmall));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						table.addCell(cell);
						
					    NumberFormat formatter = new DecimalFormat("#0.000000");
					    //next line is a bit freaky, sorry!
						String tmpDelta=formatter.format(Double.parseDouble(unimodTable.getItem(i).getText(1)));
					    cell = new PdfPCell(new Paragraph(tmpDelta,fontSmall));
						//cell = new PdfPCell(new Paragraph(""+formatter.format(mod.monoisotopic),fontSmall));
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						table.addCell(cell);
						
						cell = new PdfPCell(new Paragraph(unimodTable.getItem(i).getText(2),fontSmall));
						//cell = new PdfPCell(new Paragraph(mod.composition,fontSmall));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						table.addCell(cell);
						
						cell = new PdfPCell(new Paragraph(unimodTable.getItem(i).getText(3),fontSmall));
						//cell = new PdfPCell(new Paragraph(mod.fullName,fontSmall));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						table.addCell(cell);
						
						cell = new PdfPCell(new Paragraph(unimodTable.getItem(i).getText(6),fontSmall));
						//cell = new PdfPCell(new Paragraph(""+mod.unimodID,fontSmall));
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setBackgroundColor(new Color(0xFF, 0xFF, 0xFF));
						table.addCell(cell);	
						//tableItem.setText(new String[] {mod.shortName, ""+mod.monoisotopic, mod.composition, mod.fullName, mod.postedDate, mod.modifiedDate, ""+mod.unimodID, ""+Math.abs(mod.monoisotopic)});
						if(i==493){
							logger.info("OK, reached number 493.");
						}
					}
					document.add(table);
				}catch(Exception ee){
					logger.error("unimod-pdf-exception:"+ee.toString());
				};
				try{
				document.close();
				}catch(Exception badclose){
					logger.error("could not close unimod-pdf file:"+badclose.toString());
				}
				try {//wait a bit before opening the doc ... 
			        long numMillisecondsToSleep = 100; // 10th of a second
			        Thread.sleep(numMillisecondsToSleep);
			    } catch (InterruptedException eee) {
			    	logger.error("sleep problems:"+eee.toString());
			    }

				if (ExternalFileHandler.open("tmp/UNIMOD.pdf")!=0)
					logger.error("graphicsToPdfAdapter:pdf error:Acrobat Reader not installled or file association for pdf is not there");
			}
		};
		unimodPdfButton.addSelectionListener(unimodPdfDialogEvent);

		for(int i=0;i<unimodTable.getColumnCount(); i++)
			unimodTable.getColumn(i).addListener(SWT.Selection, new TableSortHandler(i));		
		// show the screen
		dialogShell.open();	
	}
}