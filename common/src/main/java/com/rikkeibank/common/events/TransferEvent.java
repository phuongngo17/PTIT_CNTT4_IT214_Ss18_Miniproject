package com.rikkeibank.common.events;

import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;

public record TransferEvent(UUID transactionId, UUID sourceAccountId, UUID targetAccountId, UUID customerId, BigDecimal amount, String status, String reason, Instant occurredAt) {}
