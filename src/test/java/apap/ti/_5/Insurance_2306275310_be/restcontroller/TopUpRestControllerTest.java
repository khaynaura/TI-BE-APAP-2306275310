package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.TopUpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
public class TopUpRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private TopUpService topUpService;
    @Autowired private ObjectMapper objectMapper;

    // 1. Test Get All (Superadmin)
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetAllTransactions_Success() throws Exception {
        when(topUpService.getAllTransactions()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/top-up/all"))
                .andExpect(status().isOk());
    }

    // 2. Test Get History - SUPERADMIN (Bypass Validasi ID)
    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testGetHistory_AsSuperadmin_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        when(topUpService.getHistoryByUserId(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/top-up/history/" + userId))
                .andExpect(status().isOk());
    }

    // 3. Test Get History - CUSTOMER (Own ID) -> Success
    @Test
    @WithMockUser(username = "c06102e8-9316-4ce7-b5cc-cecb96dee8ad", roles = "CUSTOMER")
    void testGetHistory_AsCustomer_OwnData_Success() throws Exception {
        UUID userId = UUID.fromString("c06102e8-9316-4ce7-b5cc-cecb96dee8ad");
        when(topUpService.getHistoryByUserId(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/top-up/history/" + userId))
                .andExpect(status().isOk());
    }

    // 4. Test Get History - CUSTOMER (Other ID) -> Forbidden
    @Test
    @WithMockUser(username = "maling", roles = "CUSTOMER")
    void testGetHistory_AsCustomer_OtherData_Forbidden() throws Exception {
        UUID userId = UUID.randomUUID(); // ID Orang lain
        mockMvc.perform(get("/api/top-up/history/" + userId))
                .andExpect(status().isForbidden());
    }

    // 5. Test Create Top Up - Success
    @Test
    @WithMockUser(username = "c06102e8-9316-4ce7-b5cc-cecb96dee8ad", roles = "CUSTOMER")
    void testCreateTopUp_Success() throws Exception {
        CreateTopUpRequestDTO dto = new CreateTopUpRequestDTO();
        dto.setAmount(50000L);
        dto.setPaymentMethodId(UUID.randomUUID());

        when(topUpService.createTopUp(any())).thenReturn(new TopUpTransaction());

        mockMvc.perform(post("/api/top-up/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    // 6. Test Delete - Success
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testDeleteTransaction_Success() throws Exception {
        UUID id = UUID.randomUUID();
        // void method, tidak perlu when(...).thenReturn(...)
        
        mockMvc.perform(delete("/api/top-up/" + id).with(csrf()))
                .andExpect(status().isOk());
    }

    // 7. Test Update Status - Success
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testUpdateStatus_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStatusTopUpRequestDTO dto = new UpdateStatusTopUpRequestDTO();
        dto.setStatus("Success");

        when(topUpService.updateStatusTopUp(eq(id), any())).thenReturn(new TopUpTransaction());

        mockMvc.perform(put("/api/top-up/" + id + "/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}