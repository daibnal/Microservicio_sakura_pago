package com.sakura.pago.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.sakura.pago.model.Comprobante;

@Service
public class ComprobantePdf {

	public byte[] generar(Comprobante comprobante) {
		return pdf("Comprobante Sakura Salón", List.of(
				"Cliente: " + comprobante.getCliente(),
				"Servicio: " + comprobante.getServicio(),
				"Fecha de la cita: " + comprobante.getFecha(),
				"Tipo: " + comprobante.getTipo(),
				"Monto: $" + comprobante.getMonto(),
				"Fecha del pago: " + comprobante.getFechaPago()));
	}

	private byte[] pdf(String titulo, List<String> lineas) {
		try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
			Document documento = new Document();
			PdfWriter.getInstance(documento, salida);
			documento.open();
			documento.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
			for (String linea : lineas) {
				documento.add(new Paragraph(linea, FontFactory.getFont(FontFactory.HELVETICA, 11)));
			}
			documento.close();
			return salida.toByteArray();
		} catch (Exception ex) {
			throw new IllegalStateException("No se pudo generar el PDF");
		}
	}

}
