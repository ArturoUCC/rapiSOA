package com.api.rapi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.api.rapi.models.PedidoModel;

public interface PedidoService {

    List<PedidoModel> obtenerPedidos();

    Optional<PedidoModel> buscarPedidoPorId(Long id);

    List<PedidoModel> buscarPedidosPorCliente(Long clienteId);

    List<PedidoModel> buscarPedidosPorRestaurante(Long restauranteId);

    List<PedidoModel> buscarPedidosPorEstado(String estado);

    List<PedidoModel> buscarPedidosPorRestauranteYEstado(
            Long restauranteId, String estado);

    PedidoModel crearPedido(
            Long clienteId,
            Long restauranteId,
            String direccionEntrega);

    PedidoModel confirmarPagoPedido(Long pedidoId);

    PedidoModel aceptarPedido(Long pedidoId, Long restauranteId);

    PedidoModel rechazarPedido(Long pedidoId, Long restauranteId);

    PedidoModel cambiarEstadoPedido(Long pedidoId, String nuevoEstado);

    PedidoModel cancelarPedido(Long pedidoId);

    List<PedidoModel> obtenerPedidosConAceptacionVencida(
            LocalDateTime fechaActual);

    void cancelarPedidosVencidos();

}
