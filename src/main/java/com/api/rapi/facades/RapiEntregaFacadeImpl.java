package com.api.rapi.facades;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.DetallePedidoModel;
import com.api.rapi.models.EntregaModel;
import com.api.rapi.models.PedidoModel;

import com.api.rapi.services.DetallePedidoService;
import com.api.rapi.services.EntregaService;
import com.api.rapi.services.PedidoService;

@Service
public class RapiEntregaFacadeImpl implements RapiEntregaFacade {

    private final PedidoService pedidoService;
    private final DetallePedidoService detallePedidoService;
    private final EntregaService entregaService;

    public RapiEntregaFacadeImpl(
            PedidoService pedidoService,
            DetallePedidoService detallePedidoService,
            EntregaService entregaService) {

        this.pedidoService = pedidoService;
        this.detallePedidoService = detallePedidoService;
        this.entregaService = entregaService;
    }

    @Override
    @Transactional
    public PedidoModel crearPedido(
            Long clienteId,
            Long restauranteId,
            String direccionEntrega) {

        return pedidoService.crearPedido(
                clienteId,
                restauranteId,
                direccionEntrega);
    }

    @Override
    @Transactional
    public DetallePedidoModel agregarProducto(
            Long pedidoId,
            Long productoId,
            Integer cantidad) {

        return detallePedidoService.agregarProductoAlPedido(
                pedidoId,
                productoId,
                cantidad);
    }

    @Override
    public BigDecimal calcularTotal(Long pedidoId) {

        return detallePedidoService.calcularTotalPedido(pedidoId);
    }

    @Override
    public List<DetallePedidoModel> consultarProductos(Long pedidoId) {

        return detallePedidoService.obtenerDetallesPorPedido(pedidoId);
    }

    @Override
    @Transactional
    public PedidoModel aceptarPedido(
            Long pedidoId,
            Long restauranteId) {

        PedidoModel pedido = pedidoService.aceptarPedido(
                pedidoId,
                restauranteId);

        // Solo preparar la entrega si el pedido fue aceptado.
        if ("Aceptado".equals(pedido.getEstado())) {

            // Evitar crear una entrega duplicada.
            if (entregaService.buscarEntregaPorPedido(pedidoId)
                    .isEmpty()) {

                entregaService.crearEntrega(pedidoId);
            }
        }

        return pedido;
    }

    @Override
    @Transactional
    public EntregaModel asignarRepartidor(Long pedidoId) {

        EntregaModel entrega = entregaService
                .buscarEntregaPorPedido(pedidoId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El pedido no tiene una entrega registrada"));

        return entregaService.asignarRepartidor(
                entrega.getId());
    }

    @Override
    public EntregaModel consultarEntrega(Long pedidoId) {

        return entregaService.buscarEntregaPorPedido(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe una entrega para este pedido"));
    }
}
