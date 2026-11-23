package com.taskflow.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwsHeader;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.List;
import com.taskflow.entity.User;
import com.taskflow.entity.Role;

@Service
public class JwtService {

        private final JwtEncoder jwtEncoder;

        public JwtService(@Value("${app.jwt.secret}") String secret) {
                SecretKey secretKey = new SecretKeySpec(
                                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                "HmacSHA256");

                this.jwtEncoder = new NimbusJwtEncoder(
                                new ImmutableSecret<>(secretKey));
        }

        public String generateToken(User user) {
                Instant now = Instant.now();

                List<String> roles = user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .toList();

                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer("taskflow")
                                .issuedAt(now)
                                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                                .subject(user.getUsername())
                                .claim("roles", roles)
                                .build();

                JwtEncoderParameters parameters = JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(),
                                claims);

                return jwtEncoder.encode(parameters).getTokenValue();
        }

}
