package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.RestauranteModel;

public interface RestauranteService {

    List<RestauranteModel> obtenerRestaurantes();

    Optional<RestauranteModel> buscarRestaurantePorId(Long id);

    List<RestauranteModel> buscarRestaurantesPorNombre(String nombre);

    List<RestauranteModel> buscarRestaurantesPorTipoComida(String tipoComida);

    List<RestauranteModel> obtenerRestaurantesActivos();

    List<RestauranteModel> obtenerRestaurantesAbiertos();

    RestauranteModel registrarRestaurante(RestauranteModel restaurante);

    RestauranteModel actualizarRestaurante(
            Long id, RestauranteModel restaurante);

    RestauranteModel cambiarDisponibilidad(Long id, boolean abierto);

    RestauranteModel cambiarEstado(Long id, boolean activo);

    void eliminarRestaurante(Long id);

}
