package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restservice.PaymentMethodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMethodRestController.class)
class PaymentMethodRestControllerTest { // Nama class sudah benar

    @Autowired private MockMvc mockMvc;
    @MockBean private PaymentMethodService paymentMethodService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAll() throws Exception {
        mockMvc.perform(get("/api/payment-method/all"))
                .andExpect(status().isOk());
    }
}