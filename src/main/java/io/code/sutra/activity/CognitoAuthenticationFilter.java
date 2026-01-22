package io.code.sutra.activity;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.propagation.Propagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;

public class CognitoAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthenticationFilter.class);
    private final String cognitoJwkUrl;
    private final String cognitoIssuer;
    private JWKSet jwkSet;

    @Autowired(required = false)
    private Tracer tracer;
    @Autowired(required = false)
    private Propagator propagator;

    public CognitoAuthenticationFilter(String cognitoJwkUrl, String cognitoIssuer) {
        this.cognitoJwkUrl = cognitoJwkUrl;
        this.cognitoIssuer = cognitoIssuer;
        if (cognitoJwkUrl == null || cognitoJwkUrl.isEmpty() || cognitoJwkUrl.contains("<")) {
            logger.error("COGNITO_JWK_URL is not set or invalid. Set the environment variable to a valid Cognito JWKs URL.");
            this.jwkSet = null;
            return;
        }
        try {
            this.jwkSet = JWKSet.load(new URL(cognitoJwkUrl));
        } catch (Exception e) {
            logger.error("Failed to load Cognito JWK set", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String activeProfile = System.getProperty("spring.profiles.active", System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", ""));
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = getJwtFromRequest(request);
        // Assign traceId, spanId, correlationId if not present
        String traceId = (tracer != null && tracer.currentSpan() != null) ? tracer.currentSpan().context().traceId() : null;
        String spanId = (tracer != null && tracer.currentSpan() != null) ? tracer.currentSpan().context().spanId() : null;
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null) {
            correlationId = java.util.UUID.randomUUID().toString();
        }
        if (tracer != null && tracer.currentSpan() == null) {
            Span span = tracer.nextSpan().name("http-request").start();
            tracer.withSpan(span);
            traceId = span.context().traceId();
            spanId = span.context().spanId();
        }
        org.slf4j.MDC.put("traceId", traceId != null ? traceId : "-");
        org.slf4j.MDC.put("spanId", spanId != null ? spanId : "-");
        org.slf4j.MDC.put("correlationId", correlationId);
        // Accept JWT token = "Dummy" only for local profile
        if ("Dummy".equals(jwt) && "local".equalsIgnoreCase(activeProfile)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "local-user", null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }
        if (StringUtils.hasText(jwt)) {
            try {
                SignedJWT signedJWT = SignedJWT.parse(jwt);
                JWK jwk = jwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID());
                if (jwk == null) {
                    logger.warn("JWK not found for key ID: {}", signedJWT.getHeader().getKeyID());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                RSAKey rsaKey = (RSAKey) jwk;
                if (!signedJWT.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(rsaKey))) {
                    logger.warn("JWT signature verification failed");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                String username = claims.getSubject();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ParseException e) {
                logger.warn("Invalid JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                logger.error("JWT validation error", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            logger.warn("Missing JWT token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            org.slf4j.MDC.remove("traceId");
            org.slf4j.MDC.remove("spanId");
            org.slf4j.MDC.remove("correlationId");
            if (tracer != null && tracer.currentSpan() != null) {
                tracer.currentSpan().end();
            }
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
