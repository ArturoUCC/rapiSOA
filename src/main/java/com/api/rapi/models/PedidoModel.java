package com.api.rapi.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class PedidoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteModel cliente;

    @ManyToOne
    @JoinColumn(name = "restaurante_id", nullable = false)
    private RestauranteModel restaurante;

    private LocalDateTime fechaPedido;

    private BigDecimal total;

    private String direccionEntrega;

    private String estado;

    private LocalDateTime fechaLimiteAceptacion;

    private LocalDateTime fechaAceptacion;

    private LocalDateTime fechaEntrega;

    public PedidoModel() {
    }

    public PedidoModel(ClienteModel cliente,
                       RestauranteModel restaurante,
                       LocalDateTime fechaPedido,
                       BigDecimal total,
                       String direccionEntrega,
                       String estado,
                       LocalDateTime fechaLimiteAceptacion) {

        this.cliente = cliente;
        this.restaurante = restaurante;
        this.fechaPedido = fechaPedido;
        this.total = total;
        this.direccionEntrega = direccionEntrega;
        this.estado = estado;
        this.fechaLimiteAceptacion = fechaLimiteAceptacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public RestauranteModel getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(RestauranteModel restaurante) {
        this.restaurante = restaurante;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaLimiteAceptacion() {
        return fechaLimiteAceptacion;
    }

    public void setFechaLimiteAceptacion(LocalDateTime fechaLimiteAceptacion) {
        this.fechaLimiteAceptacion = fechaLimiteAceptacion;
    }

    public LocalDateTime getFechaAceptacion() {
        return fechaAceptacion;
    }

    public void setFechaAceptacion(LocalDateTime fechaAceptacion) {
        this.fechaAceptacion = fechaAceptacion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
}
