package com.api.rapi.exceptions;

public class RecursoNoEncontradoException
        extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(
            String recurso,
            Long id) {

        super(recurso + " con ID " + id + " no existe");
    }
}
