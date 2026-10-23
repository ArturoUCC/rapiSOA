package com.api.rapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.api.rapi.repositories.PagoRepository;

@ExtendWith(MockitoExtension.class)
public class PagoEstadoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoEstadoService pagoEstadoService;

    private PagoModel pago;

    @BeforeEach
    void prepararDatos() {

        PedidoModel pedido = new PedidoModel();
        pedido.setId(1L);

        pago = new PagoModel();
        pago.setId(1L);
        pago.setPedido(pedido);
        pago.setEstado("Confirmado");
        pago.setMonto(new BigDecimal("250.00"));
    }

    @Test
    void debeValidarPagoConfirmado() {

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        assertDoesNotThrow(() ->
                pagoEstadoService.validarPagoConfirmado(
                        1L,
                        new BigDecimal("250.00")
                )
        );

        verify(pagoRepository).findByPedidoId(1L);
    }

    @Test
    void noDebeValidarPagoPendiente() {

        pago.setEstado("Pendiente");

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        assertThrows(
                IllegalStateException.class,
                () -> pagoEstadoService.validarPagoConfirmado(
                        1L,
                        new BigDecimal("250.00")
                )
        );
    }

    @Test
    void noDebeValidarMontoDiferente() {

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        assertThrows(
                IllegalStateException.class,
                () -> pagoEstadoService.validarPagoConfirmado(
                        1L,
                        new BigDecimal("300.00")
                )
        );
    }

    @Test
    void debeMarcarReembolsoPendiente() {

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        pagoEstadoService.marcarReembolsoPendiente(1L);

        assertEquals(
                "ReembolsoPendiente",
                pago.getEstado()
        );

        verify(pagoRepository).save(pago);
    }

    @Test
    void noDebeModificarPagoYaReembolsado() {

        pago.setEstado("Reembolsado");

        when(pagoRepository.findByPedidoId(1L))
                .thenReturn(Optional.of(pago));

        pagoEstadoService.marcarReembolsoPendiente(1L);

        assertEquals("Reembolsado", pago.getEstado());

        verify(pagoRepository, never())
                .save(any(PagoModel.class));
    }

    @Test
    void debeIdentificarReembolsoPendiente() {

        when(pagoRepository.existsByPedidoIdAndEstado(
                1L,
                "ReembolsoPendiente"))
                .thenReturn(true);

        boolean resultado =
                pagoEstadoService.tieneReembolsoPendiente(1L);

        assertTrue(resultado);
    }
}

