package com.rikkeibank.gateway;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.security.config.web.server.ServerHttpSecurity; import org.springframework.security.oauth2.jwt.*; import org.springframework.security.oauth2.server.resource.authentication.*; import org.springframework.security.web.server.SecurityWebFilterChain; import javax.crypto.spec.SecretKeySpec;
@Configuration public class GatewaySecurityConfig {
 @Bean ReactiveJwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret){ return NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(),"HmacSHA256")).build(); }
 @Bean SecurityWebFilterChain security(ServerHttpSecurity http){ return http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(x->x.pathMatchers("/api/auth/**","/actuator/health").permitAll().anyExchange().authenticated()).oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(converter()))).build(); }
 private ReactiveJwtAuthenticationConverter converter(){ JwtGrantedAuthoritiesConverter g=new JwtGrantedAuthoritiesConverter(); g.setAuthoritiesClaimName("roles"); g.setAuthorityPrefix("ROLE_"); ReactiveJwtAuthenticationConverter c=new ReactiveJwtAuthenticationConverter(); c.setJwtGrantedAuthoritiesConverter(new ReactiveJwtGrantedAuthoritiesConverterAdapter(g)); return c; }
}

