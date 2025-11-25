package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.TopUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/top-up")
public class TopUpRestController {

    @Autowired
    private TopUpService topUpService;

    // Helper: Ambil ID User dari Token
    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? (String) auth.getPrincipal() : null;
    }

    private boolean isSuperAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<TopUpTransaction>> getAllTransactions() {
        return ResponseEntity.ok(topUpService.getAllTransactions());
    }

    // [PBI-BE-TU1] Customer Validation
    @GetMapping("/history/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<?> getHistory(@PathVariable("userId") UUID userId) {
        // Validasi: Customer hanya boleh lihat punya sendiri
        if (!isSuperAdmin()) {
            String currentId = getCurrentUserId();
            if (currentId == null || !currentId.equals(userId.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to view this transaction history.");
            }
        }
        return ResponseEntity.ok(topUpService.getHistoryByUserId(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<TopUpTransaction> getTransactionById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(topUpService.getTransactionById(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createTopUp(@RequestBody CreateTopUpRequestDTO request) {
        // Paksa ID user dari token agar aman
        request.setEndUserId(UUID.fromString(getCurrentUserId()));
        
        var transaction = topUpService.createTopUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdateStatusTopUpRequestDTO request) {
        var transaction = topUpService.updateStatusTopUp(id, request);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") UUID id) {
        topUpService.deleteTopUpTransaction(id);
        return ResponseEntity.ok("Top Up Transaction has been deleted successfully");
    }
}