package com.api.rapi.facades;

import java.math.BigDecimal;
import java.util.List;

import com.api.rapi.models.DetallePedidoModel;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.PedidoModel;

public interface RapiEntregaFacade {

    // Crear un nuevo pedido
    PedidoModel crearPedido(
            Long clienteId,
            Long restauranteId,
            String direccionEntrega);

    // Agregar productos a un pedido
    DetallePedidoModel agregarProducto(
            Long pedidoId,
            Long productoId,
            Integer cantidad);

    // Consultar el total del pedido
    BigDecimal calcularTotal(Long pedidoId);

    // Consultar los productos del pedido
    List<DetallePedidoModel> consultarProductos(
            Long pedidoId);

    // Aceptar un pedido y preparar su entrega
    PedidoModel aceptarPedido(
            Long pedidoId,
            Long restauranteId);

    // Asignar un repartidor a la entrega
    EntregaModel asignarRepartidor(Long pedidoId);

    // Consultar el seguimiento de un pedido
    EntregaModel consultarEntrega(Long pedidoId);

}
