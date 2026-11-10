package com.sakura.pago.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.pago.client.SesionDTO;
import com.sakura.pago.client.SesionGuard;
import com.sakura.pago.model.Comprobante;
import com.sakura.pago.model.Pago;
import com.sakura.pago.model.RegistroPago;
import com.sakura.pago.model.VerificacionClaveRequest;
import com.sakura.pago.service.ComprobantePdf;
import com.sakura.pago.service.PagoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

	@Autowired
	private PagoService pagoService;

	@Autowired
	private SesionGuard sesionGuard;

	@Autowired
	private ComprobantePdf comprobantePdf;

	@PostMapping("/registro")
	public ResponseEntity<Pago> registrar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody RegistroPago datos) {
		sesionGuard.exigirSesion(authorization);
		return new ResponseEntity<>(pagoService.registrar(datos), HttpStatus.CREATED);
	}

	@GetMapping("/mios")
	public List<Pago> mios(@RequestHeader(value = "Authorization", required = false) String authorization) {
		SesionDTO sesion = sesionGuard.exigirSesion(authorization);
		return pagoService.mios(sesion.getId());
	}

	@GetMapping("/periodo")
	public List<Pago> periodo(@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		sesionGuard.exigirAdmin(authorization);
		return pagoService.periodo(desde, hasta);
	}

	@PostMapping("/{id}/anular")
	public Pago anular(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id, @Valid @RequestBody VerificacionClaveRequest datos) {
		sesionGuard.exigirAdmin(authorization);
		return pagoService.anular(id, authorization, datos.getClave());
	}

	@GetMapping("/reserva/{reservaId}/comprobante")
	public ResponseEntity<?> comprobante(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long reservaId,
			@RequestParam(required = false) String formato) {
		sesionGuard.exigirSesion(authorization);
		Comprobante comprobante = pagoService.comprobante(reservaId);
		if ("pdf".equalsIgnoreCase(formato)) {
			byte[] pdf = comprobantePdf.generar(comprobante);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante.pdf")
					.contentType(MediaType.APPLICATION_PDF)
					.body(pdf);
		}
		return ResponseEntity.ok(comprobante);
	}

}
