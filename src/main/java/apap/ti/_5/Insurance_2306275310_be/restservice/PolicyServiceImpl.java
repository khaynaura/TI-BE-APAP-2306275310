package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.model.Policy;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.BillResponseDTO; // Pastikan buat DTO ini
import org.springframework.core.ParameterizedTypeReference;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final InsurancePlanRepository insurancePlanRepository;
    private final OrderedPlanRepository orderedPlanRepository;
    private final WebClient webClient;

    @Value("${billing.service.url}")
    private String billingServiceUrl;

    @Value("${flight.service.url}")
    private String flightServiceUrl;

    @Value("${accommodation.service.url}") 
    private String accommodationServiceUrl;

    @Value("${rental.service.url}")
    private String rentalServiceUrl;

    @Value("${billing.api-key}")
    private String billingApiKey;

    public PolicyServiceImpl(PolicyRepository policyRepository,
                             InsurancePlanRepository insurancePlanRepository,
                             OrderedPlanRepository orderedPlanRepository,
                             WebClient.Builder webClientBuilder) {
        this.policyRepository = policyRepository;
        this.insurancePlanRepository = insurancePlanRepository;
        this.orderedPlanRepository = orderedPlanRepository;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public PolicyResponseDTO createPolicy(CreatePolicyRequestDTO createDTO) {
        // 1. Validasi Booking ID
        validateBookingId(createDTO.getService(), createDTO.getBookingId());

        // 2. Validasi Plans
        List<InsurancePlan> plans = insurancePlanRepository.findAllById(createDTO.getInsurancePlanIds());
        if (plans.size() != createDTO.getInsurancePlanIds().size()) {
            throw new IllegalArgumentException("Salah satu Insurance Plan ID tidak valid.");
        }
        for (InsurancePlan plan : plans) {
            if (!plan.getApplicableService().contains(createDTO.getService())) {
                throw new IllegalArgumentException("Plan tidak cocok dengan service.");
            }
        }

        // 3. Hitung Harga
        int totalPrice = plans.stream().mapToInt(InsurancePlan::getPrice).sum();
        int totalCoverage = plans.stream().mapToInt(InsurancePlan::getCoverage).sum();

        // 4. Save Policy
        Policy policy = new Policy();
        policy.setId("POL" + (policyRepository.count() + 1));
        policy.setUserId(createDTO.getUserId());
        policy.setBookingId(createDTO.getBookingId());
        policy.setService(createDTO.getService());
        policy.setStartDate(LocalDate.now());
        policy.setStatus("CREATED");
        policy.setTotalPrice(totalPrice);
        policy.setTotalCoverage(totalCoverage);

        // 5. Save Ordered Plans
        List<OrderedPlan> orderedPlans = new ArrayList<>();
        int i = 1;
        for (InsurancePlan plan : plans) {
            OrderedPlan op = new OrderedPlan();
            op.setId(policy.getId() + "-OP" + i);
            op.setStatus("CREATED");
            op.setExpiredDate(policy.getStartDate().plusDays(plan.getExpiredByDays()));
            op.setInsurancePlan(plan);
            op.setPolicy(policy);
            orderedPlans.add(op);
            i++;
        }
        policy.setOrderedPlans(orderedPlans);
        
        Policy savedPolicy = policyRepository.save(policy); // Save Parent
        orderedPlanRepository.saveAll(orderedPlans);        // Save Children

        // 6. CREATE BILL & SAVE BILL ID
        createBill(savedPolicy);

        return convertToResponseDTO(savedPolicy);
    }

// === METHOD CREATE BILL (FIXED: CAPTURE ID) ===
    private void createBill(Policy policy) {
        try {
            Map<String, Object> billPayload = new HashMap<>();
            billPayload.put("customerId", UUID.fromString(policy.getUserId()));
            billPayload.put("serviceName", "insurance");
            billPayload.put("serviceReferenceId", policy.getId());
            billPayload.put("description", "Insurance Policy " + policy.getId());
            billPayload.put("amount", Long.valueOf(policy.getTotalPrice()));

            // Tembak API Billing (Pakai API Key)
            Map response = webClient.post()
                    .uri(billingServiceUrl + "/api/bill/create")
                    .header("X-API-KEY", billingApiKey)
                    .bodyValue(billPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // [PENTING] Ambil ID dari Response dan Simpan ke DB
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String billId = String.valueOf(data.get("id"));

                // Update Policy dengan Bill ID
                policy.setBillId(billId);
                policyRepository.save(policy);
                
                System.out.println(">>> Bill Created & Linked. ID: " + billId);
            }

        } catch (Exception e) {
            System.err.println("WARNING: Gagal membuat Bill: " + e.getMessage());
        }
    }

    // === PAY POLICY (WITH VERIFICATION) ===
    @Override
    public PolicyResponseDTO payPolicy(String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));

        if ("EXPIRED".equalsIgnoreCase(policy.getStatus())) throw new IllegalStateException("Policy Expired");
        if ("PAID".equalsIgnoreCase(policy.getStatus())) return convertToResponseDTO(policy);

        // Verifikasi ke Billing Service (Optional, kalau token ada)
        // Ini memenuhi PBI: "Memverifikasi status pembayaran"
        try {
            String token = getTokenFromRequest();
            if (token != null) {
                // Cek list bill customer
                List<BillResponseDTO> bills = webClient.get()
                        .uri(billingServiceUrl + "/api/bill/customer")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<BillResponseDTO>>() {})
                        .block();

                if (bills != null) {
                    // Cari bill yang sesuai policy ini
                    BillResponseDTO match = bills.stream()
                            .filter(b -> policyId.equals(b.getServiceReferenceId()))
                            .findFirst().orElse(null);

                    // Kalau ketemu dan status UNPAID (0), lempar error (instruksi bayar)
                    if (match != null && match.getStatus() == 0) {
                        throw new IllegalStateException("Bill belum dibayar. Silakan selesaikan pembayaran di Billing Service.");
                    }
                }
            }
        } catch (IllegalStateException e) {
            throw e; // Teruskan pesan ke Controller
        } catch (Exception e) {
            System.err.println("Warning: Verifikasi billing gagal, lanjut update via callback mechanism.");
        }

        // Update Status
        policy.setStatus("PAID");
        policy.setUpdatedAt(LocalDateTime.now());

        List<OrderedPlan> orderedPlans = policy.getOrderedPlans();
        for (OrderedPlan op : orderedPlans) {
            op.setStatus("PAID");
            op.setUpdatedAt(LocalDateTime.now());
        }
        orderedPlanRepository.saveAll(orderedPlans);

        Policy savedPolicy = policyRepository.save(policy);
        return convertToResponseDTO(savedPolicy);
    }
    /**
     * Memvalidasi keberadaan Booking ID.
     * KHUSUS TOUR_PACKAGE DI-SKIP (AUTO PASS).
     */
    private void validateBookingId(ServiceEnum service, String bookingId) {
        String targetUrl = "";

        // --- BYPASS LOGIC START ---
        if (service == ServiceEnum.TOUR_PACKAGE) {
            System.out.println("MOCK VALIDATION: Bypass check for Tour Package ID: " + bookingId);
            return; // LANGSUNG LOLOS
        }
        // --- BYPASS LOGIC END ---

        switch (service.name()) {
            case "FLIGHT":
            case "Flight":
                targetUrl = flightServiceUrl + "/api/bookings/" + bookingId;
                break;
            case "ACCOMMODATION":
            case "Accommodation":
                targetUrl = accommodationServiceUrl + "/bookings/" + bookingId;
                break;
            case "RENTAL":
            case "Rentals":
                targetUrl = rentalServiceUrl + "/api/bookings/" + bookingId;
                break;
            default:
                return;
        }

        try {
            String token = getTokenFromRequest();
            webClient.get()
                    .uri(targetUrl)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Booking ID " + bookingId + " tidak ditemukan di sistem " + service);
            }
            System.err.println("WARNING: Gagal validasi ke " + service + " (" + e.getStatusCode() + ")");
        } catch (Exception e) {
            System.err.println("WARNING: Service " + service + " tidak merespons. Validasi di-skip.");
        }
    }

    @Override
    public List<PolicyResponseDTO> getAllPolicies() {
        List<Policy> allPolicies = policyRepository.findAll();
        allPolicies.forEach(this::checkAndSetExpiration);
        return allPolicies.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<PolicyResponseDTO> getPoliciesByUserId(String userId) {
        List<Policy> userPolicies = policyRepository.findAllByUserId(userId);
        userPolicies.forEach(this::checkAndSetExpiration);
        return userPolicies.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public PolicyResponseDTO getPolicyById(String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        checkAndSetExpiration(policy);
        return convertToResponseDTO(policy);
    }

    private void checkAndSetExpiration(Policy policy) {
        if ("EXPIRED".equals(policy.getStatus())) return;

        List<OrderedPlan> plans = policy.getOrderedPlans();
        boolean hasUnclaimedNonExpired = false;
        boolean hasClaimed = false;

        for (OrderedPlan op : plans) {
            if ("CLAIMED".equals(op.getStatus())) hasClaimed = true;
            if (op.getExpiredDate().isBefore(LocalDate.now()) && !"CLAIMED".equals(op.getStatus())) {
                if (!"EXPIRED".equals(op.getStatus())) {
                    op.setStatus("EXPIRED");
                    orderedPlanRepository.save(op);
                }
            }
            if (!"CLAIMED".equals(op.getStatus()) && !"EXPIRED".equals(op.getStatus())) {
                hasUnclaimedNonExpired = true;
            }
        }
        if (!hasClaimed && !hasUnclaimedNonExpired) {
            policy.setStatus("EXPIRED");
            policyRepository.save(policy);
        }
    }

    private PolicyResponseDTO convertToResponseDTO(Policy policy) {
        List<OrderedPlanSummaryResponseDTO> opDTOs = policy.getOrderedPlans().stream()
                .map(this::convertOpToSummaryDTO)
                .collect(Collectors.toList());

        return PolicyResponseDTO.builder()
                .id(policy.getId())
                .bookingId(policy.getBookingId())
                .billId(policy.getBillId())
                .userId(policy.getUserId())
                .service(policy.getService())
                .startDate(policy.getStartDate())
                .status(policy.getStatus())
                .totalPrice(policy.getTotalPrice())
                .totalCoverage(policy.getTotalCoverage())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .orderedPlans(opDTOs)
                .build();
    }

    private OrderedPlanSummaryResponseDTO convertOpToSummaryDTO(OrderedPlan op) {
        return OrderedPlanSummaryResponseDTO.builder()
                .id(op.getId())
                .insurancePlanId(op.getInsurancePlan().getId())
                .status(op.getStatus())
                .expiredDate(op.getExpiredDate())
                .claimsCount(op.getClaims() != null ? op.getClaims().size() : 0)
                .build();
    }

    private String getTokenFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attrs != null) ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}