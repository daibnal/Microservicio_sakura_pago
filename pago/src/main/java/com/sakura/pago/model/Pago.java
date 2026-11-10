package com.sakura.pago.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long reservaId;

	@Column(nullable = false)
	private Long idCliente;

	@Column(nullable = false, length = 120)
	private String nombreCliente;

	@Column(nullable = false, length = 120)
	private String nombreServicio;

	@Column(nullable = false)
	private LocalDate fechaCita;

	@Column(nullable = false, length = 20)
	private String tipo;

	@Column(nullable = false)
	private Integer monto;

	@Column(nullable = false)
	private boolean aprobado;

	@Column(nullable = false)
	private boolean anulado = false;

	@Column(nullable = false)
	private LocalDateTime creadaEn;

}
