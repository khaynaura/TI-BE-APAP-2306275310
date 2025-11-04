package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.InsurancePlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
// @CrossOrigin(origins = "http://localhost:5173") // Sesuaikan port Vue Anda
public class InsurancePlanRestController {

    @Autowired
    private InsurancePlanService insurancePlanService;

    // --- URL Constants (mengikuti gaya Anda) ---
    public static final String BASE_URL = "/insurance-plan";
    public static final String VIEW_PLAN = BASE_URL + "/{id}";
    public static final String CREATE_PLAN = BASE_URL + "/create";
    public static final String UPDATE_PLAN = BASE_URL + "/update";
    public static final String DELETE_PLAN = BASE_URL + "/delete/{id}";

    // --- Get All Plans ---
    @GetMapping(BASE_URL)
public ResponseEntity<BaseResponseDTO<List<InsurancePlanResponseDTO>>> getAllPlans(
        @RequestParam(value = "search", required = false) String search) {

    var baseResponseDTO = new BaseResponseDTO<List<InsurancePlanResponseDTO>>();

    try {
        List<InsurancePlanResponseDTO> listPlan = (search == null || search.isBlank())
                ? insurancePlanService.getAllPlans()
                : insurancePlanService.searchPlansByName(search);

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(listPlan);
        baseResponseDTO.setMessage((search == null || search.isBlank())
                ? "Semua Insurance Plan berhasil diambil"
                : "Hasil pencarian Insurance Plan berhasil diambil");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

    } catch (Exception ex) {
        baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

    @GetMapping(VIEW_PLAN)
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> getPlanById(@PathVariable("id") String id) {
        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        InsurancePlanResponseDTO plan = insurancePlanService.getPlanById(id);

        // Menggunakan pola null-check Anda
        if (plan == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Insurance Plan dengan ID " + id + " tidak ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(plan);
        baseResponseDTO.setMessage("Data Insurance Plan berhasil ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_PLAN)
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> createInsurancePlan(
            @Valid @RequestBody CreateInsurancePlanRequestDTO createDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString().trim());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            InsurancePlanResponseDTO plan = insurancePlanService.createInsurancePlan(createDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(plan);
            baseResponseDTO.setMessage("Data Insurance Plan berhasil dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_PLAN)
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> updateInsurancePlan(
            @Valid @RequestBody UpdateInsurancePlanRequestDTO updateDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString().trim());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            InsurancePlanResponseDTO plan = insurancePlanService.updateInsurancePlan(updateDTO);

            if (plan == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Insurance Plan dengan ID " + updateDTO.getId() + " tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(plan);
            baseResponseDTO.setMessage("Data Insurance Plan berhasil diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(DELETE_PLAN)
    public ResponseEntity<BaseResponseDTO<InsurancePlanResponseDTO>> deleteInsurancePlan(
            @PathVariable("id") String id) {
        
        var baseResponseDTO = new BaseResponseDTO<InsurancePlanResponseDTO>();

        try {
            InsurancePlanResponseDTO plan = insurancePlanService.softDeletePlan(id);

            if (plan == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Insurance Plan dengan ID " + id + " tidak ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(plan); // Mengembalikan data yg dihapus
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
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(BASE_URL + "/by-service")
    public ResponseEntity<BaseResponseDTO<List<InsurancePlanResponseDTO>>> getPlansByService(
            @RequestParam("service") ServiceEnum service) {
        
        var baseResponseDTO = new BaseResponseDTO<List<InsurancePlanResponseDTO>>();
        try {
            List<InsurancePlanResponseDTO> listPlan = insurancePlanService.getPlansByApplicableService(service);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(listPlan);
            baseResponseDTO.setMessage("Insurance Plans for service " + service + " retrieved.");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}