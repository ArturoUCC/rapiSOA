package com.api.rapi.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.events.PedidoEventPublisher;
import com.api.rapi.exceptions.RecursoNoEncontradoException;

import com.api.rapi.models.ClienteModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RestauranteModel;

import com.api.rapi.repositories.ClienteRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.RestauranteRepository;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final PagoEstadoService pagoEstadoService;
    private final PedidoEventPublisher eventPublisher;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            RestauranteRepository restauranteRepository,
            PagoEstadoService pagoEstadoService,
            PedidoEventPublisher eventPublisher) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.restauranteRepository = restauranteRepository;
        this.pagoEstadoService = pagoEstadoService;
        this.eventPublisher = eventPublisher;
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

        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException(
                    "Cliente", clienteId);
        }

        return pedidoRepository.findByClienteId(clienteId);
    }

    @Override
    public List<PedidoModel> buscarPedidosPorRestaurante(
            Long restauranteId) {

        if (!restauranteRepository.existsById(restauranteId)) {
            throw new RecursoNoEncontradoException(
                    "Restaurante", restauranteId);
        }

        return pedidoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public List<PedidoModel> buscarPedidosPorEstado(String estado) {

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException(
                    "El estado del pedido es obligatorio");
        }

        return pedidoRepository.findByEstado(estado.trim());
    }

    @Override
    public List<PedidoModel> buscarPedidosPorRestauranteYEstado(
            Long restauranteId,
            String estado) {

        if (!restauranteRepository.existsById(restauranteId)) {
            throw new RecursoNoEncontradoException(
                    "Restaurante", restauranteId);
        }

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException(
                    "El estado del pedido es obligatorio");
        }

        return pedidoRepository.findByRestauranteIdAndEstado(
                restauranteId,
                estado.trim());
    }

    @Override
    @Transactional
    public PedidoModel crearPedido(
            Long clienteId,
            Long restauranteId,
            String direccionEntrega) {

        ClienteModel cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente", clienteId));

        RestauranteModel restaurante = restauranteRepository
                .findById(restauranteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Restaurante", restauranteId));

        if (!restaurante.isActivo() || !restaurante.isAbierto()) {
            throw new IllegalStateException(
                    "El restaurante no esta disponible");
        }

        if (direccionEntrega == null ||
                direccionEntrega.isBlank()) {
            throw new IllegalArgumentException(
                    "La direccion de entrega es obligatoria");
        }

        PedidoModel pedido = new PedidoModel();

        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDireccionEntrega(direccionEntrega.trim());
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setEstado("PendientePago");

        PedidoModel guardado = pedidoRepository.save(pedido);

        publicarEvento(guardado, "PEDIDO_CREADO");

        return guardado;
    }

    @Override
    @Transactional
    public PedidoModel confirmarPagoPedido(Long pedidoId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if (!"PendientePago".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido no esta pendiente de pago");
        }

        if (pedido.getTotal() == null ||
                pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El pedido debe tener productos y un total valido");
        }

        // La validacion del pago pertenece a PagoEstadoService
        pagoEstadoService.validarPagoConfirmado(
                pedidoId,
                pedido.getTotal());

        LocalDateTime ahora = LocalDateTime.now();

        pedido.setEstado("PendienteAceptacion");
        pedido.setFechaLimiteAceptacion(
                ahora.plusMinutes(5));

        PedidoModel guardado = pedidoRepository.save(pedido);

        publicarEvento(guardado, "PAGO_CONFIRMADO");

        return guardado;
    }

    @Override
    @Transactional
    public PedidoModel aceptarPedido(
            Long pedidoId,
            Long restauranteId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        validarRestaurantePedido(pedido, restauranteId);

        if (!"PendienteAceptacion".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido no esta pendiente de aceptacion");
        }

        if (pedido.getFechaLimiteAceptacion() == null) {
            throw new IllegalStateException(
                    "El pedido no tiene fecha limite de aceptacion");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (!ahora.isBefore(
                pedido.getFechaLimiteAceptacion())) {

            cancelarConReembolsoPendiente(pedido);

            PedidoModel guardado = pedidoRepository.save(pedido);

            publicarEvento(guardado, "PEDIDO_CANCELADO");

            return guardado;
        }

        pedido.setEstado("Aceptado");
        pedido.setFechaAceptacion(ahora);

        PedidoModel guardado = pedidoRepository.save(pedido);

        publicarEvento(guardado, "PEDIDO_ACEPTADO");

        return guardado;
    }

    @Override
    @Transactional
    public PedidoModel rechazarPedido(
            Long pedidoId,
            Long restauranteId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        validarRestaurantePedido(pedido, restauranteId);

        if (!"PendienteAceptacion".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden rechazar pedidos "
                    + "pendientes de aceptacion");
        }

        cancelarConReembolsoPendiente(pedido);
        pedido.setEstado("Rechazado");

        PedidoModel guardado = pedidoRepository.save(pedido);

        publicarEvento(guardado, "PEDIDO_RECHAZADO");

        return guardado;
    }

    @Override
    @Transactional
    public PedidoModel cambiarEstadoPedido(
            Long pedidoId,
            String nuevoEstado) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException(
                    "El nuevo estado es obligatorio");
        }

        if ("Aceptado".equals(pedido.getEstado()) &&
                "EnPreparacion".equals(nuevoEstado.trim())) {

            pedido.setEstado("EnPreparacion");

            PedidoModel guardado = pedidoRepository.save(pedido);

            publicarEvento(guardado, "PEDIDO_EN_PREPARACION");

            return guardado;
        }

        throw new IllegalStateException(
                "No esta permitida esta transicion de estado");
    }

    @Override
    @Transactional
    public PedidoModel cancelarPedido(Long pedidoId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if ("PendientePago".equals(pedido.getEstado())) {

            pedido.setEstado("Cancelado");

            PedidoModel guardado = pedidoRepository.save(pedido);

            publicarEvento(guardado, "PEDIDO_CANCELADO");

            return guardado;
        }

        if ("PendienteAceptacion".equals(pedido.getEstado())) {

            cancelarConReembolsoPendiente(pedido);

            PedidoModel guardado = pedidoRepository.save(pedido);

            publicarEvento(guardado, "PEDIDO_CANCELADO");

            return guardado;
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
                    && pedido.getFechaLimiteAceptacion() != null
                    && !ahora.isBefore(
                            pedido.getFechaLimiteAceptacion())) {

                cancelarConReembolsoPendiente(pedido);

                PedidoModel guardado =
                        pedidoRepository.save(pedido);

                publicarEvento(
                        guardado,
                        "PEDIDO_CANCELADO");
            }
        }
    }

    private PedidoModel obtenerPedido(Long id) {

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Pedido", id));
    }

    private void validarRestaurantePedido(
            PedidoModel pedido,
            Long restauranteId) {

        if (restauranteId == null ||
                !pedido.getRestaurante()
                        .getId()
                        .equals(restauranteId)) {

            throw new IllegalArgumentException(
                    "El pedido no pertenece a este restaurante");
        }
    }

    private void cancelarConReembolsoPendiente(
            PedidoModel pedido) {

        // El servicio de pedidos ya no modifica pagos directamente
        pagoEstadoService.marcarReembolsoPendiente(
                pedido.getId());

        pedido.setEstado("Cancelado");
    }

    private void publicarEvento(
            PedidoModel pedido,
            String tipoEvento) {

        eventPublisher.publicarEvento(
                pedido.getId(),
                pedido.getRestaurante().getId(),
                tipoEvento,
                pedido.getEstado()
        );
    }
}
