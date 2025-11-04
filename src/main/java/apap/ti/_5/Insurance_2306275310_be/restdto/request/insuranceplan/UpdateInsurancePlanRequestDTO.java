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

    @NotBlank(message = "Insurance Plan ID must not be blank")
    private String id; 

    @NotBlank(message = "Plan Name must not be blank")
    private String planName;

    @NotNull(message = "Price must not be null")
    @Min(value = 1, message = "Price must be at least 1")
    private Integer price;

    @NotNull(message = "Coverage must not be null")
    @Min(value = 1, message = "Coverage must be at least 1")
    private Integer coverage;

    @NotBlank(message = "Coverage Details must not be blank")
    private String coverageDetails;

    @NotEmpty(message = "Applicable Service must not be empty")
    private List<ServiceEnum> applicableService;

    @NotNull(message = "Expired by Days must not be null")
    @Min(value = 1, message = "Expired by Days must be at least 1")
    private Integer expiredByDays;
}