package apap.ti._5.Insurance_2306275310_be;

import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository; // <-- TAMBAH
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanSummaryResponseDTO; // <-- TAMBAH
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.ClaimService;
import apap.ti._5.Insurance_2306275310_be.restservice.InsurancePlanService;
import apap.ti._5.Insurance_2306275310_be.restservice.PolicyService;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate; // <-- TAMBAH
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class Insurance2306275310BeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Insurance2306275310BeApplication.class, args);
    }

    // @Bean
    // // @Profile("!test")
    public CommandLineRunner createDummyData(
            InsurancePlanService insurancePlanService,
            PolicyService policyService,
            ClaimService claimService,
            OrderedPlanRepository orderedPlanRepository,
            PolicyRepository policyRepository // <-- TAMBAH
    ) {
        return args -> {
            Faker faker = new Faker(Locale.of("id_ID"));
            ServiceEnum[] allServices = ServiceEnum.values();

            // ==================================================================
            // 1. Buat 20 Insurance Plan
            // ==================================================================
            System.out.println("Generating dummy insurance plans...");
            List<InsurancePlanResponseDTO> createdPlans = new ArrayList<>();
            // ... (KODE BAGIAN 1 SAMA SEPERTI SEBELUMNYA) ...
            for (int i = 0; i < 20; i++) {
                Name fakeName = faker.name();
                String planName = fakeName.firstName() + "-" + fakeName.lastName();
                String providerId = "PROV-" + faker.number().numberBetween(1, 50);
                Set<ServiceEnum> services = new HashSet<>();
                services.add(allServices[faker.random().nextInt(allServices.length)]);
                services.add(allServices[faker.random().nextInt(allServices.length)]);
                CreateInsurancePlanRequestDTO createDTO = CreateInsurancePlanRequestDTO.builder()
                        .providerId(providerId).planName(planName)
                        .price(faker.number().numberBetween(50000, 300000))
                        .coverage(faker.number().numberBetween(10000000, 50000000))
                        .coverageDetails(faker.lorem().paragraph(2))
                        .applicableService(new ArrayList<>(services))
                        .expiredByDays(faker.number().numberBetween(7, 90))
                        .build();
                createdPlans.add(insurancePlanService.createInsurancePlan(createDTO));
            }
            System.out.println(createdPlans.size() + " dummy insurance plans generated successfully.");

            // ==================================================================
            // 2. Buat 20 Policy (dan OrderedPlan)
            // ==================================================================
            System.out.println("Generating dummy policies...");
            List<PolicyResponseDTO> createdPolicies = new ArrayList<>();
            // ... (KODE BAGIAN 2 SAMA, TAPI JUMLAH JADI 20) ...
            for (int j = 0; j < 20; j++) {
                try {
                    String userId = "USER-" + faker.name().firstName().toUpperCase();
                    String bookingId = "BOOK-" + faker.number().digits(6);
                    ServiceEnum service = allServices[faker.random().nextInt(allServices.length)];
                    List<String> applicablePlanIds = createdPlans.stream()
                            .filter(p -> p.getApplicableService().contains(service))
                            .map(InsurancePlanResponseDTO::getId)
                            .collect(Collectors.toList());
                    if (applicablePlanIds.isEmpty()) continue;
                    Collections.shuffle(applicablePlanIds);
                    List<String> selectedPlanIds = applicablePlanIds.stream()
                            .limit(faker.number().numberBetween(1, 3)) // <-- Buat policy dgn 1-2 plan
                            .collect(Collectors.toList());
                    if (selectedPlanIds.isEmpty()) continue;
                    CreatePolicyRequestDTO policyDTO = CreatePolicyRequestDTO.builder()
                            .userId(userId).bookingId(bookingId).service(service)
                            .insurancePlanIds(selectedPlanIds).build();
                    PolicyResponseDTO newPolicy = policyService.createPolicy(policyDTO);
                    createdPolicies.add(newPolicy);
                    // Modifikasi createdAt untuk data Chart Statistik
                    List<OrderedPlan> ops = orderedPlanRepository.findAllById(
                        newPolicy.getOrderedPlans().stream()
                                .map(op -> op.getId())
                                .collect(Collectors.toList())
                    );
                    for (OrderedPlan op : ops) {
                        int monthsToSubtract = faker.number().numberBetween(0, 12); 
                        op.setCreatedAt(LocalDateTime.now().minusMonths(monthsToSubtract));
                        orderedPlanRepository.save(op);
                    }
                } catch (Exception e) { System.out.println("Failed to create dummy policy: " + e.getMessage()); }
            }
            System.out.println(createdPolicies.size() + " dummy policies generated. (Status: CREATED)");


            // ==================================================================
            // 3. "Bayar" 15 dari 20 Policy
            // ==================================================================
            System.out.println("Paying 15 dummy policies...");
            List<PolicyResponseDTO> paidPolicies = new ArrayList<>();
            Collections.shuffle(createdPolicies); 
            
            for (int k = 0; k < 15; k++) {
                if (k >= createdPolicies.size()) break; 
                PolicyResponseDTO policyToPay = createdPolicies.get(k);
                try {
                    PolicyResponseDTO paidPolicy = policyService.payPolicy(policyToPay.getId());
                    paidPolicies.add(paidPolicy);
                } catch (Exception e) { System.out.println("Failed to pay policy: " + e.getMessage()); }
            }
            System.out.println(paidPolicies.size() + " policies have been paid. (Status: PAID)");


            // ==================================================================
            // 4. Proses Claim untuk menghasilkan SEMUA STATUS
            // ==================================================================
            System.out.println("Generating varied claims and policy statuses...");
            Collections.shuffle(paidPolicies); // Acak lagi policy yg sudah dibayar

            int waitingCount = 0, acceptedCount = 0, rejectedCount = 0;
            int fullyClaimedCount = 0, partiallyClaimedCount = 0, rejectedPlanCount = 0;

            for (int l = 0; l < paidPolicies.size(); l++) {
                PolicyResponseDTO policy = paidPolicies.get(l);
                // Ambil 1 ordered plan dari policy ini
                String firstPlanId = policy.getOrderedPlans().get(0).getId();

                try {
                    // --- KASUS 1: FULLY_CLAIMED (l = 0, 1) ---
                    // Jadikan SEMUA ordered plan di policy ini jadi CLAIMED
                    if (l < 2) {
                        for (OrderedPlanSummaryResponseDTO opSummary : policy.getOrderedPlans()) {
                            ClaimDetailResponseDTO claim = claimService.createClaim(opSummary.getId(), 
                                new CreateClaimRequestDTO(faker.lorem().paragraph()));
                            claimService.processClaim(claim.getId(), 
                                new ProcessClaimRequestDTO(true, faker.lorem().sentence(), null, null));
                        }
                        fullyClaimedCount++;
                    } 
                    
                    // --- KASUS 2: PARTIALLY_CLAIMED (l = 2, 3) ---
                    // Jadikan HANYA 1 ordered plan jadi CLAIMED (jika policy punya > 1 plan)
                    else if (l < 4 && policy.getOrderedPlans().size() > 1) {
                         ClaimDetailResponseDTO claim = claimService.createClaim(firstPlanId, 
                                new CreateClaimRequestDTO(faker.lorem().paragraph()));
                         claimService.processClaim(claim.getId(), 
                                new ProcessClaimRequestDTO(true, faker.lorem().sentence(), null, null));
                        partiallyClaimedCount++;
                    }

                    // --- KASUS 3: ORDERED_PLAN REJECTED (l = 4, 5) ---
                    // Buat 3 claim di 1 plan, dan REJECT semuanya
                    else if (l < 6) {
                        for (int r = 0; r < 3; r++) {
                            ClaimDetailResponseDTO claim = claimService.createClaim(firstPlanId, 
                                new CreateClaimRequestDTO(faker.lorem().paragraph()));
                            claimService.processClaim(claim.getId(), 
                                new ProcessClaimRequestDTO(false, null, "Docs Incomplete", faker.lorem().sentence()));
                        }
                        rejectedPlanCount++;
                    }

                    // --- KASUS 4: WAITING_FOR_REVIEW (l = 6, 7, 8) ---
                    // Buat 1 claim dan biarkan WAITING
                    else if (l < 9) {
                        claimService.createClaim(firstPlanId, new CreateClaimRequestDTO(faker.lorem().paragraph()));
                        waitingCount++;
                    }

                    // --- KASUS 5: Sisanya (l > 8) biarkan PAID ---

                } catch (Exception e) {
                    System.out.println("Failed to submit/process claim: " + e.getMessage());
                }
            }
            
            System.out.println("--- Varied Policy Status Generated ---");
            System.out.println("- FULLY_CLAIMED: " + fullyClaimedCount);
            System.out.println("- PARTIALLY_CLAIMED: " + partiallyClaimedCount);
            System.out.println("- (Policies with REJECTED OrderedPlan): " + rejectedPlanCount);
            System.out.println("- (Policies with WAITING_FOR_REVIEW Claim): " + waitingCount);

            // ==================================================================
            // 5. Buat data EXPIRED
            // ==================================================================
            System.out.println("Generating EXPIRED policies and plans...");
            // Ambil policy yg statusnya masih CREATED (belum dibayar)
            List<PolicyResponseDTO> unpaidPolicies = createdPolicies.stream()
                .filter(p -> !paidPolicies.contains(p))
                .collect(Collectors.toList());
            
            int expiredCount = 0;
            for (int m = 0; m < 2; m++) { // Ambil 2 policy
                if (m >= unpaidPolicies.size()) break;

                PolicyResponseDTO policyToExpire = unpaidPolicies.get(m);
                try {
                    // Ambil SEMUA ordered plan di policy tsb
                    for (OrderedPlanSummaryResponseDTO opSummary : policyToExpire.getOrderedPlans()) {
                        OrderedPlan op = orderedPlanRepository.findById(opSummary.getId()).orElse(null);
                        if (op != null) {
                            // Set tanggal expired-nya ke kemarin
                            op.setExpiredDate(LocalDate.now().minusDays(1));
                            orderedPlanRepository.save(op);
                        }
                    }
                    expiredCount++;
                } catch (Exception e) { System.out.println("Failed to set expired data: " + e.getMessage()); }
            }
            System.out.println(expiredCount + " policies (and their plans) set to expire.");
            System.out.println("--- All dummy data generated ---");
        };
    }
}