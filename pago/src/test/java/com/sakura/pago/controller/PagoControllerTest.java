package com.sakura.pago.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sakura.pago.client.SesionDTO;
import com.sakura.pago.client.SesionGuard;
import com.sakura.pago.exception.NoAutenticadoException;
import com.sakura.pago.exception.RecursoNoEncontradoException;
import com.sakura.pago.model.Comprobante;
import com.sakura.pago.model.Pago;
import com.sakura.pago.service.ComprobantePdf;
import com.sakura.pago.service.PagoService;

@WebMvcTest(PagoController.class)
@ActiveProfiles("test")
class PagoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PagoService pagoService;

	@MockitoBean
	private SesionGuard sesionGuard;

	@MockitoBean
	private ComprobantePdf comprobantePdf;

	private static final String AUTH = "Bearer token-cliente";

	@BeforeEach
	void setUp() {
		SesionDTO sesion = new SesionDTO();
		sesion.setId(7L);
		when(sesionGuard.exigirSesion(any())).thenReturn(sesion);
	}

	@Test
	void registrar_Exito() throws Exception {
		when(pagoService.registrar(any())).thenReturn(pago(1L));

		mockMvc.perform(post("/api/v1/pagos/registro")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"reservaId\":4,\"idCliente\":7,\"nombreCliente\":\"Juan\",\"nombreServicio\":\"Corte\",\"fechaCita\":\"2026-05-10\",\"tipo\":\"ABONO\",\"monto\":5000,\"aprobado\":true,\"anulado\":false}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("ABONO"));
	}

	@Test
	void registrar_CuerpoVacio_Responde400() throws Exception {
		mockMvc.perform(post("/api/v1/pagos/registro")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void mios_Exito() throws Exception {
		when(pagoService.mios(7L)).thenReturn(List.of(pago(1L)));

		mockMvc.perform(get("/api/v1/pagos/mios").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].monto").value(5000));
	}

	@Test
	void mios_SinSesion_Responde401() throws Exception {
		when(sesionGuard.exigirSesion(any())).thenThrow(new NoAutenticadoException("Debe iniciar sesión"));

		mockMvc.perform(get("/api/v1/pagos/mios"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void periodo_Exito() throws Exception {
		when(pagoService.periodo(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31))).thenReturn(List.of(pago(1L)));

		mockMvc.perform(get("/api/v1/pagos/periodo")
				.header("Authorization", AUTH)
				.param("desde", "2026-05-01")
				.param("hasta", "2026-05-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));
	}

	@Test
	void anular_Exito() throws Exception {
		Pago anulado = pago(1L);
		anulado.setAnulado(true);
		when(pagoService.anular(1L, AUTH, "secreto1")).thenReturn(anulado);

		mockMvc.perform(post("/api/v1/pagos/1/anular")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"clave\":\"secreto1\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.anulado").value(true));
	}

	@Test
	void comprobanteJson_Exito() throws Exception {
		when(pagoService.comprobante(4L)).thenReturn(comprobante());

		mockMvc.perform(get("/api/v1/pagos/reserva/4/comprobante").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cliente").value("Juan"));
	}

	@Test
	void comprobantePdf_Exito() throws Exception {
		when(pagoService.comprobante(4L)).thenReturn(comprobante());
		when(comprobantePdf.generar(any())).thenReturn("%PDF".getBytes());

		mockMvc.perform(get("/api/v1/pagos/reserva/4/comprobante").header("Authorization", AUTH).param("formato", "pdf"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante.pdf"));
	}

	@Test
	void comprobante_NoExiste_Responde404() throws Exception {
		when(pagoService.comprobante(99L)).thenThrow(new RecursoNoEncontradoException("No hay un pago para esta reserva"));

		mockMvc.perform(get("/api/v1/pagos/reserva/99/comprobante").header("Authorization", AUTH))
				.andExpect(status().isNotFound());
	}

	private Pago pago(Long id) {
		Pago pago = new Pago();
		pago.setId(id);
		pago.setTipo("ABONO");
		pago.setMonto(5000);
		pago.setNombreCliente("Juan");
		return pago;
	}

	private Comprobante comprobante() {
		return new Comprobante(4L, "Juan", "Corte", LocalDate.of(2026, 5, 10), "ABONO", 5000, LocalDateTime.of(2026, 5, 10, 12, 0));
	}
}
