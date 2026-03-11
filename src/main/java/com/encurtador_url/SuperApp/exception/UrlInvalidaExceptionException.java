package com.encurtador_url.SuperApp.exception;

import lombok.Getter;

import static com.encurtador_url.SuperApp.enums.ErrorCodeEnum.ERRO_URL_INVALIDA;

/**
 * Exceção para URL inválida
 */
@Getter
public class UrlInvalidaExceptionException extends AbstractException {
    public UrlInvalidaExceptionException() {
        super(ERRO_URL_INVALIDA.getCodigo(), String.format(ERRO_URL_INVALIDA.getDescricao()));
    }

    public UrlInvalidaExceptionException(String message) {
        super(ERRO_URL_INVALIDA.getCodigo(), message);
    }

    public UrlInvalidaExceptionException(Throwable cause) {
        super(ERRO_URL_INVALIDA.getCodigo(), String.format(ERRO_URL_INVALIDA.getDescricao()), cause);
    }

    public UrlInvalidaExceptionException(String message, Throwable cause) {
        super(ERRO_URL_INVALIDA.getCodigo(), message, cause);
    }
}

