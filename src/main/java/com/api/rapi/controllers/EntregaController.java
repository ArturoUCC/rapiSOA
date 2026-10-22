package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.EntregaModel;
import com.api.rapi.services.EntregaService;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    // Obtener todas las entregas
    @GetMapping
    public List<EntregaModel> obtenerEntregas() {
        return entregaService.obtenerEntregas();
    }

    // Buscar entrega por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntregaModel> obtenerEntregaPorId(
            @PathVariable Long id) {

        return entregaService.buscarEntregaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar entrega por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EntregaModel> buscarPorPedido(
            @PathVariable Long pedidoId) {

        return entregaService.buscarEntregaPorPedido(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar entregas por repartidor
    @GetMapping("/repartidor/{repartidorId}")
    public List<EntregaModel> buscarPorRepartidor(
            @PathVariable Long repartidorId) {

        return entregaService.buscarEntregasPorRepartidor(repartidorId);
    }

    // Buscar entregas por estado
    @GetMapping("/estado")
    public List<EntregaModel> buscarPorEstado(
            @RequestParam String estado) {

        return entregaService.buscarEntregasPorEstado(estado);
    }

    // Crear entrega para un pedido aceptado
    @PostMapping("/pedido/{pedidoId}")
    public ResponseEntity<EntregaModel> crearEntrega(
            @PathVariable Long pedidoId) {

        EntregaModel nuevaEntrega =
                entregaService.crearEntrega(pedidoId);

        return ResponseEntity.status(201).body(nuevaEntrega);
    }

    // Asignar automáticamente un repartidor
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<EntregaModel> asignarRepartidor(
            @PathVariable Long id) {

        EntregaModel entrega =
                entregaService.asignarRepartidor(id);

        return ResponseEntity.ok(entrega);
    }

    // Asignar manualmente un repartidor
    @PatchMapping("/{id}/asignar/{repartidorId}")
    public ResponseEntity<EntregaModel> asignarRepartidorManual(
            @PathVariable Long id,
            @PathVariable Long repartidorId) {

        EntregaModel entrega =
                entregaService.asignarRepartidorManual(
                        id, repartidorId);

        return ResponseEntity.ok(entrega);
    }

    // Confirmar recogida del pedido
    @PatchMapping("/{id}/recogida")
    public ResponseEntity<EntregaModel> confirmarRecogida(
            @PathVariable Long id) {

        EntregaModel entrega =
                entregaService.confirmarRecogida(id);

        return ResponseEntity.ok(entrega);
    }

    // Iniciar el recorrido hacia el cliente
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<EntregaModel> iniciarEntrega(
            @PathVariable Long id) {

        EntregaModel entrega =
                entregaService.iniciarEntrega(id);

        return ResponseEntity.ok(entrega);
    }

    // Actualizar ubicación durante la entrega
    @PatchMapping("/{id}/ubicacion")
    public ResponseEntity<EntregaModel> actualizarUbicacion(
            @PathVariable Long id,
            @RequestParam Double latitud,
            @RequestParam Double longitud) {

        EntregaModel entrega =
                entregaService.actualizarUbicacion(
                        id, latitud, longitud);

        return ResponseEntity.ok(entrega);
    }

    // Confirmar que el pedido fue entregado
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<EntregaModel> confirmarEntrega(
            @PathVariable Long id) {

        EntregaModel entrega =
                entregaService.confirmarEntrega(id);

        return ResponseEntity.ok(entrega);
    }
}
