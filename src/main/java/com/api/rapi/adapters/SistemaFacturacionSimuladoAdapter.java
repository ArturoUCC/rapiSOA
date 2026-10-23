package com.api.rapi.adapters;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class SistemaFacturacionSimuladoAdapter
        implements SistemaFacturacionLegacyAdapter {

    // Almacenar facturas simuladas temporalmente
    private final Map<String, String> facturasSimuladas =
            new ConcurrentHashMap<>();

    @Override
    public String solicitarEmision(
            String rfcReceptor,
            String razonSocial,
            String correoFacturacion,
            BigDecimal montoTotal,
            Long pedidoId) {

        if (rfcReceptor == null || rfcReceptor.isBlank()) {
            throw new IllegalArgumentException(
                    "El RFC del receptor es obligatorio");
        }

        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException(
                    "La razon social es obligatoria");
        }

        if (correoFacturacion == null ||
                correoFacturacion.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo de facturacion es obligatorio");
        }

        if (montoTotal == null ||
                montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto de la factura debe ser mayor que cero");
        }

        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del pedido no es valido");
        }

        // Simular una referencia del sistema heredado
        String referenciaExterna =
                "LEGACY-" + UUID.randomUUID();

        // Generar un identificador ficticio para pruebas
        String folioSimulado =
                "SIM-" + UUID.randomUUID();

        facturasSimuladas.put(
                referenciaExterna,
                folioSimulado);

        return referenciaExterna;
    }

    @Override
    public boolean verificarEmision(String referenciaExterna) {

        if (referenciaExterna == null ||
                referenciaExterna.isBlank()) {
            return false;
        }

        return facturasSimuladas.containsKey(
                referenciaExterna.trim());
    }

    @Override
    public String obtenerFolioFiscal(String referenciaExterna) {

        if (referenciaExterna == null ||
                referenciaExterna.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia externa es obligatoria");
        }

        String folio = facturasSimuladas.get(
                referenciaExterna.trim());

        if (folio == null) {
            throw new IllegalStateException(
                    "La factura no existe en el sistema simulado");
        }

        return folio;
    }
}
