package com.rikkeibank.identity.domain;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity public class RefreshToken { @Id private UUID id; @Column(nullable=false,unique=true,length=64) private String tokenHash; @Column(nullable=false) private UUID userId; @Column(nullable=false) private Instant expiresAt; private Instant revokedAt; protected RefreshToken(){} public RefreshToken(String h,UUID u,Instant e){id=UUID.randomUUID();tokenHash=h;userId=u;expiresAt=e;} public String getTokenHash(){return tokenHash;} public UUID getUserId(){return userId;} public Instant getExpiresAt(){return expiresAt;} public boolean valid(){return revokedAt==null&&expiresAt.isAfter(Instant.now());} public void revoke(){revokedAt=Instant.now();} }

