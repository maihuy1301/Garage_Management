package com.garage.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordGeneratorTest {

    @Test
    void generateAndVerifyBCryptPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Password123@";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("RAW_PASSWORD: " + rawPassword);
        System.out.println("BCRYPT_HASH: " + encodedPassword);

        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}
