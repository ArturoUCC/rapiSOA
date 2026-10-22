package com.api.rapi.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.RepartidorModel;
import com.api.rapi.repositories.RepartidorRepository;

@Service
public class RepartidorServiceImpl implements RepartidorService {

    private final RepartidorRepository repartidorRepository;

    public RepartidorServiceImpl(RepartidorRepository repartidorRepository) {
        this.repartidorRepository = repartidorRepository;
    }

    @Override
    public List<RepartidorModel> obtenerRepartidores() {
        return repartidorRepository.findAll();
    }

    @Override
    public Optional<RepartidorModel> buscarRepartidorPorId(Long id) {
        return repartidorRepository.findById(id);
    }

    @Override
    public List<RepartidorModel> obtenerRepartidoresActivos() {
        return repartidorRepository.findByActivoTrue();
    }

    @Override
    public List<RepartidorModel> obtenerRepartidoresDisponibles() {
        return repartidorRepository.findByDisponibleTrueAndActivoTrue();
    }

    @Override
    public List<RepartidorModel> buscarRepartidoresPorNombre(String nombre) {
        return repartidorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public List<RepartidorModel> buscarRepartidoresPorTipoVehiculo(
            String tipoVehiculo) {

        return repartidorRepository.findByTipoVehiculoIgnoreCase(tipoVehiculo);
    }

    @Override
    @Transactional
    public RepartidorModel registrarRepartidor(RepartidorModel repartidor) {

        validarDatosRepartidor(repartidor);

        repartidor.setId(null);
        repartidor.setNombre(repartidor.getNombre().trim());

        if (repartidor.getLatitud() != null ||
                repartidor.getLongitud() != null) {

            validarCoordenadas(
                    repartidor.getLatitud(),
                    repartidor.getLongitud());
        }

        if (!repartidor.isActivo()) {
            repartidor.setDisponible(false);
        }

        return repartidorRepository.save(repartidor);
    }

    @Override
    @Transactional
    public RepartidorModel actualizarRepartidor(
            Long id, RepartidorModel datos) {

        validarDatosRepartidor(datos);

        RepartidorModel repartidor = obtenerRepartidor(id);

        repartidor.setNombre(datos.getNombre().trim());
        repartidor.setApellido(datos.getApellido());
        repartidor.setCorreo(datos.getCorreo());
        repartidor.setTelefono(datos.getTelefono());
        repartidor.setTipoVehiculo(datos.getTipoVehiculo());

        // El estado y la disponibilidad se actualizan
        // mediante sus operaciones específicas.

        return repartidorRepository.save(repartidor);
    }

    @Override
    @Transactional
    public RepartidorModel cambiarDisponibilidad(
            Long id, boolean disponible) {

        RepartidorModel repartidor = obtenerRepartidor(id);

        if (disponible && !repartidor.isActivo()) {
            throw new IllegalStateException(
                    "No se puede habilitar un repartidor inactivo");
        }

        repartidor.setDisponible(disponible);

        return repartidorRepository.save(repartidor);
    }

    @Override
    @Transactional
    public RepartidorModel cambiarEstado(Long id, boolean activo) {

        RepartidorModel repartidor = obtenerRepartidor(id);

        repartidor.setActivo(activo);

        if (!activo) {
            repartidor.setDisponible(false);
        }

        return repartidorRepository.save(repartidor);
    }

    @Override
    @Transactional
    public RepartidorModel actualizarUbicacion(
            Long id, Double latitud, Double longitud) {

        validarCoordenadas(latitud, longitud);

        RepartidorModel repartidor = obtenerRepartidor(id);

        if (!repartidor.isActivo()) {
            throw new IllegalStateException(
                    "No se puede actualizar la ubicación de un repartidor inactivo");
        }

        repartidor.setLatitud(latitud);
        repartidor.setLongitud(longitud);

        return repartidorRepository.save(repartidor);
    }

    @Override
    public Optional<RepartidorModel> buscarRepartidorMasCercano(
            Double latitud, Double longitud) {

        validarCoordenadas(latitud, longitud);

        return repartidorRepository.findByDisponibleTrueAndActivoTrue()
                .stream()
                .filter(r -> r.getLatitud() != null &&
                             r.getLongitud() != null)
                .min(Comparator.comparingDouble(
                        r -> calcularDistanciaKm(
                                latitud,
                                longitud,
                                r.getLatitud(),
                                r.getLongitud())));
    }

    @Override
    @Transactional
    public void eliminarRepartidor(Long id) {

        RepartidorModel repartidor = obtenerRepartidor(id);

        // No eliminar repartidores que participan en una entrega.
        // Se revisan las referencias desde EntregaRepository
        // cuando implementemos el servicio de entregas.
        repartidorRepository.delete(repartidor);
    }

    private RepartidorModel obtenerRepartidor(Long id) {
        return repartidorRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El repartidor no existe"));
    }

    private void validarDatosRepartidor(RepartidorModel repartidor) {

        if (repartidor == null) {
            throw new IllegalArgumentException(
                    "Los datos del repartidor son obligatorios");
        }

        if (repartidor.getNombre() == null ||
                repartidor.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del repartidor es obligatorio");
        }

        if (repartidor.getTipoVehiculo() == null ||
                repartidor.getTipoVehiculo().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de vehículo es obligatorio");
        }
    }

    private void validarCoordenadas(Double latitud, Double longitud) {

        if (latitud == null || longitud == null ||
                !Double.isFinite(latitud) ||
                !Double.isFinite(longitud) ||
                latitud < -90 || latitud > 90 ||
                longitud < -180 || longitud > 180) {

            throw new IllegalArgumentException(
                    "Las coordenadas geográficas no son válidas");
        }
    }

    private double calcularDistanciaKm(
            double lat1, double lon1,
            double lat2, double lon2) {

        final double radioTierraKm = 6371.0;

        double diferenciaLatitud =
                Math.toRadians(lat2 - lat1);

        double diferenciaLongitud =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.pow(Math.sin(diferenciaLatitud / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(diferenciaLongitud / 2), 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(Math.max(0, 1 - a)));

        return radioTierraKm * c;
    }
}
