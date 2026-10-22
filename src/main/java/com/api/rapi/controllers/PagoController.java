package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.PagoModel;
import com.api.rapi.services.PagoService;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Obtener todos los pagos
    @GetMapping
    public List<PagoModel> obtenerPagos() {
        return pagoService.obtenerPagos();
    }

    // Buscar pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<PagoModel> obtenerPagoPorId(
            @PathVariable Long id) {

        return pagoService.buscarPagoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar pago por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<PagoModel> obtenerPagoPorPedido(
            @PathVariable Long pedidoId) {

        return pagoService.buscarPagoPorPedido(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar pago por referencia externa
    @GetMapping("/referencia")
    public ResponseEntity<PagoModel> buscarPorReferencia(
            @RequestParam String referenciaExterna) {

        return pagoService.buscarPagoPorReferencia(referenciaExterna)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar pagos por estado
    @GetMapping("/estado")
    public List<PagoModel> buscarPagosPorEstado(
            @RequestParam String estado) {

        return pagoService.buscarPagosPorEstado(estado);
    }

    // Registrar un pago pendiente
    @PostMapping
    public ResponseEntity<PagoModel> registrarPago(
            @RequestParam Long pedidoId,
            @RequestParam String metodoPago) {

        PagoModel nuevoPago = pagoService.registrarPago(
                pedidoId, metodoPago);

        return ResponseEntity.status(201).body(nuevoPago);
    }

    // Solicitar reembolso de un pedido cancelado
    @PostMapping("/pedido/{pedidoId}/reembolso")
    public ResponseEntity<PagoModel> solicitarReembolso(
            @PathVariable Long pedidoId) {

        PagoModel pago = pagoService.solicitarReembolso(pedidoId);

        return ResponseEntity.ok(pago);
    }

    // Verificar si un pedido tiene pago confirmado
    @GetMapping("/pedido/{pedidoId}/confirmado")
    public ResponseEntity<Boolean> verificarPagoConfirmado(
            @PathVariable Long pedidoId) {

        boolean confirmado =
                pagoService.verificarPagoConfirmado(pedidoId);

        return ResponseEntity.ok(confirmado);
    }
}
