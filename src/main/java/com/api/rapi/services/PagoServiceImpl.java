package com.api.rapi.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.PagoModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.repositories.PagoRepository;
import com.api.rapi.repositories.PedidoRepository;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    @Value("${rapi.pagos.modo-pruebas:false}")
    private boolean modoPruebas;

    public PagoServiceImpl(
            PagoRepository pagoRepository,
            PedidoRepository pedidoRepository,
            PedidoService pedidoService) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
    }

    @Override
    public List<PagoModel> obtenerPagos() {
        return pagoRepository.findAll();
    }

    @Override
    public Optional<PagoModel> buscarPagoPorId(Long id) {
        return pagoRepository.findById(id);
    }

    @Override
    public Optional<PagoModel> buscarPagoPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId);
    }

    @Override
    public Optional<PagoModel> buscarPagoPorReferencia(
            String referenciaExterna) {

        return pagoRepository.findByReferenciaExterna(
                referenciaExterna);
    }

    @Override
    public List<PagoModel> buscarPagosPorEstado(String estado) {
        return pagoRepository.findByEstado(estado);
    }

    @Override
    @Transactional
    public PagoModel registrarPago(
            Long pedidoId, String metodoPago) {

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

        if (metodoPago == null ||
                !"Tarjeta".equalsIgnoreCase(metodoPago.trim())) {
            throw new IllegalArgumentException(
                    "El método de pago debe ser Tarjeta");
        }

        if (pagoRepository.findByPedidoId(pedidoId).isPresent()) {
            throw new IllegalStateException(
                    "El pedido ya tiene un pago registrado");
        }

        PagoModel pago = new PagoModel();

        pago.setPedido(pedido);
        pago.setMonto(pedido.getTotal());
        pago.setMetodoPago("Tarjeta");
        pago.setEstado("Pendiente");
        pago.setFechaPago(null);
        pago.setReferenciaExterna(null);

        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public PagoModel confirmarPago(
            Long pagoId, String referenciaExterna) {

        verificarModoPruebas();

        PagoModel pago = obtenerPago(pagoId);

        if (!"Pendiente".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "El pago no está pendiente");
        }

        validarReferencia(referenciaExterna);

        String referencia = referenciaExterna.trim();

        if (pagoRepository.findByReferenciaExterna(referencia)
                .isPresent()) {
            throw new IllegalStateException(
                    "La referencia de pago ya está registrada");
        }

        PedidoModel pedido = pago.getPedido();

        if (!"PendientePago".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido no está pendiente de pago");
        }

        if (pago.getMonto().compareTo(pedido.getTotal()) != 0) {
            throw new IllegalStateException(
                    "El monto del pago no coincide con el total");
        }

        pago.setEstado("Confirmado");
        pago.setReferenciaExterna(referencia);
        pago.setFechaPago(LocalDateTime.now());

        PagoModel guardado = pagoRepository.saveAndFlush(pago);

        // Inicia los cinco minutos para que
        // el restaurante acepte el pedido.
        pedidoService.confirmarPagoPedido(pedido.getId());

        return guardado;
    }

    @Override
    @Transactional
    public PagoModel marcarPagoFallido(Long pagoId) {

        verificarModoPruebas();

        PagoModel pago = obtenerPago(pagoId);

        if (!"Pendiente".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden marcar como fallidos "
                    + "los pagos pendientes");
        }

        pago.setEstado("Fallido");

        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public PagoModel solicitarReembolso(Long pedidoId) {

        PedidoModel pedido = obtenerPedido(pedidoId);

        if (!"Cancelado".equals(pedido.getEstado()) &&
                !"Rechazado".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "El pedido debe estar cancelado o rechazado");
        }

        PagoModel pago = pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un pago para el pedido"));

        if ("ReembolsoPendiente".equals(pago.getEstado())) {
            return pago;
        }

        if (!"Confirmado".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "El pago no permite solicitar un reembolso");
        }

        pago.setEstado("ReembolsoPendiente");

        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public PagoModel confirmarReembolso(
            Long pagoId, String referenciaReembolso) {

        verificarModoPruebas();

        PagoModel pago = obtenerPago(pagoId);

        if (!"ReembolsoPendiente".equals(pago.getEstado())) {
            throw new IllegalStateException(
                    "El pago no tiene un reembolso pendiente");
        }

        validarReferencia(referenciaReembolso);

        pago.setEstado("Reembolsado");
        pago.setReferenciaReembolso(referenciaReembolso.trim());
        pago.setFechaReembolso(LocalDateTime.now());

        return pagoRepository.save(pago);
    }

    @Override
    public boolean verificarPagoConfirmado(Long pedidoId) {

        return pagoRepository.existsByPedidoIdAndEstado(
                pedidoId, "Confirmado");
    }

    private PedidoModel obtenerPedido(Long pedidoId) {

        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));
    }

    private PagoModel obtenerPago(Long pagoId) {

        return pagoRepository.findById(pagoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pago no existe"));
    }

    private void validarReferencia(String referencia) {

        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia es obligatoria");
        }
    }

    private void verificarModoPruebas() {

        if (!modoPruebas) {
            throw new IllegalStateException(
                    "Esta operación simulada está deshabilitada. "
                    + "Se requiere verificar la transacción "
                    + "con la pasarela de pagos externa");
        }
    }
}
