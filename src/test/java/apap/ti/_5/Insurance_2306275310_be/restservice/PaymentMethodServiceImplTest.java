package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.repository.PaymentMethodRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentMethodServiceImplTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    private PaymentMethod paymentMethod;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        paymentMethod = new PaymentMethod();
        paymentMethod.setId(paymentId);
        paymentMethod.setMethodName("GoPay");
        paymentMethod.setStatus("Active");
    }

    @Test
    void testGetAllPaymentMethods() {
        List<PaymentMethod> list = new ArrayList<>();
        list.add(paymentMethod);
        
        when(paymentMethodRepository.findAll()).thenReturn(list);

        List<PaymentMethod> result = paymentMethodService.getAllPaymentMethods();
        assertEquals(1, result.size());
        assertEquals("GoPay", result.get(0).getMethodName());
    }

    @Test
    void testAddPaymentMethod() {
        AddPaymentMethodRequestDTO request = new AddPaymentMethodRequestDTO();
        request.setMethodName("OVO");
        request.setProvider("OVO Inc");
        request.setStatus("Active");

        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(invocation -> {
            PaymentMethod pm = invocation.getArgument(0);
            pm.setId(UUID.randomUUID());
            return pm;
        });

        PaymentMethod created = paymentMethodService.addPaymentMethod(request);
        
        assertEquals("OVO", created.getMethodName());
        assertEquals("Active", created.getStatus());
        verify(paymentMethodRepository, times(1)).save(any(PaymentMethod.class));
    }

    @Test
    void testUpdateStatusPaymentMethod_Success() {
        UpdatePaymentMethodStatusRequestDTO request = new UpdatePaymentMethodStatusRequestDTO();
        request.setStatus("Inactive");

        when(paymentMethodRepository.findById(paymentId)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(paymentMethod);

        PaymentMethod updated = paymentMethodService.updateStatusPaymentMethod(paymentId, request);
        
        assertEquals("Inactive", updated.getStatus());
    }

    @Test
    void testUpdateStatusPaymentMethod_NotFound() {
        UpdatePaymentMethodStatusRequestDTO request = new UpdatePaymentMethodStatusRequestDTO();
        when(paymentMethodRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            paymentMethodService.updateStatusPaymentMethod(paymentId, request);
        });
    }

    @Test
    void testDeletePaymentMethod() {
        when(paymentMethodRepository.findById(paymentId)).thenReturn(Optional.of(paymentMethod));

        paymentMethodService.deletePaymentMethod(paymentId);

        assertTrue(paymentMethod.isDeleted());
        verify(paymentMethodRepository, times(1)).save(paymentMethod);
    }
}