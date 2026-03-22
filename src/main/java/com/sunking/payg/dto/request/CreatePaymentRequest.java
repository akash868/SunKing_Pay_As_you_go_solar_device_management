package com.sunking.payg.dto.request;

import com.sunking.payg.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Request payload to record a payment")
public class CreatePaymentRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID customerId;

    @NotNull(message = "Assignment ID is required")
    @Schema(description = "Device assignment UUID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID assignmentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    @Digits(integer = 13, fraction = 2)
    @Schema(description = "Payment amount", example = "500.00")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method", example = "MOBILE_MONEY")
    private Payment.PaymentMethod paymentMethod;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number")
    @Schema(description = "Mobile number for mobile money payments", example = "+254712345678")
    private String mobileNumber;

    @Schema(description = "Idempotency key / transaction reference for deduplication", example = "TXN-2024-001")
    private String transactionReference;
}
