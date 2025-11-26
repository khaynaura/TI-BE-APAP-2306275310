package apap.ti._5.Insurance_2306275310_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO {
    private String label; // Contoh: "Budi Santoso (budi01)" atau "BOOK-ACC-001"
    private String value; // Contoh: "uuid-budi" atau "BOOK-ACC-001"
}