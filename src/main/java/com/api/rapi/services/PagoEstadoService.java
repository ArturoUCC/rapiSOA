package com.api.rapi.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.exceptions.RecursoNoEncontradoException;
import com.api.rapi.models.PagoModel;
import com.api.rapi.repositories.PagoRepository;

@Service
public class PagoEstadoService {

    private final PagoRepository pagoRepository;

    public PagoEstadoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    // Verificar que el pedido tenga un pago confirmado
    @Transactional(readOnly = true)
    public boolean tienePagoConfirmado(Long pedidoId) {

        return pagoRepository.existsByPedidoIdAndEstado(
                pedidoId,
                "Confirmado");
    }

    // Validar que el pago coincida con el total del pedido
    @Transactional(readOnly = true)
    public void validarPagoConfirmado(
            Long pedidoId,
            BigDecimal totalPedido) {

        PagoModel pago = pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Pago del pedido", pedidoId));

        if (!"Confirmado".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "El pago todavia no esta confirmado");
        }

        if (totalPedido == null ||
                pago.getMonto() == null ||
                pago.getMonto().compareTo(totalPedido) != 0) {

            throw new IllegalStateException(
                    "El monto del pago no coincide con el pedido");
        }
    }

    // Marcar un pago confirmado para su reembolso
    @Transactional
    public void marcarReembolsoPendiente(Long pedidoId) {

        PagoModel pago = pagoRepository.findByPedidoId(pedidoId)
                .orElse(null);

        if (pago == null) {
            return;
        }

        if ("Confirmado".equals(pago.getEstado())) {

            pago.setEstado("ReembolsoPendiente");

            pagoRepository.save(pago);
        }
    }

    // Consultar si un pedido necesita reembolso
    @Transactional(readOnly = true)
    public boolean tieneReembolsoPendiente(Long pedidoId) {

        return pagoRepository.existsByPedidoIdAndEstado(
                pedidoId,
                "ReembolsoPendiente");
    }
}
