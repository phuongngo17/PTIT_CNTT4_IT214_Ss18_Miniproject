package com.rikkeibank.identity.repo; import com.rikkeibank.identity.domain.RefreshToken; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.transaction.Transactional; import java.util.*;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID>{Optional<RefreshToken> findByTokenHash(String hash);@Transactional @Modifying @Query("delete from RefreshToken r where r.userId=:userId") void deleteByUserId(@Param("userId") UUID userId);}

