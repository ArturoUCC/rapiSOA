package com.api.rapi.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas")
public class FacturaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoModel pedido;

    private String rfcReceptor;

    private String razonSocial;

    private String correoFacturacion;

    private BigDecimal montoTotal;

    private String estado;

    private String folioFiscal;

    private String referenciaExterna;

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaEmision;

    public FacturaModel() {
    }

    public FacturaModel(PedidoModel pedido, String rfcReceptor,
                       String razonSocial, String correoFacturacion,
                       BigDecimal montoTotal, String estado,
                       LocalDateTime fechaSolicitud) {

        this.pedido = pedido;
        this.rfcReceptor = rfcReceptor;
        this.razonSocial = razonSocial;
        this.correoFacturacion = correoFacturacion;
        this.montoTotal = montoTotal;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PedidoModel getPedido() {
        return pedido;
    }

    public void setPedido(PedidoModel pedido) {
        this.pedido = pedido;
    }

    public String getRfcReceptor() {
        return rfcReceptor;
    }

    public void setRfcReceptor(String rfcReceptor) {
        this.rfcReceptor = rfcReceptor;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCorreoFacturacion() {
        return correoFacturacion;
    }

    public void setCorreoFacturacion(String correoFacturacion) {
        this.correoFacturacion = correoFacturacion;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFolioFiscal() {
        return folioFiscal;
    }

    public void setFolioFiscal(String folioFiscal) {
        this.folioFiscal = folioFiscal;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
