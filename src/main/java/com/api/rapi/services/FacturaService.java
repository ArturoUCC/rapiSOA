package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.FacturaModel;

public interface FacturaService {

    List<FacturaModel> obtenerFacturas();

    Optional<FacturaModel> buscarFacturaPorId(Long id);

    Optional<FacturaModel> buscarFacturaPorPedido(Long pedidoId);

    Optional<FacturaModel> buscarFacturaPorFolioFiscal(String folioFiscal);

    List<FacturaModel> buscarFacturasPorEstado(String estado);

    List<FacturaModel> buscarFacturasPorRFC(String rfcReceptor);

    FacturaModel solicitarFactura(
            Long pedidoId,
            String rfcReceptor,
            String razonSocial,
            String correoFacturacion);

    FacturaModel procesarFactura(Long facturaId);

    FacturaModel confirmarFactura(
            Long facturaId,
            String folioFiscal,
            String referenciaExterna);

    FacturaModel marcarFacturaConError(Long facturaId);

}
