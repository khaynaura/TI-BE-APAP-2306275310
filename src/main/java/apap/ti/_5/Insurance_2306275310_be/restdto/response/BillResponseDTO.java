package apap.ti._5.Insurance_2306275310_be.restdto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BillResponseDTO {
    private String id;
    private String serviceReferenceId; // Ini sama dengan Policy ID
    private Integer status; // 1 = PAID, 0 = UNPAID (Sesuai kode temanmu)
}