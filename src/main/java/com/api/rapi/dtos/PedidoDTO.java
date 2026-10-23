package com.api.rapi.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoDTO {

    private Long id;
    private Long clienteId;
    private Long restauranteId;

    private String nombreCliente;
    private String nombreRestaurante;

    private String direccionEntrega;
    private String estado;

    private BigDecimal total;

    private LocalDateTime fechaPedido;
    private LocalDateTime fechaLimiteAceptacion;
    private LocalDateTime fechaAceptacion;
    private LocalDateTime fechaEntrega;

    public PedidoDTO() {
    }

    public PedidoDTO(
            Long id,
            Long clienteId,
            Long restauranteId,
            String nombreCliente,
            String nombreRestaurante,
            String direccionEntrega,
            String estado,
            BigDecimal total,
            LocalDateTime fechaPedido,
            LocalDateTime fechaLimiteAceptacion,
            LocalDateTime fechaAceptacion,
            LocalDateTime fechaEntrega) {

        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.nombreCliente = nombreCliente;
        this.nombreRestaurante = nombreRestaurante;
        this.direccionEntrega = direccionEntrega;
        this.estado = estado;
        this.total = total;
        this.fechaPedido = fechaPedido;
        this.fechaLimiteAceptacion = fechaLimiteAceptacion;
        this.fechaAceptacion = fechaAceptacion;
        this.fechaEntrega = fechaEntrega;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreRestaurante() {
        return nombreRestaurante;
    }

    public void setNombreRestaurante(String nombreRestaurante) {
        this.nombreRestaurante = nombreRestaurante;
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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDateTime getFechaLimiteAceptacion() {
        return fechaLimiteAceptacion;
    }

    public void setFechaLimiteAceptacion(
            LocalDateTime fechaLimiteAceptacion) {
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
