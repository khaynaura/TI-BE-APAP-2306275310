package apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) untuk pembuatan paket asuransi baru.
 * Berisi seluruh informasi spesifikasi paket yang akan ditawarkan oleh provider.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInsurancePlanRequestDTO {

    /**
     * ID dari provider yang menerbitkan paket asuransi ini.
     */
    @NotBlank(message = "Provider ID must not be empty")
    private String providerId;

    /**
     * Nama paket asuransi yang akan ditampilkan.
     */
    @NotBlank(message = "Plan name must not be empty")
    private String planName;

    /**
     * Harga premi yang harus dibayar untuk membeli paket ini.
     * Harus bernilai positif (minimal 1).
     */
    @NotNull(message = "Price must not be null")
    @Min(value = 1, message = "Price must be positive")
    private Integer price;

    /**
     * Nilai pertanggungan maksimal yang bisa diklaim.
     * Harus bernilai positif (minimal 1).
     */
    @NotNull(message = "Coverage must not be null")
    @Min(value = 1, message = "Coverage must be positive")
    private Integer coverage;

    /**
     * Penjelasan detail mengenai apa saja yang ditanggung oleh paket ini.
     */
    @NotBlank(message = "Coverage details must not be empty")
    private String coverageDetails;

    /**
     * Daftar layanan yang dicakup oleh paket asuransi ini.
     * Tidak boleh kosong.
     */
    @NotEmpty(message = "Applicable services must not be empty")
    private List<ServiceEnum> applicableService;

    /**
     * Durasi masa aktif asuransi dalam satuan hari.
     * Minimal 1 hari.
     */
    @NotNull(message = "Expired by days must not be null")
    @Min(value = 1, message = "Expired by days must be at least 1 day")
    private Integer expiredByDays;
}