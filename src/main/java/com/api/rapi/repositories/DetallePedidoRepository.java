package com.api.rapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.DetallePedidoModel;

public interface DetallePedidoRepository
        extends JpaRepository<DetallePedidoModel, Long> {

    List<DetallePedidoModel> findByPedidoId(Long pedidoId);

    List<DetallePedidoModel> findByProductoId(Long productoId);

    boolean existsByPedidoIdAndProductoId(
            Long pedidoId, Long productoId);

}
