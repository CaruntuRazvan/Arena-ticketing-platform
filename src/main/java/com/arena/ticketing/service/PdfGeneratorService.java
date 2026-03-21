package com.arena.ticketing.service;

import com.arena.ticketing.dto.TicketResponseDTO;
import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;


@Service
public class PdfGeneratorService {

    public byte[] generateTicketPdf(TicketResponseDTO ticket) { // Folosim DTO aici
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A6);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("ARENA TICKETING", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));
            // Folosim câmpurile din DTO:
            document.add(new Paragraph("MECI: " + ticket.getOpponentName(), titleFont));
            document.add(new Paragraph("SECTOR: " + ticket.getSectorName(), infoFont));
            document.add(new Paragraph("RAND: " + ticket.getRowNumber() + " | LOC: " + ticket.getSeatNumber(), infoFont));
            document.add(new Paragraph("PRET: " + ticket.getFinalPrice() + " RON", infoFont));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("COD BILET:", infoFont));
            document.add(new Paragraph(ticket.getTicketCode(), FontFactory.getFont(FontFactory.COURIER, 10)));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea PDF-ului din DTO", e);
        }
        return out.toByteArray();
    }
}
