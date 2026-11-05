package M2fst.michelin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

// Même lecture du token que dans le TP de demo (preferred_username, sinon name, sinon sub).
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static String displayName(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Jwt jwt = jwtAuthentication.getToken();
            String preferred = jwt.getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            String name = jwt.getClaimAsString("name");
            if (name != null && !name.isBlank()) {
                return name;
            }
            return jwt.getSubject();
        }
        return authentication.getName();
    }

    public static String subject(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken().getSubject();
        }
        return authentication.getName();
    }
}
