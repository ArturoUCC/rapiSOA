package com.api.rapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.events.PedidoEventPublisher;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RestauranteModel;

import com.api.rapi.repositories.ClienteRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.RestauranteRepository;

@ExtendWith(MockitoExtension.class)
public class PedidoVencimientoTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private PagoEstadoService pagoEstadoService;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private PedidoModel pedido;

    @BeforeEach
    void prepararDatos() {

        RestauranteModel restaurante = new RestauranteModel();
        restaurante.setId(1L);

        pedido = new PedidoModel();
        pedido.setId(1L);
        pedido.setRestaurante(restaurante);
        pedido.setEstado("PendienteAceptacion");
        pedido.setTotal(new BigDecimal("250.00"));
    }

    @Test
    void debeCancelarPedidoVencido() {

        pedido.setFechaLimiteAceptacion(
                LocalDateTime.now().minusMinutes(1));

        when(pedidoRepository
                .findByEstadoAndFechaLimiteAceptacionBefore(
                        eq("PendienteAceptacion"),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(pedido));

        when(pedidoRepository.save(any(PedidoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        pedidoService.cancelarPedidosVencidos();

        // Comprobar que el pedido fue cancelado.
        assertEquals("Cancelado", pedido.getEstado());

        // Comprobar que se solicito el reembolso.
        verify(pagoEstadoService)
                .marcarReembolsoPendiente(1L);

        // Comprobar que se guardo el pedido.
        verify(pedidoRepository).save(pedido);

        // Comprobar que se publico el evento.
        verify(eventPublisher).publicarEvento(
                1L,
                1L,
                "PEDIDO_CANCELADO",
                "Cancelado");
    }

    @Test
    void noDebeCancelarPedidoDentroDelPlazo() {

        pedido.setFechaLimiteAceptacion(
                LocalDateTime.now().plusMinutes(4));

        when(pedidoRepository
                .findByEstadoAndFechaLimiteAceptacionBefore(
                        eq("PendienteAceptacion"),
                        any(LocalDateTime.class)))
                .thenReturn(List.of());

        pedidoService.cancelarPedidosVencidos();

        assertEquals(
                "PendienteAceptacion",
                pedido.getEstado());

        verify(pedidoRepository, never())
                .save(any(PedidoModel.class));

        verify(pagoEstadoService, never())
                .marcarReembolsoPendiente(any());
    }

    @Test
    void noDebeCancelarPedidoYaAceptado() {

        pedido.setEstado("Aceptado");

        pedido.setFechaLimiteAceptacion(
                LocalDateTime.now().minusMinutes(1));

        when(pedidoRepository
                .findByEstadoAndFechaLimiteAceptacionBefore(
                        eq("PendienteAceptacion"),
                        any(LocalDateTime.class)))
                .thenReturn(List.of());

        pedidoService.cancelarPedidosVencidos();

        assertEquals("Aceptado", pedido.getEstado());

        verify(pedidoRepository, never())
                .save(any(PedidoModel.class));

        verify(pagoEstadoService, never())
                .marcarReembolsoPendiente(any());
    }
}
