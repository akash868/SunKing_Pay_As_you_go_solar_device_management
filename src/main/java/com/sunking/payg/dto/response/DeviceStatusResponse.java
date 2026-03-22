package com.sunking.payg.dto.response;

import com.sunking.payg.entity.Device;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Device status and payment summary for an assignment")
public class DeviceStatusResponse {

    private UUID deviceId;
    private String serialNumber;
    private String model;
    private Device.DeviceStatus deviceStatus;

    private UUID assignmentId;
    private UUID customerId;
    private String customerName;
    private String customerPhone;

    private BigDecimal totalCost;
    private BigDecimal amountPaid;
    private BigDecimal remainingBalance;
    private BigDecimal progressPercent;

    private LocalDate nextPaymentDueDate;
    private LocalDate lastPaymentDate;
    private Integer daysOverdue;
    private Boolean isFullyPaid;
    private LocalDateTime fullyPaidAt;
    private LocalDateTime assignedAt;
}
