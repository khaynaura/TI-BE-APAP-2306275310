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

/**
 * Implementasi dari interface {@link PolicyService}.
 * Menangani logika bisnis terkait pembuatan, pengambilan, dan pembayaran polis asuransi.
 */
@Service
@Transactional
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final InsurancePlanRepository insurancePlanRepository;
    private final OrderedPlanRepository orderedPlanRepository;
    private final WebClient webClient;

    // URL Service Eksternal dari application.yml
    @Value("${billing.service.url}")
    private String billingServiceUrl;

    @Value("${flight.service.url}")
    private String flightServiceUrl;

    @Value("${accommodation.service.url}") // Accommodation
    private String accommodationServiceUrl;

    @Value("${rental.service.url}")
    private String rentalServiceUrl;

    @Value("${package.service.url}")
    private String packageServiceUrl;

    public PolicyServiceImpl(PolicyRepository policyRepository,
                             InsurancePlanRepository insurancePlanRepository,
                             OrderedPlanRepository orderedPlanRepository,
                             WebClient.Builder webClientBuilder) {
        this.policyRepository = policyRepository;
        this.insurancePlanRepository = insurancePlanRepository;
        this.orderedPlanRepository = orderedPlanRepository;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Membuat polis asuransi baru berdasarkan permintaan user.
     * Melakukan validasi Booking ID ke service eksternal, membuat OrderedPlan, dan mengirim tagihan ke Billing Service.
     *
     * @param createDTO Data transfer object berisi detail polis yang akan dibuat.
     * @return {@link PolicyResponseDTO} yang berisi data polis yang berhasil dibuat.
     * @throws IllegalArgumentException Jika ID booking tidak valid atau plan tidak sesuai.
     */
    @Override
    public PolicyResponseDTO createPolicy(CreatePolicyRequestDTO createDTO) {

        // 1. Validasi Booking ID ke Service Lain (Accommodation, Flight, dll)
        validateBookingId(createDTO.getService(), createDTO.getBookingId());

        // 2. Validasi & Ambil Insurance Plans
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

        // 3. Buat Object Policy
        Policy policy = new Policy();
        policy.setId("POL" + (policyRepository.count() + 1)); // ID: POL1, POL2...
        policy.setUserId(createDTO.getUserId());
        policy.setBookingId(createDTO.getBookingId());
        policy.setService(createDTO.getService());
        policy.setStartDate(LocalDate.now());
        policy.setStatus("CREATED"); // Awalnya CREATED
        policy.setTotalPrice(totalPrice);
        policy.setTotalCoverage(totalCoverage);

        // 4. Buat Object OrderedPlan
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

        // Simpan ke DB
        policy.setOrderedPlans(new ArrayList<>());
        Policy savedPolicy = policyRepository.save(policy);

        List<OrderedPlan> savedOrderedPlans = orderedPlanRepository.saveAll(orderedPlans);
        savedPolicy.setOrderedPlans(savedOrderedPlans);

        // 5. Integrasi Billing (Create Tagihan)
        createBill(savedPolicy);

        return convertToResponseDTO(savedPolicy);
    }

    /**
     * Memvalidasi keberadaan Booking ID di service eksternal terkait.
     *
     * @param service   Jenis layanan (Flight, Accommodation, dll).
     * @param bookingId ID Booking yang akan divalidasi.
     */
    private void validateBookingId(ServiceEnum service, String bookingId) {
        String targetUrl = "";

        switch (service.name()) {
            case "FLIGHT":
            case "Flight":
                targetUrl = flightServiceUrl + "/api/flight-booking/" + bookingId;
                break;
            case "ACCOMMODATION":
            case "Accommodation":
                targetUrl = accommodationServiceUrl + "/bookings/" + bookingId;
                break;
            case "RENTAL":
            case "Rentals":
                targetUrl = rentalServiceUrl + "/api/rental-booking/" + bookingId;
                break;
            // case "PACKAGE":
            // case "Tour Package":
            //     targetUrl = packageServiceUrl + "/api/package-booking/" + bookingId;
            //     break;
            default:
                return; // Skip validasi kalau service lain
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

    /**
     * Mengirim permintaan pembuatan tagihan ke Billing Service.
     *
     * @param policy Data polis yang akan dibuatkan tagihannya.
     */
    private void createBill(Policy policy) {
        try {
            Map<String, Object> billPayload = new HashMap<>();
            billPayload.put("policyId", policy.getId());
            billPayload.put("bookingId", policy.getBookingId());
            billPayload.put("amount", policy.getTotalPrice());
            billPayload.put("description", "Asuransi " + policy.getService());

            String token = getTokenFromRequest();

            webClient.post()
                    .uri(billingServiceUrl + "/api/bill/create")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(billPayload)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

        } catch (Exception e) {
            System.err.println("INFO: Gagal membuat Bill (Mungkin Service Billing mati/belum siap).");
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

    /**
     * Memproses pembayaran polis. Mengubah status polis dan ordered plan menjadi PAID.
     *
     * @param policyId ID polis yang akan dibayar.
     * @return {@link PolicyResponseDTO} yang telah diperbarui.
     */
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

    /**
     * Memeriksa dan memperbarui status kadaluarsa (EXPIRED) untuk polis dan ordered plan.
     *
     * @param policy Objek polis yang akan diperiksa.
     */
    private void checkAndSetExpiration(Policy policy) {
        if ("EXPIRED".equals(policy.getStatus())) return;

        List<OrderedPlan> plans = policy.getOrderedPlans();
        boolean hasUnclaimedNonExpired = false;
        boolean hasClaimed = false;

        for (OrderedPlan op : plans) {
            if ("CLAIMED".equals(op.getStatus())) {
                hasClaimed = true;
            }

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