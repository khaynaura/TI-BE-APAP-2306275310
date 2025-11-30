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

            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/policy/*/payment-callback").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/insurance-plan/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER", "CUSTOMER")
                
                .requestMatchers(HttpMethod.POST, "/api/insurance-plan/create").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.PUT, "/api/insurance-plan/update").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.DELETE, "/api/insurance-plan/delete/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.GET, "/api/insurance-plan/my-plans").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")
                .requestMatchers(HttpMethod.GET, "/api/claim").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER") 
                .requestMatchers(HttpMethod.PUT, "/api/claim/process/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")

                .requestMatchers(HttpMethod.POST, "/api/claim/submit/**").hasAnyRole("CUSTOMER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/policy/create").hasAnyRole("CUSTOMER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/policy/**").hasAnyRole("CUSTOMER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/ordered-plan/**").hasAnyRole("CUSTOMER", "SUPERADMIN")


                .requestMatchers(HttpMethod.GET, "/api/statistics/summary").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER", "CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/statistics/**").hasAnyRole("SUPERADMIN", "INSURANCE_PROVIDER")


                .requestMatchers("/api/external/providers").hasRole("SUPERADMIN")
                .requestMatchers("/api/external/customers").hasRole("SUPERADMIN")
   
                .requestMatchers("/api/external/bookings").authenticated() 

                // --- PAYMENT METHOD ---
                // 1. GET All (Boleh Customer & Admin) - Taruh paling atas!
                .requestMatchers(HttpMethod.GET, "/api/payment-method/all").hasAnyRole("CUSTOMER", "SUPERADMIN")

                // 2. Create, Update, Delete (Hanya Admin)
                .requestMatchers(HttpMethod.POST, "/api/payment-method/create").hasRole("SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/payment-method/*/status").hasRole("SUPERADMIN") // Pakai wildcard * untuk ID
                .requestMatchers(HttpMethod.DELETE, "/api/payment-method/*").hasRole("SUPERADMIN")

                // 3. (Opsional) Catch-all buat payment method sisanya ke Admin
                .requestMatchers("/api/payment-method/**").hasRole("SUPERADMIN")

                .requestMatchers(HttpMethod.GET, "/api/top-up/all").hasRole("SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/top-up/history/**").hasAnyRole("CUSTOMER", "SUPERADMIN")

                .requestMatchers(HttpMethod.GET, "/api/top-up/{id}").hasRole("SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/top-up/{id}").hasRole("SUPERADMIN")

                .requestMatchers(HttpMethod.POST, "/api/top-up/create").hasRole("CUSTOMER")

                .requestMatchers(HttpMethod.PUT, "/api/top-up/{id}/status").hasRole("SUPERADMIN")
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  
            "http://localhost:3000",  
            "http://2306275310-fe.hafizmuh.site"
        ));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}