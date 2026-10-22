package com.api.rapi.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "entregas")
public class EntregaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoModel pedido;

    @ManyToOne
    @JoinColumn(name = "repartidor_id")
    private RepartidorModel repartidor;

    private String estado;

    private LocalDateTime fechaAsignacion;

    private LocalDateTime fechaRecogida;

    private LocalDateTime fechaEntrega;

    private Double latitudActual;

    private Double longitudActual;

    public EntregaModel() {
    }

    public EntregaModel(PedidoModel pedido, RepartidorModel repartidor,
                       String estado, LocalDateTime fechaAsignacion,
                       LocalDateTime fechaRecogida,
                       LocalDateTime fechaEntrega,
                       Double latitudActual, Double longitudActual) {

        this.pedido = pedido;
        this.repartidor = repartidor;
        this.estado = estado;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaRecogida = fechaRecogida;
        this.fechaEntrega = fechaEntrega;
        this.latitudActual = latitudActual;
        this.longitudActual = longitudActual;
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

    public RepartidorModel getRepartidor() {
        return repartidor;
    }

    public void setRepartidor(RepartidorModel repartidor) {
        this.repartidor = repartidor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaRecogida() {
        return fechaRecogida;
    }

    public void setFechaRecogida(LocalDateTime fechaRecogida) {
        this.fechaRecogida = fechaRecogida;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Double getLatitudActual() {
        return latitudActual;
    }

    public void setLatitudActual(Double latitudActual) {
        this.latitudActual = latitudActual;
    }

    public Double getLongitudActual() {
        return longitudActual;
    }

    public void setLongitudActual(Double longitudActual) {
        this.longitudActual = longitudActual;
    }
}
