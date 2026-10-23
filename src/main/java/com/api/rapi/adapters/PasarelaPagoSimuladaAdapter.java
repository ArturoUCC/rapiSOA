package com.api.rapi.adapters;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class PasarelaPagoSimuladaAdapter implements PasarelaPagoAdapter {

    private final Map<String, BigDecimal> pagosSimulados =
            new ConcurrentHashMap<>();

    private final Set<String> reembolsosSimulados =
            ConcurrentHashMap.newKeySet();

    // Registrar una transaccion simulada
    public void registrarPagoSimulado(
            String referenciaExterna,
            BigDecimal monto) {

        validarReferencia(referenciaExterna);
        validarMonto(monto);

        String referencia = referenciaExterna.trim();

        if (pagosSimulados.putIfAbsent(referencia, monto) != null) {
            throw new IllegalStateException(
                    "La referencia de pago ya existe");
        }
    }

    @Override
    public boolean verificarPago(
            String referenciaExterna,
            BigDecimal monto) {

        if (referenciaExterna == null || monto == null) {
            return false;
        }

        BigDecimal montoRegistrado =
                pagosSimulados.get(referenciaExterna.trim());

        return montoRegistrado != null &&
                montoRegistrado.compareTo(monto) == 0;
    }

    @Override
    public String solicitarReembolso(
            String referenciaExterna,
            BigDecimal monto) {

        if (!verificarPago(referenciaExterna, monto)) {
            throw new IllegalStateException(
                    "No existe un pago simulado valido para reembolsar");
        }

        String referenciaReembolso =
                "REEMBOLSO-" + referenciaExterna.trim();

        reembolsosSimulados.add(referenciaReembolso);

        return referenciaReembolso;
    }

    @Override
    public boolean verificarReembolso(
            String referenciaReembolso) {

        if (referenciaReembolso == null) {
            return false;
        }

        return reembolsosSimulados.contains(
                referenciaReembolso.trim());
    }

    private void validarReferencia(String referencia) {

        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia de pago es obligatoria");
        }
    }

    private void validarMonto(BigDecimal monto) {

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero");
        }
    }
}
