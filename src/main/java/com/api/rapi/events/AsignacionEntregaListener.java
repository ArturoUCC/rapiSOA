package com.api.rapi.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.api.rapi.models.EntregaModel;
import com.api.rapi.services.EntregaService;

@Component
public class AsignacionEntregaListener {

    private static final Logger logger =
            LoggerFactory.getLogger(AsignacionEntregaListener.class);

    private final EntregaService entregaService;

    public AsignacionEntregaListener(
            EntregaService entregaService) {

        this.entregaService = entregaService;
    }

    // Escuchar cuando un restaurante acepta un pedido
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void procesarPedidoAceptado(PedidoEvent evento) {

        if (!"PEDIDO_ACEPTADO".equals(
                evento.getTipoEvento())) {
            return;
        }

        Long pedidoId = evento.getPedidoId();

        try {

            logger.info(
                    "Iniciando asignacion automatica "
                    + "para el pedido {}",
                    pedidoId
            );

            // Buscar una entrega existente
            EntregaModel entrega = entregaService
                    .buscarEntregaPorPedido(pedidoId)
                    .orElse(null);

            // Crear la entrega si todavia no existe
            if (entrega == null) {

                entrega = entregaService.crearEntrega(
                        pedidoId);

                logger.info(
                        "Entrega creada automaticamente "
                        + "para el pedido {}",
                        pedidoId
                );
            }

            // Evitar asignaciones duplicadas
            if (!"PendienteAsignacion".equals(
                    entrega.getEstado())) {

                logger.info(
                        "La entrega {} ya tiene estado {}",
                        entrega.getId(),
                        entrega.getEstado()
                );

                return;
            }

            // Buscar y asignar al repartidor mas cercano
            EntregaModel asignada =
                    entregaService.asignarRepartidor(
                            entrega.getId());

            logger.info(
                    "Repartidor {} asignado "
                    + "automaticamente al pedido {}",
                    asignada.getRepartidor().getId(),
                    pedidoId
            );

        } catch (IllegalStateException e) {

            logger.warn(
                    "No fue posible asignar repartidor "
                    + "al pedido {}: {}",
                    pedidoId,
                    e.getMessage()
            );

        } catch (Exception e) {

            logger.error(
                    "Error al procesar la asignacion "
                    + "automatica del pedido {}",
                    pedidoId,
                    e
            );
        }
    }
}
