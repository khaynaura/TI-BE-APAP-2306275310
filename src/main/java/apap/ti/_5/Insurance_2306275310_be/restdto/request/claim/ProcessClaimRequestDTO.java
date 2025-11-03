package apap.ti._5.Insurance_2306275310_be.restdto.request.claim;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessClaimRequestDTO {

    @NotNull(message = "Decision (isAccepted) must not be empty")
    private Boolean isAccepted;

    private String acceptedNote;
    private String rejectionReason;
    private String rejectionDescription;
}