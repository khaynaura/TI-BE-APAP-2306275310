package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-method")
public class PaymentMethodRestController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    // [PBI-BE-TU6] GET All Payment Methods
    @GetMapping("/all")
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return ResponseEntity.ok(paymentMethodService.getAllPaymentMethods());
    }

    // [PBI-BE-TU7] POST Create Payment Method
    @PostMapping("/create")
    public ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody AddPaymentMethodRequestDTO request) {
        var newMethod = paymentMethodService.addPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newMethod);
    }

    // [PBI-BE-TU8] PUT Update Payment Method Status
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentMethod> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdatePaymentMethodStatusRequestDTO request) {
        var updatedMethod = paymentMethodService.updateStatusPaymentMethod(id, request);
        return ResponseEntity.ok(updatedMethod);
    }

    // [PBI-BE-TU9] DELETE Payment Method
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePaymentMethod(@PathVariable("id") UUID id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.ok("Payment method has been deleted successfully");
    }
}