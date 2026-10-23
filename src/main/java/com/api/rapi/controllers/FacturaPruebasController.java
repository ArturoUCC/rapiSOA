package com.api.rapi.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.adapters.SistemaFacturacionLegacyAdapter;
import com.api.rapi.models.FacturaModel;
import com.api.rapi.services.FacturaService;

@RestController
@Profile("local")
@RequestMapping("/api/pruebas/facturas")
public class FacturaPruebasController {

    private final FacturaService facturaService;
    private final SistemaFacturacionLegacyAdapter adaptador;

    @Value("${rapi.facturacion.modo-pruebas:false}")
    private boolean modoPruebas;

    public FacturaPruebasController(
            FacturaService facturaService,
            SistemaFacturacionLegacyAdapter adaptador) {

        this.facturaService = facturaService;
        this.adaptador = adaptador;
    }

    // Simular el procesamiento de una factura
    @PatchMapping("/{id}/procesar")
    public ResponseEntity<FacturaModel> procesarFactura(
            @PathVariable Long id) {

        verificarModoPruebas();

        FacturaModel factura =
                facturaService.procesarFactura(id);

        return ResponseEntity.ok(factura);
    }

    // Consultar la respuesta del sistema heredado simulado
    @GetMapping("/{id}/resultado")
    public ResponseEntity<Map<String, Object>> consultarResultado(
            @PathVariable Long id) {

        verificarModoPruebas();

        FacturaModel factura = facturaService.buscarFacturaPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La factura no existe"));

        String referencia = factura.getReferenciaExterna();

        if (referencia == null || referencia.isBlank()) {
            throw new IllegalStateException(
                    "La factura todavia no ha sido procesada");
        }

        boolean emitida = adaptador.verificarEmision(referencia);

        if (!emitida) {
            throw new IllegalStateException(
                    "El sistema heredado no ha emitido la factura");
        }

        String folio = adaptador.obtenerFolioFiscal(referencia);

        return ResponseEntity.ok(Map.of(
                "facturaId", factura.getId(),
                "referenciaExterna", referencia,
                "folioSimulado", folio,
                "emitida", true
        ));
    }

    // Confirmar una factura simulada
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<FacturaModel> confirmarFactura(
            @PathVariable Long id,
            @RequestParam String folioFiscal,
            @RequestParam String referenciaExterna) {

        verificarModoPruebas();

        FacturaModel factura = facturaService.confirmarFactura(
                id,
                folioFiscal,
                referenciaExterna);

        return ResponseEntity.ok(factura);
    }

    // Simular un error de facturacion
    @PatchMapping("/{id}/error")
    public ResponseEntity<FacturaModel> marcarFacturaConError(
            @PathVariable Long id) {

        verificarModoPruebas();

        FacturaModel factura =
                facturaService.marcarFacturaConError(id);

        return ResponseEntity.ok(factura);
    }

    private void verificarModoPruebas() {

        if (!modoPruebas) {
            throw new IllegalStateException(
                    "El modo de pruebas de facturacion esta deshabilitado");
        }
    }
}
