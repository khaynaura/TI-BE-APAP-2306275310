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

/**
 * Service khusus untuk mengambil data referensi (Dropdown) dari service eksternal.
 */
@Service
public class ExternalDataServiceImpl {

    private final WebClient webClient;

    @Value("${profile.service.url}")
    private String profileServiceUrl;

    @Value("${accommodation.service.url}")
    private String accommodationServiceUrl;

    @Value("${flight.service.url}")
    private String flightServiceUrl;

    @Value("${rental.service.url}")
    private String rentalServiceUrl;

    public ExternalDataServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Mengambil daftar Insurance Provider dari Profile Service.
     */
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

    /**
     * Mengambil daftar Customer dari Profile Service.
     */
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

    /**
     * Mengambil daftar Booking ID dari service terkait (Accommodation, Flight, dll).
     */
    public List<OptionDTO> getBookingsByService(ServiceEnum service) {
        String url = "";
        String jsonIdKey = "id";

        switch (service) {
            case ACCOMMODATION:
                url = accommodationServiceUrl + "/bookings";
                jsonIdKey = "bookingID";
                break;
            case FLIGHT:
                url = flightServiceUrl + "/api/flight-bookings";
                jsonIdKey = "id";
                break;
            case RENTALS:
                url = rentalServiceUrl + "/api/rental-bookings";
                jsonIdKey = "id";
                break;
            default:
                return new ArrayList<>();
        }

        String token = getTokenFromRequest();
        final String finalIdKey = jsonIdKey;

        try {
            Map response = webClient.get().uri(url).header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve().bodyToMono(Map.class).block();

            if (response != null && response.get("data") != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

                return dataList.stream().map(item -> {
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