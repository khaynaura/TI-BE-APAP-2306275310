package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PolicyService;
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
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/policy")
public class PolicyRestController {

    private final PolicyService policyService;

    // --- Helper Methods ---
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? (String) auth.getPrincipal() : null;
    }

    private boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    // --- Endpoints ---

    // PBI-BE-I9: Create Policy (Integrasi Service Lain + Billing)
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<PolicyResponseDTO>> createPolicy(
            @Valid @RequestBody CreatePolicyRequestDTO createDTO,
            BindingResult bindingResult
    ) {
        var response = new BaseResponseDTO<PolicyResponseDTO>();
        
        // 1. Validasi Input Format
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

        // 2. Set User ID dari Token
        createDTO.setUserId(getCurrentUserId());

        try {
            // 3. Panggil Service (Termasuk Validasi Booking ID & Create Bill)
            PolicyResponseDTO data = policyService.createPolicy(createDTO);
            
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Policy created successfully. Please proceed to payment.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Error Validasi (Booking ID tidak ditemukan, Plan salah, dll)
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            // Error Server / External Service Down
            e.printStackTrace();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Server error: " + e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PBI-BE-I10: Get All Policies
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<List<PolicyResponseDTO>>> getAllPolicies() {
        var response = new BaseResponseDTO<List<PolicyResponseDTO>>();
        try {
            List<PolicyResponseDTO> data;
            if (isSuperAdmin()) {
                // Superadmin lihat semua
                data = policyService.getAllPolicies();
            } else {
                // Customer lihat punya sendiri
                data = policyService.getPoliciesByUserId(getCurrentUserId());
            }

            response.setStatus(HttpStatus.OK.value());
            response.setMessage("All policies retrieved successfully.");
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

    // PBI-BE-I11: Get Policy Detail
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<PolicyResponseDTO>> getPolicyById(@PathVariable("id") String id) {
        var response = new BaseResponseDTO<PolicyResponseDTO>();
        try {
            PolicyResponseDTO data = policyService.getPolicyById(id);
            
            // [VALIDASI KEPEMILIKAN]
            if (!isSuperAdmin() && !data.getUserId().equals(getCurrentUserId())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage("Anda tidak memiliki akses ke Policy ini.");
                response.setTimestamp(new Date());
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Policy detail retrieved successfully.");
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

    // PBI-BE-I12: Callback Pembayaran (Public Endpoint dipanggil Billing Service)
    @PostMapping("/notify-payment")
    public ResponseEntity<BaseResponseDTO<Object>> receivePaymentNotification(@RequestBody Map<String, Object> payload) {
        var response = new BaseResponseDTO<>();
        try {
            // Asumsi payload dari Billing Service: { "refId": "POL1", "status": "PAID" }
            String policyId = (String) payload.get("refId"); 
            String status = (String) payload.get("status");

            if ("PAID".equalsIgnoreCase(status)) {
                policyService.payPolicy(policyId);
                response.setMessage("Payment processed successfully for Policy ID: " + policyId);
            } else {
                response.setMessage("Notification received. Status not PAID, ignored.");
            }

            response.setStatus(HttpStatus.OK.value());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error processing payment callback: " + e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}