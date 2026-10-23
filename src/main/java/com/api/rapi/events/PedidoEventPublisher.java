package com.api.rapi.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public PedidoEventPublisher(
            ApplicationEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

    // Publicar un evento relacionado con un pedido
    public void publicarEvento(
            Long pedidoId,
            Long restauranteId,
            String tipoEvento,
            String estadoPedido) {

        PedidoEvent evento = new PedidoEvent(
                pedidoId,
                restauranteId,
                tipoEvento,
                estadoPedido
        );

        eventPublisher.publishEvent(evento);
    }
}
