package M2fst.michelin.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test
    void should_prefix_realm_roles() {
        Jwt jwt = this.jwt(Map.of("roles", List.of("USER", "ADMIN")));

        Collection<GrantedAuthority> authorities = this.converter.convert(jwt);

        assertEquals(2, authorities.size());
        assertTrue(this.has(authorities, "ROLE_USER"));
        assertTrue(this.has(authorities, "ROLE_ADMIN"));
    }

    @Test
    void should_read_a_simple_role_claim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user")
                .claim("role", "USER")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        Collection<GrantedAuthority> authorities = this.converter.convert(jwt);

        assertEquals(1, authorities.size());
        assertTrue(this.has(authorities, "ROLE_USER"));
    }

    @Test
    void should_return_nothing_when_the_token_has_no_role() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        assertTrue(this.converter.convert(jwt).isEmpty());
    }

    private Jwt jwt(Map<String, Object> realmAccess) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user")
                .claim("realm_access", realmAccess)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }

    private boolean has(Collection<GrantedAuthority> authorities, String name) {
        for (GrantedAuthority authority : authorities) {
            if (name.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
