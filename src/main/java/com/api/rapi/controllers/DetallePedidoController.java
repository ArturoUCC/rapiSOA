package com.api.rapi.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.DetallePedidoModel;
import com.api.rapi.services.DetallePedidoService;

@RestController
@RequestMapping("/api/detalles-pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(
            DetallePedidoService detallePedidoService) {

        this.detallePedidoService = detallePedidoService;
    }

    // Obtener todos los detalles
    @GetMapping
    public List<DetallePedidoModel> obtenerDetalles() {
        return detallePedidoService.obtenerDetalles();
    }

    // Buscar detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<DetallePedidoModel> obtenerDetallePorId(
            @PathVariable Long id) {

        return detallePedidoService.buscarDetallePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener productos de un pedido
    @GetMapping("/pedido/{pedidoId}")
    public List<DetallePedidoModel> obtenerDetallesPorPedido(
            @PathVariable Long pedidoId) {

        return detallePedidoService.obtenerDetallesPorPedido(pedidoId);
    }

    // Buscar detalles por producto
    @GetMapping("/producto/{productoId}")
    public List<DetallePedidoModel> obtenerDetallesPorProducto(
            @PathVariable Long productoId) {

        return detallePedidoService.obtenerDetallesPorProducto(productoId);
    }

    // Agregar producto a un pedido
    @PostMapping
    public ResponseEntity<DetallePedidoModel> agregarProducto(
            @RequestParam Long pedidoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {

        DetallePedidoModel nuevoDetalle =
                detallePedidoService.agregarProductoAlPedido(
                        pedidoId, productoId, cantidad);

        return ResponseEntity.status(201).body(nuevoDetalle);
    }

    // Actualizar cantidad de un producto
    @PatchMapping("/{id}/cantidad")
    public ResponseEntity<DetallePedidoModel> actualizarCantidad(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {

        if (detallePedidoService.buscarDetallePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DetallePedidoModel actualizado =
                detallePedidoService.actualizarCantidad(id, cantidad);

        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un producto del pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long id) {

        if (detallePedidoService.buscarDetallePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        detallePedidoService.eliminarProductoDelPedido(id);

        return ResponseEntity.noContent().build();
    }

    // Calcular el total del pedido
    @GetMapping("/pedido/{pedidoId}/total")
    public ResponseEntity<BigDecimal> calcularTotalPedido(
            @PathVariable Long pedidoId) {

        BigDecimal total =
                detallePedidoService.calcularTotalPedido(pedidoId);

        return ResponseEntity.ok(total);
    }
}

