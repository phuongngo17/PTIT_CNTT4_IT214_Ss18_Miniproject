package com.rikkeibank.gateway;
import org.springframework.core.Ordered; import org.springframework.data.redis.core.ReactiveStringRedisTemplate; import org.springframework.http.*; import org.springframework.security.core.context.ReactiveSecurityContextHolder; import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; import org.springframework.stereotype.Component; import org.springframework.web.server.*; import reactor.core.publisher.Mono;
@Component public class RevokedUserFilter implements WebFilter, Ordered {
 private final ReactiveStringRedisTemplate redis; public RevokedUserFilter(ReactiveStringRedisTemplate redis){this.redis=redis;}
 public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){ return ReactiveSecurityContextHolder.getContext().map(c->c.getAuthentication()).filter(JwtAuthenticationToken.class::isInstance).cast(JwtAuthenticationToken.class).flatMap(a->redis.hasKey("auth:revoked:"+a.getToken().getSubject()).flatMap(revoked->{ if(Boolean.TRUE.equals(revoked)){exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);return exchange.getResponse().setComplete();} return chain.filter(exchange);})).switchIfEmpty(chain.filter(exchange)); }
 public int getOrder(){return Ordered.LOWEST_PRECEDENCE-10;}
}

