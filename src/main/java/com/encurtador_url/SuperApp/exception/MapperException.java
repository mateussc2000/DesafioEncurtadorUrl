package com.encurtador_url.SuperApp.exception;

import lombok.Getter;

import static com.encurtador_url.SuperApp.enums.ErrorCodeEnum.ERRO_MAPEAMENTO;

@Getter
public class MapperException extends AbstractException {

    public MapperException(String message) {
        super(ERRO_MAPEAMENTO.getCodigo(), String.format(ERRO_MAPEAMENTO.getDescricao(), message));
    }

    public MapperException(String message, Throwable cause) {
        super(ERRO_MAPEAMENTO.getCodigo(), String.format(ERRO_MAPEAMENTO.getDescricao(),message), cause);
    }
}
