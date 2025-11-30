package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.TopUpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopUpRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TopUpRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private TopUpService topUpService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupMockUser(String userId, String role) {
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenReturn((List) Collections.singletonList(new SimpleGrantedAuthority(role)));
    }

    @Test
    void testCreateTopUp_Success() throws Exception {
        String uid = UUID.randomUUID().toString();
        setupMockUser(uid, "ROLE_CUSTOMER");
        
        CreateTopUpRequestDTO req = new CreateTopUpRequestDTO();
        req.setAmount(1000L);
        when(topUpService.createTopUp(any())).thenReturn(new TopUpTransaction());

        mockMvc.perform(post("/api/top-up/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetHistory_Success() throws Exception {
        UUID uid = UUID.randomUUID();
        setupMockUser(uid.toString(), "ROLE_CUSTOMER");
        
        mockMvc.perform(get("/api/top-up/history/" + uid))
                .andExpect(status().isOk());
    }
}