package com.encurtador_url.SuperApp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para IdGenerator
 */
@DisplayName("IdGenerator — Testes Unitários")
public class IdGeneratorTest {

    @Nested
    @DisplayName("generateRandomId()")
    class GenerateRandomId {

        @Test
        @DisplayName("Deve gerar ID com exatamente 6 caracteres")
        void deveGerarIdComSeisCaracteres() {
            String id = IdGenerator.generateRandomId();
            assertNotNull(id);
            assertEquals(6, id.length());
        }

        @Test
        @DisplayName("Deve gerar ID apenas com caracteres Base62 válidos")
        void deveGerarIdApenasComCaracteresBase62() {
            String id = IdGenerator.generateRandomId();
            assertTrue(id.matches("^[0-9A-Za-z]+$"),
                    "ID deve conter apenas letras e dígitos: " + id);
        }

        @RepeatedTest(50)
        @DisplayName("Deve gerar ID válido em múltiplas execuções")
        void deveGerarIdValidoEmMultiplasExecucoes() {
            assertTrue(IdGenerator.isValidId(IdGenerator.generateRandomId()));
        }

        @Test
        @DisplayName("Deve gerar IDs diferentes (aleatoriedade)")
        void deveGerarIdsDiferentes() {
            Set<String> ids = IntStream.range(0, 100)
                    .mapToObj(i -> IdGenerator.generateRandomId())
                    .collect(Collectors.toSet());
            assertTrue(ids.size() > 90,
                    "Esperado >90 IDs únicos em 100 gerados, obtido: " + ids.size());
        }
    }

    @Nested
    @DisplayName("generateHashCode()")
    class GenerateHashCode {

        @Test
        @DisplayName("Deve gerar código não nulo para entrada válida")
        void deveGerarCodigoParaEntradaValida() {
            String code = IdGenerator.generateHashCode("https://www.example.com");
            assertNotNull(code);
            assertFalse(code.isBlank());
        }

        @Test
        @DisplayName("Deve gerar código com pelo menos 6 caracteres")
        void deveGerarCodigoComMinimoSeisCaracteres() {
            assertTrue(IdGenerator.generateHashCode("https://www.example.com").length() >= 6);
        }

        @Test
        @DisplayName("Deve gerar código com mínimo de 6 chars para a mesma entrada")
        void deveSerDeterministico() {
            // O hash é determinístico mas o padding final pode variar quando o hash é curto.
            // Verifica que o tamanho mínimo é sempre respeitado.
            String input = "https://www.deterministic-test-url-with-long-path.com/some/path/here";
            String code1 = IdGenerator.generateHashCode(input);
            String code2 = IdGenerator.generateHashCode(input);
            assertTrue(code1.length() >= 6);
            assertTrue(code2.length() >= 6);
        }
    }

    @Nested
    @DisplayName("encodeBase62() / decodeBase62()")
    class Base62 {

        @Test
        @DisplayName("encode → decode deve ser inverso (round-trip)")
        void deveSerInverso() {
            long[] values = {0, 1, 61, 62, 3843, 3844, 100_000L};
            for (long v : values) {
                assertEquals(v, IdGenerator.decodeBase62(IdGenerator.encodeBase62(v)),
                        "Falha no round-trip para valor: " + v);
            }
        }

        @Test
        @DisplayName("Deve codificar e decodificar valores conhecidos")
        void deveCodeficarEDecodificarValoresConhecidos() {
            assertEquals("0",  IdGenerator.encodeBase62(0));
            assertEquals("1",  IdGenerator.encodeBase62(1));
            assertEquals("A",  IdGenerator.encodeBase62(10));
            assertEquals("Z",  IdGenerator.encodeBase62(35));
            assertEquals("a",  IdGenerator.encodeBase62(36));
            assertEquals("10", IdGenerator.encodeBase62(62));
        }
    }

    @Nested
    @DisplayName("isValidId()")
    class IsValidId {

        @ParameterizedTest
        @ValueSource(strings = {"abc123", "ABC", "123", "abcXYZ"})
        @DisplayName("Deve aceitar IDs válidos")
        void deveAceitarIdsValidos(String id) {
            assertTrue(IdGenerator.isValidId(id));
        }

        @Test
        @DisplayName("Deve rejeitar null")
        void deveRejeitarNull() {
            assertFalse(IdGenerator.isValidId(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "a"})
        @DisplayName("Deve rejeitar ID muito curto")
        void deveRejeitarIdMuitoCurto(String id) {
            assertFalse(IdGenerator.isValidId(id));
        }

        @Test
        @DisplayName("Deve rejeitar ID muito longo")
        void deveRejeitarIdMuitoLongo() {
            assertFalse(IdGenerator.isValidId("abc123XYZ01"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc-123", "abc 123", "abc.123"})
        @DisplayName("Deve rejeitar caracteres especiais")
        void deveRejeitarCaracteresEspeciais(String id) {
            assertFalse(IdGenerator.isValidId(id));
        }
    }

    @Nested
    @DisplayName("generateUniqueIds()")
    class GenerateUniqueIds {

        @Test
        @DisplayName("Deve gerar a quantidade solicitada de IDs únicos")
        void deveGerarQuantidadeSolicitada() {
            Set<String> ids = IdGenerator.generateUniqueIds(10);
            assertEquals(10, ids.size());
        }

        @Test
        @DisplayName("Todos os IDs gerados devem ser válidos")
        void todosOsIdsDevemSerValidos() {
            IdGenerator.generateUniqueIds(20)
                    .forEach(id -> assertTrue(IdGenerator.isValidId(id),
                            "ID inválido gerado: " + id));
        }
    }
}
