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

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${COGNITO_JWK_URL}")
    private String jwkUrl;

    @Autowired
    private Environment env;

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
            // Accept any token for local/test/dev or if JWK URL is not set
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
        // Accept any token for local, test, or if JWK URL is not set
        return token -> org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("sub", "dummy-user")
            .build();
    }
}
