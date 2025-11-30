package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.StatisticsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsRestController {

    private final StatisticsService statisticsService;

    // Helper untuk ambil ID User
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

    // Helper untuk ambil Role Pertama
    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().iterator().next().getAuthority();
        }
        return null;
    }

    // PBI-BE-I15: GET Insurance Statistics (Chart)
    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<ChartDataResponseDTO>> getChartStatistics(
            @RequestParam("period") int timePeriod, 
            @RequestParam("service") String service 
    ) {
        var response = new BaseResponseDTO<ChartDataResponseDTO>();
        try {
            // 1. Ambil Info User di Controller
            String userId = getCurrentUserId();
            String role = getCurrentUserRole();
            
            String providerId = null;

            // 2. Tentukan apakah perlu filter Provider ID
            if ("ROLE_INSURANCE_PROVIDER".equals(role)) {
                providerId = userId; // Kalau Provider, filter pakai ID dia
            }
            // Kalau Superadmin, providerId tetap null (lihat semua)

            // 3. Panggil Service (Service gak perlu cek SecurityContext lagi)
            ChartDataResponseDTO data = statisticsService.getChartStatistics(timePeriod, service, providerId);
            
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Chart data retrieved successfully.");
            response.setData(data);
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            response.setTimestamp(new Date());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Summary Homepage
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponseDTO<HomeSummaryResponseDTO>> getHomeSummary() {
        var baseResponseDTO = new BaseResponseDTO<HomeSummaryResponseDTO>();
        try {
            // 1. Ambil Info User di Controller
            String userId = getCurrentUserId();
            String role = getCurrentUserRole();

            // 2. Lempar ID dan Role ke Service
            HomeSummaryResponseDTO summaryDTO = statisticsService.getHomeSummary(userId, role);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(summaryDTO);
            baseResponseDTO.setMessage("Ringkasan data berhasil diambil");
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