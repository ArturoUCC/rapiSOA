package com.api.rapi.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.PagoModel;

public interface PagoRepository extends JpaRepository<PagoModel, Long> {

    Optional<PagoModel> findByPedidoId(Long pedidoId);

    Optional<PagoModel> findByReferenciaExterna(String referenciaExterna);

    List<PagoModel> findByEstado(String estado);

    boolean existsByPedidoIdAndEstado(Long pedidoId, String estado);

    List<PagoModel> findByEstadoIn(List<String> estados);

}
