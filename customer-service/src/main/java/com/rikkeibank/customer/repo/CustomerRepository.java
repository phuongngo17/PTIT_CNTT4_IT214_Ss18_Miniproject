package com.rikkeibank.customer.repo; import com.rikkeibank.customer.domain.Customer; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID; public interface CustomerRepository extends JpaRepository<Customer,UUID>{boolean existsByEmail(String e);boolean existsByNationalId(String id);}

