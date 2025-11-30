package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restservice.ExternalDataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExternalDataRestController.class)
class ExternalDataRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ExternalDataServiceImpl externalDataService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetBookings() throws Exception {
        // Ambil enum pertama biar pasti aman (apapun namanya: HEALTH/KESEHATAN)
        ServiceEnum firstEnum = ServiceEnum.values()[0]; 
        
        when(externalDataService.getBookingsByService(any(ServiceEnum.class))).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/external/bookings?service=" + firstEnum.name()))
                .andExpect(status().isOk());
    }
}