package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.response.OptionDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProviderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExternalDataServiceImpl {

    private final WebClient webClient;

    // --- 1. INJECT SEMUA URL SERVICE ---
    @Value("${profile.service.url:http://localhost:8081/api}")
    private String profileServiceUrl;

    @Value("${accommodation.service.url:http://localhost:8087}")
    private String accommodationServiceUrl;

    @Value("${flight.service.url:http://localhost:8086}")
    private String flightServiceUrl;

    @Value("${rental.service.url:http://localhost:8088}")
    private String rentalServiceUrl;

    @Value("${package.service.url:http://localhost:8089}")
    private String packageServiceUrl;

    public ExternalDataServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // --- GET PROVIDERS (Admin) ---
    public List<ProviderDTO> getAllProviders() {
        String url = profileServiceUrl + "/api/users/endusers?role=INSURANCE_PROVIDER";
        String token = getTokenFromRequest();

        try {
            Map response = webClient.get().uri(url).header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve().bodyToMono(Map.class).block();

            if (response != null && response.get("data") != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");
                return dataList.stream().map(item -> new ProviderDTO(
                        (String) item.get("id"),
                        (String) item.get("name"),
                        (String) item.get("username")
                )).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Gagal fetch providers: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // --- GET CUSTOMERS (Admin) ---
    public List<OptionDTO> getAllCustomers() {
        String url = profileServiceUrl + "/api/users/endusers?role=CUSTOMER";
        String token = getTokenFromRequest();

        try {
            Map response = webClient.get().uri(url).header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve().bodyToMono(Map.class).block();

            if (response != null && response.get("data") != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");
                return dataList.stream().map(item -> new OptionDTO(
                        item.get("name") + " (" + item.get("username") + ")",
                        (String) item.get("id")
                )).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Gagal fetch customers: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // --- GET BOOKINGS BY SERVICE (Dropdown) ---
    public List<OptionDTO> getBookingsByService(ServiceEnum service) {
        String url = "";
        
        // Kunci JSON ID mungkin beda tiap service (misal: bookingID, id, flightBookingId)
        // Kita set defaultnya "id"
        String jsonIdKey = "id"; 

        // 2. TENTUKAN URL & KEY JSON BERDASARKAN SERVICE
        switch (service) {
            case ACCOMMODATION:
                // Endpoint: /api/bookings
                url = accommodationServiceUrl + "/api/bookings"; 
                jsonIdKey = "bookingID"; // Sesuai JSON Accommodation kamu
                break;
                
            case FLIGHT:
                // Asumsi Endpoint Flight
                url = flightServiceUrl + "/api/flight-bookings"; 
                jsonIdKey = "id"; // Sesuaikan dengan JSON temanmu
                break;
                
            case RENTALS:
                // Asumsi Endpoint Rental
                url = rentalServiceUrl + "/api/rental-bookings";
                jsonIdKey = "id";
                break;
                
            case TOUR_PACKAGE:
                // Asumsi Endpoint Package
                url = packageServiceUrl + "/api/package-bookings";
                jsonIdKey = "id";
                break;
                
            default:
                return new ArrayList<>();
        }

        String token = getTokenFromRequest();
        final String finalIdKey = jsonIdKey; // Variable final untuk lambda

        try {
            Map response = webClient.get().uri(url).header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve().bodyToMono(Map.class).block();

            if (response != null && response.get("data") != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");
                
                return dataList.stream().map(item -> {
                    // Ambil ID menggunakan Key yang sesuai
                    String bookingId = String.valueOf(item.get(finalIdKey));
                    return new OptionDTO(bookingId, bookingId);
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Gagal fetch bookings dari " + service + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private String getTokenFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attrs != null) ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}