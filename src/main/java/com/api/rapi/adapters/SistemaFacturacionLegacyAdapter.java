package com.api.rapi.adapters;

import java.math.BigDecimal;

public interface SistemaFacturacionLegacyAdapter {

    // Enviar una solicitud al sistema de facturacion heredado
    String solicitarEmision(
            String rfcReceptor,
            String razonSocial,
            String correoFacturacion,
            BigDecimal montoTotal,
            Long pedidoId);

    // Consultar si el sistema heredado emitio la factura
    boolean verificarEmision(String referenciaExterna);

    // Obtener el folio fiscal de una factura emitida
    String obtenerFolioFiscal(String referenciaExterna);

}
