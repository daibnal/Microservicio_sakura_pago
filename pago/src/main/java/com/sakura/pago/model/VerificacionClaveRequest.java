package com.sakura.pago.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificacionClaveRequest {

	@NotBlank(message = "La clave es obligatoria")
	private String clave;

}
