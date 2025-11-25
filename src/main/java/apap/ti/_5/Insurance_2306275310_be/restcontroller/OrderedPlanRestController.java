package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.OrderedPlanService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ordered-plan")
public class OrderedPlanRestController {

    private final OrderedPlanService orderedPlanService;

    // PBI-BE-I12 (atau I13 di backlog baru): GET Detail Ordered Plan
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<OrderedPlanDetailResponseDTO>> getOrderedPlanDetail(
            @PathVariable("id") String id
    ) {
        var response = new BaseResponseDTO<OrderedPlanDetailResponseDTO>();
        try {
            OrderedPlanDetailResponseDTO data = orderedPlanService.getOrderedPlanDetailById(id);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Ordered Plan detail retrieved successfully.");
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
}