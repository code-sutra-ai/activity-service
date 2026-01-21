package io.code.sutra.activity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import javax.crypto.SecretKey;

/**
 * JWT Authentication Filter for validating JWT tokens on every request.
 *
 * <p>To generate a JWT token for testing, you can use the following Java code:
 * <pre>
 * String jwt = Jwts.builder()
 *     .subject("username")
 *     .signWith(Keys.hmacShaKeyFor("your_jwt_secret_key".getBytes(StandardCharsets.UTF_8)))
 *     .compact();
 * </pre>
 * Or use https://jwt.io/ with the secret key from your config.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final String jwtSecret;
    private final SecretKey secretKey;

    public JwtAuthenticationFilter() {
        String envSecret = System.getenv().getOrDefault("JWT_SECRET", "your_jwt_secret_key");
        // Ensure the key is at least 32 chars (256 bits) for HS256
        if (envSecret.length() < 32) {
            logger.warn("JWT secret too short for HS256, using fallback test key. Set JWT_SECRET to a secure 32+ char value in production!");
            this.jwtSecret = "0123456789abcdef0123456789abcdef"; // 32 chars
        } else {
            this.jwtSecret = envSecret;
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Generate or propagate correlation, trace, and span IDs for observability
        String correlationId = getOrCreateHeader(request, "X-Correlation-Id");
        String traceId = getOrCreateHeader(request, "X-B3-TraceId");
        String spanId = getOrCreateHeader(request, "X-B3-SpanId");
        MDC.put("correlationId", correlationId);
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();
                String username = claims.getSubject();
                if (username != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("JWT validated for user: {} [correlationId={}, traceId={}, spanId={}]", username, correlationId, traceId, spanId);
                }
            } catch (Exception ex) {
                logger.warn("Invalid JWT token: {} [correlationId={}, traceId={}, spanId={}]", ex.getMessage(), correlationId, traceId, spanId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT token");
                return;
            }
        } else {
            logger.warn("Missing JWT token [correlationId={}, traceId={}, spanId={}]", correlationId, traceId, spanId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing JWT token");
            return;
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getOrCreateHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            value = UUID.randomUUID().toString();
        }
        return value;
    }
}
