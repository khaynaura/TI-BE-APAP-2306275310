package apap.ti._5.Insurance_2306275310_be.restdto.request.claim;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequestDTO {

    @NotBlank(message = "Proof must not be empty")
    private String proof;
}