package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.repository.PaymentMethodRepository;
import apap.ti._5.Insurance_2306275310_be.repository.TopUpTransactionRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProfileResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class TopUpServiceImpl implements TopUpService {

    @Autowired
    private TopUpTransactionRepository topUpTransactionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    private final WebClient webClient;

    // Mengambil URL Profile dari application.yml (Misal: http://localhost:8081/api)
    @Value("${profile.service.url}")
    private String profileServiceUrl;

    @Autowired
    public TopUpServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // [PBI-BE-TU1] Superadmin melihat SEMUA transaksi
    @Override
    public List<TopUpTransaction> getAllTransactions() {
        return topUpTransactionRepository.findAll();
    }

    // [PBI-BE-TU1] Customer melihat transaksi MILIKNYA SAJA
    @Override
    public List<TopUpTransaction> getHistoryByUserId(UUID userId) {
        return topUpTransactionRepository.findAllByEndUserId(userId);
    }

    // [PBI-BE-TU2] Melihat Detail Transaksi by ID
    @Override
    public TopUpTransaction getTransactionById(UUID transactionId) {
        return topUpTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    // [PBI-BE-TU3] Create Top Up Transaction
    @Override
    public TopUpTransaction createTopUp(CreateTopUpRequestDTO request) {
        if (request.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));

        if (!paymentMethod.getStatus().equalsIgnoreCase("Active")) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is not active");
        }

        TopUpTransaction transaction = new TopUpTransaction();
        transaction.setEndUserId(request.getEndUserId());
        transaction.setAmount(request.getAmount());
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus("Pending"); // Status Awal

        return topUpTransactionRepository.save(transaction);
    }

    // [PBI-BE-TU4] Update Status & Tambah Saldo (LOGIC UTAMA)
    @Override
    public TopUpTransaction updateStatusTopUp(UUID transactionId, UpdateStatusTopUpRequestDTO request) {
        TopUpTransaction transaction = getTransactionById(transactionId);
        
        String oldStatus = transaction.getStatus();
        String newStatus = request.getStatus();

        if (newStatus == null || newStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }

        // Update status di database lokal dulu
        transaction.setStatus(newStatus);
        TopUpTransaction updatedTransaction = topUpTransactionRepository.save(transaction);

        // Cek: Apakah status berubah menjadi Success?
        // Dan pastikan sebelumnya belum success (supaya saldo ga nambah 2x kalau diklik ulang)
        if ("Success".equalsIgnoreCase(newStatus) && !"Success".equalsIgnoreCase(oldStatus)) {
            try {
                // Panggil fungsi update saldo ke Profile Service
                updateBalanceInProfileService(transaction.getEndUserId(), transaction.getAmount());
            } catch (Exception e) {
                // Log Error tapi jangan batalkan status 'Success' transaksi (Distributed System best practice)
                // Atau kalau mau strict, bisa throw RuntimeException biar rollback DB lokal.
                System.err.println("!!! CRITICAL ERROR: GAGAL UPDATE SALDO USER !!!");
                System.err.println("User ID: " + transaction.getEndUserId());
                System.err.println("Error: " + e.getMessage());
            }
        } 

        return updatedTransaction;
    }

    // [PBI-BE-TU5] Soft Delete Transaction
    @Override
    public void deleteTopUpTransaction(UUID transactionId) {
        TopUpTransaction transaction = getTransactionById(transactionId);
        transaction.setDeleted(true); // Soft Delete logic
        topUpTransactionRepository.save(transaction);
    }

    // ========================================================================
    // INTEGRASI PROFILE SERVICE (GET -> CALCULATE -> UPDATE)
    // ========================================================================
    private void updateBalanceInProfileService(UUID userId, Long topUpAmount) {
        // Target URL: http://localhost:8081/api/users/{id}
        String url = profileServiceUrl + "/api/users/" + userId; 
        
        // Ambil Token Superadmin dari request saat ini
        String token = getTokenFromRequest();

        // 1. GET Data User Sekarang (Untuk tahu saldo awal)
        ProfileResponseDTO currentProfile = webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(ProfileResponseDTO.class)
                .block(); // Blocking sync

        if (currentProfile == null || currentProfile.getData() == null) {
            throw new RuntimeException("Gagal mengambil data user dari Profile Service (Response Null)");
        }

        // 2. Hitung Saldo Baru
        BigDecimal currentSaldo = currentProfile.getData().getSaldo();
        if (currentSaldo == null) currentSaldo = BigDecimal.ZERO;

        BigDecimal addAmount = BigDecimal.valueOf(topUpAmount);
        BigDecimal newSaldo = currentSaldo.add(addAmount);

        // 3. PUT Update Data User (Dengan Saldo Baru)
        Map<String, Object> payload = new HashMap<>();
        // Payload harus sesuai DTO UpdateUserRequest temanmu
        payload.put("saldo", newSaldo); 

        webClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity() // Kita cuma butuh status 200 OK
                .block();
        
        System.out.println(">>> SALDO UPDATED SUCCESS: " + currentSaldo + " + " + topUpAmount + " = " + newSaldo);
    }

    // Helper: Ambil Token dari Header Request
    private String getTokenFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null; // Atau throw error jika wajib ada
    }
}