package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.ClaimService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/claim")
public class ClaimRestController {

    private final ClaimService claimService;

    // --- HELPER METHODS UNTUK SECURITY ---
    
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? (String) auth.getPrincipal() : null;
    }

    private boolean isCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
    }

    // --- ENDPOINTS ---

    // PBI-BE-I7: GET All Claims (Hanya untuk Admin & Provider)
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<List<ClaimSummaryResponseDTO>>> getAllClaimsFiltered(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "planId", required = false) String planId
    ) {
        var response = new BaseResponseDTO<List<ClaimSummaryResponseDTO>>();
        try {
            List<ClaimSummaryResponseDTO> data = claimService.getAllClaimsFiltered(status, planId);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Claim data retrieved successfully.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Server error: " + e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET Detail Claim (Admin, Provider, Customer Pemilik)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER', 'CUSTOMER')")
    public ResponseEntity<BaseResponseDTO<ClaimDetailResponseDTO>> getClaimById(@PathVariable("id") String id) {
        var response = new BaseResponseDTO<ClaimDetailResponseDTO>();
        try {
            // 1. Ambil Data Claim
            ClaimDetailResponseDTO data = claimService.getClaimById(id);
            
            // 2. VALIDASI KEPEMILIKAN KHUSUS CUSTOMER
            // Jika yang request adalah Customer, cek apakah dia pemilik claim ini?
            if (isCustomer()) {
                String currentUserId = getCurrentUserId();
                
                // Pastikan method isClaimOwner sudah ada di ClaimService interface & impl!
                boolean isOwner = claimService.isClaimOwner(id, currentUserId); 
                
                if (!isOwner) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setMessage("Anda tidak memiliki izin untuk melihat Claim ini.");
                    response.setTimestamp(new Date());
                    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                }
            }

            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Claim detail retrieved successfully.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // PBI-BE-I14: POST Submit Claim (Customer, Admin)
    @PostMapping("/submit/{orderedPlanId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<ClaimDetailResponseDTO>> submitClaim(
            @PathVariable("orderedPlanId") String orderedPlanId,
            @Valid @RequestBody CreateClaimRequestDTO createDTO,
            BindingResult bindingResult
    ) {
        var response = new BaseResponseDTO<ClaimDetailResponseDTO>();
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.append(error.getDefaultMessage()).append("; ");
            }
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(errors.toString());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            ClaimDetailResponseDTO data = claimService.createClaim(orderedPlanId, createDTO);
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Claim submitted successfully.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage(e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PBI-BE-I8: PUT Process Claim (Admin, Provider)
    @PutMapping("/process/{claimId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<ClaimDetailResponseDTO>> processClaim(
            @PathVariable("claimId") String claimId,
            @Valid @RequestBody ProcessClaimRequestDTO processDTO,
            BindingResult bindingResult
    ) {
        var response = new BaseResponseDTO<ClaimDetailResponseDTO>();
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.append(error.getDefaultMessage()).append("; ");
            }
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(errors.toString());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            ClaimDetailResponseDTO data = claimService.processClaim(claimId, processDTO);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Claim processed successfully.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to process claim: " + e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}