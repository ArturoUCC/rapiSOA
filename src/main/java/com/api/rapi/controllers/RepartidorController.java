package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.RepartidorModel;
import com.api.rapi.services.RepartidorService;

@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

    private final RepartidorService repartidorService;

    public RepartidorController(RepartidorService repartidorService) {
        this.repartidorService = repartidorService;
    }

    // Obtener todos los repartidores
    @GetMapping
    public List<RepartidorModel> obtenerRepartidores() {
        return repartidorService.obtenerRepartidores();
    }

    // Buscar repartidor por ID
    @GetMapping("/{id}")
    public ResponseEntity<RepartidorModel> obtenerRepartidorPorId(
            @PathVariable Long id) {

        return repartidorService.buscarRepartidorPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener repartidores activos
    @GetMapping("/activos")
    public List<RepartidorModel> obtenerRepartidoresActivos() {
        return repartidorService.obtenerRepartidoresActivos();
    }

    // Obtener repartidores disponibles
    @GetMapping("/disponibles")
    public List<RepartidorModel> obtenerRepartidoresDisponibles() {
        return repartidorService.obtenerRepartidoresDisponibles();
    }

    // Buscar repartidores por nombre
    @GetMapping("/buscar")
    public List<RepartidorModel> buscarPorNombre(
            @RequestParam String nombre) {

        return repartidorService.buscarRepartidoresPorNombre(nombre);
    }

    // Buscar repartidores por tipo de vehículo
    @GetMapping("/vehiculo")
    public List<RepartidorModel> buscarPorTipoVehiculo(
            @RequestParam String tipoVehiculo) {

        return repartidorService.buscarRepartidoresPorTipoVehiculo(
                tipoVehiculo);
    }

    // Registrar repartidor
    @PostMapping
    public ResponseEntity<RepartidorModel> registrarRepartidor(
            @RequestBody RepartidorModel repartidor) {

        RepartidorModel nuevoRepartidor =
                repartidorService.registrarRepartidor(repartidor);

        return ResponseEntity.status(201).body(nuevoRepartidor);
    }

    // Actualizar repartidor
    @PutMapping("/{id}")
    public ResponseEntity<RepartidorModel> actualizarRepartidor(
            @PathVariable Long id,
            @RequestBody RepartidorModel repartidor) {

        if (repartidorService.buscarRepartidorPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepartidorModel actualizado =
                repartidorService.actualizarRepartidor(id, repartidor);

        return ResponseEntity.ok(actualizado);
    }

    // Cambiar disponibilidad del repartidor
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<RepartidorModel> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible) {

        if (repartidorService.buscarRepartidorPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepartidorModel actualizado =
                repartidorService.cambiarDisponibilidad(id, disponible);

        return ResponseEntity.ok(actualizado);
    }

    // Activar o desactivar repartidor
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RepartidorModel> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {

        if (repartidorService.buscarRepartidorPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepartidorModel actualizado =
                repartidorService.cambiarEstado(id, activo);

        return ResponseEntity.ok(actualizado);
    }

    // Actualizar ubicación del repartidor
    @PatchMapping("/{id}/ubicacion")
    public ResponseEntity<RepartidorModel> actualizarUbicacion(
            @PathVariable Long id,
            @RequestParam Double latitud,
            @RequestParam Double longitud) {

        if (repartidorService.buscarRepartidorPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepartidorModel actualizado =
                repartidorService.actualizarUbicacion(
                        id, latitud, longitud);

        return ResponseEntity.ok(actualizado);
    }

    // Buscar repartidor disponible más cercano
    @GetMapping("/mas-cercano")
    public ResponseEntity<RepartidorModel> buscarRepartidorMasCercano(
            @RequestParam Double latitud,
            @RequestParam Double longitud) {

        return repartidorService.buscarRepartidorMasCercano(
                        latitud, longitud)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar repartidor
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRepartidor(
            @PathVariable Long id) {

        if (repartidorService.buscarRepartidorPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repartidorService.eliminarRepartidor(id);

        return ResponseEntity.noContent().build();
    }
}
