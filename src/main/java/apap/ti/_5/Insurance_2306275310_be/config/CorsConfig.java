package apap.ti._5.Insurance_2306275310_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CorsConfig {

    // KARENA GAGAL FETCH, KITA AKAN ABAIKAN CORS_ALLOWED_ORIGINS DARI SECRET SEMENTARA
    // DAN MENGGUNAKAN WILDCARD (*) UNTUK MENGUJI KONEKSI INTERNAL.

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                
                // MENGGUNAKAN WILDCARD UNTUK MENGIZINKAN SEMUA ORIGIN.
                // JIKA INI BERHASIL, MASALAHNYA ADA PADA NILAI GITHUB SECRET CORS_ALLOWED_ORIGINS.
                registry.addMapping("/**")
                        .allowedOrigins("*") // Mengizinkan semua origin untuk debug
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .exposedHeaders("Authorization");
            }
        };
    }
}