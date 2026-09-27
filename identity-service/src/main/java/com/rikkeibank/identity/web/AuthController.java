package com.rikkeibank.identity.web;
import com.rikkeibank.identity.service.AuthService; import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/auth") public class AuthController {private final AuthService service;public AuthController(AuthService s){service=s;}
 @PostMapping("/login") AuthService.TokenPair login(@Valid @RequestBody Login r){return service.login(r.username(),r.password());} @PostMapping("/refresh") AuthService.TokenPair refresh(@Valid @RequestBody Refresh r){return service.refresh(r.refreshToken());}
 @DeleteMapping("/sessions/{userId}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) void revoke(@PathVariable UUID userId){service.revoke(userId);} public record Login(@NotBlank String username,@NotBlank String password){} public record Refresh(@NotBlank String refreshToken){}
}

