package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.CalificacionModel;

public interface CalificacionService {

    List<CalificacionModel> obtenerCalificaciones();

    Optional<CalificacionModel> buscarCalificacionPorId(Long id);

    Optional<CalificacionModel> buscarCalificacionPorPedido(Long pedidoId);

    List<CalificacionModel> buscarCalificacionesPorRestaurante(
            Long restauranteId);

    List<CalificacionModel> buscarCalificacionesPorCliente(
            Long clienteId);

    CalificacionModel registrarCalificacion(
            Long pedidoId,
            Integer puntuacionRestaurante,
            Integer puntuacionRepartidor,
            String comentario);

    CalificacionModel actualizarCalificacion(
            Long calificacionId,
            Integer puntuacionRestaurante,
            Integer puntuacionRepartidor,
            String comentario);

    void eliminarCalificacion(Long calificacionId);

}
