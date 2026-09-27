package com.rikkeibank.identity.config;
import com.rikkeibank.identity.domain.AppUser; import com.rikkeibank.identity.repo.UserRepository; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.*; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class SeedData {@Bean CommandLineRunner seedAdmin(UserRepository r,PasswordEncoder p){return a->{if(!r.existsByUsername("admin"))r.save(new AppUser("admin",p.encode("Admin@123"),AppUser.Role.ADMIN,"System Administrator",null));};}}

