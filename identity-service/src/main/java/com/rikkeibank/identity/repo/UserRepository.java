package com.rikkeibank.identity.repo; import com.rikkeibank.identity.domain.AppUser; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<AppUser,UUID>{Optional<AppUser> findByUsername(String username);boolean existsByUsername(String username);}

