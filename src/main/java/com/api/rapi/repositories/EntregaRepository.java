package com.api.rapi.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.EntregaModel;

public interface EntregaRepository extends JpaRepository<EntregaModel, Long> {

    Optional<EntregaModel> findByPedidoId(Long pedidoId);

    List<EntregaModel> findByRepartidorId(Long repartidorId);

    List<EntregaModel> findByEstado(String estado);

    List<EntregaModel> findByRepartidorIdAndEstado(
            Long repartidorId, String estado);

    boolean existsByPedidoId(Long pedidoId);

}
