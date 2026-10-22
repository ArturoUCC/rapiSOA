package com.api.rapi.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.FacturaModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.repositories.FacturaRepository;
import com.api.rapi.repositories.PagoRepository;
import com.api.rapi.repositories.PedidoRepository;

@Service
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;

    @Value("${rapi.facturacion.modo-pruebas:false}")
    private boolean modoPruebas;

    public FacturaServiceImpl(
            FacturaRepository facturaRepository,
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository) {

        this.facturaRepository = facturaRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    public List<FacturaModel> obtenerFacturas() {
        return facturaRepository.findAll();
    }

    @Override
    public Optional<FacturaModel> buscarFacturaPorId(Long id) {
        return facturaRepository.findById(id);
    }

    @Override
    public Optional<FacturaModel> buscarFacturaPorPedido(Long pedidoId) {
        return facturaRepository.findByPedidoId(pedidoId);
    }

    @Override
    public Optional<FacturaModel> buscarFacturaPorFolioFiscal(
            String folioFiscal) {

        return facturaRepository.findByFolioFiscal(folioFiscal);
    }

    @Override
    public List<FacturaModel> buscarFacturasPorEstado(String estado) {
        return facturaRepository.findByEstado(estado);
    }

    @Override
    public List<FacturaModel> buscarFacturasPorRFC(String rfcReceptor) {
        return facturaRepository.findByRfcReceptor(rfcReceptor);
    }

    @Override
    @Transactional
    public FacturaModel solicitarFactura(
            Long pedidoId,
            String rfcReceptor,
            String razonSocial,
            String correoFacturacion) {

        PedidoModel pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));

        if (facturaRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException(
                    "El pedido ya tiene una factura solicitada");
        }

        if (rfcReceptor == null || rfcReceptor.isBlank()) {
            throw new IllegalArgumentException(
                    "El RFC del receptor es obligatorio");
        }

        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException(
                    "La razón social es obligatoria");
        }

        if (correoFacturacion == null ||
                correoFacturacion.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo de facturación es obligatorio");
        }

        boolean pagoConfirmado = pagoRepository
                .existsByPedidoIdAndEstado(pedidoId, "Confirmado");

        if (!pagoConfirmado) {
            throw new IllegalStateException(
                    "El pedido debe tener un pago confirmado");
        }

        if (pedido.getTotal() == null ||
                pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El pedido debe tener un total válido");
        }

        FacturaModel factura = new FacturaModel();

        factura.setPedido(pedido);
        factura.setRfcReceptor(rfcReceptor.trim().toUpperCase());
        factura.setRazonSocial(razonSocial.trim());
        factura.setCorreoFacturacion(correoFacturacion.trim());
        factura.setMontoTotal(pedido.getTotal());
        factura.setEstado("Solicitada");
        factura.setFechaSolicitud(LocalDateTime.now());

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public FacturaModel procesarFactura(Long facturaId) {

        FacturaModel factura = obtenerFactura(facturaId);

        if (!"Solicitada".equals(factura.getEstado())) {
            throw new IllegalStateException(
                    "La factura no está pendiente de procesamiento");
        }

        factura.setEstado("EnProceso");

        // Pendiente: enviar la solicitud al sistema
        // de facturación heredado mediante un adaptador.

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public FacturaModel confirmarFactura(
            Long facturaId,
            String folioFiscal,
            String referenciaExterna) {

        verificarModoPruebas();

        FacturaModel factura = obtenerFactura(facturaId);

        if (!"EnProceso".equals(factura.getEstado())) {
            throw new IllegalStateException(
                    "La factura no está en proceso");
        }

        if (folioFiscal == null || folioFiscal.isBlank()) {
            throw new IllegalArgumentException(
                    "El folio fiscal es obligatorio");
        }

        if (referenciaExterna == null ||
                referenciaExterna.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia externa es obligatoria");
        }

        String folio = folioFiscal.trim();
        String referencia = referenciaExterna.trim();

        Optional<FacturaModel> facturaExistente =
                facturaRepository.findByFolioFiscal(folio);

        if (facturaExistente.isPresent() &&
                !facturaExistente.get().getId().equals(facturaId)) {

            throw new IllegalStateException(
                    "El folio fiscal ya está registrado");
        }

        factura.setFolioFiscal(folio);
        factura.setReferenciaExterna(referencia);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado("Emitida");

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public FacturaModel marcarFacturaConError(Long facturaId) {

        verificarModoPruebas();

        FacturaModel factura = obtenerFactura(facturaId);

        if (!"EnProceso".equals(factura.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden marcar con error "
                    + "las facturas en proceso");
        }

        factura.setEstado("Error");

        return facturaRepository.save(factura);
    }

    private FacturaModel obtenerFactura(Long id) {

        return facturaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La factura no existe"));
    }

    private void verificarModoPruebas() {

        if (!modoPruebas) {
            throw new IllegalStateException(
                    "La confirmación simulada de facturas "
                    + "está deshabilitada");
        }
    }
}
