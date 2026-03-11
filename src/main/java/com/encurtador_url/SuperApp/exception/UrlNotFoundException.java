package com.encurtador_url.SuperApp.exception;

import static com.encurtador_url.SuperApp.enums.ErrorCodeEnum.ERRO_URL_NAO_ENCONTRADA;

public class UrlNotFoundException extends AbstractException {

    public UrlNotFoundException() {
        super(ERRO_URL_NAO_ENCONTRADA.getCodigo(), String.format(ERRO_URL_NAO_ENCONTRADA.getDescricao()));
    }

    public UrlNotFoundException(Throwable cause) {
        super(ERRO_URL_NAO_ENCONTRADA.getCodigo(), String.format(ERRO_URL_NAO_ENCONTRADA.getDescricao()), cause);
    }
}
