package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.FacturaModel;
import com.api.rapi.services.FacturaService;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    // Obtener todas las facturas
    @GetMapping
    public List<FacturaModel> obtenerFacturas() {
        return facturaService.obtenerFacturas();
    }

    // Buscar factura por ID
    @GetMapping("/{id}")
    public ResponseEntity<FacturaModel> obtenerFacturaPorId(
            @PathVariable Long id) {

        return facturaService.buscarFacturaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar factura por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<FacturaModel> buscarPorPedido(
            @PathVariable Long pedidoId) {

        return facturaService.buscarFacturaPorPedido(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar factura por folio fiscal
    @GetMapping("/folio")
    public ResponseEntity<FacturaModel> buscarPorFolio(
            @RequestParam String folioFiscal) {

        return facturaService.buscarFacturaPorFolioFiscal(folioFiscal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar facturas por estado
    @GetMapping("/estado")
    public List<FacturaModel> buscarPorEstado(
            @RequestParam String estado) {

        return facturaService.buscarFacturasPorEstado(estado);
    }

    // Buscar facturas por RFC
    @GetMapping("/rfc")
    public List<FacturaModel> buscarPorRFC(
            @RequestParam String rfcReceptor) {

        return facturaService.buscarFacturasPorRFC(rfcReceptor);
    }

    // Solicitar una factura
    @PostMapping
    public ResponseEntity<FacturaModel> solicitarFactura(
            @RequestParam Long pedidoId,
            @RequestParam String rfcReceptor,
            @RequestParam String razonSocial,
            @RequestParam String correoFacturacion) {

        FacturaModel nuevaFactura =
                facturaService.solicitarFactura(
                        pedidoId,
                        rfcReceptor,
                        razonSocial,
                        correoFacturacion);

        return ResponseEntity.status(201).body(nuevaFactura);
    }

    // Consultar el estado de una factura
    @GetMapping("/{id}/estado")
    public ResponseEntity<String> consultarEstado(
            @PathVariable Long id) {

        return facturaService.buscarFacturaPorId(id)
                .map(factura -> ResponseEntity.ok(
                        factura.getEstado()))
                .orElse(ResponseEntity.notFound().build());
    }
}

