package com.encurtador_url.SuperApp.exception;

import static com.encurtador_url.SuperApp.enums.ErrorCodeEnum.ERRO_PARAMETRO_INVALIDO;

public class ValidationException extends AbstractException {

    public  ValidationException(String parametro) {
        super(ERRO_PARAMETRO_INVALIDO.getCodigo(), String.format(ERRO_PARAMETRO_INVALIDO.getDescricao(), parametro));
    }
    public ValidationException(String parametro, Throwable cause) {
        super(ERRO_PARAMETRO_INVALIDO.getCodigo(), String.format(ERRO_PARAMETRO_INVALIDO.getDescricao(), parametro), cause);
    }
}
