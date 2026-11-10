package com.sakura.pago.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistroPago {

	@NotNull(message = "La reserva es obligatoria")
	private Long reservaId;

	@NotNull(message = "El cliente es obligatorio")
	private Long idCliente;

	@NotBlank(message = "El nombre del cliente es obligatorio")
	private String nombreCliente;

	@NotBlank(message = "El servicio es obligatorio")
	private String nombreServicio;

	@NotNull(message = "La fecha de la cita es obligatoria")
	private LocalDate fechaCita;

	@NotBlank(message = "El tipo es obligatorio")
	private String tipo;

	@NotNull(message = "El monto es obligatorio")
	private Integer monto;

	private boolean aprobado;

}
