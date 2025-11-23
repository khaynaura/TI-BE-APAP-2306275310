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

    @Override
    public TopUpTransaction createTopUp(CreateTopUpRequestDTO request) {
        // 1. Validasi Amount
        if (request.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        // 2. Validasi Payment Method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));

        if (!paymentMethod.getStatus().equalsIgnoreCase("Active")) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is not active");
        }

        // 3. Simpan Transaksi Pending
        TopUpTransaction transaction = new TopUpTransaction();
        transaction.setEndUserId(request.getEndUserId());
        transaction.setAmount(request.getAmount());
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus("Pending"); 

        return topUpTransactionRepository.save(transaction);
    }

    @Override
    public TopUpTransaction updateStatusTopUp(UUID transactionId, UpdateStatusTopUpRequestDTO request) {
        TopUpTransaction transaction = topUpTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        String newStatus = request.getStatus();
        transaction.setStatus(newStatus);
        
        TopUpTransaction updatedTransaction = topUpTransactionRepository.save(transaction);

        // TODO: Integrasi ke Profile Service
        if ("Success".equalsIgnoreCase(newStatus)) {
            System.out.println("Simulasi: Saldo user " + transaction.getEndUserId() + " bertambah " + transaction.getAmount());
            // Call API Profile Service here...
        }

        return updatedTransaction;
    }

    @Override
    public List<TopUpTransaction> getHistoryByUserId(UUID userId) {
        return topUpTransactionRepository.findAllByEndUserId(userId);
    }

    @Override
    public List<TopUpTransaction> getAllTransactions() {
        return topUpTransactionRepository.findAll();
    }
}