package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.dtos.PedidoDTO;
import com.api.rapi.mappers.PedidoMapper;
import com.api.rapi.services.PedidoService;

@RestController
@RequestMapping("/api/pedidos/dto")
public class PedidoDTOController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

    public PedidoDTOController(
            PedidoService pedidoService,
            PedidoMapper pedidoMapper) {

        this.pedidoService = pedidoService;
        this.pedidoMapper = pedidoMapper;
    }

    // Obtener todos los pedidos en formato DTO
    @GetMapping
    public ResponseEntity<List<PedidoDTO>> obtenerPedidos() {

        List<PedidoDTO> pedidos = pedidoMapper.convertirListaADTO(
                pedidoService.obtenerPedidos());

        return ResponseEntity.ok(pedidos);
    }

    // Buscar un pedido por ID en formato DTO
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorId(
            @PathVariable Long id) {

        return pedidoService.buscarPedidoPorId(id)
                .map(pedidoMapper::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener pedidos de un cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoDTO>> obtenerPorCliente(
            @PathVariable Long clienteId) {

        List<PedidoDTO> pedidos = pedidoMapper.convertirListaADTO(
                pedidoService.buscarPedidosPorCliente(clienteId));

        return ResponseEntity.ok(pedidos);
    }

    // Obtener pedidos de un restaurante
    @GetMapping("/restaurante/{restauranteId}")
    public ResponseEntity<List<PedidoDTO>> obtenerPorRestaurante(
            @PathVariable Long restauranteId) {

        List<PedidoDTO> pedidos = pedidoMapper.convertirListaADTO(
                pedidoService.buscarPedidosPorRestaurante(restauranteId));

        return ResponseEntity.ok(pedidos);
    }

    // Obtener pedidos por estado
    @GetMapping("/estado")
    public ResponseEntity<List<PedidoDTO>> obtenerPorEstado(
            @RequestParam String estado) {

        List<PedidoDTO> pedidos = pedidoMapper.convertirListaADTO(
                pedidoService.buscarPedidosPorEstado(estado));

        return ResponseEntity.ok(pedidos);
    }

    // Obtener pedidos por restaurante y estado
    @GetMapping("/restaurante/{restauranteId}/estado")
    public ResponseEntity<List<PedidoDTO>> obtenerPorRestauranteYEstado(
            @PathVariable Long restauranteId,
            @RequestParam String estado) {

        List<PedidoDTO> pedidos = pedidoMapper.convertirListaADTO(
                pedidoService.buscarPedidosPorRestauranteYEstado(
                        restauranteId, estado));

        return ResponseEntity.ok(pedidos);
    }
}
