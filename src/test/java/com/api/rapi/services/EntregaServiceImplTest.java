package com.api.rapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.events.PedidoEventPublisher;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RepartidorModel;
import com.api.rapi.models.RestauranteModel;

import com.api.rapi.repositories.EntregaRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.RepartidorRepository;

@ExtendWith(MockitoExtension.class)
public class EntregaServiceImplTest {

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private RepartidorService repartidorService;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private EntregaServiceImpl entregaService;

    private PedidoModel pedido;
    private EntregaModel entrega;
    private RepartidorModel repartidor;

    @BeforeEach
    void prepararDatos() {

        RestauranteModel restaurante = new RestauranteModel();
        restaurante.setId(1L);
        restaurante.setLatitud(19.1738);
        restaurante.setLongitud(-96.1342);

        pedido = new PedidoModel();
        pedido.setId(1L);
        pedido.setRestaurante(restaurante);
        pedido.setEstado("Aceptado");

        repartidor = new RepartidorModel();
        repartidor.setId(2L);
        repartidor.setActivo(true);
        repartidor.setDisponible(true);
        repartidor.setLatitud(19.1750);
        repartidor.setLongitud(-96.1360);

        entrega = new EntregaModel();
        entrega.setId(3L);
        entrega.setPedido(pedido);
        entrega.setEstado("PendienteAsignacion");
    }

    @Test
    void debeCrearEntregaParaPedidoAceptado() {

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(entregaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(entregaRepository.save(any(EntregaModel.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        EntregaModel resultado = entregaService.crearEntrega(1L);

        assertEquals("PendienteAsignacion", resultado.getEstado());

        verify(eventPublisher).publicarEvento(
                1L, 1L, "ENTREGA_CREADA", "Aceptado");
    }

    @Test
    void noDebeCrearEntregaDuplicada() {

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(entregaRepository.existsByPedidoId(1L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> entregaService.crearEntrega(1L)
        );

        verify(entregaRepository, never())
                .save(any(EntregaModel.class));
    }

    @Test
    void debeAsignarRepartidorUsandoUbicacionRestaurante() {

        when(entregaRepository.findById(3L))
                .thenReturn(Optional.of(entrega));

        when(repartidorService.buscarRepartidorMasCercano(
                19.1738, -96.1342))
                .thenReturn(Optional.of(repartidor));

        when(repartidorRepository.findById(2L))
                .thenReturn(Optional.of(repartidor));

        when(entregaRepository.save(any(EntregaModel.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        EntregaModel resultado =
                entregaService.asignarRepartidor(3L);

        assertEquals("Asignada", resultado.getEstado());
        assertEquals(2L, resultado.getRepartidor().getId());
        assertFalse(repartidor.isDisponible());

        verify(repartidorService).buscarRepartidorMasCercano(
                19.1738, -96.1342);
    }

    @Test
    void noDebeAsignarRepartidorSinCoordenadas() {

        pedido.getRestaurante().setLatitud(null);
        pedido.getRestaurante().setLongitud(null);

        when(entregaRepository.findById(3L))
                .thenReturn(Optional.of(entrega));

        assertThrows(
                IllegalArgumentException.class,
                () -> entregaService.asignarRepartidor(3L)
        );

        verify(repartidorService, never())
                .buscarRepartidorMasCercano(any(), any());
    }

    @Test
    void debeConfirmarEntregaYLiberarRepartidor() {

        entrega.setEstado("EnCamino");
        entrega.setRepartidor(repartidor);
        pedido.setEstado("EnCamino");

        when(entregaRepository.findById(3L))
                .thenReturn(Optional.of(entrega));

        when(entregaRepository.save(any(EntregaModel.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        EntregaModel resultado =
                entregaService.confirmarEntrega(3L);

        assertEquals("Entregada", resultado.getEstado());
        assertEquals("Entregado", pedido.getEstado());
        assertTrue(repartidor.isDisponible());
        assertNotNull(resultado.getFechaEntrega());

        verify(eventPublisher).publicarEvento(
                1L, 1L, "PEDIDO_ENTREGADO", "Entregado");
    }
}
