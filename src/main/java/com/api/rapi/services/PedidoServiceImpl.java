package com.api.rapi.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.ClienteModel;
import com.api.rapi.models.PagoModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RestauranteModel;

import com.api.rapi.repositories.ClienteRepository;
import com.api.rapi.repositories.PagoRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.RestauranteRepository;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final PagoRepository pagoRepository;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            RestauranteRepository restauranteRepository,
            PagoRepository pagoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.restauranteRepository = restauranteRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    public List<PedidoModel> obtenerPedidos() {
        return pedidoRepository.findAll();
    }

    @Override
    public Optional<PedidoModel> buscarPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Override
    public List<PedidoModel> buscarPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Override
    public List<PedidoModel> buscarPedidosPorRestaurante(Long restauranteId) {
        return pedidoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public List<PedidoModel> buscarPedidosPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    @Override
    public List<PedidoModel> buscarPedidosPorRestauranteYEstado(
            Long restauranteId, String estado) {

        return pedidoRepository.findByRestauranteIdAndEstado(
                restauranteId, estado);
    }

    @Override
    @Transactional
    public PedidoModel crearPedido(
            Long clienteId,
            Long restauranteId,
            String direccionEntrega) {

        ClienteModel cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El cliente no existe"));

        RestauranteModel restaurante = restauranteRepository
                .findById(restauranteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El restaurante no existe"));

        if (!restaurante.isActivo() || !restaurante.isAbierto()) {
            throw new IllegalStateException(
                    "El restaurante no está disponible");
        }

        if (direccionEntrega == null ||
                direccionEntrega.isBlank()) {
            throw new IllegalArgumentException(
                    "La dirección de entrega es obligatoria");
        }

        PedidoModel pedido = new PedidoModel();

        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDireccionEntrega(direccionEntrega.trim());
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setEstado("PendientePago");

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public PedidoModel confirmarPagoPedido(Long pedidoId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if (!"PendientePago".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido no está pendiente de pago");
        }

        if (pedido.getTotal() == null ||
                pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El pedido debe tener productos y un total válido");
        }

        PagoModel pago = pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe un pago para este pedido"));

        if (!"Confirmado".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "El pago todavía no está confirmado");
        }

        LocalDateTime ahora = LocalDateTime.now();

        pedido.setEstado("PendienteAceptacion");
        pedido.setFechaLimiteAceptacion(ahora.plusMinutes(5));

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public PedidoModel aceptarPedido(
            Long pedidoId, Long restauranteId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        validarRestaurantePedido(pedido, restauranteId);

        if (!"PendienteAceptacion".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido no está pendiente de aceptación");
        }

        if (pedido.getFechaLimiteAceptacion() == null) {
            throw new IllegalStateException(
                    "El pedido no tiene fecha límite de aceptación");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (!ahora.isBefore(pedido.getFechaLimiteAceptacion())) {
            cancelarConReembolsoPendiente(pedido);

            return pedidoRepository.save(pedido);
        }

        pedido.setEstado("Aceptado");
        pedido.setFechaAceptacion(ahora);

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public PedidoModel rechazarPedido(
            Long pedidoId, Long restauranteId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        validarRestaurantePedido(pedido, restauranteId);

        if (!"PendienteAceptacion".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden rechazar pedidos pendientes de aceptación");
        }

        cancelarConReembolsoPendiente(pedido);

        pedido.setEstado("Rechazado");

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public PedidoModel cambiarEstadoPedido(
            Long pedidoId, String nuevoEstado) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        // Los estados de entrega serán administrados
        // posteriormente por EntregaService.

        if ("Aceptado".equals(pedido.getEstado()) &&
                "EnPreparacion".equals(nuevoEstado)) {

            pedido.setEstado("EnPreparacion");

            return pedidoRepository.save(pedido);
        }

        throw new IllegalStateException(
                "No está permitida esta transición de estado");
    }

    @Override
    @Transactional
    public PedidoModel cancelarPedido(Long pedidoId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if ("PendientePago".equals(pedido.getEstado())) {
            pedido.setEstado("Cancelado");
            return pedidoRepository.save(pedido);
        }

        if ("PendienteAceptacion".equals(pedido.getEstado())) {
            cancelarConReembolsoPendiente(pedido);
            return pedidoRepository.save(pedido);
        }

        throw new IllegalStateException(
                "No se puede cancelar un pedido en su estado actual");
    }

    @Override
    public List<PedidoModel> obtenerPedidosConAceptacionVencida(
            LocalDateTime fechaActual) {

        if (fechaActual == null) {
            throw new IllegalArgumentException(
                    "La fecha de consulta es obligatoria");
        }

        return pedidoRepository
                .findByEstadoAndFechaLimiteAceptacionBefore(
                        "PendienteAceptacion",
                        fechaActual.plusNanos(1));
    }

    @Override
    @Transactional
    public void cancelarPedidosVencidos() {

        LocalDateTime ahora = LocalDateTime.now();

        List<PedidoModel> vencidos =
                obtenerPedidosConAceptacionVencida(ahora);

        for (PedidoModel pedido : vencidos) {

            if ("PendienteAceptacion".equals(pedido.getEstado())
                    && !ahora.isBefore(
                            pedido.getFechaLimiteAceptacion())) {

                cancelarConReembolsoPendiente(pedido);

                pedidoRepository.save(pedido);
            }
        }
    }

    private PedidoModel obtenerPedido(Long id) {

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));
    }

    private void validarRestaurantePedido(
            PedidoModel pedido, Long restauranteId) {

        if (restauranteId == null ||
                !pedido.getRestaurante().getId().equals(restauranteId)) {

            throw new IllegalArgumentException(
                    "El pedido no pertenece a este restaurante");
        }
    }

    private void cancelarConReembolsoPendiente(
            PedidoModel pedido) {

        PagoModel pago = pagoRepository
                .findByPedidoId(pedido.getId())
                .orElse(null);

        if (pago != null &&
                "Confirmado".equals(pago.getEstado())) {

            pago.setEstado("ReembolsoPendiente");
            pagoRepository.save(pago);
        }

        pedido.setEstado("Cancelado");
    }
}
