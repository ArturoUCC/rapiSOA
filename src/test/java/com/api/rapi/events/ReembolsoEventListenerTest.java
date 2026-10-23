package com.api.rapi.events;


import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.models.PagoModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.services.PagoService;

@ExtendWith(MockitoExtension.class)
public class ReembolsoEventListenerTest {

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private ReembolsoEventListener listener;

    private PedidoModel pedido;
    private PagoModel pago;

    @BeforeEach
    void prepararDatos() {

        pedido = new PedidoModel();
        pedido.setId(1L);
        pedido.setEstado("Cancelado");

        pago = new PagoModel();
        pago.setId(1L);
        pago.setPedido(pedido);
        pago.setMonto(new BigDecimal("250.00"));
        pago.setEstado("ReembolsoPendiente");
        pago.setReferenciaExterna("PRUEBA001");
    }

    private PedidoEvent crearEvento(String tipo) {

        return new PedidoEvent(
                1L,
                1L,
                tipo,
                pedido.getEstado()
        );
    }

    @Test
    void debeProcesarReembolsoPorCancelacion() {

        when(pagoService.buscarPagoPorPedido(1L))
                .thenReturn(Optional.of(pago));

        pago.setReferenciaReembolso("REEMBOLSO-PRUEBA001");

        when(pagoService.solicitarReembolso(1L))
                .thenReturn(pago);

        PagoModel confirmado = new PagoModel();
        confirmado.setId(1L);
        confirmado.setEstado("Reembolsado");

        when(pagoService.confirmarReembolso(
                1L, "REEMBOLSO-PRUEBA001"))
                .thenReturn(confirmado);

        listener.procesarEvento(
                crearEvento("PEDIDO_CANCELADO"));

        verify(pagoService).solicitarReembolso(1L);

        verify(pagoService).confirmarReembolso(
                1L, "REEMBOLSO-PRUEBA001");
    }

    @Test
    void debeProcesarReembolsoPorRechazo() {

        pedido.setEstado("Rechazado");

        when(pagoService.buscarPagoPorPedido(1L))
                .thenReturn(Optional.of(pago));

        pago.setReferenciaReembolso("REEMBOLSO-PRUEBA001");

        when(pagoService.solicitarReembolso(1L))
                .thenReturn(pago);

        PagoModel confirmado = new PagoModel();
        confirmado.setEstado("Reembolsado");

        when(pagoService.confirmarReembolso(
                1L, "REEMBOLSO-PRUEBA001"))
                .thenReturn(confirmado);

        listener.procesarEvento(
                crearEvento("PEDIDO_RECHAZADO"));

        verify(pagoService).solicitarReembolso(1L);
        verify(pagoService).confirmarReembolso(
                1L, "REEMBOLSO-PRUEBA001");
    }

    @Test
    void noDebeReembolsarPedidoYaReembolsado() {

        pago.setEstado("Reembolsado");

        when(pagoService.buscarPagoPorPedido(1L))
                .thenReturn(Optional.of(pago));

        listener.procesarEvento(
                crearEvento("PEDIDO_CANCELADO"));

        verify(pagoService, never())
                .solicitarReembolso(anyLong());
    }

    @Test
    void noDebeReembolsarEventoNoRelacionado() {

        listener.procesarEvento(
                crearEvento("PEDIDO_ACEPTADO"));

        verifyNoInteractions(pagoService);
    }

    @Test
    void noDebeReembolsarPedidoSinPago() {

        when(pagoService.buscarPagoPorPedido(1L))
                .thenReturn(Optional.empty());

        listener.procesarEvento(
                crearEvento("PEDIDO_CANCELADO"));

        verify(pagoService, never())
                .solicitarReembolso(anyLong());
    }
}
