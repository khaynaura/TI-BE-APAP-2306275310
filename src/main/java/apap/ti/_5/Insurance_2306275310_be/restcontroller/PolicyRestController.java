package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PolicyService;
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
@RequestMapping("/api/policy")
public class PolicyRestController {

    @Autowired
    private PolicyService policyService;

    public static final String GET_ALL = "";
    public static final String GET_BY_ID = "/{id}";
    public static final String CREATE = "/create";
    public static final String PAY = "/pay/{id}";

    @PostMapping(CREATE)
    public ResponseEntity<BaseResponseDTO<PolicyResponseDTO>> createPolicy(
            @Valid @RequestBody CreatePolicyRequestDTO createDTO,
            BindingResult bindingResult
    ) {
        var response = new BaseResponseDTO<PolicyResponseDTO>();
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
            PolicyResponseDTO data = policyService.createPolicy(createDTO);
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Policy created successfully.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Server error: " + e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(GET_ALL)
    public ResponseEntity<BaseResponseDTO<List<PolicyResponseDTO>>> getAllPolicies() {
        var response = new BaseResponseDTO<List<PolicyResponseDTO>>();
        try {
            List<PolicyResponseDTO> data = policyService.getAllPolicies();
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

    @GetMapping(GET_BY_ID)
    public ResponseEntity<BaseResponseDTO<PolicyResponseDTO>> getPolicyById(@PathVariable("id") String id) {
        var response = new BaseResponseDTO<PolicyResponseDTO>();
        try {
            PolicyResponseDTO data = policyService.getPolicyById(id);
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

    @PutMapping(PAY)
    public ResponseEntity<BaseResponseDTO<PolicyResponseDTO>> payPolicy(@PathVariable("id") String id) {
        var response = new BaseResponseDTO<PolicyResponseDTO>();
        try {
            PolicyResponseDTO data = policyService.payPolicy(id);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Payment successful.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalStateException e) {
             response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Server error: " + e.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}