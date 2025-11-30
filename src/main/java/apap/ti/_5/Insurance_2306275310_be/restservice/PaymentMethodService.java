package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {
    PaymentMethod addPaymentMethod(AddPaymentMethodRequestDTO request);
    List<PaymentMethod> getAllPaymentMethods();

    PaymentMethod updateStatusPaymentMethod(UUID id, UpdatePaymentMethodStatusRequestDTO request);
    void deletePaymentMethod(UUID id);
}