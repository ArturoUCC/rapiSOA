package com.api.rapi.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.DetallePedidoModel;
import com.api.rapi.models.PedidoModel;
import com.api.rapi.models.ProductoModel;

import com.api.rapi.repositories.DetallePedidoRepository;
import com.api.rapi.repositories.PedidoRepository;
import com.api.rapi.repositories.ProductoRepository;

@Service
public class DetallePedidoServiceImpl implements DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public DetallePedidoServiceImpl(
            DetallePedidoRepository detallePedidoRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository) {

        this.detallePedidoRepository = detallePedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<DetallePedidoModel> obtenerDetalles() {
        return detallePedidoRepository.findAll();
    }

    @Override
    public Optional<DetallePedidoModel> buscarDetallePorId(Long id) {
        return detallePedidoRepository.findById(id);
    }

    @Override
    public List<DetallePedidoModel> obtenerDetallesPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<DetallePedidoModel> obtenerDetallesPorProducto(Long productoId) {
        return detallePedidoRepository.findByProductoId(productoId);
    }

    @Override
    @Transactional
    public DetallePedidoModel agregarProductoAlPedido(
            Long pedidoId,
            Long productoId,
            Integer cantidad) {

        validarCantidad(cantidad);

        PedidoModel pedido = obtenerPedidoEditable(pedidoId);

        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"));

        if (!producto.getRestaurante().getId()
                .equals(pedido.getRestaurante().getId())) {
            throw new IllegalStateException(
                    "El producto no pertenece al restaurante del pedido");
        }

        if (!producto.isDisponible()) {
            throw new IllegalStateException(
                    "El producto no está disponible");
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El producto tiene un precio inválido");
        }

        if (detallePedidoRepository.existsByPedidoIdAndProductoId(
                pedidoId, productoId)) {
            throw new IllegalStateException(
                    "El producto ya está agregado al pedido. "
                    + "Utiliza la operación de actualizar cantidad");
        }

        DetallePedidoModel detalle = new DetallePedidoModel();

        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());

        detalle.setSubtotal(
                producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));

        DetallePedidoModel guardado = detallePedidoRepository.save(detalle);

        actualizarTotalPedido(pedido);

        return guardado;
    }

    @Override
    @Transactional
    public DetallePedidoModel actualizarCantidad(
            Long detalleId,
            Integer cantidad) {

        validarCantidad(cantidad);

        DetallePedidoModel detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El detalle del pedido no existe"));

        PedidoModel pedido = obtenerPedidoEditable(
                detalle.getPedido().getId());

        detalle.setCantidad(cantidad);

        detalle.setSubtotal(
                detalle.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(cantidad)));

        DetallePedidoModel actualizado =
                detallePedidoRepository.save(detalle);

        actualizarTotalPedido(pedido);

        return actualizado;
    }

    @Override
    @Transactional
    public void eliminarProductoDelPedido(Long detalleId) {

        DetallePedidoModel detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El detalle del pedido no existe"));

        PedidoModel pedido = obtenerPedidoEditable(
                detalle.getPedido().getId());

        detallePedidoRepository.delete(detalle);
        detallePedidoRepository.flush();

        actualizarTotalPedido(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPedido(Long pedidoId) {

        if (!pedidoRepository.existsById(pedidoId)) {
            throw new IllegalArgumentException(
                    "El pedido no existe");
        }

        return calcularTotalDetalles(pedidoId);
    }

    private PedidoModel obtenerPedidoEditable(Long pedidoId) {

        PedidoModel pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"));

        if (!"PendientePago".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "Los productos solo se pueden modificar "
                    + "cuando el pedido está pendiente de pago");
        }

        return pedido;
    }

    private void validarCantidad(Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero");
        }
    }

    private BigDecimal calcularTotalDetalles(Long pedidoId) {

        return detallePedidoRepository.findByPedidoId(pedidoId)
                .stream()
                .map(DetallePedidoModel::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void actualizarTotalPedido(PedidoModel pedido) {

        BigDecimal total = calcularTotalDetalles(pedido.getId());

        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }
}
