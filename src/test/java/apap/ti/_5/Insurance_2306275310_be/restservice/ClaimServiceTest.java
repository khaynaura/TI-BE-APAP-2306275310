// package apap.ti._5.Insurance_2306275310_be.restservice;

// import apap.ti._5.Insurance_2306275310_be.model.*;
// import apap.ti._5.Insurance_2306275310_be.repository.ClaimRepository;
// import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
// import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
// import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
// import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
// import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
// import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class ClaimServiceTest {

//     @Mock
//     private ClaimRepository claimRepository;

//     @Mock
//     private OrderedPlanRepository orderedPlanRepository;

//     @Mock
//     private PolicyRepository policyRepository;

//     @InjectMocks
//     private ClaimServiceImpl claimService;

//     private OrderedPlan orderedPlan;
//     private InsurancePlan insurancePlan;
//     private Claim claim;
//     private Policy policy;
//     private CreateClaimRequestDTO createDTO;
//     private ProcessClaimRequestDTO processAcceptDTO;
//     private ProcessClaimRequestDTO processRejectDTO;

//     @BeforeEach
//     void setUp() {
//         insurancePlan = InsurancePlan.builder().id("INS1").planName("Test Plan").build();

//         policy = new Policy();
//         policy.setId("POL1");
        
//         orderedPlan = OrderedPlan.builder()
//                 .id("OP1")
//                 .status("PAID") // Default status for claiming
//                 .expiredDate(LocalDate.now().plusDays(10)) // Not expired
//                 .insurancePlan(insurancePlan)
//                 .policy(policy)
//                 .claims(new ArrayList<>()) // No claims yet
//                 .build();
        
//         policy.setOrderedPlans(new ArrayList<>(List.of(orderedPlan)));

//         claim = Claim.builder()
//                 .id("OP1-CLAIM1")
//                 .status("WAITING_FOR_REVIEW")
//                 .proof("proof.jpg")
//                 .orderedPlan(orderedPlan)
//                 .createdAt(LocalDateTime.now().minusDays(1))
//                 .build();

//         createDTO = new CreateClaimRequestDTO("new_proof.jpg");
//         processAcceptDTO = new ProcessClaimRequestDTO(true, "Accepted", null, null);
//         processRejectDTO = new ProcessClaimRequestDTO(false, null, "REASON_A", "Desc A");
//     }

//     @Test
//     void testGetAllClaimsFiltered_NoFilters() {
//         when(claimRepository.findAll()).thenReturn(List.of(claim));
//         List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered(null, null);
//         assertEquals(1, result.size());
//         verify(claimRepository, times(1)).findAll();
//     }

//     @Test
//     void testGetAllClaimsFiltered_StatusOnly() {
//         when(claimRepository.findAllByStatus("WAITING_FOR_REVIEW")).thenReturn(List.of(claim));
//         List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered("WAITING_FOR_REVIEW", null);
//         assertEquals(1, result.size());
//         verify(claimRepository, times(1)).findAllByStatus(anyString());
//     }

//     @Test
//     void testGetAllClaimsFiltered_PlanIdOnly() {
//         when(claimRepository.findAllByOrderedPlan_InsurancePlan_Id("INS1")).thenReturn(List.of(claim));
//         List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered(null, "INS1");
//         assertEquals(1, result.size());
//         verify(claimRepository, times(1)).findAllByOrderedPlan_InsurancePlan_Id(anyString());
//     }

//     @Test
//     void testGetAllClaimsFiltered_BothFilters() {
//         when(claimRepository.findAllByStatusAndOrderedPlan_InsurancePlan_Id("WAITING_FOR_REVIEW", "INS1"))
//                 .thenReturn(List.of(claim));
//         List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered("WAITING_FOR_REVIEW", "INS1");
//         assertEquals(1, result.size());
//         verify(claimRepository, times(1)).findAllByStatusAndOrderedPlan_InsurancePlan_Id(anyString(), anyString());
//     }

//     @Test
//     void testGetClaimById_Success() {
//         when(claimRepository.findById("OP1-CLAIM1")).thenReturn(Optional.of(claim));
//         ClaimDetailResponseDTO result = claimService.getClaimById("OP1-CLAIM1");
//         assertNotNull(result);
//         assertEquals("OP1-CLAIM1", result.getId());
//     }

//     @Test
//     void testGetClaimById_NotFound() {
//         when(claimRepository.findById("BAD-ID")).thenReturn(Optional.empty());
//         Exception e = assertThrows(RuntimeException.class, () -> claimService.getClaimById("BAD-ID"));
//         assertEquals("Claim not found", e.getMessage());
//     }

//     @Test
//     void testCreateClaim_Success() {
//         when(orderedPlanRepository.findById("OP1")).thenReturn(Optional.of(orderedPlan));
//         when(claimRepository.save(any(Claim.class))).thenReturn(claim);

//         ClaimDetailResponseDTO result = claimService.createClaim("OP1", createDTO);

//         assertNotNull(result);
//         assertEquals("OP1-CLAIM1", result.getId());
//         assertEquals("WAITING_FOR_REVIEW", result.getStatus());
//         // Verify OrderedPlan status was updated
//         assertEquals("WAITING_FOR_REVIEW", orderedPlan.getStatus());
//         verify(orderedPlanRepository, times(1)).save(orderedPlan);
//     }

//     @Test
//     void testCreateClaim_Fail_OrderedPlanNotFound() {
//         when(orderedPlanRepository.findById("BAD-ID")).thenReturn(Optional.empty());
//         Exception e = assertThrows(RuntimeException.class, () -> claimService.createClaim("BAD-ID", createDTO));
//         assertEquals("Ordered Plan not found", e.getMessage());
//     }

//     @Test
//     void testCreateClaim_Fail_PlanExpired() {
//         orderedPlan.setExpiredDate(LocalDate.now().minusDays(1)); // Expired
//         when(orderedPlanRepository.findById("OP1")).thenReturn(Optional.of(orderedPlan));
//         Exception e = assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP1", createDTO));
//         assertEquals("Cannot submit claim, ordered plan has expired.", e.getMessage());
//     }

//     @Test
//     void testCreateClaim_Fail_AlreadyClaimed() {
//         orderedPlan.setStatus("CLAIMED");
//         when(orderedPlanRepository.findById("OP1")).thenReturn(Optional.of(orderedPlan));
//         Exception e = assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP1", createDTO));
//         assertEquals("Cannot submit claim, this plan has already been claimed.", e.getMessage());
//     }

//     @Test
//     void testCreateClaim_Fail_WaitingForReview() {
//         orderedPlan.setStatus("WAITING_FOR_REVIEW");
//         when(orderedPlanRepository.findById("OP1")).thenReturn(Optional.of(orderedPlan));
//         Exception e = assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP1", createDTO));
        

//         assertEquals("Cannot submit new claim, another claim is already waiting for review.", e.getMessage());
//     }

//     @Test
//     void testCreateClaim_Fail_MaxClaimsReached() {
//         orderedPlan.getClaims().addAll(List.of(new Claim(), new Claim(), new Claim()));
//         when(orderedPlanRepository.findById("OP1")).thenReturn(Optional.of(orderedPlan));
//         Exception e = assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP1", createDTO));
//         assertEquals("Cannot submit new claim, the maximum number of 3 claims has been reached.", e.getMessage());
//     }

//     @Test
//     void testProcessClaim_Fail_ClaimNotFound() {
//         when(claimRepository.findById("BAD-ID")).thenReturn(Optional.empty());
//         Exception e = assertThrows(RuntimeException.class, () -> claimService.processClaim("BAD-ID", processAcceptDTO));
//         assertEquals("Claim not found", e.getMessage());
//     }

//     @Test
//     void testProcessClaim_Fail_NotWaitingForReview() {
//         claim.setStatus("ACCEPTED");
//         when(claimRepository.findById("OP1-CLAIM1")).thenReturn(Optional.of(claim));
//         Exception e = assertThrows(IllegalStateException.class, () -> claimService.processClaim("OP1-CLAIM1", processAcceptDTO));
//         assertEquals("Claim is not waiting for review.", e.getMessage());
//     }

//     @Test
//     void testProcessClaim_Accept_PolicyPartiallyClaimed() {

//         OrderedPlan op2 = OrderedPlan.builder().id("OP2").status("PAID").build();
//         policy.getOrderedPlans().add(op2);
        
//         when(claimRepository.findById("OP1-CLAIM1")).thenReturn(Optional.of(claim));
//         when(claimRepository.save(any(Claim.class))).thenReturn(claim);

//         ClaimDetailResponseDTO result = claimService.processClaim("OP1-CLAIM1", processAcceptDTO);

//         assertEquals("ACCEPTED", result.getStatus());
//         assertNotNull(result.getAcceptedTimestamp());
//         assertEquals("Accepted", result.getAcceptedNote());
        
//         assertEquals("CLAIMED", orderedPlan.getStatus());
        
//         assertEquals("PARTIALLY_CLAIMED", policy.getStatus());
        
//         verify(orderedPlanRepository, times(1)).save(orderedPlan);
//         verify(policyRepository, times(1)).save(policy);
//     }

//     @Test
//     void testProcessClaim_Accept_PolicyFullyClaimed() {
//         when(claimRepository.findById("OP1-CLAIM1")).thenReturn(Optional.of(claim));
//         when(claimRepository.save(any(Claim.class))).thenReturn(claim);

//         ClaimDetailResponseDTO result = claimService.processClaim("OP1-CLAIM1", processAcceptDTO);

//         assertEquals("ACCEPTED", result.getStatus());
//         assertEquals("CLAIMED", orderedPlan.getStatus());
        
//         assertEquals("FULLY_CLAIMED", policy.getStatus());
        
//         verify(policyRepository, times(1)).save(policy);
//     }

//     @Test
//     void testProcessClaim_Reject_FirstRejection() {
//         when(claimRepository.findById("OP1-CLAIM1")).thenReturn(Optional.of(claim));
//         when(claimRepository.save(any(Claim.class))).thenReturn(claim);

//         ClaimDetailResponseDTO result = claimService.processClaim("OP1-CLAIM1", processRejectDTO);

//         assertEquals("REJECTED", result.getStatus());
//         assertNotNull(result.getRejectionTimestamp());
//         assertEquals("REASON_A", result.getRejectionReason());

//         assertEquals("PAID", orderedPlan.getStatus());
        
//         verify(orderedPlanRepository, times(1)).save(orderedPlan);
//         verify(policyRepository, never()).save(any()); 
//     }

//     @Test
//     void testProcessClaim_Reject_ThirdRejection() {
      
//         Claim r1 = Claim.builder().status("REJECTED").build();
//         Claim r2 = Claim.builder().status("REJECTED").build();

//         orderedPlan.getClaims().addAll(List.of(r1, r2, claim)); 
        
//         when(claimRepository.findById("OP1-CLAIM1")).thenReturn(Optional.of(claim));
//         when(claimRepository.save(any(Claim.class))).thenReturn(claim);

//         ClaimDetailResponseDTO result = claimService.processClaim("OP1-CLAIM1", processRejectDTO);

//         assertEquals("REJECTED", result.getStatus());
        
//         assertEquals("REJECTED", orderedPlan.getStatus());
        
//         verify(orderedPlanRepository, times(1)).save(orderedPlan);
//     }
// }