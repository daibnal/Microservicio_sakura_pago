package com.sakura.pago.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.pago.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

	List<Pago> findByReservaIdAndAprobadoTrueAndAnuladoFalseOrderByCreadaEnDesc(Long reservaId);

	List<Pago> findByIdClienteAndAprobadoTrueOrderByCreadaEnDesc(Long idCliente);

	List<Pago> findByCreadaEnBetweenAndAprobadoTrueAndAnuladoFalse(LocalDateTime desde, LocalDateTime hasta);

}
