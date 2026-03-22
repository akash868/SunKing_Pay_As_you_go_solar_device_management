package com.sunking.payg.dto.response;

import com.sunking.payg.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Payment record response")
public class PaymentResponse {

    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID assignmentId;
    private UUID deviceId;
    private String deviceSerialNumber;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private Payment.PaymentMethod paymentMethod;
    private String transactionReference;
    private String externalTransactionId;
    private String mobileNumber;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
