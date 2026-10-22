package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.RestauranteModel;
import com.api.rapi.services.RestauranteService;

@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {

    private final RestauranteService restauranteService;

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    // Obtener todos los restaurantes
    @GetMapping
    public List<RestauranteModel> obtenerRestaurantes() {
        return restauranteService.obtenerRestaurantes();
    }

    // Buscar restaurante por ID
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteModel> obtenerRestaurantePorId(
            @PathVariable Long id) {

        return restauranteService.buscarRestaurantePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar restaurantes por nombre
    @GetMapping("/buscar")
    public List<RestauranteModel> buscarPorNombre(
            @RequestParam String nombre) {

        return restauranteService.buscarRestaurantesPorNombre(nombre);
    }

    // Buscar restaurantes por tipo de comida
    @GetMapping("/tipo-comida")
    public List<RestauranteModel> buscarPorTipoComida(
            @RequestParam String tipoComida) {

        return restauranteService.buscarRestaurantesPorTipoComida(tipoComida);
    }

    // Obtener restaurantes activos
    @GetMapping("/activos")
    public List<RestauranteModel> obtenerRestaurantesActivos() {
        return restauranteService.obtenerRestaurantesActivos();
    }

    // Obtener restaurantes abiertos y activos
    @GetMapping("/abiertos")
    public List<RestauranteModel> obtenerRestaurantesAbiertos() {
        return restauranteService.obtenerRestaurantesAbiertos();
    }

    // Registrar restaurante
    @PostMapping
    public ResponseEntity<RestauranteModel> registrarRestaurante(
            @RequestBody RestauranteModel restaurante) {

        RestauranteModel nuevoRestaurante =
                restauranteService.registrarRestaurante(restaurante);

        return ResponseEntity.status(201).body(nuevoRestaurante);
    }

    // Actualizar restaurante
    @PutMapping("/{id}")
    public ResponseEntity<RestauranteModel> actualizarRestaurante(
            @PathVariable Long id,
            @RequestBody RestauranteModel restaurante) {

        if (restauranteService.buscarRestaurantePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RestauranteModel actualizado =
                restauranteService.actualizarRestaurante(id, restaurante);

        return ResponseEntity.ok(actualizado);
    }

    // Cambiar disponibilidad del restaurante
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<RestauranteModel> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean abierto) {

        if (restauranteService.buscarRestaurantePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RestauranteModel actualizado =
                restauranteService.cambiarDisponibilidad(id, abierto);

        return ResponseEntity.ok(actualizado);
    }

    // Activar o desactivar restaurante
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RestauranteModel> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {

        if (restauranteService.buscarRestaurantePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RestauranteModel actualizado =
                restauranteService.cambiarEstado(id, activo);

        return ResponseEntity.ok(actualizado);
    }

    // Eliminar restaurante
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRestaurante(
            @PathVariable Long id) {

        if (restauranteService.buscarRestaurantePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        restauranteService.eliminarRestaurante(id);

        return ResponseEntity.noContent().build();
    }
}
