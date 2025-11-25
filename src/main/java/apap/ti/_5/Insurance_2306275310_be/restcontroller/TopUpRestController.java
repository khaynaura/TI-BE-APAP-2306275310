package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.TopUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/top-up")
public class TopUpRestController {

    @Autowired
    private TopUpService topUpService;

    // [PBI-BE-TU1] Superadmin melihat seluruh daftar
    @GetMapping("/all")
    public ResponseEntity<List<TopUpTransaction>> getAllTransactions() {
        return ResponseEntity.ok(topUpService.getAllTransactions());
    }

    // [PBI-BE-TU1] Customer melihat riwayat sendiri
    // Note: Validasi JWT token biasanya dilakukan di Filter/SecurityConfig. 
    // Di sini kita anggap userId dikirim sebagai parameter/path variable.
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TopUpTransaction>> getHistory(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(topUpService.getHistoryByUserId(userId));
    }

    // [PBI-BE-TU2] GET Top Up Transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<TopUpTransaction> getTransactionById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(topUpService.getTransactionById(id));
    }

    // [PBI-BE-TU3] POST Create Top Up Transaction
    @PostMapping("/create")
    public ResponseEntity<?> createTopUp(@RequestBody CreateTopUpRequestDTO request) {
        var transaction = topUpService.createTopUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // [PBI-BE-TU4] PUT Update Top Up Status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdateStatusTopUpRequestDTO request) {
        var transaction = topUpService.updateStatusTopUp(id, request);
        return ResponseEntity.ok(transaction);
    }

    // [PBI-BE-TU5] DELETE Top Up Transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") UUID id) {
        topUpService.deleteTopUpTransaction(id);
        return ResponseEntity.ok("Top Up Transaction has been deleted successfully");
    }
}