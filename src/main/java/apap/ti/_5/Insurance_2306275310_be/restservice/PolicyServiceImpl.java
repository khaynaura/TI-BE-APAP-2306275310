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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${profile.service.url}") 
    private String accommodationServiceUrl;

    @Value("${rental.service.url}")
    private String rentalServiceUrl;

    @Value("${package.service.url}")
    private String packageServiceUrl;

    @Value("${insurance.api-key:super-secret-key-123}") 
    private String apiKey;

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
        validateBookingId(createDTO.getService(), createDTO.getBookingId());

        List<InsurancePlan> plans = insurancePlanRepository.findAllById(createDTO.getInsurancePlanIds());
        
        if (plans.size() != createDTO.getInsurancePlanIds().size()) {
            throw new IllegalArgumentException("Salah satu Insurance Plan ID tidak valid.");
        }

        for (InsurancePlan plan : plans) {
            if (!plan.getApplicableService().contains(createDTO.getService())) {
                throw new IllegalArgumentException("Plan '" + plan.getPlanName() + "' tidak cocok untuk layanan " + createDTO.getService());
            }
        }

        int totalPrice = plans.stream().mapToInt(InsurancePlan::getPrice).sum();
        int totalCoverage = plans.stream().mapToInt(InsurancePlan::getCoverage).sum();

        Policy policy = new Policy();
        policy.setId("POL" + (policyRepository.count() + 1));
        policy.setUserId(createDTO.getUserId());
        policy.setBookingId(createDTO.getBookingId());
        policy.setService(createDTO.getService());
        policy.setStartDate(LocalDate.now());
        policy.setStatus("CREATED");
        policy.setTotalPrice(totalPrice);
        policy.setTotalCoverage(totalCoverage);

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

        policy.setOrderedPlans(new ArrayList<>());
        Policy savedPolicy = policyRepository.save(policy);

        List<OrderedPlan> savedOrderedPlans = orderedPlanRepository.saveAll(orderedPlans);
        savedPolicy.setOrderedPlans(savedOrderedPlans);

        createBill(savedPolicy);

        return convertToResponseDTO(savedPolicy);
    }

    private void validateBookingId(ServiceEnum service, String bookingId) {
        String targetUrl = "";

        switch (service.name()) {
            case "FLIGHT": case "Flight":
                targetUrl = flightServiceUrl + "/api/flight-booking/" + bookingId;
                break;
            case "ACCOMMODATION": case "Accommodation":
                targetUrl = accommodationServiceUrl + "/api/bookings/" + bookingId; 
                break;
            case "RENTAL": case "Rentals":
                targetUrl = rentalServiceUrl + "/api/rental-booking/" + bookingId;
                break;
            case "PACKAGE": case "Tour Package":
                targetUrl = packageServiceUrl + "/api/package-booking/" + bookingId;
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

    private void createBill(Policy policy) {
        try {
            Map<String, Object> billPayload = new HashMap<>();
            billPayload.put("customerId", policy.getUserId());
            billPayload.put("serviceName", "Insurance");
            billPayload.put("serviceReferenceId", policy.getId());
            billPayload.put("description", "Insurance Payment for Booking " + policy.getBookingId());
            billPayload.put("amount", policy.getTotalPrice());
            
            Map response = webClient.post()
                    .uri(billingServiceUrl + "/api/bill/create")
                    .header("API-KEY", apiKey)
                    .bodyValue(billPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String billIdFromBilling = (String) data.get("id");
                
                policy.setBillId(billIdFromBilling);
                policyRepository.save(policy);
                
                System.out.println(">>> BILL CREATED. ID: " + billIdFromBilling);
            }

        } catch (Exception e) {
            System.err.println("INFO: Gagal membuat Bill (Service Billing mungkin mati/belum siap).");
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

    @Override
    public PolicyResponseDTO payPolicy(String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));

        if (!"CREATED".equalsIgnoreCase(policy.getStatus())) {
            return convertToResponseDTO(policy);
        }

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
                .userId(policy.getUserId())
                .billId(policy.getBillId())
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
        if (attrs != null) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}