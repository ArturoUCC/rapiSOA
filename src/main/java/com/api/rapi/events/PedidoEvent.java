package com.api.rapi.events;

import java.time.LocalDateTime;

public class PedidoEvent {

    private final Long pedidoId;
    private final Long restauranteId;
    private final String tipoEvento;
    private final String estadoPedido;
    private final LocalDateTime fechaEvento;

    public PedidoEvent(
            Long pedidoId,
            Long restauranteId,
            String tipoEvento,
            String estadoPedido) {

        this.pedidoId = pedidoId;
        this.restauranteId = restauranteId;
        this.tipoEvento = tipoEvento;
        this.estadoPedido = estadoPedido;
        this.fechaEvento = LocalDateTime.now();
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }
}
