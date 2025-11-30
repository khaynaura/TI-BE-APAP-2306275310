package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.InsurancePlanService;
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
@RequestMapping("/api/insurance-plan")
public class InsurancePlanRestController {

    private final InsurancePlanService insurancePlanService;

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        
        Object principal = auth.getPrincipal();
        // Jika String (JWT Production)
        if (principal instanceof String) {
            return (String) principal;
        } 
        // Jika UserDetails (Unit Test @WithMockUser)
        else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        
        return principal.toString();
    }

    private boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER', 'CUSTOMER')")
    public ResponseEntity<BaseResponseDTO<List<InsurancePlanResponseDTO>>> getAllPlans(
            @RequestParam(value = "search", required = false) String search) {

        var baseResponseDTO = new BaseResponseDTO<List<InsurancePlanResponseDTO>>();
        try {
            List<InsurancePlanResponseDTO> listPlan = (search == null || search.isBlank())
                    ? insurancePlanService.getAllPlans()
                    : insurancePlanService.searchPlansByName(search);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(listPlan);
            baseResponseDTO.setMessage("Data Insurance Plan berhasil diambil");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-provider")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<List<InsurancePlanResponseDTO>>> getPlansByProvider(
            @RequestParam(required = false) String providerId) {

        var baseResponseDTO = new BaseResponseDTO<List<InsurancePlanResponseDTO>>();
        String targetProviderId;

        if (isSuperAdmin()) {
            if (providerId == null || providerId.isBlank()) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Superadmin wajib menyertakan parameter providerId");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
            targetProviderId = providerId;
        } else {
            targetProviderId = getCurrentUserId();
        }

        try {
            List<InsurancePlanResponseDTO> plans = insurancePlanService.getPlansByProviderId(targetProviderId);
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(plans);
            baseResponseDTO.setMessage("Data Insurance Plan milik Provider berhasil diambil");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER', 'CUSTOMER')")
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> getPlanById(@PathVariable("id") String id) {
        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();
        InsurancePlanResponseDTO plan = insurancePlanService.getPlanById(id);

        if (plan == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Insurance Plan dengan ID " + id + " tidak ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(plan);
        baseResponseDTO.setMessage("Detail Insurance Plan ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> createInsurancePlan(
            @Valid @RequestBody CreateInsurancePlanRequestDTO createDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString().trim());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        if (isSuperAdmin()) {
            if (createDTO.getProviderId() == null || createDTO.getProviderId().isBlank()) {
                baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                baseResponseDTO.setMessage("Superadmin wajib mengisi providerId");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
            }
        } else {
            createDTO.setProviderId(getCurrentUserId());
        }

        try {
            InsurancePlanResponseDTO plan = insurancePlanService.createInsurancePlan(createDTO);
            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(plan);
            baseResponseDTO.setMessage("Insurance Plan berhasil dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> updateInsurancePlan(
            @Valid @RequestBody UpdateInsurancePlanRequestDTO updateDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString().trim());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            // [VALIDASI KEPEMILIKAN]
            InsurancePlanResponseDTO existingPlan = insurancePlanService.getPlanById(updateDTO.getId());
            if (existingPlan == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Insurance Plan tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            // Jika bukan Superadmin, pastikan Provider ID sama dengan user yang login
            if (!isSuperAdmin() && !existingPlan.getProviderId().equals(getCurrentUserId())) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("Anda tidak memiliki izin untuk mengubah Plan ini.");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }

            InsurancePlanResponseDTO plan = insurancePlanService.updateInsurancePlan(updateDTO);
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(plan);
            baseResponseDTO.setMessage("Insurance Plan berhasil diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> deleteInsurancePlan(
            @PathVariable("id") String id) {
        
        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        try {
            // [VALIDASI KEPEMILIKAN]
            InsurancePlanResponseDTO existingPlan = insurancePlanService.getPlanById(id);
            if (existingPlan == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Insurance Plan tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            if (!isSuperAdmin() && !existingPlan.getProviderId().equals(getCurrentUserId())) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("Anda tidak memiliki izin untuk menghapus Plan ini.");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }

            InsurancePlanResponseDTO plan = insurancePlanService.softDeletePlan(id);
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(plan);
            baseResponseDTO.setMessage("Data Insurance Plan berhasil dihapus");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalStateException ex) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Gagal menghapus: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-service")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CUSTOMER')")
    public ResponseEntity<BaseResponseDTO<List<InsurancePlanResponseDTO>>> getPlansByService(
            @RequestParam("service") ServiceEnum service) {
        var baseResponseDTO = new BaseResponseDTO<List<InsurancePlanResponseDTO>>();
        try {
            List<InsurancePlanResponseDTO> listPlan = insurancePlanService.getPlansByApplicableService(service);
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(listPlan);
            baseResponseDTO.setMessage("Insurance Plans untuk service " + service + " berhasil diambil.");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Error: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}