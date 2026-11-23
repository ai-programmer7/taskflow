
package com.taskflow.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {

                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth

                                                // Public endpoints
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api/health")
                                                .permitAll()

                                                // User registration is public
                                                .requestMatchers(HttpMethod.POST, "/api/users")
                                                .permitAll()

                                                // Only ADMIN can view and delete users
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/users",
                                                                "/api/users/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/users/**")
                                                .hasRole("ADMIN")

                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter())));

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder(
                        @Value("${app.jwt.secret}") String secret) {

                SecretKey secretKey = new SecretKeySpec(
                                secret.getBytes(StandardCharsets.UTF_8),
                                "HmacSHA256");

                return NimbusJwtDecoder.withSecretKey(secretKey)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

                authoritiesConverter.setAuthoritiesClaimName("roles");
                authoritiesConverter.setAuthorityPrefix("");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

                return converter;
        }
}
