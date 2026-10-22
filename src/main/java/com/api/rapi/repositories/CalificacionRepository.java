package com.api.rapi.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.CalificacionModel;

public interface CalificacionRepository
        extends JpaRepository<CalificacionModel, Long> {

    Optional<CalificacionModel> findByPedidoId(Long pedidoId);

    boolean existsByPedidoId(Long pedidoId);

    List<CalificacionModel> findByPedidoRestauranteId(Long restauranteId);

    List<CalificacionModel> findByPedidoClienteId(Long clienteId);

}

