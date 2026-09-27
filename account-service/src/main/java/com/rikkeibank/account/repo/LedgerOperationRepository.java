package com.rikkeibank.account.repo;

import com.rikkeibank.account.domain.LedgerOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerOperationRepository extends JpaRepository<LedgerOperation, UUID> {
  boolean existsByOperationIdAndKind(UUID operationId, String kind);
}
