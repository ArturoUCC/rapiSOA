package com.api.rapi.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.api.rapi.models.DetallePedidoModel;

public interface DetallePedidoService {

    List<DetallePedidoModel> obtenerDetalles();

    Optional<DetallePedidoModel> buscarDetallePorId(Long id);

    List<DetallePedidoModel> obtenerDetallesPorPedido(Long pedidoId);

    List<DetallePedidoModel> obtenerDetallesPorProducto(Long productoId);

    DetallePedidoModel agregarProductoAlPedido(
            Long pedidoId,
            Long productoId,
            Integer cantidad);

    DetallePedidoModel actualizarCantidad(
            Long detalleId,
            Integer cantidad);

    void eliminarProductoDelPedido(Long detalleId);

    BigDecimal calcularTotalPedido(Long pedidoId);

}
