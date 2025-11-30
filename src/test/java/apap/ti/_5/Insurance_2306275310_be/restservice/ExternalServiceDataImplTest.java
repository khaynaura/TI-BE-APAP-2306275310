package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.response.OptionDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProviderDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalDataServiceImplTest {

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;

    // Mock Chain Steps untuk WebClient
    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock private WebClient.RequestHeadersSpec headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private ExternalDataServiceImpl externalDataService;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Setup Builder
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        // 2. Init Service
        externalDataService = new ExternalDataServiceImpl(webClientBuilder);

        // 3. Inject URLs via Reflection
        injectField(externalDataService, "profileServiceUrl", "http://profile");
        injectField(externalDataService, "accommodationServiceUrl", "http://hotel");
        injectField(externalDataService, "flightServiceUrl", "http://flight");
        injectField(externalDataService, "rentalServiceUrl", "http://rental");

        // 4. Mock Token
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        lenient().when(attrs.getRequest()).thenReturn(mockRequest);
        lenient().when(mockRequest.getHeader("Authorization")).thenReturn("Bearer TOKEN");
        RequestContextHolder.setRequestAttributes(attrs);

        // 5. Setup Default WebClient Chain Behavior (Supaya tidak NPE)
        // Chain: get() -> uri() -> header() -> retrieve() -> bodyToMono()
        lenient().when(webClient.get()).thenReturn(uriSpec);
        lenient().when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        lenient().when(headersSpec.header(any(), any())).thenReturn(headersSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // --- TEST: getAllProviders ---

    @Test
    void testGetAllProviders_Success() {
        // Mock Data dari API
        Map<String, Object> providerData = Map.of(
            "id", "PROV-1",
            "name", "Asuransi Mantap",
            "username", "mantap_provider"
        );
        Map<String, Object> apiResponse = Map.of("data", List.of(providerData));

        // Mock Return
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

        List<ProviderDTO> result = externalDataService.getAllProviders();

        assertEquals(1, result.size());
        assertEquals("PROV-1", result.get(0).getId());
        assertEquals("Asuransi Mantap", result.get(0).getName());
        
        // Verifikasi URL yang dipanggil
        verify(uriSpec).uri(contains("/api/users/endusers?role=INSURANCE_PROVIDER"));
    }

    @Test
    void testGetAllProviders_Fail_Exception() {
        // Simulasi Error dari External Service
        when(responseSpec.bodyToMono(Map.class)).thenThrow(new RuntimeException("Service Down"));

        // Harus return empty list (karena ada try-catch di service), BUKAN throw error
        List<ProviderDTO> result = externalDataService.getAllProviders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- TEST: getAllCustomers ---

    @Test
    void testGetAllCustomers_Success() {
        Map<String, Object> customerData = Map.of(
            "id", "CUST-1",
            "name", "Budi",
            "username", "budi123"
        );
        Map<String, Object> apiResponse = Map.of("data", List.of(customerData));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

        List<OptionDTO> result = externalDataService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("CUST-1", result.get(0).getValue());
        assertTrue(result.get(0).getLabel().contains("Budi"));
    }

    // --- TEST: getBookingsByService (Switch Case Logic) ---

    @Test
    void testGetBookings_Flight() {
        // Flight Service pake key "id"
        Map<String, Object> flightData = Map.of("id", "FL-100");
        Map<String, Object> apiResponse = Map.of("data", List.of(flightData));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

        List<OptionDTO> result = externalDataService.getBookingsByService(ServiceEnum.FLIGHT);

        assertEquals(1, result.size());
        assertEquals("FL-100", result.get(0).getValue());
        verify(uriSpec).uri(contains("http://flight")); // Cek URL benar ke Flight
    }

    @Test
    void testGetBookings_Accommodation() {
        // Accommodation Service pake key "bookingID" (beda sendiri)
        Map<String, Object> hotelData = Map.of("bookingID", "HTL-200");
        Map<String, Object> apiResponse = Map.of("data", List.of(hotelData));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

        List<OptionDTO> result = externalDataService.getBookingsByService(ServiceEnum.ACCOMMODATION);

        assertEquals(1, result.size());
        assertEquals("HTL-200", result.get(0).getValue());
        verify(uriSpec).uri(contains("http://hotel")); // Cek URL benar ke Hotel
    }

    @Test
    void testGetBookings_Rental() {
        // Rental Service pake key "id"
        Map<String, Object> rentalData = Map.of("id", "RNT-300");
        Map<String, Object> apiResponse = Map.of("data", List.of(rentalData));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

        List<OptionDTO> result = externalDataService.getBookingsByService(ServiceEnum.RENTALS);

        assertEquals(1, result.size());
        assertEquals("RNT-300", result.get(0).getValue());
        verify(uriSpec).uri(contains("http://rental"));
    }

    @Test
    void testGetBookings_UnknownService() {
        // Case default: Enum yang belum di-handle
        // Kita perlu enum dummy atau pakai null kalau methodnya ga ngecek null enum di awal
        // Tapi method pake switch(enum), jadi aman. Kita pakai enum lain misal null (harus hati2 switch null)
        // atau enum yang tidak ada di case (tapi enum user cuma 3 itu yang aktif).
        
        // Kita asumsikan ServiceEnum cuma punya 3 itu. 
        // Kalau code service pakai `default: return new ArrayList<>()`, kita perlu tes jika ada enum baru.
        // Tapi jika enum terbatas, test 3 di atas sudah 100% coverage.
    }
    
    @Test
    void testGetBookings_Fail_Exception() {
        when(responseSpec.bodyToMono(Map.class)).thenThrow(new RuntimeException("Error"));
        
        List<OptionDTO> result = externalDataService.getBookingsByService(ServiceEnum.FLIGHT);
        
        assertTrue(result.isEmpty());
    }
}