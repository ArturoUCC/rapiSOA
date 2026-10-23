package com.api.rapi.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.facades.RapiEntregaFacade;
import com.api.rapi.models.DetallePedidoModel;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.PedidoModel;

@RestController
@RequestMapping("/api/rapientrega")
public class RapiEntregaFacadeController {

    private final RapiEntregaFacade rapiEntregaFacade;

    public RapiEntregaFacadeController(
            RapiEntregaFacade rapiEntregaFacade) {

        this.rapiEntregaFacade = rapiEntregaFacade;
    }

    // Crear un pedido mediante la fachada
    @PostMapping("/pedidos")
    public ResponseEntity<PedidoModel> crearPedido(
            @RequestParam Long clienteId,
            @RequestParam Long restauranteId,
            @RequestParam String direccionEntrega) {

        PedidoModel pedido = rapiEntregaFacade.crearPedido(
                clienteId,
                restauranteId,
                direccionEntrega);

        return ResponseEntity.status(201).body(pedido);
    }

    // Agregar productos al pedido
    @PostMapping("/pedidos/{pedidoId}/productos")
    public ResponseEntity<DetallePedidoModel> agregarProducto(
            @PathVariable Long pedidoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {

        DetallePedidoModel detalle =
                rapiEntregaFacade.agregarProducto(
                        pedidoId,
                        productoId,
                        cantidad);

        return ResponseEntity.status(201).body(detalle);
    }

    // Consultar los productos de un pedido
    @GetMapping("/pedidos/{pedidoId}/productos")
    public ResponseEntity<List<DetallePedidoModel>> consultarProductos(
            @PathVariable Long pedidoId) {

        List<DetallePedidoModel> productos =
                rapiEntregaFacade.consultarProductos(pedidoId);

        return ResponseEntity.ok(productos);
    }

    // Consultar el total del pedido
    @GetMapping("/pedidos/{pedidoId}/total")
    public ResponseEntity<BigDecimal> calcularTotal(
            @PathVariable Long pedidoId) {

        BigDecimal total =
                rapiEntregaFacade.calcularTotal(pedidoId);

        return ResponseEntity.ok(total);
    }

    // Aceptar un pedido y preparar su entrega
    @PatchMapping("/pedidos/{pedidoId}/aceptar")
    public ResponseEntity<PedidoModel> aceptarPedido(
            @PathVariable Long pedidoId,
            @RequestParam Long restauranteId) {

        PedidoModel pedido = rapiEntregaFacade.aceptarPedido(
                pedidoId,
                restauranteId);

        return ResponseEntity.ok(pedido);
    }

    // Asignar el repartidor disponible mas cercano
    @PatchMapping("/pedidos/{pedidoId}/asignar-repartidor")
    public ResponseEntity<EntregaModel> asignarRepartidor(
            @PathVariable Long pedidoId) {

        EntregaModel entrega =
                rapiEntregaFacade.asignarRepartidor(pedidoId);

        return ResponseEntity.ok(entrega);
    }

    // Consultar el seguimiento de la entrega
    @GetMapping("/pedidos/{pedidoId}/entrega")
    public ResponseEntity<EntregaModel> consultarEntrega(
            @PathVariable Long pedidoId) {

        EntregaModel entrega =
                rapiEntregaFacade.consultarEntrega(pedidoId);

        return ResponseEntity.ok(entrega);
    }
}
