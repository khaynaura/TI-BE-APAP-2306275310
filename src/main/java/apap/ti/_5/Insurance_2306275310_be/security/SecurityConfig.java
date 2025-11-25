package apap.ti._5.Insurance_2306275310_be.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Matikan CSRF karena kita pakai Token (bukan session browser biasa)
            .csrf(csrf -> csrf.disable())

            // 2. Aktifkan CORS dengan konfigurasi di bawah (agar Vue.js bisa masuk)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. Set Session jadi Stateless (Wajib untuk JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. Pasang Filter JWT kita sebelum filter bawaan Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 5. Aturan Akses (RBAC) sesuai PBI
            .authorizeHttpRequests(auth -> auth
                // --- PUBLIC & SYSTEM ENDPOINTS ---
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/policy/notify-payment").permitAll() // Callback dari Bill Service

                // --- INSURANCE PLAN ---
                // I1, I3: Read All & Detail (Semua User yang login boleh lihat)
                .requestMatchers(HttpMethod.GET, "/api/insurance-plan/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER", "CUSTOMER")
                
                // I2, I4, I5: Create, Update, Delete, My Plans (Hanya Admin & Provider)
                .requestMatchers(HttpMethod.POST, "/api/insurance-plan/create").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.PUT, "/api/insurance-plan/update").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.DELETE, "/api/insurance-plan/delete/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.GET, "/api/insurance-plan/my-plans").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")

                // --- CLAIMS ---
                // I7, I8: Read All & Process (Hanya Admin & Provider)
                .requestMatchers(HttpMethod.GET, "/api/claim").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER") 
                .requestMatchers(HttpMethod.PUT, "/api/claim/process/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")

                // I14: Create Claim (Hanya Customer & Admin)
                .requestMatchers(HttpMethod.POST, "/api/claim/submit/**").hasAnyRole("CUSTOMER", "SUPERADMIN")

                // --- POLICY & ORDERED PLAN ---
                // I9, I10, I11, I12: Create, Read, Detail (Customer & Admin)
                .requestMatchers(HttpMethod.POST, "/api/policy/create").hasAnyRole("CUSTOMER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/policy/**").hasAnyRole("CUSTOMER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/ordered-plan/**").hasAnyRole("CUSTOMER", "SUPERADMIN")

                // --- STATISTICS ---
                // I15: View Stats (Hanya Admin & Provider)
                .requestMatchers(HttpMethod.GET, "/api/statistics/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")

                // SISANYA WAJIB LOGIN
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // --- KONFIGURASI CORS (PENTING UNTUK FRONTEND) ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Daftar URL Frontend yang diizinkan
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vue.js Development
            "http://localhost:3000"   // Port alternatif
            // Nanti tambahkan URL deploy di sini, misal: "http://insurance.hafizmuh.site"
        ));
        
        // Izinkan Method apa saja
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Izinkan Header apa saja (terutama Authorization untuk kirim Token)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        
        // Izinkan credentials (cookies/auth headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Terapkan ke semua endpoint
        return source;
    }
}