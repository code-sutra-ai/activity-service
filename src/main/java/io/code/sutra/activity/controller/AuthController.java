package io.code.sutra.activity.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Endpoint for generating JWT tokens for testing/demo purposes.
 * Not for production use.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final String jwtSecret;
    private final SecretKey key;

    public AuthController() {
        String envSecret = System.getenv().getOrDefault("JWT_SECRET", "your_jwt_secret_key");
        if (envSecret.length() < 32) {
            this.jwtSecret = "0123456789abcdef0123456789abcdef";
        } else {
            this.jwtSecret = envSecret;
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for a given username.
     * Example: POST /auth/token { "username": "testuser" }
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateToken(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "testuser");
        String jwt = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}
