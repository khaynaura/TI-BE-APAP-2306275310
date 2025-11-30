package apap.ti._5.Insurance_2306275310_be.restdto.request.policy;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) untuk pembuatan Polis (Policy) baru.
 * Objek ini mengumpulkan data transaksi pemesanan (booking) dan daftar paket asuransi yang dipilih.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePolicyRequestDTO {

    /**
     * ID pengguna (Customer) yang membeli polis.
     */
    @NotBlank(message = "User ID must not be empty")
    private String userId;

    /**
     * ID transaksi eksternal (misal: ID Booking Tiket) yang diasuransikan.
     */
    @NotBlank(message = "Booking ID must not be empty")
    private String bookingId;

    /**
     * Jenis layanan utama (contoh: FLIGHT, HOTEL).
     */
    @NotNull(message = "Service must not be null")
    private ServiceEnum service;

    /**
     * Daftar ID paket asuransi (Insurance Plan) yang dipilih oleh user.
     * Minimal harus memilih satu paket.
     */
    @NotEmpty(message = "Choose at least one insurance plan")
    private List<String> insurancePlanIds;
}