package apap.ti._5.Insurance_2306275310_be.restdto.response.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDetailResponseDTO {
    private String id; 
    private String status;
    private String proof;
    private String rejectionReason;
    private String rejectionDescription;
    private LocalDateTime rejectionTimestamp;
    private String acceptedNote;
    private LocalDateTime acceptedTimestamp;
}