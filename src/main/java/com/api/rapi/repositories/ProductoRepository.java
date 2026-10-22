package com.api.rapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.ProductoModel;

public interface ProductoRepository extends JpaRepository<ProductoModel, Long> {

    List<ProductoModel> findByRestauranteId(Long restauranteId);

    List<ProductoModel> findByRestauranteIdAndDisponibleTrue(Long restauranteId);

    List<ProductoModel> findByNombreContainingIgnoreCase(String nombre);

    List<ProductoModel> findByRestauranteIdAndNombreContainingIgnoreCase(
            Long restauranteId, String nombre);

}
