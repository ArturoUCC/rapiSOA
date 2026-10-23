package com.api.rapi.adapters;

import java.math.BigDecimal;

public interface PasarelaPagoAdapter {

    // Verificar si una transaccion fue aprobada
    boolean verificarPago(
            String referenciaExterna,
            BigDecimal monto);

    // Solicitar la devolucion de un pago
    String solicitarReembolso(
            String referenciaExterna,
            BigDecimal monto);

    // Consultar si un reembolso fue completado
    boolean verificarReembolso(
            String referenciaReembolso);

}
