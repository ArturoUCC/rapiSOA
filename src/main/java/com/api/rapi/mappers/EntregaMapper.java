package com.api.rapi.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.api.rapi.dtos.EntregaDTO;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.RepartidorModel;

@Component
public class EntregaMapper {

    // Convertir una entidad EntregaModel a EntregaDTO
    public EntregaDTO convertirADTO(EntregaModel entrega) {

        if (entrega == null) {
            return null;
        }

        EntregaDTO dto = new EntregaDTO();

        dto.setId(entrega.getId());

        if (entrega.getPedido() != null) {
            dto.setPedidoId(entrega.getPedido().getId());
            dto.setEstadoPedido(entrega.getPedido().getEstado());
        }

        RepartidorModel repartidor = entrega.getRepartidor();

        if (repartidor != null) {

            dto.setRepartidorId(repartidor.getId());

            String nombreCompleto =
                    (repartidor.getNombre() == null
                            ? ""
                            : repartidor.getNombre())
                    + " "
                    + (repartidor.getApellido() == null
                            ? ""
                            : repartidor.getApellido());

            dto.setNombreRepartidor(nombreCompleto.trim());

            dto.setTipoVehiculo(repartidor.getTipoVehiculo());
        }

        dto.setEstadoEntrega(entrega.getEstado());

        dto.setLatitudActual(entrega.getLatitudActual());
        dto.setLongitudActual(entrega.getLongitudActual());

        dto.setFechaAsignacion(entrega.getFechaAsignacion());
        dto.setFechaRecogida(entrega.getFechaRecogida());
        dto.setFechaEntrega(entrega.getFechaEntrega());

        return dto;
    }

    // Convertir una lista de entregas a DTOs
    public List<EntregaDTO> convertirListaADTO(
            List<EntregaModel> entregas) {

        return entregas.stream()
                .map(this::convertirADTO)
                .toList();
    }
}
