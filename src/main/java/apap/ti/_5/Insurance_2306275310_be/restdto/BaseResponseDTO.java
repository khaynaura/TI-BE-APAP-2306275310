package apap.ti._5.Insurance_2306275310_be.restdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Wrapper standar untuk semua respons API.
 * Menjamin format JSON yang konsisten: { status, message, timestamp, data }.
 * @param <T> Tipe data konten (payload).
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseResponseDTO<T> {
    
    /** Kode status HTTP atau bisnis logic. */
    private int status;
    
    /** Pesan deskriptif (Success/Error message). */
    private String message;
    
    /** Waktu respons dibuat (Timezone Asia/Jakarta). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Jakarta")
    private Date timestamp;
    
    /** Payload data utama. */
    private T data;
}