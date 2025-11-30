package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.repository.PaymentMethodRepository;
import apap.ti._5.Insurance_2306275310_be.repository.TopUpTransactionRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProfileResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopUpServiceImplTest {

    @Mock
    private TopUpTransactionRepository topUpTransactionRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    private TopUpServiceImpl topUpService;

    @BeforeEach
    void setUp() throws Exception {
        // Setup WebClient Builder
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        topUpService = new TopUpServiceImpl(webClientBuilder);

        // Inject Fields via Reflection
        injectField(topUpService, "topUpTransactionRepository", topUpTransactionRepository);
        injectField(topUpService, "paymentMethodRepository", paymentMethodRepository);
        injectField(topUpService, "profileServiceUrl", "http://mock-profile-url");

        // Mock RequestContextHolder (Token)
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        lenient().when(attrs.getRequest()).thenReturn(mockRequest);
        lenient().when(mockRequest.getHeader("Authorization")).thenReturn("Bearer TEST-TOKEN");
        RequestContextHolder.setRequestAttributes(attrs);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testCreateTopUp_Success() {
        CreateTopUpRequestDTO req = new CreateTopUpRequestDTO();
        req.setAmount(50000L);
        req.setPaymentMethodId(UUID.randomUUID());
        req.setEndUserId(UUID.randomUUID());

        PaymentMethod pm = new PaymentMethod();
        pm.setStatus("Active");

        when(paymentMethodRepository.findById(any())).thenReturn(Optional.of(pm));
        when(topUpTransactionRepository.save(any(TopUpTransaction.class))).thenAnswer(i -> i.getArgument(0));

        TopUpTransaction result = topUpService.createTopUp(req);

        assertNotNull(result);
        assertEquals("Pending", result.getStatus());
        assertEquals(50000L, result.getAmount());
    }

    @Test
    void testCreateTopUp_Fail_NegativeAmount() {
        CreateTopUpRequestDTO req = new CreateTopUpRequestDTO();
        req.setAmount(-1000L); 
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> topUpService.createTopUp(req));
        assertTrue(e.getMessage().contains("must be positive"));
    }

    @Test
    void testCreateTopUp_Fail_MethodNotFound() {
        CreateTopUpRequestDTO req = new CreateTopUpRequestDTO();
        req.setAmount(10000L);
        req.setPaymentMethodId(UUID.randomUUID());

        when(paymentMethodRepository.findById(any())).thenReturn(Optional.empty());
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> topUpService.createTopUp(req));
        assertTrue(e.getMessage().contains("Payment method not found"));
    }

    @Test
    void testCreateTopUp_Fail_InactiveMethod() {
        CreateTopUpRequestDTO req = new CreateTopUpRequestDTO();
        req.setAmount(10000L);
        req.setPaymentMethodId(UUID.randomUUID());

        PaymentMethod pm = new PaymentMethod();
        pm.setStatus("Inactive");

        when(paymentMethodRepository.findById(any())).thenReturn(Optional.of(pm));
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> topUpService.createTopUp(req));
        assertTrue(e.getMessage().contains("Payment method is not active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateStatus_ToSuccess_UpdatesBalance() {
        // Setup Transaction
        UUID txId = UUID.randomUUID();
        TopUpTransaction tx = new TopUpTransaction();
        tx.setId(txId);
        tx.setStatus("Pending");
        tx.setAmount(50000L);
        tx.setEndUserId(UUID.randomUUID());

        when(topUpTransactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        when(topUpTransactionRepository.save(any())).thenReturn(tx);

        // --- EXPLICIT MOCKING FOR WEBCLIENT (ANTI-NPE) ---
        
        // 1. MOCK GET CHAIN (Ambil Saldo)
        var getUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        var getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        var getResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.header(any(), any())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);

        ProfileResponseDTO profileResp = new ProfileResponseDTO();
        ProfileResponseDTO.UserData data = new ProfileResponseDTO.UserData();
        data.setSaldo(new BigDecimal("10000"));
        profileResp.setData(data);
        
        when(getResponseSpec.bodyToMono(ProfileResponseDTO.class)).thenReturn(Mono.just(profileResp));

        // 2. MOCK PUT CHAIN (Update Saldo)
        var putUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        var putBodySpec = mock(WebClient.RequestBodySpec.class);
        var putHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        var putResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.put()).thenReturn(putUriSpec);
        when(putUriSpec.uri(anyString())).thenReturn(putBodySpec);
        when(putBodySpec.header(any(), any())).thenReturn(putBodySpec);
        when(putBodySpec.bodyValue(any())).thenReturn(putHeadersSpec);
        when(putHeadersSpec.retrieve()).thenReturn(putResponseSpec);
        when(putResponseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        // EXECUTE
        UpdateStatusTopUpRequestDTO req = new UpdateStatusTopUpRequestDTO();
        req.setStatus("Success");

        TopUpTransaction result = topUpService.updateStatusTopUp(txId, req);

        assertEquals("Success", result.getStatus());
        // Verify PUT dipanggil
        verify(webClient, times(1)).put();
    }

    @Test
    void testUpdateStatus_ToFailed_NoBalanceUpdate() {
        UUID txId = UUID.randomUUID();
        TopUpTransaction tx = new TopUpTransaction();
        tx.setId(txId);
        tx.setStatus("Pending");

        when(topUpTransactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        when(topUpTransactionRepository.save(any())).thenReturn(tx);

        UpdateStatusTopUpRequestDTO req = new UpdateStatusTopUpRequestDTO();
        req.setStatus("Failed");

        TopUpTransaction result = topUpService.updateStatusTopUp(txId, req);

        assertEquals("Failed", result.getStatus());
        // Verify WebClient TIDAK dipanggil
        verify(webClient, never()).get();
        verify(webClient, never()).put();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateStatus_WebClientError_TransactionStillSaved() {
        UUID txId = UUID.randomUUID();
        TopUpTransaction tx = new TopUpTransaction();
        tx.setId(txId);
        tx.setStatus("Pending");
        tx.setAmount(50000L);

        when(topUpTransactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        when(topUpTransactionRepository.save(any())).thenReturn(tx);

        // --- EXPLICIT MOCKING GET CHAIN (Throw Error) ---
        var getUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        var getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        var getResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.header(any(), any())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        
        // Simulasikan Error saat bodyToMono dipanggil
        when(getResponseSpec.bodyToMono(ProfileResponseDTO.class)).thenThrow(new RuntimeException("Service Down"));

        UpdateStatusTopUpRequestDTO req = new UpdateStatusTopUpRequestDTO();
        req.setStatus("Success");

        // Execute (Harus aman tidak throw exception)
        assertDoesNotThrow(() -> topUpService.updateStatusTopUp(txId, req));
        
        // Status tetap tersimpan
        assertEquals("Success", tx.getStatus());
    }

    @Test
    void testUpdateStatus_Fail_EmptyStatus() {
        UUID txId = UUID.randomUUID();
        TopUpTransaction tx = new TopUpTransaction();
        when(topUpTransactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        UpdateStatusTopUpRequestDTO req = new UpdateStatusTopUpRequestDTO();
        req.setStatus("");

        assertThrows(ResponseStatusException.class, () -> topUpService.updateStatusTopUp(txId, req));
    }

    @Test
    void testGetAllTransactions() {
        when(topUpTransactionRepository.findAll()).thenReturn(List.of(new TopUpTransaction()));
        List<TopUpTransaction> res = topUpService.getAllTransactions();
        assertEquals(1, res.size());
    }

    @Test
    void testGetHistoryByUserId() {
        UUID uid = UUID.randomUUID();
        when(topUpTransactionRepository.findAllByEndUserId(uid)).thenReturn(List.of(new TopUpTransaction()));
        List<TopUpTransaction> res = topUpService.getHistoryByUserId(uid);
        assertEquals(1, res.size());
    }

    @Test
    void testGetTransactionById_Success() {
        UUID uid = UUID.randomUUID();
        TopUpTransaction tx = new TopUpTransaction();
        tx.setId(uid);
        when(topUpTransactionRepository.findById(uid)).thenReturn(Optional.of(tx));
        
        TopUpTransaction res = topUpService.getTransactionById(uid);
        assertEquals(uid, res.getId());
    }

    @Test
    void testGetTransactionById_NotFound() {
        UUID uid = UUID.randomUUID();
        when(topUpTransactionRepository.findById(uid)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> topUpService.getTransactionById(uid));
    }

    @Test
    void testDeleteTopUpTransaction() {
        UUID uid = UUID.randomUUID();
        TopUpTransaction tx = new TopUpTransaction();
        tx.setDeleted(false);
        when(topUpTransactionRepository.findById(uid)).thenReturn(Optional.of(tx));

        topUpService.deleteTopUpTransaction(uid);
        assertTrue(tx.isDeleted());
        verify(topUpTransactionRepository).save(tx);
    }
}