package com.sunking.payg.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Request payload to create a new customer")
public class CreateCustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Customer's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Customer's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(description = "Customer phone number (unique)", example = "+254712345678")
    private String phoneNumber;

    @Email(message = "Invalid email address")
    @Schema(description = "Customer email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "National ID or passport number", example = "KE12345678")
    private String nationalId;

    @Schema(description = "Customer's region or county", example = "Nairobi")
    private String region;

    @Schema(description = "Customer's physical address", example = "123 Main Street, Nairobi")
    private String address;
}
