package apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) untuk memperbarui data paket asuransi yang sudah ada.
 * Memerlukan ID paket untuk identifikasi target update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInsurancePlanRequestDTO {

    /**
     * ID unik paket asuransi yang datanya ingin diperbarui.
     */
    @NotBlank(message = "Insurance Plan ID must not be blank")
    private String id;

    /**
     * Nama baru untuk paket asuransi.
     */
    @NotBlank(message = "Plan Name must not be blank")
    private String planName;

    /**
     * Harga premi baru.
     * Harus bernilai positif.
     */
    @NotNull(message = "Price must not be null")
    @Min(value = 1, message = "Price must be at least 1")
    private Integer price;

    /**
     * Nilai pertanggungan baru.
     * Harus bernilai positif.
     */
    @NotNull(message = "Coverage must not be null")
    @Min(value = 1, message = "Coverage must be at least 1")
    private Integer coverage;

    /**
     * Detail cakupan baru.
     */
    @NotBlank(message = "Coverage Details must not be blank")
    private String coverageDetails;

    /**
     * Daftar layanan baru yang dicakup.
     */
    @NotEmpty(message = "Applicable Service must not be empty")
    private List<ServiceEnum> applicableService;

    /**
     * Durasi masa aktif baru (dalam hari).
     */
    @NotNull(message = "Expired by Days must not be null")
    @Min(value = 1, message = "Expired by Days must be at least 1")
    private Integer expiredByDays;
}