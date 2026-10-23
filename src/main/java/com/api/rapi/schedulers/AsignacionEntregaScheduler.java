package com.api.rapi.schedulers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.api.rapi.models.EntregaModel;
import com.api.rapi.services.EntregaService;

@Component
public class AsignacionEntregaScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AsignacionEntregaScheduler.class);

    private final EntregaService entregaService;

    public AsignacionEntregaScheduler(
            EntregaService entregaService) {

        this.entregaService = entregaService;
    }

    // Revisar entregas pendientes cada 30 segundos
    @Scheduled(fixedDelay = 30000)
    public void reintentarAsignacionesPendientes() {

        List<EntregaModel> pendientes;

        try {

            pendientes = entregaService
                    .buscarEntregasPorEstado(
                            "PendienteAsignacion");

        } catch (Exception e) {

            logger.error(
                    "No fue posible consultar las "
                    + "entregas pendientes",
                    e
            );

            return;
        }

        if (pendientes.isEmpty()) {
            return;
        }

        logger.info(
                "Se encontraron {} entregas "
                + "pendientes de asignacion",
                pendientes.size()
        );

        for (EntregaModel entrega : pendientes) {

            Long entregaId = entrega.getId();

            try {

                // Asignar el repartidor disponible mas cercano
                EntregaModel asignada =
                        entregaService.asignarRepartidor(
                                entregaId);

                logger.info(
                        "Asignacion exitosa. "
                        + "Entrega: {} | Repartidor: {}",
                        entregaId,
                        asignada.getRepartidor().getId()
                );

            } catch (IllegalStateException e) {

                logger.warn(
                        "La entrega {} sigue pendiente: {}",
                        entregaId,
                        e.getMessage()
                );

            } catch (Exception e) {

                logger.error(
                        "Error inesperado al intentar "
                        + "asignar la entrega {}",
                        entregaId,
                        e
                );
            }
        }
    }
}
