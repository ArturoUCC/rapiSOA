package com.api.rapi.realtime;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.rapi.events.PedidoEvent;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.repositories.EntregaRepository;
import com.api.rapi.repositories.PedidoRepository;

@Service
public class PedidoSseService {

    private static final Logger logger =
            LoggerFactory.getLogger(PedidoSseService.class);

    private static final long TIEMPO_CONEXION = 30 * 60 * 1000L;

    private final Map<Long, List<SseEmitter>> conexiones =
            new ConcurrentHashMap<>();

    private final PedidoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;

    public PedidoSseService(
            PedidoRepository pedidoRepository,
            EntregaRepository entregaRepository) {

        this.pedidoRepository = pedidoRepository;
        this.entregaRepository = entregaRepository;
    }

    // Crear una conexion SSE y enviar el estado actual
    public SseEmitter suscribirPedido(Long pedidoId) {

        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del pedido no es valido");
        }

        PedidoModel pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));

        SseEmitter emitter = new SseEmitter(TIEMPO_CONEXION);

        emitter.onCompletion(() ->
                eliminarConexion(pedidoId, emitter));

        emitter.onTimeout(() ->
                eliminarConexion(pedidoId, emitter));

        emitter.onError(error ->
                eliminarConexion(pedidoId, emitter));

        // Obtener el estado de la entrega, si existe
        EntregaModel entrega = entregaRepository
                .findByPedidoId(pedidoId)
                .orElse(null);

        Map<String, Object> estadoInicial =
                crearEstadoActual(pedido, entrega);

        try {

            emitter.send(
                    SseEmitter.event()
                            .name("ESTADO_ACTUAL")
                            .data(estadoInicial)
            );

            // No mantener conexiones de pedidos finalizados
            if (esEstadoFinal(pedido.getEstado())) {
                emitter.complete();
                return emitter;
            }

            conexiones.computeIfAbsent(
                    pedidoId,
                    id -> new CopyOnWriteArrayList<>()
            ).add(emitter);

        } catch (IOException e) {

            eliminarConexion(pedidoId, emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // Publicar eventos a los clientes conectados
    public void publicarActualizacion(PedidoEvent evento) {

        Long pedidoId = evento.getPedidoId();

        List<SseEmitter> suscriptores =
                conexiones.get(pedidoId);

        if (suscriptores == null || suscriptores.isEmpty()) {
            return;
        }

        boolean finalizar = esEstadoFinal(
                evento.getEstadoPedido());

        // Consultar coordenadas actualizadas si existen
        EntregaModel entrega = entregaRepository
                .findByPedidoId(pedidoId)
                .orElse(null);

        Map<String, Object> datos =
                crearDatosEvento(evento, entrega);

        for (SseEmitter emitter : suscriptores) {

            try {

                emitter.send(
                        SseEmitter.event()
                                .name(evento.getTipoEvento())
                                .data(datos)
                );

                if (finalizar) {
                    emitter.complete();
                    eliminarConexion(pedidoId, emitter);
                }

            } catch (Exception e) {

                logger.warn(
                        "No se pudo enviar el evento SSE "
                        + "del pedido {}",
                        pedidoId);

                eliminarConexion(pedidoId, emitter);
                emitter.completeWithError(e);
            }
        }
    }

    // Crear la informacion inicial del pedido
    private Map<String, Object> crearEstadoActual(
            PedidoModel pedido,
            EntregaModel entrega) {

        Map<String, Object> datos =
                new java.util.LinkedHashMap<>();

        datos.put("pedidoId", pedido.getId());
        datos.put("estadoPedido", pedido.getEstado());
        datos.put("total", pedido.getTotal());
        datos.put("fechaPedido", pedido.getFechaPedido());
        datos.put(
                "fechaLimiteAceptacion",
                pedido.getFechaLimiteAceptacion());

        if (entrega != null) {

            datos.put("entregaId", entrega.getId());
            datos.put("estadoEntrega", entrega.getEstado());
            datos.put("latitud", entrega.getLatitudActual());
            datos.put("longitud", entrega.getLongitudActual());

        } else {

            datos.put("entregaId", null);
            datos.put("estadoEntrega", null);
            datos.put("latitud", null);
            datos.put("longitud", null);
        }

        return datos;
    }

    // Crear datos para cada evento SSE
    private Map<String, Object> crearDatosEvento(
            PedidoEvent evento,
            EntregaModel entrega) {

        Map<String, Object> datos =
                new java.util.LinkedHashMap<>();

        datos.put("pedidoId", evento.getPedidoId());
        datos.put("restauranteId", evento.getRestauranteId());
        datos.put("tipoEvento", evento.getTipoEvento());
        datos.put("estadoPedido", evento.getEstadoPedido());
        datos.put("fechaEvento", evento.getFechaEvento());

        if (entrega != null) {

            datos.put("entregaId", entrega.getId());
            datos.put("estadoEntrega", entrega.getEstado());
            datos.put("latitud", entrega.getLatitudActual());
            datos.put("longitud", entrega.getLongitudActual());
        }

        return datos;
    }

    // Identificar pedidos que ya finalizaron
    private boolean esEstadoFinal(String estado) {

        return "Entregado".equals(estado) ||
                "Cancelado".equals(estado) ||
                "Rechazado".equals(estado);
    }

    // Eliminar conexiones cerradas
    private void eliminarConexion(
            Long pedidoId,
            SseEmitter emitter) {

        List<SseEmitter> suscriptores =
                conexiones.get(pedidoId);

        if (suscriptores != null) {

            suscriptores.remove(emitter);

            if (suscriptores.isEmpty()) {
                conexiones.remove(pedidoId, suscriptores);
            }
        }
    }

    // Obtener cantidad de conexiones activas
    public int obtenerConexionesActivas(Long pedidoId) {

        List<SseEmitter> suscriptores =
                conexiones.get(pedidoId);

        return suscriptores == null
                ? 0
                : suscriptores.size();
    }
}
