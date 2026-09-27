package com.rikkeibank.account.repo;

import com.rikkeibank.account.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountTypeRepository extends JpaRepository<AccountType, UUID> {
  boolean existsByCode(String code);
}
