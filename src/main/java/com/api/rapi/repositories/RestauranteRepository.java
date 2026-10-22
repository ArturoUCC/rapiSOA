package com.api.rapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.RestauranteModel;

public interface RestauranteRepository extends JpaRepository<RestauranteModel, Long> {

    List<RestauranteModel> findByNombreContainingIgnoreCase(String nombre);

    List<RestauranteModel> findByTipoComidaContainingIgnoreCase(String tipoComida);

    List<RestauranteModel> findByActivoTrue();

    List<RestauranteModel> findByAbiertoTrueAndActivoTrue();

}
