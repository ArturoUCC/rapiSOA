package com.api.rapi.events;


import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.RepartidorModel;
import com.api.rapi.services.EntregaService;

@ExtendWith(MockitoExtension.class)
public class AsignacionEntregaListenerTest {

    @Mock
    private EntregaService entregaService;

    @InjectMocks
    private AsignacionEntregaListener listener;

    private EntregaModel entrega;
    private RepartidorModel repartidor;

    @BeforeEach
    void prepararDatos() {

        entrega = new EntregaModel();
        entrega.setId(1L);
        entrega.setEstado("PendienteAsignacion");

        repartidor = new RepartidorModel();
        repartidor.setId(1L);
    }

    private PedidoEvent crearEvento(String tipo) {

        return new PedidoEvent(
                1L,
                1L,
                tipo,
                "Aceptado"
        );
    }

    @Test
    void debeCrearYAsignarEntregaAutomaticamente() {

        when(entregaService.buscarEntregaPorPedido(1L))
                .thenReturn(Optional.empty());

        when(entregaService.crearEntrega(1L))
                .thenReturn(entrega);

        EntregaModel asignada = new EntregaModel();
        asignada.setId(1L);
        asignada.setEstado("Asignada");
        asignada.setRepartidor(repartidor);

        when(entregaService.asignarRepartidor(1L))
                .thenReturn(asignada);

        listener.procesarPedidoAceptado(
                crearEvento("PEDIDO_ACEPTADO"));

        verify(entregaService).crearEntrega(1L);
        verify(entregaService).asignarRepartidor(1L);
    }

    @Test
    void debeAsignarEntregaPendienteExistente() {

        when(entregaService.buscarEntregaPorPedido(1L))
                .thenReturn(Optional.of(entrega));

        EntregaModel asignada = new EntregaModel();
        asignada.setId(1L);
        asignada.setEstado("Asignada");
        asignada.setRepartidor(repartidor);

        when(entregaService.asignarRepartidor(1L))
                .thenReturn(asignada);

        listener.procesarPedidoAceptado(
                crearEvento("PEDIDO_ACEPTADO"));

        verify(entregaService, never())
                .crearEntrega(anyLong());

        verify(entregaService).asignarRepartidor(1L);
    }

    @Test
    void noDebeAsignarEntregaYaAsignada() {

        entrega.setEstado("Asignada");
        entrega.setRepartidor(repartidor);

        when(entregaService.buscarEntregaPorPedido(1L))
                .thenReturn(Optional.of(entrega));

        listener.procesarPedidoAceptado(
                crearEvento("PEDIDO_ACEPTADO"));

        verify(entregaService, never())
                .crearEntrega(anyLong());

        verify(entregaService, never())
                .asignarRepartidor(anyLong());
    }

    @Test
    void noDebeAsignarPorEventoNoRelacionado() {

        listener.procesarPedidoAceptado(
                crearEvento("PEDIDO_CANCELADO"));

        verifyNoInteractions(entregaService);
    }
}
