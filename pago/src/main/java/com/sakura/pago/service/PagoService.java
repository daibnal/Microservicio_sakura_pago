package com.sakura.pago.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakura.pago.client.PersonasCliente;
import com.sakura.pago.exception.RecursoNoEncontradoException;
import com.sakura.pago.model.Comprobante;
import com.sakura.pago.model.Pago;
import com.sakura.pago.model.RegistroPago;
import com.sakura.pago.repository.PagoRepository;

@Service
public class PagoService {

	@Autowired
	private PagoRepository pagoRepository;

	@Autowired
	private PersonasCliente personasCliente;

	public Pago registrar(RegistroPago datos) {
		Pago pago = new Pago();
		pago.setReservaId(datos.getReservaId());
		pago.setIdCliente(datos.getIdCliente());
		pago.setNombreCliente(datos.getNombreCliente());
		pago.setNombreServicio(datos.getNombreServicio());
		pago.setFechaCita(datos.getFechaCita());
		pago.setTipo(datos.getTipo());
		pago.setMonto(datos.getMonto());
		pago.setAprobado(datos.isAprobado());
		pago.setAnulado(false);
		pago.setCreadaEn(LocalDateTime.now());
		return pagoRepository.save(pago);
	}

	public List<Pago> mios(Long idCliente) {
		return pagoRepository.findByIdClienteAndAprobadoTrueOrderByCreadaEnDesc(idCliente);
	}

	public List<Pago> periodo(LocalDate desde, LocalDate hasta) {
		return pagoRepository.findByCreadaEnBetweenAndAprobadoTrueAndAnuladoFalse(desde.atStartOfDay(),
				hasta.atTime(LocalTime.MAX));
	}

	public Comprobante comprobante(Long reservaId) {
		List<Pago> pagos = pagoRepository.findByReservaIdAndAprobadoTrueAndAnuladoFalseOrderByCreadaEnDesc(reservaId);
		if (pagos.isEmpty()) {
			throw new RecursoNoEncontradoException("No hay un pago para esta reserva");
		}
		Pago pago = pagos.get(0);
		return new Comprobante(pago.getReservaId(), pago.getNombreCliente(), pago.getNombreServicio(),
				pago.getFechaCita(), pago.getTipo(), pago.getMonto(), pago.getCreadaEn());
	}

	public Pago anular(Long pagoId, String authorization, String clave) {
		personasCliente.verificarClave(authorization, clave);
		Pago pago = pagoRepository.findById(pagoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el pago"));
		pago.setAnulado(true);
		return pagoRepository.save(pago);
	}

}
