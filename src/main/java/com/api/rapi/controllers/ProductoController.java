package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.ProductoModel;
import com.api.rapi.services.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Obtener todos los productos
    @GetMapping
    public List<ProductoModel> obtenerProductos() {
        return productoService.obtenerProductos();
    }

    // Buscar producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoModel> obtenerProductoPorId(
            @PathVariable Long id) {

        return productoService.buscarProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener productos de un restaurante
    @GetMapping("/restaurante/{restauranteId}")
    public List<ProductoModel> obtenerProductosPorRestaurante(
            @PathVariable Long restauranteId) {

        return productoService.buscarProductosPorRestaurante(
                restauranteId);
    }

    // Obtener productos disponibles de un restaurante
    @GetMapping("/restaurante/{restauranteId}/disponibles")
    public List<ProductoModel> obtenerProductosDisponibles(
            @PathVariable Long restauranteId) {

        return productoService.obtenerProductosDisponibles(
                restauranteId);
    }

    // Buscar productos por nombre
    @GetMapping("/buscar")
    public List<ProductoModel> buscarProductosPorNombre(
            @RequestParam String nombre) {

        return productoService.buscarProductosPorNombre(nombre);
    }

    // Buscar productos por nombre y restaurante
    @GetMapping("/buscar/restaurante/{restauranteId}")
    public List<ProductoModel> buscarPorNombreYRestaurante(
            @PathVariable Long restauranteId,
            @RequestParam String nombre) {

        return productoService.buscarProductosPorNombreYRestaurante(
                restauranteId, nombre);
    }

    // Registrar producto
    @PostMapping
    public ResponseEntity<ProductoModel> registrarProducto(
            @RequestBody ProductoModel producto) {

        ProductoModel nuevoProducto =
                productoService.registrarProducto(producto);

        return ResponseEntity.status(201).body(nuevoProducto);
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductoModel> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoModel producto) {

        if (productoService.buscarProductoPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProductoModel actualizado =
                productoService.actualizarProducto(id, producto);

        return ResponseEntity.ok(actualizado);
    }

    // Cambiar disponibilidad del producto
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ProductoModel> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible) {

        if (productoService.buscarProductoPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProductoModel actualizado =
                productoService.cambiarDisponibilidad(id, disponible);

        return ResponseEntity.ok(actualizado);
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long id) {

        if (productoService.buscarProductoPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productoService.eliminarProducto(id);

        return ResponseEntity.noContent().build();
    }
}
