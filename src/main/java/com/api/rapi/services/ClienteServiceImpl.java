package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.ClienteModel;
import com.api.rapi.repositories.ClienteRepository;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<ClienteModel> obtenerClientes() {
        return clienteRepository.findAll();
    }

    @Override
    public Optional<ClienteModel> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Optional<ClienteModel> buscarClientePorCorreo(String correo) {
        return clienteRepository.findByCorreo(correo);
    }

    @Override
    public List<ClienteModel> buscarClientesPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional
    public ClienteModel registrarCliente(ClienteModel cliente) {

        if (cliente.getNombre() == null ||
                cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del cliente es obligatorio");
        }

        if (cliente.getCorreo() == null ||
                cliente.getCorreo().isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio");
        }

        String correo = cliente.getCorreo().trim();

        if (clienteRepository.existsByCorreo(correo)) {
            throw new IllegalStateException(
                    "Ya existe un cliente registrado con ese correo");
        }

        cliente.setId(null);
        cliente.setCorreo(correo);

        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public ClienteModel actualizarCliente(Long id, ClienteModel datos) {

        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El cliente no existe"));

        if (datos.getNombre() == null ||
                datos.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del cliente es obligatorio");
        }

        if (datos.getCorreo() == null ||
                datos.getCorreo().isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio");
        }

        String correo = datos.getCorreo().trim();

        Optional<ClienteModel> clienteConCorreo =
                clienteRepository.findByCorreo(correo);

        if (clienteConCorreo.isPresent() &&
                !clienteConCorreo.get().getId().equals(id)) {
            throw new IllegalStateException(
                    "El correo ya pertenece a otro cliente");
        }

        cliente.setNombre(datos.getNombre());
        cliente.setApellido(datos.getApellido());
        cliente.setCorreo(correo);
        cliente.setTelefono(datos.getTelefono());
        cliente.setDireccion(datos.getDireccion());

        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) {

        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El cliente no existe"));

        clienteRepository.delete(cliente);
    }
}
