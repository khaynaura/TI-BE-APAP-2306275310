package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.repository.PaymentMethodRepository;
import apap.ti._5.Insurance_2306275310_be.repository.TopUpTransactionRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TopUpServiceImpl implements TopUpService {

    @Autowired
    private TopUpTransactionRepository topUpTransactionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    // [PBI-BE-TU1] Superadmin melihat SEMUA transaksi
    @Override
    public List<TopUpTransaction> getAllTransactions() {
        return topUpTransactionRepository.findAll(); 
        // Note: Data yang isDeleted=true otomatis hilang karena @Where di Model
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
        // Validasi Amount Positif
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

    // [PBI-BE-TU4] Update Status (Success/Failed)
    @Override
    public TopUpTransaction updateStatusTopUp(UUID transactionId, UpdateStatusTopUpRequestDTO request) {
        TopUpTransaction transaction = getTransactionById(transactionId);
        
        String newStatus = request.getStatus();
        if (newStatus == null || newStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }

        transaction.setStatus(newStatus);
        TopUpTransaction updatedTransaction = topUpTransactionRepository.save(transaction);

        // Jika Success -> Tambah Saldo (Simulasi)
        if ("Success".equalsIgnoreCase(newStatus)) {
            System.out.println("LOGIC: Menambah saldo user " + transaction.getEndUserId() + " sebesar " + transaction.getAmount());
        } 
        // Jika Failed -> Tidak ada perubahan saldo

        return updatedTransaction;
    }

    // [PBI-BE-TU5] Soft Delete Transaction
    @Override
    public void deleteTopUpTransaction(UUID transactionId) {
        TopUpTransaction transaction = getTransactionById(transactionId);
        transaction.setDeleted(true); // Soft Delete logic
        topUpTransactionRepository.save(transaction);
    }
}