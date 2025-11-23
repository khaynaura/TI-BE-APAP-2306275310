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

    @PostMapping("/create")
    public ResponseEntity<?> createTopUp(@RequestBody CreateTopUpRequestDTO request) {
        var transaction = topUpService.createTopUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdateStatusTopUpRequestDTO request) {
        var transaction = topUpService.updateStatusTopUp(id, request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TopUpTransaction>> getHistory(@PathVariable("userId") UUID userId) {
        var history = topUpService.getHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TopUpTransaction>> getAllTransactions() {
        var allTransactions = topUpService.getAllTransactions();
        return ResponseEntity.ok(allTransactions);
    }
}