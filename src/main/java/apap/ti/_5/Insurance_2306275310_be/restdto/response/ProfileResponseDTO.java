package apap.ti._5.Insurance_2306275310_be.restdto.response;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO khusus untuk mengambil data profil pengguna dari API eksternal (Auth Service).
 */
@Data
public class ProfileResponseDTO {
    
    private Integer status;
    private String message;
    private UserData data;

    /**
     * Inner class untuk detail data user.
     */
    @Data
    public static class UserData {
        private String id;
        private String username;
        private BigDecimal saldo;
    }
}