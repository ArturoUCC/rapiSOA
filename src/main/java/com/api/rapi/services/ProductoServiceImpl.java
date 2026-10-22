package com.api.rapi.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.rapi.models.ProductoModel;
import com.api.rapi.models.RestauranteModel;
import com.api.rapi.repositories.ProductoRepository;
import com.api.rapi.repositories.RestauranteRepository;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final RestauranteRepository restauranteRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            RestauranteRepository restauranteRepository) {

        this.productoRepository = productoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    @Override
    public List<ProductoModel> obtenerProductos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<ProductoModel> buscarProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    public List<ProductoModel> buscarProductosPorRestaurante(Long restauranteId) {
        return productoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public List<ProductoModel> obtenerProductosDisponibles(Long restauranteId) {
        return productoRepository.findByRestauranteIdAndDisponibleTrue(restauranteId);
    }

    @Override
    public List<ProductoModel> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public List<ProductoModel> buscarProductosPorNombreYRestaurante(
            Long restauranteId, String nombre) {

        return productoRepository
                .findByRestauranteIdAndNombreContainingIgnoreCase(
                        restauranteId, nombre);
    }

    @Override
    @Transactional
    public ProductoModel registrarProducto(ProductoModel producto) {

        validarProducto(producto);

        if (producto.getRestaurante() == null ||
                producto.getRestaurante().getId() == null) {
            throw new IllegalArgumentException(
                    "Debes indicar el restaurante del producto");
        }

        RestauranteModel restaurante = restauranteRepository
                .findById(producto.getRestaurante().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El restaurante no existe"));

        if (!restaurante.isActivo()) {
            throw new IllegalStateException(
                    "No se pueden registrar productos en un restaurante inactivo");
        }

        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setRestaurante(restaurante);

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public ProductoModel actualizarProducto(Long id, ProductoModel datos) {

        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"));

        validarProducto(datos);

        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setDisponible(datos.isDisponible());

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public ProductoModel cambiarDisponibilidad(Long id, boolean disponible) {

        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"));

        if (disponible && !producto.getRestaurante().isActivo()) {
            throw new IllegalStateException(
                    "No puedes habilitar productos de un restaurante inactivo");
        }

        producto.setDisponible(disponible);

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {

        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"));

        productoRepository.delete(producto);
    }

    private void validarProducto(ProductoModel producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios");
        }

        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio");
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero");
        }
    }
}
