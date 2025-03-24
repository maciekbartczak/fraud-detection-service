package dev.b6k.fds;

import io.smallrye.jwt.build.Jwt;

import java.time.Duration;
import java.time.Instant;

public class JwtGenerator {
    public static void main(String[] args) {
        System.out.println(generateToken());
    }

    public static String generateToken() {
        return Jwt
                .issuer("fraud-detection-service")
                .subject("user")
                .groups("api")
                .expiresAt(Instant.now().plus(Duration.ofDays(1L)))
                .sign();
    }
}