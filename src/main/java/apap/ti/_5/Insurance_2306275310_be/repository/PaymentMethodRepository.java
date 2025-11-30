package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository untuk mengakses data {@link PaymentMethod}.
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    // Method bawaan JpaRepository sudah cukup untuk CRUD standar.
}