package com.encurtador_url.SuperApp.exception;

import com.encurtador_url.SuperApp.enums.ErrorCodeEnum;
import lombok.Getter;

import static com.encurtador_url.SuperApp.enums.ErrorCodeEnum.ERRO_CONEXAO_BANCO_DADOS;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class RepositoryException extends AbstractException {

    @Getter
    private Integer httpRepositoryErrorStatusCode;
    private String additionalErrorMessage;

    public RepositoryException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getCodigo(), errorCodeEnum.getDescricao(), cause);
    }

    public RepositoryException(ErrorCodeEnum errorCodeEnum, Throwable cause, String additionalErrorMessage) {
        super(errorCodeEnum.getCodigo(), errorCodeEnum.getDescricao(), cause);
        this.additionalErrorMessage = additionalErrorMessage;
    }

    public RepositoryException(ErrorCodeEnum errorCodeEnum, Integer httpRepositoryErrorStatusCode) {
        super(errorCodeEnum.getCodigo(), errorCodeEnum.getDescricao());
        this.httpRepositoryErrorStatusCode = httpRepositoryErrorStatusCode;
    }

    public RepositoryException(ErrorCodeEnum errorCodeEnum, String message, Throwable cause) {
        super(errorCodeEnum.getCodigo(), String.format(errorCodeEnum.getDescricao(), message), cause);
    }

    public RepositoryException(ErrorCodeEnum errorCodeEnum, String additionalErrorMessage) {
        super(errorCodeEnum.getCodigo(), errorCodeEnum.getDescricao());
        this.additionalErrorMessage = additionalErrorMessage;
    }

    public RepositoryException(String message) {
        super(ERRO_CONEXAO_BANCO_DADOS.getCodigo(), String.format(ERRO_CONEXAO_BANCO_DADOS.getDescricao(), message));
    }

    public RepositoryException(String message, Throwable cause) {
        super(ERRO_CONEXAO_BANCO_DADOS.getCodigo(), String.format(ERRO_CONEXAO_BANCO_DADOS.getDescricao(), message), cause);
    }

    public boolean hasRepositoryErrorStatusCode() {
        return httpRepositoryErrorStatusCode != null;
    }

    public String getAdditionalErrorMessage() {
        return ". " + trimToEmpty(additionalErrorMessage);
    }
}
