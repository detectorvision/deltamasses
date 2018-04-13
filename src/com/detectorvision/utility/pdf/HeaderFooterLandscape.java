/* $Id: HeaderFooterLandscape.java 452 2013-01-26 08:28:41Z frank $ */

package com.detectorvision.utility.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.log4j.Logger;


public class HeaderFooterLandscape extends PdfPageEventHelper {

	protected PdfPTable header;
	protected PdfPTable footer;
	protected PdfTemplate total;
	protected BaseFont helv;

	// Logging with log4j
	static Logger logger = Logger.getLogger(HeaderFooterLandscape.class.getName());

	/**
	 * adds a Header and a Footer.
	 */
	public HeaderFooterLandscape() {
		int tableWidth=750;//table width in points
		ArrayList<String> footerArray = new ArrayList<String>();
		footerArray=Footer.getInfo();
		
		header = new PdfPTable(3);
		float[] widths2 = { 1f, 4f, 1f };
		try {
			header.setWidths(widths2);
		} catch (DocumentException e1) {
			e1.printStackTrace();
		}
		
		
		header.setTotalWidth(tableWidth);
		header.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		header.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);	
		try {
			Image png1 = Image.getInstance("images/detectorvision_logo_for_pdf_header.png");
			png1.scaleToFit(120, 50);
			Image png4 = Image.getInstance("images/deltaMasses.png");
			png4.scaleToFit(80, 40);
			String laboratoryLogo="config/laboratory_logo.png";
			File laboratoryLogoFile=new File(laboratoryLogo);
			if(laboratoryLogoFile.exists()){

				logger.info("Customer logo found");
				Image customerLogoPic=Image.getInstance(laboratoryLogo);
				customerLogoPic.scaleToFit(196, 44);
				PdfPCell cell1 = new PdfPCell(customerLogoPic,false);
				cell1.setBorder(PdfPCell.NO_BORDER);
				cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell1.setVerticalAlignment(Element.ALIGN_BOTTOM);
				header.addCell(cell1);

				PdfPCell cell2 =  new PdfPCell();
				//PdfPCell cell2 = new PdfPCell(png4,false);
				cell2.setBorder(PdfPCell.NO_BORDER);
				cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell2.setVerticalAlignment(Element.ALIGN_BOTTOM);
				header.addCell(cell2);

				PdfPCell cell3 = new PdfPCell(png1,false);
				cell3.setBorder(PdfPCell.NO_BORDER);
				cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell3.setVerticalAlignment(Element.ALIGN_BOTTOM);
				header.addCell(new PdfPCell(cell3));
			}
			else{
				logger.info("Customer logo not found");				
				header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
				header.addCell(png4);
				header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
				header.addCell(new Paragraph("          "));	
				header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
				header.addCell(png1);
			}
			PdfPCell cell4 = new PdfPCell();
			cell4.setColspan(3);
			cell4.setFixedHeight(10);
			cell4.setBorder(PdfPCell.NO_BORDER);
			header.addCell(cell4);
			
		} catch (BadElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		footer = new PdfPTable(4);
		footer.setTotalWidth(tableWidth);
		footer.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		footer.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);	
		try {

			PdfPCell cell4 = new PdfPCell();//whitespace between bar and text
			cell4.setColspan(4);
			cell4.setFixedHeight(10);
			cell4.setBorder(PdfPCell.NO_BORDER);
			header.addCell(cell4);

			//TODO clean up the code below 
			PdfPCell cell5 = new PdfPCell();
			Paragraph p = new Paragraph(footerArray.get(0),
					FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI,
					BaseFont.NOT_EMBEDDED, 9));
			p.setAlignment(Element.ALIGN_LEFT);
			cell5.setBorder(PdfPCell.NO_BORDER);
			cell5.addElement(p);
			footer.addCell(cell5);
			
			
			PdfPCell cell7 = new PdfPCell();
			Paragraph p3 = new Paragraph(footerArray.get(1),
					FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI,
					BaseFont.NOT_EMBEDDED, 9));
			p3.setAlignment(Element.ALIGN_CENTER);
			cell7.setBorder(PdfPCell.NO_BORDER);
			cell7.addElement(p3);
			footer.addCell(cell7);		
			
			PdfPCell cell8 = new PdfPCell();
			Paragraph p4 = new Paragraph(footerArray.get(2),
					FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI,
					BaseFont.NOT_EMBEDDED, 9));
			p4.setAlignment(Element.ALIGN_CENTER);
			cell8.setBorder(PdfPCell.NO_BORDER);
			cell8.addElement(p4);
			footer.addCell(cell8);
			
			PdfPCell cell9 = new PdfPCell();
			Paragraph p5 = new Paragraph(footerArray.get(3),
					FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI,
					BaseFont.NOT_EMBEDDED, 8));
			p5.setAlignment(Element.ALIGN_RIGHT);
			cell9.setBorder(PdfPCell.NO_BORDER);
			
			cell9.addElement(p5);
			footer.addCell(cell9);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		} 
	}
	
	public void onOpenDocument(PdfWriter writer, Document document) {
		total = writer.getDirectContent().createTemplate(100, 100);
		total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
		try {
			helv = BaseFont.createFont(BaseFont.HELVETICA,
					BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ExceptionConverter(e);
		}
	}

	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte cb = writer.getDirectContent();

		if(document.getPageNumber()>1){
			cb.saveState();
			String text = "page " + writer.getPageNumber() + " of ";
			float textBase = document.bottom() - 6;
			float textSize = helv.getWidthPoint(text, 9);
			cb.beginText();
			cb.setFontAndSize(helv, 9);
			if ((writer.getPageNumber() % 2) == 1) {
				cb.setTextMatrix(document.left(), textBase);
				cb.showText(text);
				cb.endText();
				cb.addTemplate(total, document.left() + textSize, textBase);
			}
			else {
				float adjust = helv.getWidthPoint("0", 9)+7;
				cb.setTextMatrix(
						document.right() - textSize - adjust, textBase);
				cb.showText(text);
				cb.endText();
				cb.addTemplate(total, document.right() - adjust, textBase);
			}
			cb.restoreState();
		}

		header.writeSelectedRows(0, -1, document.leftMargin(), document.top()+80, cb);
		footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottom() - 10, cb);
	}
	
	public void onCloseDocument(PdfWriter writer, Document document) {
		total.beginText();
		total.setFontAndSize(helv, 9);
		total.setTextMatrix(0, 0);
		total.showText(String.valueOf(writer.getPageNumber() - 1));
		total.endText();
		}
	
}