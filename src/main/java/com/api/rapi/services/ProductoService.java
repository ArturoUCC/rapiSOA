package com.api.rapi.services;

import java.util.List;
import java.util.Optional;

import com.api.rapi.models.ProductoModel;

public interface ProductoService {

    List<ProductoModel> obtenerProductos();

    Optional<ProductoModel> buscarProductoPorId(Long id);

    List<ProductoModel> buscarProductosPorRestaurante(Long restauranteId);

    List<ProductoModel> obtenerProductosDisponibles(Long restauranteId);

    List<ProductoModel> buscarProductosPorNombre(String nombre);

    List<ProductoModel> buscarProductosPorNombreYRestaurante(
            Long restauranteId, String nombre);

    ProductoModel registrarProducto(ProductoModel producto);

    ProductoModel actualizarProducto(Long id, ProductoModel producto);

    ProductoModel cambiarDisponibilidad(Long id, boolean disponible);

    void eliminarProducto(Long id);

}
