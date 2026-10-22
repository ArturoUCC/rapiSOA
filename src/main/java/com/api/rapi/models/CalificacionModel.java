package com.api.rapi.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "calificaciones")
public class CalificacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoModel pedido;

    private Integer puntuacionRestaurante;

    private Integer puntuacionRepartidor;

    private String comentario;

    private LocalDateTime fechaCalificacion;

    public CalificacionModel() {
    }

    public CalificacionModel(PedidoModel pedido,
                            Integer puntuacionRestaurante,
                            Integer puntuacionRepartidor,
                            String comentario,
                            LocalDateTime fechaCalificacion) {

        this.pedido = pedido;
        this.puntuacionRestaurante = puntuacionRestaurante;
        this.puntuacionRepartidor = puntuacionRepartidor;
        this.comentario = comentario;
        this.fechaCalificacion = fechaCalificacion;
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

    public Integer getPuntuacionRestaurante() {
        return puntuacionRestaurante;
    }

    public void setPuntuacionRestaurante(Integer puntuacionRestaurante) {
        this.puntuacionRestaurante = puntuacionRestaurante;
    }

    public Integer getPuntuacionRepartidor() {
        return puntuacionRepartidor;
    }

    public void setPuntuacionRepartidor(Integer puntuacionRepartidor) {
        this.puntuacionRepartidor = puntuacionRepartidor;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFechaCalificacion() {
        return fechaCalificacion;
    }

    public void setFechaCalificacion(LocalDateTime fechaCalificacion) {
        this.fechaCalificacion = fechaCalificacion;
    }
}
