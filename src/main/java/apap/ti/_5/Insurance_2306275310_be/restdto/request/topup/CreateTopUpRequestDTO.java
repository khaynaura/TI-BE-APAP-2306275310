package apap.ti._5.Insurance_2306275310_be.restdto.request.topup;

import lombok.Data;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) untuk memulai transaksi Top-Up saldo.
 * Berisi informasi siapa yang top-up, berapa jumlahnya, dan pakai metode apa.
 */
@Data
public class CreateTopUpRequestDTO {

    /**
     * UUID dari pengguna yang melakukan top-up.
     */
    private UUID endUserId;

    /**
     * Nominal uang yang akan ditambahkan.
     */
    private Long amount;

    /**
     * UUID dari metode pembayaran yang dipilih (PaymentMethod).
     */
    private UUID paymentMethodId;
}