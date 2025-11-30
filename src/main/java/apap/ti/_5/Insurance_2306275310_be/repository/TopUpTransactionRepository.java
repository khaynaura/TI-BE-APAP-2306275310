package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository untuk mengakses data {@link TopUpTransaction}.
 */
@Repository
public interface TopUpTransactionRepository extends JpaRepository<TopUpTransaction, UUID> {

    /**
     * Mengambil semua transaksi top-up milik user tertentu.
     *
     * @param endUserId ID User.
     * @return List transaksi top-up.
     */
    List<TopUpTransaction> findAllByEndUserId(UUID endUserId);
}