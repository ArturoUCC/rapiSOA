package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.dtos.EntregaDTO;
import com.api.rapi.mappers.EntregaMapper;
import com.api.rapi.services.EntregaService;

@RestController
@RequestMapping("/api/entregas/dto")
public class EntregaDTOController {

    private final EntregaService entregaService;
    private final EntregaMapper entregaMapper;

    public EntregaDTOController(
            EntregaService entregaService,
            EntregaMapper entregaMapper) {

        this.entregaService = entregaService;
        this.entregaMapper = entregaMapper;
    }

    // Obtener todas las entregas en formato DTO
    @GetMapping
    public ResponseEntity<List<EntregaDTO>> obtenerEntregas() {

        List<EntregaDTO> entregas =
                entregaMapper.convertirListaADTO(
                        entregaService.obtenerEntregas());

        return ResponseEntity.ok(entregas);
    }

    // Buscar entrega por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntregaDTO> obtenerEntregaPorId(
            @PathVariable Long id) {

        return entregaService.buscarEntregaPorId(id)
                .map(entregaMapper::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar entrega por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EntregaDTO> obtenerEntregaPorPedido(
            @PathVariable Long pedidoId) {

        return entregaService.buscarEntregaPorPedido(pedidoId)
                .map(entregaMapper::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar entregas por repartidor
    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<List<EntregaDTO>> obtenerPorRepartidor(
            @PathVariable Long repartidorId) {

        List<EntregaDTO> entregas =
                entregaMapper.convertirListaADTO(
                        entregaService.buscarEntregasPorRepartidor(
                                repartidorId));

        return ResponseEntity.ok(entregas);
    }

    // Buscar entregas por estado
    @GetMapping("/estado")
    public ResponseEntity<List<EntregaDTO>> obtenerPorEstado(
            @RequestParam String estado) {

        List<EntregaDTO> entregas =
                entregaMapper.convertirListaADTO(
                        entregaService.buscarEntregasPorEstado(estado));

        return ResponseEntity.ok(entregas);
    }
}
