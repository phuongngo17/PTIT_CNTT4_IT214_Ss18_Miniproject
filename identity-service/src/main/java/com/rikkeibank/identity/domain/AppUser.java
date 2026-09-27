package com.rikkeibank.identity.domain;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="app_users",uniqueConstraints=@UniqueConstraint(columnNames="username")) public class AppUser {
 @Id private UUID id; @Column(nullable=false) private String username; @Column(nullable=false) private String passwordHash; @Column(nullable=false) @Enumerated(EnumType.STRING) private Role role; @Column(nullable=false) private boolean active=true; private UUID customerId; private String fullName; private Instant createdAt;
 protected AppUser(){} public AppUser(String u,String p,Role r,String n,UUID c){id=UUID.randomUUID();username=u;passwordHash=p;role=r;fullName=n;customerId=c;createdAt=Instant.now();}
 public UUID getId(){return id;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;} public boolean isActive(){return active;} public UUID getCustomerId(){return customerId;} public String getFullName(){return fullName;} public Instant getCreatedAt(){return createdAt;}
 public void update(String n,Role r,boolean a,UUID c){fullName=n;role=r;active=a;customerId=c;} public void setPasswordHash(String v){passwordHash=v;}
 public enum Role { ADMIN,TELLER,CUSTOMER }
}

