package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-method")
public class PaymentMethodRestController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    // Endpoint untuk Superadmin menambah Payment Method
    @PostMapping("/create")
    public ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody AddPaymentMethodRequestDTO request) {
        var newMethod = paymentMethodService.addPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newMethod);
    }

    // Endpoint untuk melihat semua Payment Method yang tersedia
    @GetMapping("/all")
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return ResponseEntity.ok(paymentMethodService.getAllPaymentMethods());
    }
}