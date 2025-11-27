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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePolicyRequestDTO {

    @NotBlank(message = "User ID must not be empty")
    private String userId;

    @NotBlank(message = "Booking ID must not be empty")
    private String bookingId;

    @NotNull(message = "Service must not be null")
    private ServiceEnum service;

    @NotEmpty(message = "Choose at least one insurance plan")
    private List<String> insurancePlanIds; 
    
}