package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.repository.PaymentMethodRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Implementasi dari interface {@link PaymentMethodService}.
 * Menangani operasi CRUD untuk metode pembayaran.
 */
@Service
@Transactional
@AllArgsConstructor // Menggantikan @Autowired field injection dengan Constructor Injection
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    /**
     * Mengambil semua metode pembayaran yang tersedia.
     * Data yang terhapus (soft delete) otomatis difilter oleh anotasi @Where di Model.
     *
     * @return List dari {@link PaymentMethod}.
     */
    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    /**
     * Menambahkan metode pembayaran baru.
     *
     * @param request DTO yang berisi nama metode, provider, dan status awal.
     * @return Entitas {@link PaymentMethod} yang baru disimpan.
     */
    @Override
    public PaymentMethod addPaymentMethod(AddPaymentMethodRequestDTO request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setMethodName(request.getMethodName());
        paymentMethod.setProvider(request.getProvider());
        paymentMethod.setStatus(request.getStatus());

        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * Memperbarui status (Active/Inactive) dari metode pembayaran.
     *
     * @param id      UUID dari metode pembayaran.
     * @param request DTO berisi status baru.
     * @return Entitas {@link PaymentMethod} yang telah diperbarui.
     * @throws ResponseStatusException Jika ID tidak ditemukan.
     */
    @Override
    public PaymentMethod updateStatusPaymentMethod(UUID id, UpdatePaymentMethodStatusRequestDTO request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));

        paymentMethod.setStatus(request.getStatus());
        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * Menghapus metode pembayaran secara Soft Delete.
     *
     * @param id UUID dari metode pembayaran.
     * @throws ResponseStatusException Jika ID tidak ditemukan.
     */
    @Override
    public void deletePaymentMethod(UUID id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));

        paymentMethod.setDeleted(true); // Soft delete flag
        paymentMethodRepository.save(paymentMethod);
    }
}