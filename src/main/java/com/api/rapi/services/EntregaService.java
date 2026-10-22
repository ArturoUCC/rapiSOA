package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.EntregaModel;

public interface EntregaService {

    List<EntregaModel> obtenerEntregas();

    Optional<EntregaModel> buscarEntregaPorId(Long id);

    Optional<EntregaModel> buscarEntregaPorPedido(Long pedidoId);

    List<EntregaModel> buscarEntregasPorRepartidor(Long repartidorId);

    List<EntregaModel> buscarEntregasPorEstado(String estado);

    EntregaModel crearEntrega(Long pedidoId);

    EntregaModel asignarRepartidor(Long entregaId);

    EntregaModel asignarRepartidorManual(
            Long entregaId, Long repartidorId);

    EntregaModel confirmarRecogida(Long entregaId);

    EntregaModel iniciarEntrega(Long entregaId);

    EntregaModel actualizarUbicacion(
            Long entregaId,
            Double latitud,
            Double longitud);

    EntregaModel confirmarEntrega(Long entregaId);

}
