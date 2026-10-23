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

@Service
public class PedidoSseService {

    private static final Logger logger =
            LoggerFactory.getLogger(PedidoSseService.class);

    private static final long TIEMPO_CONEXION = 30 * 60 * 1000L;

    private final Map<Long, List<SseEmitter>> conexiones =
            new ConcurrentHashMap<>();

    // Crear una conexion SSE para un pedido
    public SseEmitter suscribirPedido(Long pedidoId) {

        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del pedido no es valido");
        }

        SseEmitter emitter = new SseEmitter(TIEMPO_CONEXION);

        conexiones.computeIfAbsent(
                pedidoId,
                id -> new CopyOnWriteArrayList<>()
        ).add(emitter);

        emitter.onCompletion(() ->
                eliminarConexion(pedidoId, emitter));

        emitter.onTimeout(() ->
                eliminarConexion(pedidoId, emitter));

        emitter.onError(error ->
                eliminarConexion(pedidoId, emitter));

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("CONEXION_ESTABLECIDA")
                            .data("Conectado al seguimiento del pedido "
                                    + pedidoId)
            );

        } catch (IOException e) {
            eliminarConexion(pedidoId, emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // Enviar actualizaciones a los clientes suscritos
    public void publicarActualizacion(PedidoEvent evento) {

        Long pedidoId = evento.getPedidoId();

        List<SseEmitter> suscriptores = conexiones.get(pedidoId);

        if (suscriptores == null || suscriptores.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : suscriptores) {

            try {
                emitter.send(
                        SseEmitter.event()
                                .name(evento.getTipoEvento())
                                .data(evento)
                );

            } catch (Exception e) {

                logger.warn(
                        "No se pudo enviar evento SSE del pedido {}",
                        pedidoId
                );

                eliminarConexion(pedidoId, emitter);
            }
        }
    }

    // Eliminar conexiones cerradas
    private void eliminarConexion(
            Long pedidoId,
            SseEmitter emitter) {

        List<SseEmitter> suscriptores = conexiones.get(pedidoId);

        if (suscriptores != null) {

            suscriptores.remove(emitter);

            if (suscriptores.isEmpty()) {
                conexiones.remove(pedidoId, suscriptores);
            }
        }
    }

    // Consultar cantidad de conexiones activas
    public int obtenerConexionesActivas(Long pedidoId) {

        List<SseEmitter> suscriptores = conexiones.get(pedidoId);

        return suscriptores == null ? 0 : suscriptores.size();
    }
}
