package com.api.rapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import com.api.rapi.adapters.PasarelaPagoAdapter;
import com.api.rapi.models.PagoModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.repositories.PagoRepository;
import com.api.rapi.repositories.PedidoRepository;

import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
public class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoService pedidoService;

    @Mock
    private ObjectProvider<PasarelaPagoAdapter> pasarelaProvider;

    @Mock
    private PasarelaPagoAdapter pasarela;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private PedidoModel pedido;
    private PagoModel pago;

    @BeforeEach
    void prepararDatos() {

        ReflectionTestUtils.setField(
                pagoService, "modoPruebas", true);

        pedido = new PedidoModel();
        pedido.setId(1L);
        pedido.setEstado("PendientePago");
        pedido.setTotal(new BigDecimal("250.00"));

        pago = new PagoModel();
        pago.setId(1L);
        pago.setPedido(pedido);
        pago.setMonto(new BigDecimal("250.00"));
        pago.setMetodoPago("Tarjeta");
        pago.setEstado("Pendiente");
    }

    @Test
    void debeRegistrarPagoPendiente() {

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.empty());

        when(pagoRepository.save(any(PagoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PagoModel resultado =
                pagoService.registrarPago(1L, "Tarjeta");

        assertEquals("Pendiente", resultado.getEstado());
        assertEquals(0, new BigDecimal("250.00")
                .compareTo(resultado.getMonto()));

        verify(pagoRepository).save(any(PagoModel.class));
    }

    @Test
    void noDebeRegistrarPagoSinProductos() {

        pedido.setTotal(BigDecimal.ZERO);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        assertThrows(
                IllegalStateException.class,
                () -> pagoService.registrarPago(1L, "Tarjeta")
        );

        verify(pagoRepository, never())
                .save(any(PagoModel.class));
    }

    @Test
    void debeConfirmarPagoVerificado() {

        when(pasarelaProvider.getIfAvailable())
                .thenReturn(pasarela);

        when(pagoRepository.findById(1L))
                .thenReturn(Optional.of(pago));

        when(pagoRepository.findByReferenciaExterna("PRUEBA001"))
                .thenReturn(Optional.empty());

        when(pasarela.verificarPago(
                eq("PRUEBA001"), any(BigDecimal.class)))
                .thenReturn(true);

        when(pagoRepository.saveAndFlush(any(PagoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PagoModel resultado =
                pagoService.confirmarPago(1L, "PRUEBA001");

        assertEquals("Confirmado", resultado.getEstado());
        assertEquals("PRUEBA001", resultado.getReferenciaExterna());
        assertNotNull(resultado.getFechaPago());

        verify(pedidoService).confirmarPagoPedido(1L);
    }

    @Test
    void noDebeConfirmarPagoNoVerificado() {

        when(pasarelaProvider.getIfAvailable())
                .thenReturn(pasarela);

        when(pagoRepository.findById(1L))
                .thenReturn(Optional.of(pago));

        when(pagoRepository.findByReferenciaExterna("INVALIDO"))
                .thenReturn(Optional.empty());

        when(pasarela.verificarPago(
                eq("INVALIDO"), any(BigDecimal.class)))
                .thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> pagoService.confirmarPago(1L, "INVALIDO")
        );

        verify(pedidoService, never())
                .confirmarPagoPedido(anyLong());
    }

    @Test
    void debeMarcarPagoFallido() {

        when(pasarelaProvider.getIfAvailable())
                .thenReturn(pasarela);

        when(pagoRepository.findById(1L))
                .thenReturn(Optional.of(pago));

        when(pagoRepository.save(any(PagoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PagoModel resultado =
                pagoService.marcarPagoFallido(1L);

        assertEquals("Fallido", resultado.getEstado());
    }

    @Test
    void debeSolicitarReembolso() {

        pedido.setEstado("Cancelado");
        pago.setEstado("ReembolsoPendiente");
        pago.setReferenciaExterna("PRUEBA001");

        when(pasarelaProvider.getIfAvailable())
                .thenReturn(pasarela);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        when(pasarela.solicitarReembolso(
                eq("PRUEBA001"), any(BigDecimal.class)))
                .thenReturn("REEMBOLSO-PRUEBA001");

        when(pagoRepository.save(any(PagoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PagoModel resultado =
                pagoService.solicitarReembolso(1L);

        assertEquals(
                "ReembolsoPendiente", resultado.getEstado());

        assertEquals(
                "REEMBOLSO-PRUEBA001",
                resultado.getReferenciaReembolso());
    }

    @Test
    void debeConfirmarReembolsoVerificado() {

        pago.setEstado("ReembolsoPendiente");
        pago.setReferenciaReembolso("REEMBOLSO-PRUEBA001");

        when(pasarelaProvider.getIfAvailable())
                .thenReturn(pasarela);

        when(pagoRepository.findById(1L))
                .thenReturn(Optional.of(pago));

        when(pasarela.verificarReembolso("REEMBOLSO-PRUEBA001"))
                .thenReturn(true);

        when(pagoRepository.save(any(PagoModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        PagoModel resultado = pagoService.confirmarReembolso(
                1L, "REEMBOLSO-PRUEBA001");

        assertEquals("Reembolsado", resultado.getEstado());
        assertNotNull(resultado.getFechaReembolso());
    }
}
