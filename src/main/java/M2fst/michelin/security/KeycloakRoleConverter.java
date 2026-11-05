package M2fst.michelin.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// La doc Spring Security dit de convertir les claims du JWT en GrantedAuthority, avec le préfixe ROLE_ pour hasRole.
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            for (Object role : roles) {
                this.addRole(authorities, String.valueOf(role));
            }
        }
        Object roleClaim = jwt.getClaim("role");
        if (roleClaim instanceof String text) {
            this.addRole(authorities, text);
        } else if (roleClaim instanceof List<?> roles) {
            for (Object role : roles) {
                this.addRole(authorities, String.valueOf(role));
            }
        }
        return authorities;
    }

    private void addRole(List<GrantedAuthority> authorities, String role) {
        if (role == null || role.isBlank()) {
            return;
        }
        String name = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        authorities.add(new SimpleGrantedAuthority(name));
    }
}
