package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.ClienteModel;

public interface ClienteService {

    List<ClienteModel> obtenerClientes();

    Optional<ClienteModel> buscarClientePorId(Long id);

    Optional<ClienteModel> buscarClientePorCorreo(String correo);

    List<ClienteModel> buscarClientesPorNombre(String nombre);

    ClienteModel registrarCliente(ClienteModel cliente);

    ClienteModel actualizarCliente(Long id, ClienteModel cliente);

    void eliminarCliente(Long id);

}
