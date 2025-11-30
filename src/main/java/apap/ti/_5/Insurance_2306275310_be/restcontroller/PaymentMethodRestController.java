package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller untuk mengelola Payment Method.
 */
@RestController
@RequestMapping("/api/payment-method")
public class PaymentMethodRestController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    /**
     * Mengambil semua Payment Method.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return ResponseEntity.ok(paymentMethodService.getAllPaymentMethods());
    }

    /**
     * Menambahkan Payment Method baru.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody AddPaymentMethodRequestDTO request) {
        var newMethod = paymentMethodService.addPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newMethod);
    }

    /**
     * Memperbarui status Payment Method.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PaymentMethod> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdatePaymentMethodStatusRequestDTO request) {
        var updatedMethod = paymentMethodService.updateStatusPaymentMethod(id, request);
        return ResponseEntity.ok(updatedMethod);
    }

    /**
     * Menghapus Payment Method.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> deletePaymentMethod(@PathVariable("id") UUID id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.ok("Payment method has been deleted successfully");
    }
}