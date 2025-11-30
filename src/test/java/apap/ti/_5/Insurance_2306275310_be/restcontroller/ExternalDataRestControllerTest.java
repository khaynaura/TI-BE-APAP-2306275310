package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.response.OptionDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProviderDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.ExternalDataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "profile.service.url=http://localhost:8081/api",
    "billing.service.url=http://localhost:8085/api",
    "accommodation.service.url=http://localhost:8087/api",
    "flight.service.url=http://localhost:8086/api",
    "rental.service.url=http://localhost:8088/api",
    "package.service.url=http://localhost:8089/api",
    "insurance.api-key=test-key",
    "frontend.url=http://localhost:5173"
})
@AutoConfigureMockMvc
public class ExternalDataRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ExternalDataServiceImpl externalDataService;

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetAllProviders_AsAdmin_Success() throws Exception {
        ProviderDTO provider = new ProviderDTO("1", "AXA", "axa_insurance");
        when(externalDataService.getAllProviders()).thenReturn(List.of(provider));

        mockMvc.perform(get("/api/external/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].username").value("axa_insurance"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllProviders_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/external/providers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetCustomers_AsAdmin_Success() throws Exception {
        OptionDTO customer = new OptionDTO("Budi", "uuid-budi");
        when(externalDataService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/external/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].value").value("uuid-budi"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetBookings_AsCustomer_Success() throws Exception {
        OptionDTO booking = new OptionDTO("BOOK-001", "BOOK-001");
        when(externalDataService.getBookingsByService(ServiceEnum.FLIGHT)).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/external/bookings").param("service", "FLIGHT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].label").value("BOOK-001"));
    }

    @Test
    // Tidak Login -> Harus 401/403
    void testGetBookings_Unauthenticated_Fail() throws Exception {
        mockMvc.perform(get("/api/external/bookings").param("service", "FLIGHT"))
                .andExpect(status().isForbidden()); // atau isUnauthorized tergantung config
    }
}