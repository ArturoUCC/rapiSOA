package com.api.rapi.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.FacturaModel;

public interface FacturaRepository
        extends JpaRepository<FacturaModel, Long> {

    Optional<FacturaModel> findByPedidoId(Long pedidoId);

    Optional<FacturaModel> findByFolioFiscal(String folioFiscal);

    Optional<FacturaModel> findByReferenciaExterna(String referenciaExterna);

    List<FacturaModel> findByEstado(String estado);

    List<FacturaModel> findByRfcReceptor(String rfcReceptor);

    boolean existsByPedidoId(Long pedidoId);

}
