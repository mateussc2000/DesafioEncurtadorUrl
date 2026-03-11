package com.encurtador_url.SuperApp.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para IdGenerator
 */
public class IdGeneratorTest {

    @Test
    public void testGenerateRandomId() {
        String code = IdGenerator.generateRandomId();

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(IdGenerator.isValidId(code));
    }

    @Test
    public void testGenerateHashCode() {
        String code = IdGenerator.generateHashCode("https://www.example.com");

        assertNotNull(code);
        assertFalse(code.isEmpty());
        assertTrue(IdGenerator.isValidId(code));
    }

    @Test
    public void testEncodeBase62() {
        assertEquals("0", IdGenerator.encodeBase62(0));
        assertEquals("1", IdGenerator.encodeBase62(1));
        assertEquals("A", IdGenerator.encodeBase62(10));
        assertEquals("Z", IdGenerator.encodeBase62(35));
        assertEquals("a", IdGenerator.encodeBase62(36));
        assertEquals("10", IdGenerator.encodeBase62(62));
    }

    @Test
    public void testDecodeBase62() {
        assertEquals(0, IdGenerator.decodeBase62("0"));
        assertEquals(1, IdGenerator.decodeBase62("1"));
        assertEquals(10, IdGenerator.decodeBase62("A"));
        assertEquals(35, IdGenerator.decodeBase62("Z"));
        assertEquals(36, IdGenerator.decodeBase62("a"));
        assertEquals(62, IdGenerator.decodeBase62("10"));
    }

    @Test
    public void testIsValidId() {
        assertTrue(IdGenerator.isValidId("abc123"));
        assertTrue(IdGenerator.isValidId("ABC"));
        assertTrue(IdGenerator.isValidId("123"));

        assertFalse(IdGenerator.isValidId("ab"));  // muito curto
        assertFalse(IdGenerator.isValidId("abc123XYZ01"));  // muito longo
        assertFalse(IdGenerator.isValidId("abc-123"));  // caractere inválido
        assertFalse(IdGenerator.isValidId(null));  // null
    }

    @Test
    public void testGenerateUniqueCodes() {
        var codes = IdGenerator.generateUniqueIds(10);

        assertNotNull(codes);
        assertEquals(10, codes.size());

        // Verifica que todos são únicos
        var codeList = codes.stream().toList();
        assertEquals(codeList.size(), codeList.stream().distinct().count());
    }
}
