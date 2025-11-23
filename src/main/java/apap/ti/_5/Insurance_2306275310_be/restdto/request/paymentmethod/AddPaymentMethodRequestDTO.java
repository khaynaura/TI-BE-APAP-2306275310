package apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod;

import lombok.Data;

@Data
public class AddPaymentMethodRequestDTO {
    private String methodName; // Contoh: QRIS
    private String provider;   // Contoh: BCA
    private String status;     // Contoh: Active
}