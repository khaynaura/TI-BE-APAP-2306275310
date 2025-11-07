package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.BaseResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.StatisticsService;
import lombok.AllArgsConstructor; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public static final String SUMMARY_URL = "/summary";
    public static final String CHART_URL = "/chart";

    @GetMapping(SUMMARY_URL)
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

    @GetMapping(CHART_URL)
    public ResponseEntity<BaseResponseDTO<ChartDataResponseDTO>> getChartStatistics(
            @RequestParam("period") int timePeriod, 
            @RequestParam("service") String service 
    ) {
        var response = new BaseResponseDTO<ChartDataResponseDTO>();
        try {
            ChartDataResponseDTO data = statisticsService.getChartStatistics(timePeriod, service);
            
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
    
}
