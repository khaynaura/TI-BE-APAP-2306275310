package apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInsurancePlanRequestDTO {

    @NotBlank(message = "Plan name must not be empty")
    private String planName;

    @NotNull(message = "Price must not be null")
    @Min(value = 1, message = "Price must be positive")
    private Integer price;

    @NotNull(message = "Coverage must not be null")
    @Min(value = 1, message = "Coverage must be positive")
    private Integer coverage;

    @NotBlank(message = "Coverage details must not be empty")
    private String coverageDetails;

    @NotEmpty(message = "Applicable services must not be empty")
    private List<ServiceEnum> applicableService;

    @NotNull(message = "Expired by days must not be null")
    @Min(value = 1, message = "Expired by days must be at least 1 day")
    private Integer expiredByDays;
}