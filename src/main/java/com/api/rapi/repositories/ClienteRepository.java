package com.api.rapi.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.rapi.models.ClienteModel;

public interface ClienteRepository extends JpaRepository<ClienteModel, Long> {

    Optional<ClienteModel> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<ClienteModel> findByNombreContainingIgnoreCase(String nombre);

}
