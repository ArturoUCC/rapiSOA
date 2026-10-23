package com.api.rapi.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.api.rapi.models.PagoModel;
import com.api.rapi.services.PagoService;

@Component
@Profile("local")
public class ReembolsoEventListener {

    private static final Logger logger =
            LoggerFactory.getLogger(ReembolsoEventListener.class);

    private final PagoService pagoService;

    public ReembolsoEventListener(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Escuchar cancelaciones y rechazos de pedidos.
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void procesarEvento(PedidoEvent evento) {

        String tipo = evento.getTipoEvento();

        if (!"PEDIDO_CANCELADO".equals(tipo) &&
                !"PEDIDO_RECHAZADO".equals(tipo)) {
            return;
        }

        Long pedidoId = evento.getPedidoId();

        try {

            PagoModel pago = pagoService
                    .buscarPagoPorPedido(pedidoId)
                    .orElse(null);

            if (pago == null) {
                logger.info(
                        "Pedido {} sin pago registrado. "
                        + "No requiere reembolso.",
                        pedidoId);
                return;
            }

            if ("Reembolsado".equals(pago.getEstado())) {
                logger.info(
                        "El pedido {} ya fue reembolsado.",
                        pedidoId);
                return;
            }

            if (!"Confirmado".equals(pago.getEstado()) &&
                    !"ReembolsoPendiente".equals(pago.getEstado())) {

                logger.info(
                        "El pago del pedido {} no requiere "
                        + "reembolso. Estado: {}",
                        pedidoId,
                        pago.getEstado());
                return;
            }

            // Solicitar el reembolso al adaptador simulado.
            PagoModel reembolso =
                    pagoService.solicitarReembolso(pedidoId);

            String referencia =
                    reembolso.getReferenciaReembolso();

            if (referencia == null || referencia.isBlank()) {
                throw new IllegalStateException(
                        "No se genero una referencia de reembolso");
            }

            // Confirmar el resultado con la pasarela simulada.
            PagoModel confirmado =
                    pagoService.confirmarReembolso(
                            reembolso.getId(),
                            referencia);

            logger.info(
                    "Reembolso simulado completado. "
                    + "Pedido: {} | Pago: {} | Estado: {}",
                    pedidoId,
                    confirmado.getId(),
                    confirmado.getEstado());

        } catch (Exception e) {

            logger.error(
                    "No se pudo completar el reembolso "
                    + "simulado del pedido {}. "
                    + "Revisar para reintentar.",
                    pedidoId,
                    e);
        }
    }
}
