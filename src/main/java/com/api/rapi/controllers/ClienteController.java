package com.api.rapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.rapi.models.ClienteModel;
import com.api.rapi.services.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Obtener todos los clientes
    @GetMapping
    public List<ClienteModel> obtenerClientes() {
        return clienteService.obtenerClientes();
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteModel> obtenerClientePorId(
            @PathVariable Long id) {

        return clienteService.buscarClientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar cliente por correo
    @GetMapping("/correo")
    public ResponseEntity<ClienteModel> buscarPorCorreo(
            @RequestParam String correo) {

        return clienteService.buscarClientePorCorreo(correo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar clientes por nombre
    @GetMapping("/buscar")
    public List<ClienteModel> buscarPorNombre(
            @RequestParam String nombre) {

        return clienteService.buscarClientesPorNombre(nombre);
    }

    // Registrar cliente
    @PostMapping
    public ResponseEntity<ClienteModel> registrarCliente(
            @RequestBody ClienteModel cliente) {

        ClienteModel nuevoCliente =
                clienteService.registrarCliente(cliente);

        return ResponseEntity.status(201).body(nuevoCliente);
    }

    // Actualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteModel> actualizarCliente(
            @PathVariable Long id,
            @RequestBody ClienteModel cliente) {

        if (clienteService.buscarClientePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ClienteModel actualizado =
                clienteService.actualizarCliente(id, cliente);

        return ResponseEntity.ok(actualizado);
    }

    // Eliminar cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(
            @PathVariable Long id) {

        if (clienteService.buscarClientePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        clienteService.eliminarCliente(id);

        return ResponseEntity.noContent().build();
    }
}
