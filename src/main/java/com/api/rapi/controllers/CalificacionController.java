package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.CalificacionModel;
import com.api.rapi.services.CalificacionService;

@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(
            CalificacionService calificacionService) {

        this.calificacionService = calificacionService;
    }

    // Obtener todas las calificaciones
    @GetMapping
    public List<CalificacionModel> obtenerCalificaciones() {
        return calificacionService.obtenerCalificaciones();
    }

    // Buscar calificación por ID
    @GetMapping("/{id}")
    public ResponseEntity<CalificacionModel> obtenerCalificacionPorId(
            @PathVariable Long id) {

        return calificacionService.buscarCalificacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar calificación por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CalificacionModel> buscarPorPedido(
            @PathVariable Long pedidoId) {

        return calificacionService.buscarCalificacionPorPedido(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar calificaciones por restaurante
    @GetMapping("/restaurante/{restauranteId}")
    public List<CalificacionModel> buscarPorRestaurante(
            @PathVariable Long restauranteId) {

        return calificacionService.buscarCalificacionesPorRestaurante(
                restauranteId);
    }

    // Buscar calificaciones por cliente
    @GetMapping("/cliente/{clienteId}")
    public List<CalificacionModel> buscarPorCliente(
            @PathVariable Long clienteId) {

        return calificacionService.buscarCalificacionesPorCliente(
                clienteId);
    }

    // Registrar una calificación
    @PostMapping
    public ResponseEntity<CalificacionModel> registrarCalificacion(
            @RequestParam Long pedidoId,
            @RequestParam Integer puntuacionRestaurante,
            @RequestParam Integer puntuacionRepartidor,
            @RequestParam(required = false) String comentario) {

        CalificacionModel nuevaCalificacion =
                calificacionService.registrarCalificacion(
                        pedidoId,
                        puntuacionRestaurante,
                        puntuacionRepartidor,
                        comentario);

        return ResponseEntity.status(201).body(nuevaCalificacion);
    }

    // Actualizar una calificación
    @PutMapping("/{id}")
    public ResponseEntity<CalificacionModel> actualizarCalificacion(
            @PathVariable Long id,
            @RequestParam Integer puntuacionRestaurante,
            @RequestParam Integer puntuacionRepartidor,
            @RequestParam(required = false) String comentario) {

        if (calificacionService.buscarCalificacionPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CalificacionModel actualizada =
                calificacionService.actualizarCalificacion(
                        id,
                        puntuacionRestaurante,
                        puntuacionRepartidor,
                        comentario);

        return ResponseEntity.ok(actualizada);
    }

    // Eliminar una calificación
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCalificacion(
            @PathVariable Long id) {

        if (calificacionService.buscarCalificacionPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        calificacionService.eliminarCalificacion(id);

        return ResponseEntity.noContent().build();
    }
}
