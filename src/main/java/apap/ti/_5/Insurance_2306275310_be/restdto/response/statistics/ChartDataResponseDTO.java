package apap.ti._5.Insurance_2306275310_be.restdto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataResponseDTO {
    private List<String> labels; 
    private List<Long> data;   
}