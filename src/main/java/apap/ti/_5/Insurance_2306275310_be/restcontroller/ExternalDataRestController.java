package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.OptionDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProviderDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.ExternalDataServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/external")
@AllArgsConstructor
public class ExternalDataRestController {

    private final ExternalDataServiceImpl externalDataService;

    // 1. Providers (Untuk Create Plan - Admin)
    @GetMapping("/providers")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<List<ProviderDTO>>> getAllProviders() {
        var res = new BaseResponseDTO<List<ProviderDTO>>();
        res.setData(externalDataService.getAllProviders());
        res.setStatus(200);
        res.setMessage("Success fetch providers");
        return ResponseEntity.ok(res);
    }

    // 2. Customers (Untuk Create Policy - Admin)
    @GetMapping("/customers")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<List<OptionDTO>>> getCustomers() {
        var res = new BaseResponseDTO<List<OptionDTO>>();
        res.setData(externalDataService.getAllCustomers());
        res.setStatus(200);
        res.setMessage("Success fetch customers");
        return ResponseEntity.ok(res);
    }

    // 3. Bookings (Untuk Create Policy - Admin & Customer)
    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponseDTO<List<OptionDTO>>> getBookings(@RequestParam ServiceEnum service) {
        var res = new BaseResponseDTO<List<OptionDTO>>();
        res.setData(externalDataService.getBookingsByService(service));
        res.setStatus(200);
        res.setMessage("Success fetch bookings");
        return ResponseEntity.ok(res);
    }
}