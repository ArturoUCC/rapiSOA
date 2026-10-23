package com.api.rapi.schedulers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.api.rapi.services.PedidoService;

@ExtendWith(MockitoExtension.class)
public class PedidoSchedulerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoScheduler pedidoScheduler;

    @BeforeEach
    void prepararDatos() {
        clearInvocations(pedidoService);
    }

    @Test
    void debeEjecutarRevisionDePedidosVencidos() {

        pedidoScheduler.verificarPedidosVencidos();

        verify(pedidoService, times(1))
                .cancelarPedidosVencidos();
    }

    @Test
    void debeManejarErroresSinDetenerElScheduler() {

        doThrow(new RuntimeException("Error simulado"))
                .when(pedidoService)
                .cancelarPedidosVencidos();

        assertDoesNotThrow(() ->
                pedidoScheduler.verificarPedidosVencidos());

        verify(pedidoService, times(1))
                .cancelarPedidosVencidos();
    }
}
