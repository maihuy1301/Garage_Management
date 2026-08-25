package com.garage.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        String username = "admin_test";
        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // Already expired
        String token = jwtService.generateToken("admin_test");

        assertFalse(jwtService.validateToken(token));
    }
}
