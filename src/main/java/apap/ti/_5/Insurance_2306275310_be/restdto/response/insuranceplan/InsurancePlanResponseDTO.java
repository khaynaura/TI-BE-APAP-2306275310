package apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO untuk menampilkan detail produk asuransi yang tersedia (katalog).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlanResponseDTO {
    
    private String id;
    private String providerId;
    private String planName;
    
    /** Harga premi. */
    private Integer price;
    
    /** Nilai pertanggungan maksimal. */
    private Integer coverage;
    
    /** Deskripsi cakupan. */
    private String coverageDetails;
    
    /** Daftar layanan yang dicakup (FLIGHT, HOTEL, dll). */
    private List<ServiceEnum> applicableService;
    
    /** Masa aktif dalam hari. */
    private Integer expiredByDays;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}