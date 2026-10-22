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
@Table(name = "pagos")
public class PagoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoModel pedido;

    private BigDecimal monto;

    private String metodoPago;

    private String estado;

    private String referenciaExterna;

    private LocalDateTime fechaPago;

    private LocalDateTime fechaReembolso;

    private String referenciaReembolso;

    public PagoModel() {
    }

    public PagoModel(PedidoModel pedido, BigDecimal monto,
                     String metodoPago, String estado,
                     String referenciaExterna,
                     LocalDateTime fechaPago) {
        this.pedido = pedido;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.referenciaExterna = referenciaExterna;
        this.fechaPago = fechaPago;
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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public LocalDateTime getFechaReembolso() {
        return fechaReembolso;
    }

    public void setFechaReembolso(LocalDateTime fechaReembolso) {
        this.fechaReembolso = fechaReembolso;
    }

    public String getReferenciaReembolso() {
        return referenciaReembolso;
    }

    public void setReferenciaReembolso(String referenciaReembolso) {
        this.referenciaReembolso = referenciaReembolso;
    }
}
