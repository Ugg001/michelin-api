package M2fst.michelin.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            @Value("${app.security.jwt-enabled:false}") boolean jwtEnabled) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        if (jwtEnabled) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/h2-console/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/restaurant/*/evaluation").authenticated()
                    .requestMatchers(HttpMethod.GET, "/evaluation/mine").authenticated()
                    .anyRequest().permitAll());
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    // @PreAuthorize n'est actif que si on lance avec le profil oidc.
    @Configuration
    @ConditionalOnProperty(name = "app.security.jwt-enabled", havingValue = "true")
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }
}
