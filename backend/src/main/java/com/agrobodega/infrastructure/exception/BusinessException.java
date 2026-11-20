package com.agrobodega.infrastructure.exception;

/**
 * Error de negocio identificado por un codigo del catalogo de mensajes
 * (por ejemplo PRODUCT_NOT_FOUND). El texto se busca en la base de datos.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code) {
        super(code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
