package com.sunking.payg.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Request payload to assign a device to a customer")
public class AssignDeviceRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer to assign the device to", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID customerId;
}
