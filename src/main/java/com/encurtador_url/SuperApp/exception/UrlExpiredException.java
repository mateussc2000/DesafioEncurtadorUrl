package com.encurtador_url.SuperApp.exception;

import static com.encurtador_url.SuperApp.enums.ErrorCodeEnum.ERRO_URL_EXPIRADA;

/**
 * Exceção para URL expirada
 */
public class UrlExpiredException extends AbstractException {

    public UrlExpiredException() {
        super(ERRO_URL_EXPIRADA.getCodigo(), String.format(ERRO_URL_EXPIRADA.getDescricao()));
    }

    public UrlExpiredException(Throwable cause) {
        super(ERRO_URL_EXPIRADA.getCodigo(), String.format(ERRO_URL_EXPIRADA.getDescricao(), cause));
    }
}

