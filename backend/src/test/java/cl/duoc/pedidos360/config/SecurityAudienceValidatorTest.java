package cl.duoc.pedidos360.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityAudienceValidatorTest {

    private final SecurityConfig.AudienceValidator validator =
            new SecurityConfig.AudienceValidator("api://pedidos360-api", "backend-client-id");

    @Test
    void acceptsConfiguredAudienceUriAndClientId() {
        assertFalse(validator.validate(token("api://pedidos360-api")).hasErrors());
        assertFalse(validator.validate(token("backend-client-id")).hasErrors());
    }

    @Test
    void rejectsAudienceThatOnlyContainsExpectedProductName() {
        assertTrue(validator.validate(token("api://attacker-pedidos360.invalid")).hasErrors());
    }

    private Jwt token(String audience) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
    }
}