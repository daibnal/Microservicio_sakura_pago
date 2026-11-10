package com.sakura.pago.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comprobante {

	private Long reservaId;

	private String cliente;

	private String servicio;

	private LocalDate fecha;

	private String tipo;

	private int monto;

	private LocalDateTime fechaPago;

}
