package com.api.rapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.events.PedidoEventPublisher;
import com.api.rapi.models.PagoModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RestauranteModel;

import com.api.rapi.repositories.ClienteRepository;
import com.api.rapi.repositories.PagoRepository;
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
    private PagoRepository pagoRepository;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private PedidoModel pedido;
    private PagoModel pago;

    @BeforeEach
    void prepararDatos() {

        RestauranteModel restaurante = new RestauranteModel();
        restaurante.setId(1L);

        pedido = new PedidoModel();
        pedido.setId(1L);
        pedido.setRestaurante(restaurante);
        pedido.setEstado("PendienteAceptacion");
        pedido.setTotal(new BigDecimal("250.00"));

        pago = new PagoModel();
        pago.setId(1L);
        pago.setPedido(pedido);
        pago.setEstado("Confirmado");
        pago.setMonto(new BigDecimal("250.00"));
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

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        when(pedidoRepository.save(any(PedidoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        pedidoService.cancelarPedidosVencidos();

        assertEquals("Cancelado", pedido.getEstado());
        assertEquals("ReembolsoPendiente", pago.getEstado());

        verify(pedidoRepository).save(pedido);
        verify(pagoRepository).save(pago);

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

        assertEquals("PendienteAceptacion", pedido.getEstado());

        verify(pedidoRepository, never())
                .save(any(PedidoModel.class));

        verify(pagoRepository, never())
                .save(any(PagoModel.class));
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
    }
}
