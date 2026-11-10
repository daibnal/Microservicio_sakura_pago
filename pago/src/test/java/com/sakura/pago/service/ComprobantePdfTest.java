package com.sakura.pago.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.pdfbox.Loader; // Importar Loader
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sakura.pago.model.Comprobante;

import static org.assertj.core.api.Assertions.assertThat;

class ComprobantePdfTest {

	private ComprobantePdf comprobantePdf;

	@BeforeEach
	void setUp() {
		comprobantePdf = new ComprobantePdf();
	}

	@Test
	@DisplayName("Debe generar un PDF válido en arreglo de bytes no vacío")
	void debeGenerarPdfValido() {
		// Given
		Comprobante comprobante = crearComprobanteEjemplo();

		// When
		byte[] pdfBytes = comprobantePdf.generar(comprobante);

		// Then
		assertThat(pdfBytes).isNotNull();
		assertThat(pdfBytes.length).isGreaterThan(0);
	}

	@Test
	@DisplayName("Debe contener los datos del comprobante dentro del contenido del PDF")
	void debeContenerDatosDelComprobanteEnElPdf() throws IOException {
		// Given
		Comprobante comprobante = crearComprobanteEjemplo();

		// When
		byte[] pdfBytes = comprobantePdf.generar(comprobante);

		// Then - Lectura del contenido con Apache PDFBox
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String textoPdf = stripper.getText(document);

            assertThat(textoPdf)
                    .contains("Comprobante Sakura Salón")
                    .contains("Cliente: Sakura Kinomoto")
                    .contains("Servicio: Corte de Cabello")
                    .contains("Fecha de la cita: 2026-10-15")
                    .contains("Tipo: Tarjeta")
                    .contains("Monto: $25000")
                    .contains("Fecha del pago: 2026-10-12T10:30");
        }
	}

	private Comprobante crearComprobanteEjemplo() {
		return new Comprobante(
				100L,
				"Sakura Kinomoto",
				"Corte de Cabello",
				LocalDate.of(2026, 10, 15),
				"Tarjeta",
				25000,
				LocalDateTime.of(2026, 10, 12, 10, 30)
		);
	}

}