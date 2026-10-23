package com.api.rapi.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.api.rapi.realtime.PedidoSseService;

@Component
public class PedidoEventListener {

    private static final Logger logger =
            LoggerFactory.getLogger(PedidoEventListener.class);

    private final PedidoSseService pedidoSseService;

    public PedidoEventListener(PedidoSseService pedidoSseService) {
        this.pedidoSseService = pedidoSseService;
    }

    // Procesar eventos despues de confirmar la transaccion
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void recibirEvento(PedidoEvent evento) {

        logger.info(
                "Evento recibido: {} | Pedido: {} | Restaurante: {} | Estado: {} | Fecha: {}",
                evento.getTipoEvento(),
                evento.getPedidoId(),
                evento.getRestauranteId(),
                evento.getEstadoPedido(),
                evento.getFechaEvento()
        );

        // Enviar el evento a los clientes conectados por SSE
        pedidoSseService.publicarActualizacion(evento);

        switch (evento.getTipoEvento()) {

            case "PEDIDO_CREADO":
                logger.info(
                        "Nuevo pedido registrado: {}",
                        evento.getPedidoId()
                );
                break;

            case "PAGO_CONFIRMADO":
                logger.info(
                        "Pago confirmado para el pedido: {}",
                        evento.getPedidoId()
                );
                break;

            case "PEDIDO_ACEPTADO":
                logger.info(
                        "Pedido {} aceptado por el restaurante {}",
                        evento.getPedidoId(),
                        evento.getRestauranteId()
                );
                break;

            case "PEDIDO_RECHAZADO":
                logger.info(
                        "Pedido rechazado: {}",
                        evento.getPedidoId()
                );
                break;

            case "PEDIDO_CANCELADO":
                logger.info(
                        "Pedido cancelado: {}",
                        evento.getPedidoId()
                );
                break;

            case "PEDIDO_EN_PREPARACION":
                logger.info(
                        "Pedido en preparacion: {}",
                        evento.getPedidoId()
                );
                break;

            case "ENTREGA_CREADA":
                logger.info(
                        "Entrega creada para el pedido: {}",
                        evento.getPedidoId()
                );
                break;

            case "REPARTIDOR_ASIGNADO":
                logger.info(
                        "Repartidor asignado al pedido: {}",
                        evento.getPedidoId()
                );
                break;

            case "PEDIDO_RECOGIDO":
                logger.info(
                        "Pedido recogido por el repartidor: {}",
                        evento.getPedidoId()
                );
                break;

            case "PEDIDO_EN_CAMINO":
                logger.info(
                        "Pedido en camino: {}",
                        evento.getPedidoId()
                );
                break;

            case "UBICACION_ACTUALIZADA":
                logger.info(
                        "Ubicacion actualizada para el pedido: {}",
                        evento.getPedidoId()
                );
                break;

            case "PEDIDO_ENTREGADO":
                logger.info(
                        "Pedido entregado: {}",
                        evento.getPedidoId()
                );
                break;

            default:
                logger.info(
                        "Evento registrado: {}",
                        evento.getTipoEvento()
                );
                break;
        }
    }
}
