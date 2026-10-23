package com.api.rapi.controllers;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.adapters.PasarelaPagoSimuladaAdapter;
import com.api.rapi.models.PagoModel;
import com.api.rapi.services.PagoService;

@RestController
@Profile("local")
@RequestMapping("/api/pruebas/pagos")
public class PagoPruebasController {

    private final PagoService pagoService;
    private final PasarelaPagoSimuladaAdapter pasarelaSimulada;

    @Value("${rapi.pagos.modo-pruebas:false}")
    private boolean modoPruebas;

    public PagoPruebasController(
            PagoService pagoService,
            PasarelaPagoSimuladaAdapter pasarelaSimulada) {

        this.pagoService = pagoService;
        this.pasarelaSimulada = pasarelaSimulada;
    }

    // Registrar una transaccion simulada
    @PostMapping("/simular")
    public ResponseEntity<Map<String, Object>> simularPago(
            @RequestParam Long pagoId,
            @RequestParam String referenciaExterna) {

        verificarModoPruebas();

        PagoModel pago = pagoService.buscarPagoPorId(pagoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pago no existe"));

        if (!"Pendiente".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "El pago debe estar pendiente");
        }

        BigDecimal monto = pago.getMonto();

        pasarelaSimulada.registrarPagoSimulado(
                referenciaExterna,
                monto);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Pago de prueba registrado",
                "pagoId", pagoId,
                "referenciaExterna", referenciaExterna,
                "monto", monto
        ));
    }

    // Confirmar un pago simulado
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PagoModel> confirmarPago(
            @PathVariable Long id,
            @RequestParam String referenciaExterna) {

        verificarModoPruebas();

        PagoModel pago = pagoService.confirmarPago(
                id, referenciaExterna);

        return ResponseEntity.ok(pago);
    }

    // Simular un pago fallido
    @PatchMapping("/{id}/fallido")
    public ResponseEntity<PagoModel> marcarPagoFallido(
            @PathVariable Long id) {

        verificarModoPruebas();

        PagoModel pago = pagoService.marcarPagoFallido(id);

        return ResponseEntity.ok(pago);
    }

    // Solicitar un reembolso simulado
    @PostMapping("/pedido/{pedidoId}/reembolso")
    public ResponseEntity<PagoModel> solicitarReembolso(
            @PathVariable Long pedidoId) {

        verificarModoPruebas();

        PagoModel pago = pagoService.solicitarReembolso(
                pedidoId);

        return ResponseEntity.ok(pago);
    }

    // Confirmar un reembolso simulado
    @PatchMapping("/{id}/confirmar-reembolso")
    public ResponseEntity<PagoModel> confirmarReembolso(
            @PathVariable Long id,
            @RequestParam String referenciaReembolso) {

        verificarModoPruebas();

        PagoModel pago = pagoService.confirmarReembolso(
                id, referenciaReembolso);

        return ResponseEntity.ok(pago);
    }

    private void verificarModoPruebas() {

        if (!modoPruebas) {
            throw new IllegalStateException(
                    "El modo de pruebas de pagos esta deshabilitado");
        }
    }
}
