package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.repository.PaymentMethodRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    // [PBI-BE-TU6] Get All Payment Methods (yg terhapus hidden by @Where)
    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    // [PBI-BE-TU7] Create Payment Method
    @Override
    public PaymentMethod addPaymentMethod(AddPaymentMethodRequestDTO request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setMethodName(request.getMethodName());
        paymentMethod.setProvider(request.getProvider());
        paymentMethod.setStatus(request.getStatus());
        
        return paymentMethodRepository.save(paymentMethod);
    }

    // [PBI-BE-TU8] Update Status Payment Method (Active/Inactive)
    @Override
    public PaymentMethod updateStatusPaymentMethod(UUID id, UpdatePaymentMethodStatusRequestDTO request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));
        
        paymentMethod.setStatus(request.getStatus());
        return paymentMethodRepository.save(paymentMethod);
    }

    // [PBI-BE-TU9] Soft Delete Payment Method
    @Override
    public void deletePaymentMethod(UUID id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));
        
        paymentMethod.setDeleted(true); // Soft delete
        paymentMethodRepository.save(paymentMethod);
    }
}