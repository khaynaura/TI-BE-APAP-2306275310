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

    // PBI-BE-I15: GET Insurance Statistics (Admin & Provider)
    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'INSURANCE_PROVIDER')")
    public ResponseEntity<BaseResponseDTO<ChartDataResponseDTO>> getChartStatistics(
            @RequestParam("period") int timePeriod, 
            @RequestParam("service") String service 
    ) {
        var response = new BaseResponseDTO<ChartDataResponseDTO>();
        try {
            // --- LOGIKA BARU: CEK ROLE ---
            String providerId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Cek apakah user yang login adalah INSURANCE_PROVIDER
            boolean isProvider = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE_PROVIDER"));
            
            if (isProvider) {
                // Jika Provider, ambil ID-nya biar data difilter
                providerId = (String) auth.getPrincipal();
            }
            // Jika Superadmin, providerId tetap null (artinya ambil semua data)

            // Panggil service dengan parameter tambahan providerId
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
    
    // Summary Homepage (Gak perlu diubah, ini global summary)
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponseDTO<HomeSummaryResponseDTO>> getHomeSummary() {
        var baseResponseDTO = new BaseResponseDTO<HomeSummaryResponseDTO>();
        try {
            HomeSummaryResponseDTO summaryDTO = statisticsService.getHomeSummary();

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