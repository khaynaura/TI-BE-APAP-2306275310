package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TopUpService {
    // Basic Features
    TopUpTransaction createTopUp(CreateTopUpRequestDTO request);
    TopUpTransaction updateStatusTopUp(UUID transactionId, UpdateStatusTopUpRequestDTO request);
    List<TopUpTransaction> getHistoryByUserId(UUID userId);
    List<TopUpTransaction> getAllTransactions();

    // --- TAMBAHAN BARU (Supaya PBI Lengkap) ---
    TopUpTransaction getTransactionById(UUID transactionId);
    void deleteTopUpTransaction(UUID transactionId);
}