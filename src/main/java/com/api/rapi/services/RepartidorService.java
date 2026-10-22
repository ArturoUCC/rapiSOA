package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.RepartidorModel;

public interface RepartidorService {

    List<RepartidorModel> obtenerRepartidores();

    Optional<RepartidorModel> buscarRepartidorPorId(Long id);

    List<RepartidorModel> obtenerRepartidoresActivos();

    List<RepartidorModel> obtenerRepartidoresDisponibles();

    List<RepartidorModel> buscarRepartidoresPorNombre(String nombre);

    List<RepartidorModel> buscarRepartidoresPorTipoVehiculo(
            String tipoVehiculo);

    RepartidorModel registrarRepartidor(RepartidorModel repartidor);

    RepartidorModel actualizarRepartidor(
            Long id, RepartidorModel repartidor);

    RepartidorModel cambiarDisponibilidad(
            Long id, boolean disponible);

    RepartidorModel cambiarEstado(Long id, boolean activo);

    RepartidorModel actualizarUbicacion(
            Long id, Double latitud, Double longitud);

    Optional<RepartidorModel> buscarRepartidorMasCercano(
            Double latitud, Double longitud);

    void eliminarRepartidor(Long id);

}
