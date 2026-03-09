package com.encurtador_url.SuperApp.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ShortCodeGenerator
 */
public class ShortCodeGeneratorTest {

    @Test
    public void testGenerateRandomCode() {
        String code = ShortCodeGenerator.generateRandomCode();

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(ShortCodeGenerator.isValidShortCode(code));
    }

    @Test
    public void testGenerateHashCode() {
        String code = ShortCodeGenerator.generateHashCode("https://www.example.com");

        assertNotNull(code);
        assertFalse(code.isEmpty());
        assertTrue(ShortCodeGenerator.isValidShortCode(code));
    }

    @Test
    public void testEncodeBase62() {
        assertEquals("0", ShortCodeGenerator.encodeBase62(0));
        assertEquals("1", ShortCodeGenerator.encodeBase62(1));
        assertEquals("A", ShortCodeGenerator.encodeBase62(10));
        assertEquals("Z", ShortCodeGenerator.encodeBase62(35));
        assertEquals("a", ShortCodeGenerator.encodeBase62(36));
        assertEquals("10", ShortCodeGenerator.encodeBase62(62));
    }

    @Test
    public void testDecodeBase62() {
        assertEquals(0, ShortCodeGenerator.decodeBase62("0"));
        assertEquals(1, ShortCodeGenerator.decodeBase62("1"));
        assertEquals(10, ShortCodeGenerator.decodeBase62("A"));
        assertEquals(35, ShortCodeGenerator.decodeBase62("Z"));
        assertEquals(36, ShortCodeGenerator.decodeBase62("a"));
        assertEquals(62, ShortCodeGenerator.decodeBase62("10"));
    }

    @Test
    public void testIsValidShortCode() {
        assertTrue(ShortCodeGenerator.isValidShortCode("abc123"));
        assertTrue(ShortCodeGenerator.isValidShortCode("ABC"));
        assertTrue(ShortCodeGenerator.isValidShortCode("123"));

        assertFalse(ShortCodeGenerator.isValidShortCode("ab"));  // muito curto
        assertFalse(ShortCodeGenerator.isValidShortCode("abc123XYZ01"));  // muito longo
        assertFalse(ShortCodeGenerator.isValidShortCode("abc-123"));  // caractere inválido
        assertFalse(ShortCodeGenerator.isValidShortCode(null));  // null
    }

    @Test
    public void testGenerateUniqueCodes() {
        var codes = ShortCodeGenerator.generateUniqueCodes(10);

        assertNotNull(codes);
        assertEquals(10, codes.size());

        // Verifica que todos são únicos
        var codeList = codes.stream().toList();
        assertEquals(codeList.size(), codeList.stream().distinct().count());
    }
}

