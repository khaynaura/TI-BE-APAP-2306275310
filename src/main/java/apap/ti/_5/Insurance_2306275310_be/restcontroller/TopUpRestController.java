package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.TopUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller untuk menangani transaksi Top-Up Saldo.
 */
@RestController
@RequestMapping("/api/top-up")
public class TopUpRestController {

    @Autowired
    private TopUpService topUpService;

    /**
     * Helper untuk mengambil ID user yang sedang login dari SecurityContext.
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    /**
     * Helper untuk mengecek apakah user adalah Superadmin.
     */
    private boolean isSuperAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    /**
     * [PBI-BE-TU1] Mengambil semua transaksi top-up.
     * Hanya untuk Superadmin.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<TopUpTransaction>> getAllTransactions() {
        return ResponseEntity.ok(topUpService.getAllTransactions());
    }

    /**
     * [PBI-BE-TU1] Mengambil riwayat top-up milik user tertentu.
     * Customer hanya boleh melihat miliknya sendiri. Superadmin boleh melihat punya siapa saja.
     *
     * @param userId ID User yang ingin dilihat riwayatnya.
     */
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

    /**
     * [PBI-BE-TU2] Mengambil detail transaksi berdasarkan ID.
     * Hanya untuk Superadmin.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<TopUpTransaction> getTransactionById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(topUpService.getTransactionById(id));
    }

    /**
     * [PBI-BE-TU3] Membuat pengajuan top-up baru.
     * Hanya untuk Customer. ID User diambil otomatis dari token.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createTopUp(@RequestBody CreateTopUpRequestDTO request) {
        // Paksa ID user dari token agar aman
        request.setEndUserId(UUID.fromString(getCurrentUserId()));

        var transaction = topUpService.createTopUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * [PBI-BE-TU4] Memperbarui status transaksi (Approve/Reject).
     * Hanya untuk Superadmin.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdateStatusTopUpRequestDTO request) {
        var transaction = topUpService.updateStatusTopUp(id, request);
        return ResponseEntity.ok(transaction);
    }

    /**
     * [PBI-BE-TU5] Menghapus transaksi top-up (Soft Delete).
     * Hanya untuk Superadmin.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") UUID id) {
        topUpService.deleteTopUpTransaction(id);
        return ResponseEntity.ok("Top Up Transaction has been deleted successfully");
    }
}