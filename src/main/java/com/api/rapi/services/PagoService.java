package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.PagoModel;

public interface PagoService {

    List<PagoModel> obtenerPagos();

    Optional<PagoModel> buscarPagoPorId(Long id);

    Optional<PagoModel> buscarPagoPorPedido(Long pedidoId);

    Optional<PagoModel> buscarPagoPorReferencia(String referenciaExterna);

    List<PagoModel> buscarPagosPorEstado(String estado);

    PagoModel registrarPago(Long pedidoId, String metodoPago);

    PagoModel confirmarPago(Long pagoId, String referenciaExterna);

    PagoModel marcarPagoFallido(Long pagoId);

    PagoModel solicitarReembolso(Long pedidoId);

    PagoModel confirmarReembolso(
            Long pagoId, String referenciaReembolso);

    boolean verificarPagoConfirmado(Long pedidoId);

}
