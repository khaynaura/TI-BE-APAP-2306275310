package apap.ti._5.Insurance_2306275310_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO sederhana untuk mengisi elemen Dropdown (Select Option) di Frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO {
    
    /** Teks yang ditampilkan ke user (contoh: "Budi Santoso"). */
    private String label;
    
    /** Nilai yang dikirim ke backend (contoh: UUID user). */
    private String value;
}