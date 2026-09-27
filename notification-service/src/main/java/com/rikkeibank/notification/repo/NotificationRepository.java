package com.rikkeibank.notification.repo;import com.rikkeibank.notification.domain.Notification;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;public interface NotificationRepository extends JpaRepository<Notification,UUID>{List<Notification>findByCustomerIdOrderByCreatedAtDesc(UUID id);boolean existsByTransactionIdAndEventStatus(UUID id,String status);}

