package com.arena.notification.service;


import com.arena.notification.dto.external.TicketResponseDTO;
import com.arena.notification.exception.NotificationException;
import com.arena.notification.util.QrCodeGenerator;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;


@Service
public class PdfGeneratorService {

    public byte[] generateTicketPdf(TicketResponseDTO ticket) { // Folosim DTO aici
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy - HH:mm");

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font matchFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 7);
            Paragraph title = new Paragraph("ARENA TICKETING", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));
            // Folosim câmpurile din DTO:
            document.add(new Paragraph("MECI: " + ticket.getOpponentName(), matchFont));
            document.add(new Paragraph("DATA: " + (ticket.getMatchDate() != null ? ticket.getMatchDate().format(formatter) : "N/A"), infoFont));
            document.add(new Paragraph("SECTOR: " + ticket.getSectorName(), infoFont));
            document.add(new Paragraph("RAND: " + ticket.getRowNumber() + " | LOC: " + ticket.getSeatNumber(), infoFont));
            //document.add(new Paragraph("PRET: " + ticket.getFinalPrice() + " RON", infoFont));
            document.add(new Paragraph("PRET: " + String.format("%.2f", ticket.getFinalPrice()) + " RON", infoFont));
            //document.add(new Paragraph("STATUS: " + ticket.getStatus(), infoFont));

            document.add(new Paragraph("\n"));

            byte[] qrBytes = QrCodeGenerator.generateQrCodeImage(ticket.getTicketCode());
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.scaleToFit(160, 160); // Să arate bine pe A6
            document.add(qrImage);


            document.close();
        } catch (Exception e) {
            throw new NotificationException("Eroare la generarea PDF-ului din DTO", e);
        }
        return out.toByteArray();
    }
}