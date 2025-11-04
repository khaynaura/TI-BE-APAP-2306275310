package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.ClaimService;
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
@RequestMapping("/api/claim")
public class ClaimRestController {

    @Autowired
    private ClaimService claimService;

    public static final String GET_ALL_FILTERED = "";
    public static final String GET_BY_ID = "/{id}";
    public static final String SUBMIT_CLAIM = "/submit/{orderedPlanId}";
    public static final String PROCESS_CLAIM = "/process/{claimId}";


    @GetMapping(GET_ALL_FILTERED)
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


    @GetMapping(GET_BY_ID)
    public ResponseEntity<BaseResponseDTO<ClaimDetailResponseDTO>> getClaimById(@PathVariable("id") String id) {
        var response = new BaseResponseDTO<ClaimDetailResponseDTO>();
        try {
            ClaimDetailResponseDTO data = claimService.getClaimById(id);
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

    @PostMapping(SUBMIT_CLAIM)
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

    @PutMapping(PROCESS_CLAIM)
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