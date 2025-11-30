package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.*;
import apap.ti._5.Insurance_2306275310_be.repository.ClaimRepository;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private OrderedPlanRepository orderedPlanRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ClaimServiceImpl claimService;

    // Dummy Data
    private OrderedPlan orderedPlan;
    private Policy policy;
    private InsurancePlan insurancePlan;
    private Claim claim;

    @BeforeEach
    void setUp() {
        // Setup dasar untuk setiap test
        insurancePlan = new InsurancePlan();
        insurancePlan.setId("INS-1");
        insurancePlan.setPlanName("Sehat Selalu");

        policy = new Policy();
        policy.setId("POL-1");
        policy.setUserId("USER-123");
        policy.setStatus("PAID");

        orderedPlan = new OrderedPlan();
        orderedPlan.setId("OP-1");
        orderedPlan.setStatus("PAID");
        orderedPlan.setExpiredDate(LocalDate.now().plusDays(30)); // Masih aktif
        orderedPlan.setInsurancePlan(insurancePlan);
        orderedPlan.setPolicy(policy);
        orderedPlan.setClaims(new ArrayList<>()); // List claim kosong

        policy.setOrderedPlans(new ArrayList<>(List.of(orderedPlan))); // Link policy ke OP

        claim = new Claim();
        claim.setId("OP-1-CLAIM1");
        claim.setStatus("WAITING_FOR_REVIEW");
        claim.setCreatedAt(LocalDateTime.now());
        claim.setOrderedPlan(orderedPlan);
    }

    // --- TEST: getAllClaimsFiltered ---

    @Test
    void testGetAllClaimsFiltered_FilterByStatusAndPlan() {
        // Branch: if (filterByStatus && filterByPlan)
        when(claimRepository.findAllByStatusAndOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc("ACCEPTED", "INS-1"))
                .thenReturn(List.of(claim));

        List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered("ACCEPTED", "INS-1");
        
        assertEquals(1, result.size());
        verify(claimRepository).findAllByStatusAndOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc("ACCEPTED", "INS-1");
    }

    @Test
    void testGetAllClaimsFiltered_FilterByStatusOnly() {
        // Branch: else if (filterByStatus)
        when(claimRepository.findAllByStatusOrderByCreatedAtDesc("ACCEPTED"))
                .thenReturn(List.of(claim));

        List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered("ACCEPTED", null);
        
        assertEquals(1, result.size());
        verify(claimRepository).findAllByStatusOrderByCreatedAtDesc("ACCEPTED");
    }

    @Test
    void testGetAllClaimsFiltered_FilterByPlanOnly() {
        // Branch: else if (filterByPlan)
        when(claimRepository.findAllByOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc("INS-1"))
                .thenReturn(List.of(claim));

        List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered(null, "INS-1");
        
        assertEquals(1, result.size());
        verify(claimRepository).findAllByOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc("INS-1");
    }

    @Test
    void testGetAllClaimsFiltered_NoFilter() {
        // Branch: else (No Filter)
        when(claimRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(claim));

        List<ClaimSummaryResponseDTO> result = claimService.getAllClaimsFiltered(null, null);
        
        assertEquals(1, result.size());
        verify(claimRepository).findAllByOrderByCreatedAtDesc();
    }

    // --- TEST: getClaimById ---

    @Test
    void testGetClaimById_Success() {
        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        ClaimDetailResponseDTO res = claimService.getClaimById("C1");
        assertEquals(claim.getId(), res.getId());
    }

    @Test
    void testGetClaimById_NotFound() {
        when(claimRepository.findById("XX")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> claimService.getClaimById("XX"));
    }

    // --- TEST: createClaim (Sangat Penting untuk Coverage) ---

    @Test
    void testCreateClaim_Success() {
        CreateClaimRequestDTO req = new CreateClaimRequestDTO();
        req.setProof("http://bukti.jpg");

        when(orderedPlanRepository.findById("OP-1")).thenReturn(Optional.of(orderedPlan));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        ClaimDetailResponseDTO res = claimService.createClaim("OP-1", req);

        assertEquals("WAITING_FOR_REVIEW", res.getStatus());
        assertEquals("WAITING_FOR_REVIEW", orderedPlan.getStatus()); // Cek side effect
        verify(orderedPlanRepository).save(orderedPlan); // Pastikan status OP disimpan
    }

    @Test
    void testCreateClaim_Fail_OrderedPlanNotFound() {
        when(orderedPlanRepository.findById("OP-XX")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> claimService.createClaim("OP-XX", new CreateClaimRequestDTO()));
    }

    @Test
    void testCreateClaim_Fail_Expired() {
        orderedPlan.setExpiredDate(LocalDate.now().minusDays(1)); // Sudah expired kemarin
        when(orderedPlanRepository.findById("OP-1")).thenReturn(Optional.of(orderedPlan));

        assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP-1", new CreateClaimRequestDTO()));
    }

    @Test
    void testCreateClaim_Fail_StatusCreated() {
        orderedPlan.setStatus("CREATED"); // Belum dibayar
        when(orderedPlanRepository.findById("OP-1")).thenReturn(Optional.of(orderedPlan));

        assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP-1", new CreateClaimRequestDTO()));
    }

    @Test
    void testCreateClaim_Fail_StatusClaimed() {
        orderedPlan.setStatus("CLAIMED"); // Sudah selesai klaim sebelumnya
        when(orderedPlanRepository.findById("OP-1")).thenReturn(Optional.of(orderedPlan));

        assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP-1", new CreateClaimRequestDTO()));
    }

    @Test
    void testCreateClaim_Fail_StatusWaitingForReview() {
        orderedPlan.setStatus("WAITING_FOR_REVIEW"); // Masih ada yang pending
        when(orderedPlanRepository.findById("OP-1")).thenReturn(Optional.of(orderedPlan));

        assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP-1", new CreateClaimRequestDTO()));
    }

    @Test
    void testCreateClaim_Fail_MaxClaimsReached() {
        // Simulasi sudah ada 3 claim di list
        orderedPlan.getClaims().add(new Claim());
        orderedPlan.getClaims().add(new Claim());
        orderedPlan.getClaims().add(new Claim());

        when(orderedPlanRepository.findById("OP-1")).thenReturn(Optional.of(orderedPlan));
        
        Exception e = assertThrows(IllegalStateException.class, () -> claimService.createClaim("OP-1", new CreateClaimRequestDTO()));
        assertTrue(e.getMessage().contains("maximum number of 3 claims"));
    }

    // --- TEST: processClaim & updatePolicyStatus ---

    @Test
    void testProcessClaim_Accept_FullyClaimed() {
        // Skenario: Policy punya 1 plan saja, di-approve -> Policy jadi FULLY_CLAIMED
        ProcessClaimRequestDTO req = new ProcessClaimRequestDTO(true, "Oke ACC", null, null);

        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        
        claimService.processClaim("C1", req);

        assertEquals("ACCEPTED", claim.getStatus());
        assertEquals("CLAIMED", orderedPlan.getStatus());
        assertEquals("FULLY_CLAIMED", policy.getStatus()); // Validasi private method updatePolicyStatus
        
        verify(policyRepository).save(policy);
    }

    @Test
    void testProcessClaim_Accept_PartiallyClaimed() {
        // Skenario: Policy punya 2 plan. 1 di-claim, 1 masih PAID. -> Policy jadi PARTIALLY_CLAIMED
        OrderedPlan op2 = new OrderedPlan();
        op2.setStatus("PAID");
        policy.getOrderedPlans().add(op2); // Policy punya 2 plan sekarang

        ProcessClaimRequestDTO req = new ProcessClaimRequestDTO(true, "Oke ACC", null, null);

        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        claimService.processClaim("C1", req);

        assertEquals("ACCEPTED", claim.getStatus());
        assertEquals("CLAIMED", orderedPlan.getStatus());
        assertEquals("PARTIALLY_CLAIMED", policy.getStatus()); // Validasi private method updatePolicyStatus branch else
    }

    @Test
    void testProcessClaim_Reject_RevertToPaid() {
        // Skenario: Reject, tapi jumlah reject < 3 -> Status OrderedPlan balik ke PAID
        ProcessClaimRequestDTO req = new ProcessClaimRequestDTO(false, null, "Foto buram", "Tolong foto ulang");

        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        claimService.processClaim("C1", req);

        assertEquals("REJECTED", claim.getStatus());
        assertEquals("PAID", orderedPlan.getStatus()); // Revert
    }

    @Test
    void testProcessClaim_Reject_MaxLimitReached() {
        // Skenario: Reject ke-3 kalinya -> Status OrderedPlan jadi REJECTED (hangus)
        
        // Setup: Sudah ada 2 claim REJECTED sebelumnya
        Claim r1 = new Claim(); r1.setStatus("REJECTED");
        Claim r2 = new Claim(); r2.setStatus("REJECTED");
        orderedPlan.getClaims().add(r1);
        orderedPlan.getClaims().add(r2);
        // Note: 'claim' yang sedang diproses belum masuk list getClaims() secara otomatis di mock object,
        // tapi logika service menghitung filter dari list existing + setStatus claim saat ini.
        // Di code service: `claim` yang sedang diproses statusnya diubah jadi REJECTED dulu, baru count.
        // Namun karena `claim` di test ini adalah referensi objek yang sama dengan yang ada di `orderedPlan` (jika kita masukkan),
        // kita perlu hati-hati.
        
        // Cara paling aman mock logic filter:
        // Service: long rejectedCount = orderedPlan.getClaims().stream().filter(...).count();
        // Mari kita masukkan 'claim' yang sedang di test ke dalam list claims milik orderedPlan
        orderedPlan.getClaims().add(claim); 

        ProcessClaimRequestDTO req = new ProcessClaimRequestDTO(false, null, "Alasan", "Desc");

        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        claimService.processClaim("C1", req);

        // Claim ini jadi REJECTED. Total di list orderedPlan jadi 3 REJECTED.
        assertEquals("REJECTED", claim.getStatus());
        assertEquals("REJECTED", orderedPlan.getStatus()); // OrderedPlan hangus
    }

    @Test
    void testProcessClaim_Fail_ClaimNotFound() {
        when(claimRepository.findById("XX")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> 
            claimService.processClaim("XX", new ProcessClaimRequestDTO())
        );
    }

    @Test
    void testProcessClaim_Fail_NotWaitingForReview() {
        claim.setStatus("ACCEPTED"); // Status bukan WAITING
        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        
        assertThrows(IllegalStateException.class, () -> 
            claimService.processClaim("C1", new ProcessClaimRequestDTO())
        );
    }
    
    // --- TEST: updatePolicyStatus (Edge Case Null) ---
    // Karena methodnya private, kita test lewat trigger processClaim tapi simulasikan policy null (walau jarang terjadi)
    // Di code: `if (policy == null) return;`
    @Test
    void testUpdatePolicyStatus_PolicyNull_Safe() {
        orderedPlan.setPolicy(null); // Putus hubungan policy
        ProcessClaimRequestDTO req = new ProcessClaimRequestDTO(true, "ok", null, null);
        
        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenReturn(claim);
        
        // Seharusnya tidak error NullPointer
        assertDoesNotThrow(() -> claimService.processClaim("C1", req));
    }

    // --- TEST: isClaimOwner ---

    @Test
    void testIsClaimOwner_True() {
        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        assertTrue(claimService.isClaimOwner("C1", "USER-123"));
    }

    @Test
    void testIsClaimOwner_False_WrongUser() {
        when(claimRepository.findById("C1")).thenReturn(Optional.of(claim));
        assertFalse(claimService.isClaimOwner("C1", "ORANG-LAIN"));
    }

    @Test
    void testIsClaimOwner_False_ClaimNotFound() {
        when(claimRepository.findById("XX")).thenReturn(Optional.empty());
        assertFalse(claimService.isClaimOwner("XX", "USER-123"));
    }
}