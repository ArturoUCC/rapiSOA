package com.api.rapi.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.PedidoModel;
import com.api.rapi.services.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Obtener todos los pedidos
    @GetMapping
    public List<PedidoModel> obtenerPedidos() {
        return pedidoService.obtenerPedidos();
    }

    // Buscar pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<PedidoModel> obtenerPedidoPorId(
            @PathVariable Long id) {

        return pedidoService.buscarPedidoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar pedidos por cliente
    @GetMapping("/cliente/{clienteId}")
    public List<PedidoModel> buscarPorCliente(
            @PathVariable Long clienteId) {

        return pedidoService.buscarPedidosPorCliente(clienteId);
    }

    // Buscar pedidos por restaurante
    @GetMapping("/restaurante/{restauranteId}")
    public List<PedidoModel> buscarPorRestaurante(
            @PathVariable Long restauranteId) {

        return pedidoService.buscarPedidosPorRestaurante(restauranteId);
    }

    // Buscar pedidos por estado
    @GetMapping("/estado")
    public List<PedidoModel> buscarPorEstado(
            @RequestParam String estado) {

        return pedidoService.buscarPedidosPorEstado(estado);
    }

    // Buscar pedidos por restaurante y estado
    @GetMapping("/restaurante/{restauranteId}/estado")
    public List<PedidoModel> buscarPorRestauranteYEstado(
            @PathVariable Long restauranteId,
            @RequestParam String estado) {

        return pedidoService.buscarPedidosPorRestauranteYEstado(
                restauranteId, estado);
    }

    // Crear un pedido
    @PostMapping
    public ResponseEntity<PedidoModel> crearPedido(
            @RequestParam Long clienteId,
            @RequestParam Long restauranteId,
            @RequestParam String direccionEntrega) {

        PedidoModel nuevoPedido = pedidoService.crearPedido(
                clienteId, restauranteId, direccionEntrega);

        return ResponseEntity.status(201).body(nuevoPedido);
    }

    // Aceptar un pedido
    @PatchMapping("/{id}/aceptar")
    public ResponseEntity<PedidoModel> aceptarPedido(
            @PathVariable Long id,
            @RequestParam Long restauranteId) {

        PedidoModel actualizado = pedidoService.aceptarPedido(
                id, restauranteId);

        return ResponseEntity.ok(actualizado);
    }

    // Rechazar un pedido
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<PedidoModel> rechazarPedido(
            @PathVariable Long id,
            @RequestParam Long restauranteId) {

        PedidoModel actualizado = pedidoService.rechazarPedido(
                id, restauranteId);

        return ResponseEntity.ok(actualizado);
    }

    // Cambiar el estado de un pedido
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoModel> cambiarEstadoPedido(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {

        PedidoModel actualizado = pedidoService.cambiarEstadoPedido(
                id, nuevoEstado);

        return ResponseEntity.ok(actualizado);
    }

    // Cancelar un pedido
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoModel> cancelarPedido(
            @PathVariable Long id) {

        PedidoModel actualizado = pedidoService.cancelarPedido(id);

        return ResponseEntity.ok(actualizado);
    }

    // Consultar pedidos cuyo tiempo de aceptación venció
    @GetMapping("/vencidos")
    public List<PedidoModel> obtenerPedidosVencidos() {

        return pedidoService.obtenerPedidosConAceptacionVencida(
                LocalDateTime.now());
    }
}
