package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.*;
import apap.ti._5.Insurance_2306275310_be.repository.*;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private InsurancePlanRepository insurancePlanRepository;
    @Mock
    private OrderedPlanRepository orderedPlanRepository;

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;

    // Mocks untuk Rantai WebClient (Chain)
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private PolicyServiceImpl policyService;

    @BeforeEach
    void setUp() {
        // 1. Mock WebClient Builder agar mengembalikan Mock WebClient
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        // 2. Inisialisasi Service
        policyService = new PolicyServiceImpl(policyRepository, insurancePlanRepository, orderedPlanRepository, webClientBuilder);

        // 3. Inject Value URL menggunakan ReflectionTestUtils (Lebih Aman & Stabil)
        ReflectionTestUtils.setField(policyService, "flightServiceUrl", "http://flight-service");
        ReflectionTestUtils.setField(policyService, "accommodationServiceUrl", "http://hotel-service");
        ReflectionTestUtils.setField(policyService, "rentalServiceUrl", "http://rental-service");
        ReflectionTestUtils.setField(policyService, "billingServiceUrl", "http://billing-service");
        ReflectionTestUtils.setField(policyService, "apiKey", "test-key");

        // 4. Mock Token Header (RequestContextHolder)
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        lenient().when(attrs.getRequest()).thenReturn(mockRequest);
        lenient().when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer TEST-TOKEN");
        RequestContextHolder.setRequestAttributes(attrs);

        // 5. Setup Mocking WebClient Chain (Default Behavior)
        // Chain untuk GET: webClient.get() -> uri() -> header() -> retrieve()
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Chain untuk POST: webClient.post() -> uri() -> header() -> bodyValue() -> retrieve()
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec); 
        // Note: bodyValue mengembalikan RequestHeadersSpec, yang sudah di-mock di atas untuk return responseSpec saat retrieve()
    }

    @Test
    void testCreatePolicy_Success() {
        // Setup DTO Request
        CreatePolicyRequestDTO req = new CreatePolicyRequestDTO();
        req.setBookingId("B1");
        req.setService(ServiceEnum.FLIGHT);
        req.setUserId("U1");
        req.setInsurancePlanIds(List.of("INS1"));

        // Setup Plan
        InsurancePlan plan = new InsurancePlan();
        plan.setId("INS1");
        plan.setPrice(100);
        plan.setCoverage(1000);
        plan.setExpiredByDays(5);
        plan.setApplicableService(List.of(ServiceEnum.FLIGHT));

        // Mock Repository
        when(insurancePlanRepository.findAllById(any())).thenReturn(List.of(plan));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> {
            Policy p = i.getArgument(0);
            p.setId("POL1");
            return p;
        });
        when(orderedPlanRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        // Mock WebClient Responses
        // 1. Validasi Booking (GET) -> Return Success (Bodiless)
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(mock(ResponseEntity.class)));

        // 2. Create Bill (POST) -> Return ID Bill
        Map<String, Object> billResp = Map.of("data", Map.of("id", "BILL-1"));
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(billResp));

        // Execute
        PolicyResponseDTO res = policyService.createPolicy(req);

        // Assert
        assertNotNull(res);
        assertEquals("POL1", res.getId());
        assertEquals("BILL-1", res.getBillId());
        verify(policyRepository, times(2)).save(any(Policy.class));
    }

    @Test
    void testCreatePolicy_Fail_BookingNotFound() {
        CreatePolicyRequestDTO req = new CreatePolicyRequestDTO();
        req.setBookingId("INVALID");
        req.setService(ServiceEnum.FLIGHT);

        // Mock GET -> Throw 404 Exception
        when(responseSpec.toBodilessEntity()).thenThrow(
            WebClientResponseException.create(404, "Not Found", null, null, null)
        );

        // Execute & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(req));
        assertTrue(e.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void testCreatePolicy_Fail_PlanMismatch() {
        CreatePolicyRequestDTO req = new CreatePolicyRequestDTO();
        req.setBookingId("B1");
        req.setService(ServiceEnum.FLIGHT);
        req.setInsurancePlanIds(List.of("INS-HOTEL"));

        InsurancePlan plan = new InsurancePlan();
        plan.setPlanName("Hotel Plan");
        plan.setApplicableService(List.of(ServiceEnum.ACCOMMODATION)); // Mismatch Service

        // Mock Validasi Sukses (Biar lanjut ke pengecekan Plan)
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(mock(ResponseEntity.class)));
        
        when(insurancePlanRepository.findAllById(any())).thenReturn(List.of(plan));

        // Execute & Assert
        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(req));
    }

    @Test
    void testPayPolicy_Success() {
        Policy policy = new Policy();
        policy.setId("P1");
        policy.setStatus("CREATED");

        // Setup Insurance Plan (PENTING: Biar gak NPE pas getInsurancePlan().getId())
        InsurancePlan plan = new InsurancePlan();
        plan.setId("INS-1"); 

        OrderedPlan op = new OrderedPlan();
        op.setStatus("CREATED");
        op.setInsurancePlan(plan); // Masukkan plan ke ordered plan
        
        policy.setOrderedPlans(List.of(op));

        when(policyRepository.findById("P1")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        policyService.payPolicy("P1");

        assertEquals("PAID", policy.getStatus());
        assertEquals("PAID", op.getStatus());
    }

    @Test
    void testPayPolicy_IgnoreIfNotCreated() {
        Policy policy = new Policy();
        policy.setId("P1");
        policy.setStatus("PAID");
        
        // PENTING: Inisialisasi list kosong agar tidak NPE saat stream()
        policy.setOrderedPlans(new ArrayList<>()); 

        when(policyRepository.findById("P1")).thenReturn(Optional.of(policy));

        policyService.payPolicy("P1");

        verify(policyRepository, never()).save(any());
    }

    @Test
    void testGetPolicyById_AutoExpire() {
        Policy policy = new Policy();
        policy.setId("P1");
        policy.setStatus("PAID");

        OrderedPlan op = new OrderedPlan();
        op.setExpiredDate(LocalDate.now().minusDays(1)); // Sudah Expired
        op.setStatus("PAID");
        op.setInsurancePlan(new InsurancePlan());

        policy.setOrderedPlans(List.of(op));

        when(policyRepository.findById("P1")).thenReturn(Optional.of(policy));

        policyService.getPolicyById("P1");

        assertEquals("EXPIRED", op.getStatus());
        assertEquals("EXPIRED", policy.getStatus());
    }

    @Test
    void testGetAllPolicies() {
        when(policyRepository.findAll()).thenReturn(new ArrayList<>());
        assertNotNull(policyService.getAllPolicies());
    }

    @Test
    void testGetPoliciesByUserId() {
        when(policyRepository.findAllByUserId(anyString())).thenReturn(new ArrayList<>());
        assertNotNull(policyService.getPoliciesByUserId("U1"));
    }
}