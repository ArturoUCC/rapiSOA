package com.api.rapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.RepartidorModel;

public interface RepartidorRepository extends JpaRepository<RepartidorModel, Long> {

    List<RepartidorModel> findByActivoTrue();

    List<RepartidorModel> findByDisponibleTrueAndActivoTrue();

    List<RepartidorModel> findByTipoVehiculoIgnoreCase(String tipoVehiculo);

    List<RepartidorModel> findByNombreContainingIgnoreCase(String nombre);

}
