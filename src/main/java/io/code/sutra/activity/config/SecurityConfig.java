package io.code.sutra.activity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${spring.security.oauth2.client.provider.cognito.issuerUri:}")
    private String jwkUrl;

    @Autowired
    private Environment env;

    // Dummy token properties for local/test convenience
    @Value("${app.security.dummy-token:Dummy}")
    private String dummyToken;

    @Value("${app.security.dummy-user:local-user}")
    private String dummyUser;

    @Value("${app.security.dummy-roles:ROLE_USER}")
    private String dummyRoles;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            );
        String[] profiles = env.getActiveProfiles();
        boolean isLocal = java.util.Arrays.asList(profiles).contains("local");
        boolean isTest = java.util.Arrays.asList(profiles).contains("test");
        if (!isLocal && !isTest && jwkUrl != null && !jwkUrl.isEmpty() && !jwkUrl.contains("dummy-jwk")) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri(jwkUrl)
                )
            );
        } else {
            // Accept any token for local/test/dev or if JWK URL is not set — JwtDecoder controls behavior
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        }
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String[] profiles = env.getActiveProfiles();
        boolean isLocal = java.util.Arrays.asList(profiles).contains("local");
        boolean isTest = java.util.Arrays.asList(profiles).contains("test");
        if (!isLocal && !isTest && jwkUrl != null && !jwkUrl.isEmpty() && !jwkUrl.contains("dummy-jwk")) {
            return NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
        }
        // Local/test: return a decoder that recognizes the configured dummy token explicitly and
        // constructs a Jwt with useful claims; if the token is different, keep the previous tolerant behavior
        return token -> {
            try {
                if (token != null && token.equals(dummyToken)) {
                    Map<String, Object> headers = new HashMap<>();
                    headers.put("alg", "none");

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("sub", dummyUser);
                    claims.put("preferred_username", dummyUser);
                    claims.put("roles", dummyRoles);
                    claims.put("iat", Instant.now().getEpochSecond());
                    claims.put("exp", Instant.now().plusSeconds(60 * 60).getEpochSecond());

                    return org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                        .headers(h -> h.putAll(headers))
                        .claims(c -> c.putAll(claims))
                        .build();
                }
            } catch (Exception e) {
                // fall through to tolerant default below
            }
            // Tolerant fallback used previously: accept any token and return a dummy jwt with sub=dummy-user
            return org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", dummyUser)
                .build();
        };
    }
}
