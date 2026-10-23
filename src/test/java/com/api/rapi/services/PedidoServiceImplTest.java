package com.api.rapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.events.PedidoEventPublisher;
import com.api.rapi.models.ClienteModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RestauranteModel;

import com.api.rapi.repositories.ClienteRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.RestauranteRepository;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceImplTest {

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

    private ClienteModel cliente;
    private RestauranteModel restaurante;
    private PedidoModel pedido;

    @BeforeEach
    void prepararDatos() {

        cliente = new ClienteModel();
        cliente.setId(1L);
        cliente.setNombre("Cliente Prueba");

        restaurante = new RestauranteModel();
        restaurante.setId(1L);
        restaurante.setNombre("Restaurante Prueba");
        restaurante.setActivo(true);
        restaurante.setAbierto(true);

        pedido = new PedidoModel();
        pedido.setId(1L);
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setTotal(new BigDecimal("250.00"));
        pedido.setEstado("PendientePago");
    }

    @Test
    void debeCrearPedidoCorrectamente() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(restauranteRepository.findById(1L))
                .thenReturn(Optional.of(restaurante));

        when(pedidoRepository.save(any(PedidoModel.class)))
                .thenAnswer(invocacion -> {
                    PedidoModel nuevo =
                            invocacion.getArgument(0);
                    nuevo.setId(1L);
                    return nuevo;
                });

        PedidoModel resultado = pedidoService.crearPedido(
                1L,
                1L,
                "Calle Principal 123");

        assertNotNull(resultado);
        assertEquals("PendientePago", resultado.getEstado());
        assertEquals(BigDecimal.ZERO, resultado.getTotal());
        assertEquals(1L, resultado.getId());

        verify(eventPublisher).publicarEvento(
                1L,
                1L,
                "PEDIDO_CREADO",
                "PendientePago");
    }

    @Test
    void noDebeCrearPedidoConRestauranteCerrado() {

        restaurante.setAbierto(false);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(restauranteRepository.findById(1L))
                .thenReturn(Optional.of(restaurante));

        assertThrows(
                IllegalStateException.class,
                () -> pedidoService.crearPedido(
                        1L,
                        1L,
                        "Calle Principal 123")
        );

        verify(pedidoRepository, never())
                .save(any(PedidoModel.class));
    }

    @Test
    void debeConfirmarPagoRegistrado() {

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        // El servicio de pagos valida el estado y monto.
        // Al no lanzar excepcion, la validacion es exitosa.
        doNothing()
                .when(pagoEstadoService)
                .validarPagoConfirmado(
                        1L,
                        new BigDecimal("250.00"));

        when(pedidoRepository.save(any(PedidoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PedidoModel resultado =
                pedidoService.confirmarPagoPedido(1L);

        assertEquals(
                "PendienteAceptacion",
                resultado.getEstado());

        assertNotNull(resultado.getFechaLimiteAceptacion());

        verify(pagoEstadoService).validarPagoConfirmado(
                1L,
                new BigDecimal("250.00"));

        verify(eventPublisher).publicarEvento(
                1L,
                1L,
                "PAGO_CONFIRMADO",
                "PendienteAceptacion");
    }

    @Test
    void noDebeConfirmarPagoSinVerificacion() {

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        // Simular que PagoEstadoService rechaza el pago.
        doThrow(new IllegalStateException(
                "El pago todavia no esta confirmado"))
                .when(pagoEstadoService)
                .validarPagoConfirmado(
                        1L,
                        new BigDecimal("250.00"));

        assertThrows(
                IllegalStateException.class,
                () -> pedidoService.confirmarPagoPedido(1L)
        );

        verify(pedidoRepository, never())
                .save(any(PedidoModel.class));

        verify(eventPublisher, never())
                .publicarEvento(
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void debeCancelarPedidoPendienteDePago() {

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(pedidoRepository.save(any(PedidoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PedidoModel resultado =
                pedidoService.cancelarPedido(1L);

        assertEquals("Cancelado", resultado.getEstado());

        verify(pagoEstadoService, never())
                .marcarReembolsoPendiente(any());

        verify(eventPublisher).publicarEvento(
                1L,
                1L,
                "PEDIDO_CANCELADO",
                "Cancelado");
    }
}
