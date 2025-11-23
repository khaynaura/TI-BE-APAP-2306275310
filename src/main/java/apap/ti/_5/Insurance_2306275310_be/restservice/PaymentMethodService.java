package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import java.util.List;

public interface PaymentMethodService {
    PaymentMethod addPaymentMethod(AddPaymentMethodRequestDTO request);
    List<PaymentMethod> getAllPaymentMethods();
}