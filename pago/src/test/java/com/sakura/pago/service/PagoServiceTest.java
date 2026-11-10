package com.sakura.pago.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sakura.pago.client.PersonasCliente;
import com.sakura.pago.exception.RecursoNoEncontradoException;
import com.sakura.pago.model.Comprobante;
import com.sakura.pago.model.Pago;
import com.sakura.pago.model.RegistroPago;
import com.sakura.pago.repository.PagoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

	@Mock
	private PagoRepository pagoRepository;

	@Mock
	private PersonasCliente personasCliente;

	@InjectMocks
	private PagoService pagoService;

	@Captor
	private ArgumentCaptor<Pago> pagoCaptor;

	@Nested
	@DisplayName("Pruebas de registrar()")
	class RegistrarTests {

		@Test
		@DisplayName("Debe registrar un nuevo pago correctamente")
		void debeRegistrarPago() {
			// Given
			RegistroPago dto = crearRegistroPagoEjemplo();
			
			when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
				Pago p = invocation.getArgument(0);
				p.setId(1L);
				return p;
			});

			// When
			Pago resultado = pagoService.registrar(dto);

			// Then
			verify(pagoRepository).save(pagoCaptor.capture());
			Pago pagoGuardado = pagoCaptor.getValue();

			assertThat(pagoGuardado.getReservaId()).isEqualTo(dto.getReservaId());
			assertThat(pagoGuardado.getIdCliente()).isEqualTo(dto.getIdCliente());
			assertThat(pagoGuardado.getNombreCliente()).isEqualTo(dto.getNombreCliente());
			assertThat(pagoGuardado.getNombreServicio()).isEqualTo(dto.getNombreServicio());
			assertThat(pagoGuardado.getFechaCita()).isEqualTo(dto.getFechaCita());
			assertThat(pagoGuardado.getTipo()).isEqualTo(dto.getTipo());
			assertThat(pagoGuardado.getMonto()).isEqualTo(dto.getMonto());
			assertThat(pagoGuardado.isAprobado()).isEqualTo(dto.isAprobado());
			assertThat(pagoGuardado.isAnulado()).isFalse();
			assertThat(pagoGuardado.getCreadaEn()).isNotNull();

			assertThat(resultado.getId()).isEqualTo(1L);
		}
	}

	@Nested
	@DisplayName("Pruebas de mios()")
	class MiosTests {

		@Test
		@DisplayName("Debe obtener la lista de pagos aprobados del cliente")
		void debeObtenerPagosDelCliente() {
			// Given
			Long idCliente = 10L;
			List<Pago> pagosEsperados = List.of(crearPagoEjemplo(1L, idCliente));
			
			when(pagoRepository.findByIdClienteAndAprobadoTrueOrderByCreadaEnDesc(idCliente))
					.thenReturn(pagosEsperados);

			// When
			List<Pago> resultado = pagoService.mios(idCliente);

			// Then
			assertThat(resultado).hasSize(1);
			assertThat(resultado.get(0).getIdCliente()).isEqualTo(idCliente);
			verify(pagoRepository).findByIdClienteAndAprobadoTrueOrderByCreadaEnDesc(idCliente);
		}
	}

	@Nested
	@DisplayName("Pruebas de periodo()")
	class PeriodoTests {

		@Test
		@DisplayName("Debe buscar pagos en el rango exacto de inicio y fin de día")
		void debeBuscarPagosEnPeriodo() {
			// Given
			LocalDate desde = LocalDate.of(2026, 10, 1);
			LocalDate hasta = LocalDate.of(2026, 10, 15);
			
			LocalDateTime inicioEsperado = desde.atStartOfDay();
			LocalDateTime finEsperado = hasta.atTime(LocalTime.MAX);

			when(pagoRepository.findByCreadaEnBetweenAndAprobadoTrueAndAnuladoFalse(inicioEsperado, finEsperado))
					.thenReturn(List.of(crearPagoEjemplo(1L, 10L)));

			// When
			List<Pago> resultado = pagoService.periodo(desde, hasta);

			// Then
			assertThat(resultado).hasSize(1);
			verify(pagoRepository).findByCreadaEnBetweenAndAprobadoTrueAndAnuladoFalse(inicioEsperado, finEsperado);
		}
	}

	@Nested
	@DisplayName("Pruebas de comprobante()")
	class ComprobanteTests {

		@Test
		@DisplayName("Debe retornar el comprobante cuando existe un pago válido para la reserva")
		void debeGenerarComprobante() {
			// Given
			Long reservaId = 50L;
			Pago pago = crearPagoEjemplo(1L, 10L);
			pago.setReservaId(reservaId);

			when(pagoRepository.findByReservaIdAndAprobadoTrueAndAnuladoFalseOrderByCreadaEnDesc(reservaId))
					.thenReturn(List.of(pago));

			// When
			Comprobante comprobante = pagoService.comprobante(reservaId);

			// Then
			assertThat(comprobante).isNotNull();
			assertThat(comprobante.getReservaId()).isEqualTo(reservaId);
			assertThat(comprobante.getCliente()).isEqualTo(pago.getNombreCliente());
			assertThat(comprobante.getServicio()).isEqualTo(pago.getNombreServicio());
			assertThat(comprobante.getFecha()).isEqualTo(pago.getFechaCita());
			assertThat(comprobante.getTipo()).isEqualTo(pago.getTipo());
			assertThat(comprobante.getMonto()).isEqualTo(pago.getMonto());
			assertThat(comprobante.getFechaPago()).isEqualTo(pago.getCreadaEn());
		}

		@Test
		@DisplayName("Debe lanzar RecursoNoEncontradoException cuando no hay pago para la reserva")
		void debeLanzarExcepcionSiNoHayPago() {
			// Given
			Long reservaId = 99L;
			when(pagoRepository.findByReservaIdAndAprobadoTrueAndAnuladoFalseOrderByCreadaEnDesc(reservaId))
					.thenReturn(Collections.emptyList());

			// When & Then
			assertThatThrownBy(() -> pagoService.comprobante(reservaId))
					.isInstanceOf(RecursoNoEncontradoException.class)
					.hasMessage("No hay un pago para esta reserva");
		}
	}

	@Nested
	@DisplayName("Pruebas de anular()")
	class AnularTests {

		@Test
		@DisplayName("Debe anular el pago si la verificación de clave es exitosa y el pago existe")
		void debeAnularPago() {
			// Given
			Long pagoId = 1L;
			String token = "Bearer token123";
			String clave = "1234";
			Pago pagoExistente = crearPagoEjemplo(pagoId, 10L);

			when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pagoExistente));
			when(pagoRepository.save(any(Pago.class))).thenAnswer(i -> i.getArgument(0));

			// When
			Pago pagoAnulado = pagoService.anular(pagoId, token, clave);

			// Then
			verify(personasCliente).verificarClave(token, clave);
			verify(pagoRepository).save(pagoCaptor.capture());

			assertThat(pagoCaptor.getValue().isAnulado()).isTrue();
			assertThat(pagoAnulado.isAnulado()).isTrue();
		}

		@Test
		@DisplayName("Debe lanzar excepción y no buscar ni anular pago si la clave es errónea")
		void debeLanzarExcepcionSiClaveEsInvalida() {
			// Given
			Long pagoId = 1L;
			String token = "Bearer token123";
			String claveErronea = "0000";

			doThrow(new RuntimeException("Clave incorrecta"))
					.when(personasCliente).verificarClave(token, claveErronea);

			// When & Then
			assertThatThrownBy(() -> pagoService.anular(pagoId, token, claveErronea))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("Clave incorrecta");

			verify(pagoRepository, never()).findById(any());
			verify(pagoRepository, never()).save(any());
		}

		@Test
		@DisplayName("Debe lanzar RecursoNoEncontradoException si el pago no existe")
		void debeLanzarExcepcionSiPagoNoExiste() {
			// Given
			Long pagoId = 99L;
			String token = "Bearer token123";
			String clave = "1234";

			when(pagoRepository.findById(pagoId)).thenReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> pagoService.anular(pagoId, token, clave))
					.isInstanceOf(RecursoNoEncontradoException.class)
					.hasMessage("No existe el pago");

			verify(personasCliente).verificarClave(token, clave);
			verify(pagoRepository, never()).save(any());
		}
	}

	// --- Helper Methods ---

	private RegistroPago crearRegistroPagoEjemplo() {
		RegistroPago dto = new RegistroPago();
		dto.setReservaId(50L);
		dto.setIdCliente(10L);
		dto.setNombreCliente("Sakura Kinomoto");
		dto.setNombreServicio("Corte de Cabello");
		dto.setFechaCita(LocalDate.of(2026, 10, 15));
		dto.setTipo("Tarjeta");
		dto.setMonto(25000);
		dto.setAprobado(true);
		return dto;
	}

	private Pago crearPagoEjemplo(Long id, Long idCliente) {
		Pago pago = new Pago();
		pago.setId(id);
		pago.setReservaId(50L);
		pago.setIdCliente(idCliente);
		pago.setNombreCliente("Sakura Kinomoto");
		pago.setNombreServicio("Corte de Cabello");
		pago.setFechaCita(LocalDate.of(2026, 10, 15));
		pago.setTipo("Tarjeta");
		pago.setMonto(25000);
		pago.setAprobado(true);
		pago.setAnulado(false);
		pago.setCreadaEn(LocalDateTime.of(2026, 10, 12, 10, 30));
		return pago;
	}

}