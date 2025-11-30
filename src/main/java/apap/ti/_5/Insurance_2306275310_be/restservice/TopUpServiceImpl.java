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

/**
 * Implementasi {@link TopUpService}.
 * Menangani manajemen transaksi Top-Up dan integrasi penambahan saldo ke Profile Service.
 */
@Service
@Transactional
public class TopUpServiceImpl implements TopUpService {

    @Autowired
    private TopUpTransactionRepository topUpTransactionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    private final WebClient webClient;

    @Value("${profile.service.url}")
    private String profileServiceUrl;

    @Autowired
    public TopUpServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public List<TopUpTransaction> getAllTransactions() {
        return topUpTransactionRepository.findAll();
    }

    @Override
    public List<TopUpTransaction> getHistoryByUserId(UUID userId) {
        return topUpTransactionRepository.findAllByEndUserId(userId);
    }

    @Override
    public TopUpTransaction getTransactionById(UUID transactionId) {
        return topUpTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

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
        transaction.setStatus("Pending");

        return topUpTransactionRepository.save(transaction);
    }

    /**
     * Memperbarui status transaksi top-up.
     * Jika status berubah menjadi 'Success', otomatis melakukan update saldo ke Profile Service.
     */
    @Override
    public TopUpTransaction updateStatusTopUp(UUID transactionId, UpdateStatusTopUpRequestDTO request) {
        TopUpTransaction transaction = getTransactionById(transactionId);

        String oldStatus = transaction.getStatus();
        String newStatus = request.getStatus();

        if (newStatus == null || newStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }

        transaction.setStatus(newStatus);
        TopUpTransaction updatedTransaction = topUpTransactionRepository.save(transaction);

        // Integrasi Profile Service jika Success
        if ("Success".equalsIgnoreCase(newStatus) && !"Success".equalsIgnoreCase(oldStatus)) {
            try {
                updateBalanceInProfileService(transaction.getEndUserId(), transaction.getAmount());
            } catch (Exception e) {
                System.err.println("!!! CRITICAL ERROR: GAGAL UPDATE SALDO USER !!!");
                System.err.println("User ID: " + transaction.getEndUserId());
                System.err.println("Error: " + e.getMessage());
            }
        }

        return updatedTransaction;
    }

    @Override
    public void deleteTopUpTransaction(UUID transactionId) {
        TopUpTransaction transaction = getTransactionById(transactionId);
        transaction.setDeleted(true); // Soft Delete
        topUpTransactionRepository.save(transaction);
    }

    /**
     * Mengupdate saldo user di Profile Service menggunakan mekanisme Get-Calculate-Update.
     */
    private void updateBalanceInProfileService(UUID userId, Long topUpAmount) {
        String url = profileServiceUrl + "/api/users/" + userId;
        String token = getTokenFromRequest();

        // 1. Get Saldo Saat Ini
        ProfileResponseDTO currentProfile = webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(ProfileResponseDTO.class)
                .block();

        if (currentProfile == null || currentProfile.getData() == null) {
            throw new RuntimeException("Gagal mengambil data user dari Profile Service (Response Null)");
        }

        BigDecimal currentSaldo = currentProfile.getData().getSaldo();
        if (currentSaldo == null) currentSaldo = BigDecimal.ZERO;

        // 2. Hitung
        BigDecimal addAmount = BigDecimal.valueOf(topUpAmount);
        BigDecimal newSaldo = currentSaldo.add(addAmount);

        // 3. Update
        Map<String, Object> payload = new HashMap<>();
        payload.put("saldo", newSaldo);

        webClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();

        System.out.println(">>> SALDO UPDATED SUCCESS: " + currentSaldo + " + " + topUpAmount + " = " + newSaldo);
    }

    private String getTokenFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}