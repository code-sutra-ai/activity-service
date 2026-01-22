package io.code.sutra.activity.config;

import io.code.sutra.activity.CognitoAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public CognitoAuthenticationFilter cognitoAuthenticationFilter() {
        // Use empty string defaults for local dev, require env vars for production
        String jwkUrl = System.getenv().getOrDefault("COGNITO_JWK_URL", "");
        String issuer = System.getenv().getOrDefault("COGNITO_ISSUER", "");
        return new CognitoAuthenticationFilter(jwkUrl, issuer);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CognitoAuthenticationFilter cognitoAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(cognitoAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
