package com.encurtador_url.SuperApp.enums;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    ERRO_PARAMETRO_INVALIDO("BFF00002", "Parâmetro inválido: %s"),
    ERRO_EXECUCAO_QUERY("BFF00003", "Erro ao executar a query: %s"),

    ERRO_CONEXAO_BANCO_DADOS("BFF00100", "Erro de conexão com o banco de dados: %s"),

    ERRO_MAPEAMENTO("BFF00200", "Erro ao mapear resultados: %s"),
    ERRO_CONVERSAO_DADOS("BFF00201", "Erro ao converter dados: %s"),

    ERRO_TOKEN("BFF00300", "Erro ao obter token"),

    ERRO_API_KEY_AUSENTE("BFF00501", "Header X-API-Key é obrigatório"),
    ERRO_API_KEY_INVALIDA("BFF00502", "X-API-Key inválida"),

    ERRO_URL_EXPIRADA("BFF00401", "URL expirada"),
    ERRO_URL_NAO_ENCONTRADA("BFF00402", "URL não encontrada"),
    ERRO_URL_INVALIDA("BFF00403", "URL inválida"),

    ERRO_SISTEMICO("BFF99999", "Erro interno do servidor");

    private final String codigo;
    private final String descricao;

    ErrorCodeEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return String.join(codigo," - ", descricao, " ");
    }
}
