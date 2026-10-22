package com.api.rapi.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.PedidoModel;

public interface PedidoRepository extends JpaRepository<PedidoModel, Long> {

    List<PedidoModel> findByClienteId(Long clienteId);

    List<PedidoModel> findByRestauranteId(Long restauranteId);

    List<PedidoModel> findByEstado(String estado);

    List<PedidoModel> findByRestauranteIdAndEstado(
            Long restauranteId, String estado);

    List<PedidoModel> findByEstadoAndFechaLimiteAceptacionBefore(
            String estado, LocalDateTime fechaActual);

    List<PedidoModel> findByFechaPedidoBetween(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

}
