package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PaymentMethodService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
public class PaymentMethodRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PaymentMethodService paymentMethodService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testCreate_Success() throws Exception {
        AddPaymentMethodRequestDTO dto = new AddPaymentMethodRequestDTO();
        dto.setMethodName("OVO");
        
        when(paymentMethodService.addPaymentMethod(any())).thenReturn(new PaymentMethod());

        mockMvc.perform(post("/api/payment-method/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testUpdateStatus_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePaymentMethodStatusRequestDTO dto = new UpdatePaymentMethodStatusRequestDTO();
        dto.setStatus("Inactive");

        when(paymentMethodService.updateStatusPaymentMethod(eq(id), any())).thenReturn(new PaymentMethod());

        mockMvc.perform(put("/api/payment-method/" + id + "/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testDelete_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/payment-method/" + id).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testDelete_Forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/payment-method/" + id).with(csrf()))
                .andExpect(status().isForbidden());
    }
}