package com.sakura.pago.exception;

public class NoAutenticadoException extends RuntimeException {
	public NoAutenticadoException(String mensaje) {
		super(mensaje);
	}
}
