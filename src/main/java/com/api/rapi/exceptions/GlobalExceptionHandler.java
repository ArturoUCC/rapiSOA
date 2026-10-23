package com.api.rapi.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: recurso inexistente
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                "RECURSO_NO_ENCONTRADO",
                ex.getMessage(),
                request);
    }

    // 400: argumentos invalidos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "DATOS_INVALIDOS",
                ex.getMessage(),
                request);
    }

    // 409: conflicto con el estado del recurso
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarEstadoInvalido(
            IllegalStateException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                "CONFLICTO_ESTADO",
                ex.getMessage(),
                request);
    }

    // 409: conflicto con restricciones de MySQL
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                "CONFLICTO_DATOS",
                "La operacion entra en conflicto con los datos existentes",
                request);
    }

    // 400: parametro obligatorio faltante
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> manejarParametroFaltante(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_OBLIGATORIO",
                "Falta el parametro: " + ex.getParameterName(),
                request);
    }

    // 400: tipo de parametro incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> manejarTipoInvalido(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "TIPO_DATO_INVALIDO",
                "Valor invalido para el parametro: " + ex.getName(),
                request);
    }

    // 400: cuerpo JSON invalido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "JSON_INVALIDO",
                "El cuerpo JSON de la solicitud no es valido",
                request);
    }

    // 400: validaciones de campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + ": "
                        + error.getDefaultMessage())
                .orElse("Los datos de la solicitud no son validos");

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "VALIDACION_FALLIDA",
                mensaje,
                request);
    }

    // 404: ruta inexistente
    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<Map<String, Object>> manejarRutaInexistente(
            Exception ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                "RUTA_NO_ENCONTRADA",
                "La ruta solicitada no existe",
                request);
    }

    // 500: error inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        logger.error(
                "Error interno en la ruta {}",
                request.getRequestURI(),
                ex);

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrio un error interno en el servidor",
                request);
    }

    // Formato comun de errores de RapiEntrega
    private ResponseEntity<Map<String, Object>> crearRespuesta(
            HttpStatus estado,
            String codigo,
            String mensaje,
            HttpServletRequest request) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("status", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("codigo", codigo);
        respuesta.put("mensaje", mensaje);
        respuesta.put("ruta", request.getRequestURI());

        return ResponseEntity
                .status(estado)
                .body(respuesta);
    }
}
