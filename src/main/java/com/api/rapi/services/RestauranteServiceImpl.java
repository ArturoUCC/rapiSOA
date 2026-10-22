package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.RestauranteModel;
import com.api.rapi.repositories.RestauranteRepository;

@Service
public class RestauranteServiceImpl implements RestauranteService {

    private final RestauranteRepository restauranteRepository;

    public RestauranteServiceImpl(
            RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    @Override
    public List<RestauranteModel> obtenerRestaurantes() {
        return restauranteRepository.findAll();
    }

    @Override
    public Optional<RestauranteModel> buscarRestaurantePorId(Long id) {
        return restauranteRepository.findById(id);
    }

    @Override
    public List<RestauranteModel> buscarRestaurantesPorNombre(String nombre) {
        return restauranteRepository
                .findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public List<RestauranteModel> buscarRestaurantesPorTipoComida(
            String tipoComida) {
        return restauranteRepository
                .findByTipoComidaContainingIgnoreCase(tipoComida);
    }

    @Override
    public List<RestauranteModel> obtenerRestaurantesActivos() {
        return restauranteRepository.findByActivoTrue();
    }

    @Override
    public List<RestauranteModel> obtenerRestaurantesAbiertos() {
        return restauranteRepository.findByAbiertoTrueAndActivoTrue();
    }

    @Override
    @Transactional
    public RestauranteModel registrarRestaurante(
            RestauranteModel restaurante) {

        if (restaurante.getNombre() == null ||
                restaurante.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del restaurante es obligatorio");
        }

        if (restaurante.getDireccion() == null ||
                restaurante.getDireccion().isBlank()) {
            throw new IllegalArgumentException(
                    "La dirección del restaurante es obligatoria");
        }

        if (restaurante.getTipoComida() == null ||
                restaurante.getTipoComida().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de comida es obligatorio");
        }

        restaurante.setId(null);
        restaurante.setNombre(restaurante.getNombre().trim());
        restaurante.setDireccion(restaurante.getDireccion().trim());

        return restauranteRepository.save(restaurante);
    }

    @Override
    @Transactional
    public RestauranteModel actualizarRestaurante(
            Long id, RestauranteModel datos) {

        RestauranteModel restaurante = restauranteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El restaurante no existe"));

        if (datos.getNombre() == null ||
                datos.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del restaurante es obligatorio");
        }

        if (datos.getDireccion() == null ||
                datos.getDireccion().isBlank()) {
            throw new IllegalArgumentException(
                    "La dirección del restaurante es obligatoria");
        }

        if (datos.getTipoComida() == null ||
                datos.getTipoComida().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de comida es obligatorio");
        }

        restaurante.setNombre(datos.getNombre().trim());
        restaurante.setDireccion(datos.getDireccion().trim());
        restaurante.setTelefono(datos.getTelefono());
        restaurante.setCorreo(datos.getCorreo());
        restaurante.setTipoComida(datos.getTipoComida().trim());

        return restauranteRepository.save(restaurante);
    }

    @Override
    @Transactional
    public RestauranteModel cambiarDisponibilidad(
            Long id, boolean abierto) {

        RestauranteModel restaurante = restauranteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El restaurante no existe"));

        if (abierto && !restaurante.isActivo()) {
            throw new IllegalStateException(
                    "No se puede abrir un restaurante inactivo");
        }

        restaurante.setAbierto(abierto);

        return restauranteRepository.save(restaurante);
    }

    @Override
    @Transactional
    public RestauranteModel cambiarEstado(Long id, boolean activo) {

        RestauranteModel restaurante = restauranteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El restaurante no existe"));

        restaurante.setActivo(activo);

        if (!activo) {
            restaurante.setAbierto(false);
        }

        return restauranteRepository.save(restaurante);
    }

    @Override
    @Transactional
    public void eliminarRestaurante(Long id) {

        RestauranteModel restaurante = restauranteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El restaurante no existe"));

        restauranteRepository.delete(restaurante);
    }
}
