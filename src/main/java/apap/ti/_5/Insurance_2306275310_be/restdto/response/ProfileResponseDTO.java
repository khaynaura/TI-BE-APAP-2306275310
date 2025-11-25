package apap.ti._5.Insurance_2306275310_be.restdto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProfileResponseDTO {
    private Integer status;
    private String message;
    private UserData data;

    @Data
    public static class UserData {
        private String id;
        private String username;
        private BigDecimal saldo; 
    }
}