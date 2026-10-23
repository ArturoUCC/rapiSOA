package com.api.rapi.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.api.rapi.dtos.PedidoDTO;
import com.api.rapi.models.PedidoModel;

@Component
public class PedidoMapper {

    // Convertir una entidad PedidoModel a PedidoDTO
    public PedidoDTO convertirADTO(PedidoModel pedido) {

        if (pedido == null) {
            return null;
        }

        PedidoDTO dto = new PedidoDTO();

        dto.setId(pedido.getId());

        dto.setClienteId(
                pedido.getCliente().getId());

        dto.setRestauranteId(
                pedido.getRestaurante().getId());

        dto.setNombreCliente(
                pedido.getCliente().getNombre());

        dto.setNombreRestaurante(
                pedido.getRestaurante().getNombre());

        dto.setDireccionEntrega(
                pedido.getDireccionEntrega());

        dto.setEstado(pedido.getEstado());

        dto.setTotal(pedido.getTotal());

        dto.setFechaPedido(
                pedido.getFechaPedido());

        dto.setFechaLimiteAceptacion(
                pedido.getFechaLimiteAceptacion());

        dto.setFechaAceptacion(
                pedido.getFechaAceptacion());

        dto.setFechaEntrega(
                pedido.getFechaEntrega());

        return dto;
    }

    // Convertir una lista de pedidos a DTOs
    public List<PedidoDTO> convertirListaADTO(
            List<PedidoModel> pedidos) {

        return pedidos.stream()
                .map(this::convertirADTO)
                .toList();
    }
}
