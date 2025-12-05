package com.foreign_trade.service;

import com.foreign_trade.model.License;
// Correct and necessary iText imports:
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    private static final String FILE_DIR = "generated_licenses/";

    /**
     * Generates a PDF document for the issued license.
     * @param license The issued License object.
     * @param exporterFirmName The name of the firm.
     * @return The path to the saved PDF file.
     * @throws DocumentException, IOException If PDF creation fails.
     */
    public String generateLicensePdf(License license, String exporterFirmName) throws DocumentException, IOException {

        // 1. Ensure the output directory exists
        new java.io.File(FILE_DIR).mkdirs();

        // 2. Define the output file path
        String fileName = FILE_DIR + license.getLicenseNumber() + "_" + license.getExporterId() + ".pdf";
        Document document = new Document();

        // 3. Create the PDF Writer instance
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // --- Document Content ---

        // Title (Line 46 fixed with explicit import resolution)
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("FOREIGN TRADE EXPORT LICENSE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // License Details
        document.add(new Paragraph("License Number: " + license.getLicenseNumber(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD)));
        document.add(new Paragraph("Issued To: " + exporterFirmName));
        document.add(new Paragraph("Issue Date: " + license.getIssueDate().format(DateTimeFormatter.ISO_DATE)));
        document.add(new Paragraph("Expiry Date: " + license.getExpiryDate().format(DateTimeFormatter.ISO_DATE), FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.RED)));
        document.add(Chunk.NEWLINE);

        // Signature Placeholder
        document.add(new Paragraph("This document is digitally issued and valid as per trade regulations.", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        document.add(new Paragraph("\n\nSignature Authority: [See file: " + license.getSignatureUrl() + "]"));

        // --- Close Document ---
        document.close();

        return fileName;
    }
}
