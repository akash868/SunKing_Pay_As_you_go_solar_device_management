package com.sunking.payg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunking.payg.dto.request.CreateCustomerRequest;
import com.sunking.payg.dto.request.LoginRequest;
import com.sunking.payg.dto.response.ApiResponse;
import com.sunking.payg.dto.response.AuthResponse;
import com.sunking.payg.dto.response.CustomerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("Customer API Integration Tests")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login to get admin token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("Admin@123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        @SuppressWarnings("unchecked")
        java.util.LinkedHashMap<String, Object> data = (java.util.LinkedHashMap<String, Object>) apiResponse.getData();
        adminToken = (String) data.get("accessToken");
    }

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomer() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhoneNumber("+254711" + System.currentTimeMillis() % 1000000);
        request.setRegion("Nairobi");

        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Jane");
    }

    @Test
    @DisplayName("Should return 409 on duplicate phone number")
    void shouldRejectDuplicatePhone() throws Exception {
        String phone = "+254700000001";

        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhoneNumber(phone);

        // First creation
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        // Duplicate creation
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 401 without token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return paginated customer list")
    void shouldReturnPaginatedCustomers() throws Exception {
        mockMvc.perform(get("/api/v1/customers?page=0&size=10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.pagination").exists());
    }

    @Test
    @DisplayName("Should fail validation on invalid phone number")
    void shouldFailValidationOnInvalidPhone() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhoneNumber("invalid-phone");

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
