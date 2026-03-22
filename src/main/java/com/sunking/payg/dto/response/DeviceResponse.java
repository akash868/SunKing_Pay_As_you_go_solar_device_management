package com.sunking.payg.dto.response;

import com.sunking.payg.entity.Device;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Device details response")
public class DeviceResponse {

    private UUID id;
    private String serialNumber;
    private String model;
    private String description;
    private BigDecimal totalCost;
    private BigDecimal dailyRate;
    private Integer gracePeriodDays;
    private Device.DeviceStatus status;
    private LocalDateTime lockedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
