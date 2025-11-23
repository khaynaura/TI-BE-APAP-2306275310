package apap.ti._5.Insurance_2306275310_be.restdto.request.topup;


import lombok.Data;
import java.util.UUID;

@Data
public class CreateTopUpRequestDTO {
    private UUID endUserId;
    private Long amount;
    private UUID paymentMethodId;
}