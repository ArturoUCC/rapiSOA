package com.api.rapi.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.rapi.realtime.PedidoSseService;
import com.api.rapi.services.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoSseController {

    private final PedidoSseService pedidoSseService;
    private final PedidoService pedidoService;

    public PedidoSseController(
            PedidoSseService pedidoSseService,
            PedidoService pedidoService) {

        this.pedidoSseService = pedidoSseService;
        this.pedidoService = pedidoService;
    }

    // Suscribirse a los eventos de un pedido en tiempo real
    @GetMapping(
            value = "/{id}/eventos",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter escucharEventos(
            @PathVariable Long id) {

        if (pedidoService.buscarPedidoPorId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido no existe");
        }

        return pedidoSseService.suscribirPedido(id);
    }

    // Consultar conexiones activas para pruebas locales
    // Sin exponer este dato en la API publica
    @GetMapping("/eventos/estado")
    public ResponseEntity<String> estadoServicio() {
        return ResponseEntity.ok(
                "Servicio de eventos SSE disponible");
    }
}
