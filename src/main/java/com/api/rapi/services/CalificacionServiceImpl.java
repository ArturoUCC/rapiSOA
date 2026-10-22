package com.api.rapi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.CalificacionModel;
import com.api.rapi.models.PedidoModel;

import com.api.rapi.repositories.CalificacionRepository;
import com.api.rapi.repositories.PedidoRepository;

@Service
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final PedidoRepository pedidoRepository;

    public CalificacionServiceImpl(
            CalificacionRepository calificacionRepository,
            PedidoRepository pedidoRepository) {

        this.calificacionRepository = calificacionRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public List<CalificacionModel> obtenerCalificaciones() {
        return calificacionRepository.findAll();
    }

    @Override
    public Optional<CalificacionModel> buscarCalificacionPorId(Long id) {
        return calificacionRepository.findById(id);
    }

    @Override
    public Optional<CalificacionModel> buscarCalificacionPorPedido(
            Long pedidoId) {

        return calificacionRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<CalificacionModel> buscarCalificacionesPorRestaurante(
            Long restauranteId) {

        return calificacionRepository
                .findByPedidoRestauranteId(restauranteId);
    }

    @Override
    public List<CalificacionModel> buscarCalificacionesPorCliente(
            Long clienteId) {

        return calificacionRepository.findByPedidoClienteId(clienteId);
    }

    @Override
    @Transactional
    public CalificacionModel registrarCalificacion(
            Long pedidoId,
            Integer puntuacionRestaurante,
            Integer puntuacionRepartidor,
            String comentario) {

        validarPuntuaciones(
                puntuacionRestaurante,
                puntuacionRepartidor);

        PedidoModel pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));

        if (!"Entregado".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden calificar pedidos entregados");
        }

        if (calificacionRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException(
                    "Este pedido ya tiene una calificación registrada");
        }

        CalificacionModel calificacion = new CalificacionModel();

        calificacion.setPedido(pedido);
        calificacion.setPuntuacionRestaurante(puntuacionRestaurante);
        calificacion.setPuntuacionRepartidor(puntuacionRepartidor);
        calificacion.setComentario(comentario);
        calificacion.setFechaCalificacion(LocalDateTime.now());

        return calificacionRepository.save(calificacion);
    }

    @Override
    @Transactional
    public CalificacionModel actualizarCalificacion(
            Long calificacionId,
            Integer puntuacionRestaurante,
            Integer puntuacionRepartidor,
            String comentario) {

        validarPuntuaciones(
                puntuacionRestaurante,
                puntuacionRepartidor);

        CalificacionModel calificacion = calificacionRepository
                .findById(calificacionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La calificación no existe"));

        calificacion.setPuntuacionRestaurante(puntuacionRestaurante);
        calificacion.setPuntuacionRepartidor(puntuacionRepartidor);
        calificacion.setComentario(comentario);

        return calificacionRepository.save(calificacion);
    }

    @Override
    @Transactional
    public void eliminarCalificacion(Long calificacionId) {

        CalificacionModel calificacion = calificacionRepository
                .findById(calificacionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La calificación no existe"));

        calificacionRepository.delete(calificacion);
    }

    private void validarPuntuaciones(
            Integer puntuacionRestaurante,
            Integer puntuacionRepartidor) {

        if (puntuacionRestaurante == null ||
                puntuacionRestaurante < 1 ||
                puntuacionRestaurante > 5) {

            throw new IllegalArgumentException(
                    "La puntuación del restaurante debe estar entre 1 y 5");
        }

        if (puntuacionRepartidor == null ||
                puntuacionRepartidor < 1 ||
                puntuacionRepartidor > 5) {

            throw new IllegalArgumentException(
                    "La puntuación del repartidor debe estar entre 1 y 5");
        }
    }
}
