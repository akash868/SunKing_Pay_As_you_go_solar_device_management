package com.sunking.payg.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload to register a new solar device")
public class CreateDeviceRequest {

    @NotBlank(message = "Serial number is required")
    @Size(max = 100)
    @Schema(description = "Unique device serial number", example = "SK-2024-001234")
    private String serialNumber;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    @Schema(description = "Device model name", example = "SunKing Pro 200")
    private String model;

    @Schema(description = "Device description", example = "200W solar home system with 4 bulbs")
    private String description;

    @NotNull(message = "Total cost is required")
    @DecimalMin(value = "0.01", message = "Total cost must be positive")
    @Digits(integer = 13, fraction = 2)
    @Schema(description = "Total PAYG cost of the device", example = "15000.00")
    private BigDecimal totalCost;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be positive")
    @Digits(integer = 13, fraction = 2)
    @Schema(description = "Daily payment rate", example = "100.00")
    private BigDecimal dailyRate;

    @Min(value = 0, message = "Grace period cannot be negative")
    @Max(value = 30, message = "Grace period cannot exceed 30 days")
    @Schema(description = "Days allowed overdue before device is locked", example = "3")
    private Integer gracePeriodDays = 3;
}
