package com.api.rapi.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.adapters.SistemaFacturacionLegacyAdapter;
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

    private final ObjectProvider<SistemaFacturacionLegacyAdapter>
            adaptadorProvider;

    @Value("${rapi.facturacion.modo-pruebas:false}")
    private boolean modoPruebas;

    public FacturaServiceImpl(
            FacturaRepository facturaRepository,
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository,
            ObjectProvider<SistemaFacturacionLegacyAdapter>
                    adaptadorProvider) {

        this.facturaRepository = facturaRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.adaptadorProvider = adaptadorProvider;
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
                    "La razon social es obligatoria");
        }

        if (correoFacturacion == null ||
                correoFacturacion.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo de facturacion es obligatorio");
        }

        boolean pagoConfirmado = pagoRepository
                .existsByPedidoIdAndEstado(
                        pedidoId, "Confirmado");

        if (!pagoConfirmado) {
            throw new IllegalStateException(
                    "El pedido debe tener un pago confirmado");
        }

        if (pedido.getTotal() == null ||
                pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El pedido debe tener un total valido");
        }

        FacturaModel factura = new FacturaModel();

        factura.setPedido(pedido);
        factura.setRfcReceptor(
                rfcReceptor.trim().toUpperCase());
        factura.setRazonSocial(razonSocial.trim());
        factura.setCorreoFacturacion(
                correoFacturacion.trim());
        factura.setMontoTotal(pedido.getTotal());
        factura.setEstado("Solicitada");
        factura.setFechaSolicitud(LocalDateTime.now());

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public FacturaModel procesarFactura(Long facturaId) {

        SistemaFacturacionLegacyAdapter adaptador =
                obtenerAdaptadorPruebas();

        FacturaModel factura = obtenerFactura(facturaId);

        if (!"Solicitada".equals(factura.getEstado())) {
            throw new IllegalStateException(
                    "La factura no esta pendiente de procesamiento");
        }

        String referenciaExterna =
                adaptador.solicitarEmision(
                        factura.getRfcReceptor(),
                        factura.getRazonSocial(),
                        factura.getCorreoFacturacion(),
                        factura.getMontoTotal(),
                        factura.getPedido().getId()
                );

        factura.setReferenciaExterna(referenciaExterna);
        factura.setEstado("EnProceso");

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public FacturaModel confirmarFactura(
            Long facturaId,
            String folioFiscal,
            String referenciaExterna) {

        SistemaFacturacionLegacyAdapter adaptador =
                obtenerAdaptadorPruebas();

        FacturaModel factura = obtenerFactura(facturaId);

        if (!"EnProceso".equals(factura.getEstado())) {
            throw new IllegalStateException(
                    "La factura no esta en proceso");
        }

        if (referenciaExterna == null ||
                referenciaExterna.isBlank() ||
                !referenciaExterna.trim().equals(
                        factura.getReferenciaExterna())) {
            throw new IllegalStateException(
                    "La referencia externa no coincide");
        }

        if (!adaptador.verificarEmision(referenciaExterna)) {
            throw new IllegalStateException(
                    "El sistema heredado no ha emitido la factura");
        }

        String folioVerificado =
                adaptador.obtenerFolioFiscal(referenciaExterna);

        if (folioFiscal == null ||
                !folioVerificado.equals(folioFiscal.trim())) {
            throw new IllegalStateException(
                    "El folio no coincide con el sistema heredado");
        }

        Optional<FacturaModel> existente =
                facturaRepository.findByFolioFiscal(folioVerificado);

        if (existente.isPresent() &&
                !existente.get().getId().equals(facturaId)) {
            throw new IllegalStateException(
                    "El folio ya esta registrado");
        }

        factura.setFolioFiscal(folioVerificado);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado("Emitida");

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public FacturaModel marcarFacturaConError(Long facturaId) {

        obtenerAdaptadorPruebas();

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

    private SistemaFacturacionLegacyAdapter
            obtenerAdaptadorPruebas() {

        if (!modoPruebas) {
            throw new IllegalStateException(
                    "La facturacion simulada esta deshabilitada");
        }

        SistemaFacturacionLegacyAdapter adaptador =
                adaptadorProvider.getIfAvailable();

        if (adaptador == null) {
            throw new IllegalStateException(
                    "No existe un adaptador de facturacion activo. "
                    + "Verifica el perfil local");
        }

        return adaptador;
    }
}
