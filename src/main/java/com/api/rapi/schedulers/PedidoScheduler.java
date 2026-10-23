package com.api.rapi.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.api.rapi.services.PedidoService;

@Component
public class PedidoScheduler {

    private final PedidoService pedidoService;

    public PedidoScheduler(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Revisar pedidos pendientes cada 10 segundos
    @Scheduled(fixedDelay = 10000)
    public void verificarPedidosVencidos() {

        try {
            pedidoService.cancelarPedidosVencidos();

        } catch (Exception e) {
            System.err.println(
                "Error al revisar pedidos vencidos: "
                + e.getMessage()
            );
        }
    }
}

