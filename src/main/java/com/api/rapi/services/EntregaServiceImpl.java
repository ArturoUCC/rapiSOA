package com.api.rapi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.events.PedidoEventPublisher;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.RepartidorModel;

import com.api.rapi.repositories.EntregaRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.RepartidorRepository;

@Service
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;
    private final RepartidorRepository repartidorRepository;
    private final RepartidorService repartidorService;
    private final PedidoEventPublisher eventPublisher;

    public EntregaServiceImpl(
            EntregaRepository entregaRepository,
            PedidoRepository pedidoRepository,
            RepartidorRepository repartidorRepository,
            RepartidorService repartidorService,
            PedidoEventPublisher eventPublisher) {

        this.entregaRepository = entregaRepository;
        this.pedidoRepository = pedidoRepository;
        this.repartidorRepository = repartidorRepository;
        this.repartidorService = repartidorService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<EntregaModel> obtenerEntregas() {
        return entregaRepository.findAll();
    }

    @Override
    public Optional<EntregaModel> buscarEntregaPorId(Long id) {
        return entregaRepository.findById(id);
    }

    @Override
    public Optional<EntregaModel> buscarEntregaPorPedido(Long pedidoId) {
        return entregaRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<EntregaModel> buscarEntregasPorRepartidor(Long repartidorId) {
        return entregaRepository.findByRepartidorId(repartidorId);
    }

    @Override
    public List<EntregaModel> buscarEntregasPorEstado(String estado) {
        return entregaRepository.findByEstado(estado);
    }

    @Override
    @Transactional
    public EntregaModel crearEntrega(Long pedidoId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if (!"Aceptado".equals(pedido.getEstado()) &&
                !"EnPreparacion".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido debe estar aceptado o en preparacion");
        }

        if (entregaRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException(
                    "El pedido ya tiene una entrega registrada");
        }

        EntregaModel entrega = new EntregaModel();
        entrega.setPedido(pedido);
        entrega.setEstado("PendienteAsignacion");

        EntregaModel guardada = entregaRepository.save(entrega);

        publicarEvento(pedido, "ENTREGA_CREADA");

        return guardada;
    }

    @Override
    @Transactional
    public EntregaModel asignarRepartidor(Long entregaId) {

        EntregaModel entrega = obtenerEntrega(entregaId);
        validarPendienteAsignacion(entrega);

        PedidoModel pedido = entrega.getPedido();

        Double latitud = pedido.getRestaurante().getLatitud();
        Double longitud = pedido.getRestaurante().getLongitud();

        validarCoordenadas(latitud, longitud);

        RepartidorModel repartidor = repartidorService
                .buscarRepartidorMasCercano(latitud, longitud)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No hay repartidores disponibles"));

        return asignarRepartidorManual(entregaId, repartidor.getId());
    }

    @Override
    @Transactional
    public EntregaModel asignarRepartidorManual(
            Long entregaId, Long repartidorId) {

        EntregaModel entrega = obtenerEntrega(entregaId);
        validarPendienteAsignacion(entrega);

        RepartidorModel repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El repartidor no existe"));

        if (!repartidor.isActivo() || !repartidor.isDisponible()) {
            throw new IllegalStateException(
                    "El repartidor no esta activo o disponible");
        }

        repartidor.setDisponible(false);
        repartidorRepository.save(repartidor);

        entrega.setRepartidor(repartidor);
        entrega.setEstado("Asignada");
        entrega.setFechaAsignacion(LocalDateTime.now());
        entrega.setLatitudActual(repartidor.getLatitud());
        entrega.setLongitudActual(repartidor.getLongitud());

        EntregaModel guardada = entregaRepository.save(entrega);

        publicarEvento(entrega.getPedido(), "REPARTIDOR_ASIGNADO");

        return guardada;
    }

    @Override
    @Transactional
    public EntregaModel confirmarRecogida(Long entregaId) {

        EntregaModel entrega = obtenerEntrega(entregaId);

        if (!"Asignada".equals(entrega.getEstado())) {
            throw new IllegalStateException(
                    "La entrega debe estar asignada");
        }

        entrega.setEstado("Recogida");
        entrega.setFechaRecogida(LocalDateTime.now());

        EntregaModel guardada = entregaRepository.save(entrega);

        publicarEvento(entrega.getPedido(), "PEDIDO_RECOGIDO");

        return guardada;
    }

    @Override
    @Transactional
    public EntregaModel iniciarEntrega(Long entregaId) {

        EntregaModel entrega = obtenerEntrega(entregaId);

        if (!"Recogida".equals(entrega.getEstado())) {
            throw new IllegalStateException(
                    "Primero se debe confirmar la recogida");
        }

        entrega.setEstado("EnCamino");

        PedidoModel pedido = entrega.getPedido();
        pedido.setEstado("EnCamino");
        pedidoRepository.save(pedido);

        EntregaModel guardada = entregaRepository.save(entrega);

        publicarEvento(pedido, "PEDIDO_EN_CAMINO");

        return guardada;
    }

    @Override
    @Transactional
    public EntregaModel actualizarUbicacion(
            Long entregaId, Double latitud, Double longitud) {

        validarCoordenadas(latitud, longitud);

        EntregaModel entrega = obtenerEntrega(entregaId);

        if (!"EnCamino".equals(entrega.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede actualizar una entrega en camino");
        }

        entrega.setLatitudActual(latitud);
        entrega.setLongitudActual(longitud);

        RepartidorModel repartidor = entrega.getRepartidor();
        repartidor.setLatitud(latitud);
        repartidor.setLongitud(longitud);

        repartidorRepository.save(repartidor);

        EntregaModel guardada = entregaRepository.save(entrega);

        publicarEvento(
                entrega.getPedido(),
                "UBICACION_ACTUALIZADA");

        return guardada;
    }

    @Override
    @Transactional
    public EntregaModel confirmarEntrega(Long entregaId) {

        EntregaModel entrega = obtenerEntrega(entregaId);

        if (!"EnCamino".equals(entrega.getEstado())) {
            throw new IllegalStateException(
                    "La entrega debe estar en camino");
        }

        LocalDateTime ahora = LocalDateTime.now();

        entrega.setEstado("Entregada");
        entrega.setFechaEntrega(ahora);

        PedidoModel pedido = entrega.getPedido();
        pedido.setEstado("Entregado");
        pedido.setFechaEntrega(ahora);

        pedidoRepository.save(pedido);

        RepartidorModel repartidor = entrega.getRepartidor();
        repartidor.setDisponible(true);
        repartidorRepository.save(repartidor);

        EntregaModel guardada = entregaRepository.save(entrega);

        publicarEvento(pedido, "PEDIDO_ENTREGADO");

        return guardada;
    }

    private PedidoModel obtenerPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));
    }

    private EntregaModel obtenerEntrega(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La entrega no existe"));
    }

    private void validarPendienteAsignacion(EntregaModel entrega) {

        if (!"PendienteAsignacion".equals(entrega.getEstado())) {
            throw new IllegalStateException(
                    "La entrega no esta pendiente de asignacion");
        }
    }

    private void validarCoordenadas(
            Double latitud, Double longitud) {

        if (latitud == null || longitud == null ||
                !Double.isFinite(latitud) ||
                !Double.isFinite(longitud) ||
                latitud < -90 || latitud > 90 ||
                longitud < -180 || longitud > 180) {

            throw new IllegalArgumentException(
                    "Las coordenadas no son validas");
        }
    }

    private void publicarEvento(
            PedidoModel pedido, String tipoEvento) {

        eventPublisher.publicarEvento(
                pedido.getId(),
                pedido.getRestaurante().getId(),
                tipoEvento,
                pedido.getEstado()
        );
    }
}
